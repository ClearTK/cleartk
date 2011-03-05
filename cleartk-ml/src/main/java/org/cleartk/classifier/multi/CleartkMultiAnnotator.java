/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.classifier.multi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.util.CleartkInitializationException;
import org.cleartk.util.ReflectionUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 *         This class provides a framework for handling multiple classifiers within a single
 *         analysis engine.
 *         <p>
 * 
 *         Use cases that may lend themselves to {@link CleartkMultiAnnotator} over
 *         {@link CleartkAnnotator} include:
 *         <ul>
 *         <li>Explicit control for one-vs-all multi-label classification
 *         <li>Predicating training/classification on specific conditions. (e.g. if the the word to
 *         the left is a verb, use the verb classifier, else use the default classifier)
 *         <li>Voting or ensemble classification
 *         </ul>
 */
public abstract class CleartkMultiAnnotator<OUTCOME_TYPE> extends JCasAnnotator_ImplBase implements
    Initializable {

  public static final String PARAM_MULTI_CLASSIFIER_FACTORY_CLASS_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(
          CleartkMultiAnnotator.class,
          "multiClassifierFactoryClassName");

  @ConfigurationParameter(mandatory = false, description = "provides the full name of the MultiClassifierFactory class to be used.", defaultValue = "org.cleartk.classifier.multi.jar.JarMultiClassifierFactory")
  protected String multiClassifierFactoryClassName;

  public static final String PARAM_MULTI_DATA_WRITER_FACTORY_CLASS_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(
          CleartkMultiAnnotator.class,
          "multiDataWriterFactoryClassName");

  @ConfigurationParameter(mandatory = false, description = "provides the full name of the MultiDataWriterFactory class to be used.")
  protected String multiDataWriterFactoryClassName;

  public static final String PARAM_IS_TRAINING = ConfigurationParameterFactory
      .createConfigurationParameterName(CleartkMultiAnnotator.class, "isTraining");

  @ConfigurationParameter(mandatory = false, description = "determines whether this annotator is writing training data or using a classifier to annotate. Normally inferred automatically based on whether or not a DataWriterFactory class has been set.")
  private Boolean isTraining;

  private boolean primitiveIsTraining;

  protected MultiDataWriterFactory<?> multiDataWriterFactory;

  protected MultiClassifierFactory<?> multiClassifierFactory;

  protected Map<String, Classifier<OUTCOME_TYPE>> classifiers;

  protected Map<String, DataWriter<OUTCOME_TYPE>> dataWriters;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    if (multiDataWriterFactoryClassName == null && multiClassifierFactoryClassName == null) {
      CleartkInitializationException.neitherParameterSet(
          PARAM_MULTI_DATA_WRITER_FACTORY_CLASS_NAME,
          multiDataWriterFactoryClassName,
          PARAM_MULTI_CLASSIFIER_FACTORY_CLASS_NAME,
          multiClassifierFactoryClassName);
    }

    // determine whether we start out as training or predicting
    if (this.isTraining == null) {
      this.primitiveIsTraining = multiDataWriterFactoryClassName != null;
    } else {
      this.primitiveIsTraining = this.isTraining;
    }

    if (this.isTraining()) {
      // create the multiDataWriter factory and initialize a Map to hold the data writers
      this.dataWriters = new HashMap<String, DataWriter<OUTCOME_TYPE>>();

      // create the factory and instantiate the data writer
      this.multiDataWriterFactory = InitializableFactory.create(
          context,
          multiDataWriterFactoryClassName,
          MultiDataWriterFactory.class);
    }

    // create the multi classifier factory and initialize a map to hold the classifiers
    // While this may be superfluous in some cases, this is done in all instances because some
    // MultiAnnotators utilize other classifiers during training
    this.classifiers = new HashMap<String, Classifier<OUTCOME_TYPE>>();
    // create the factory and instantiate the classifier
    multiClassifierFactory = InitializableFactory.create(
        context,
        multiClassifierFactoryClassName,
        MultiClassifierFactory.class);
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    if (this.isTraining()) {
      try {
        for (DataWriter<OUTCOME_TYPE> dataWriter : dataWriters.values()) {
          dataWriter.finish();
        }
      } catch (CleartkProcessingException ctke) {
        throw new AnalysisEngineProcessException(ctke);
      }
    }
  }

  protected boolean isTraining() {
    return this.primitiveIsTraining;
  }

  /**
   * Gets the classifier associated with name. If it does not exist, this method will use the
   * {@link MultiClassifierFactory} specified at initialization to create a new one.
   * 
   * @param name
   * @return the classifier associated with name
   * @throws ResourceInitializationException
   */
  protected Classifier<OUTCOME_TYPE> getClassifier(String name)
      throws ResourceInitializationException {
    if (classifiers.containsKey(name)) {
      return classifiers.get(name);
    }

    Classifier<?> untypedClassifier;
    try {
      untypedClassifier = multiClassifierFactory.createClassifier(name);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

    Classifier<OUTCOME_TYPE> classifier = ReflectionUtil.uncheckedCast(untypedClassifier);
    ReflectionUtil.checkTypeParameterIsAssignable(
        CleartkMultiAnnotator.class,
        "OUTCOME_TYPE",
        this,
        Classifier.class,
        "OUTCOME_TYPE",
        classifier);
    InitializableFactory.initialize(untypedClassifier, this.getContext());
    this.classifiers.put(name, classifier);
    return classifier;

  }

  /**
   * Gets the dataWriter associated with name. If it does not exist, this method will use the
   * {@link MultiDataWriterFactory} specified during initialization to create a dataWriter
   * associated with the name parameter.
   * 
   * @param name
   * @return the data writer associated with name
   * @throws ResourceInitializationException
   */
  protected DataWriter<OUTCOME_TYPE> getDataWriter(String name)
      throws ResourceInitializationException {
    if (dataWriters.containsKey(name)) {
      return dataWriters.get(name);
    }

    DataWriter<?> untypedDataWriter;
    try {
      untypedDataWriter = multiDataWriterFactory.createDataWriter(name);
      DataWriter<OUTCOME_TYPE> dataWriter = ReflectionUtil.uncheckedCast(untypedDataWriter);
      this.dataWriters.put(name, dataWriter);
      return dataWriter;

    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

}
