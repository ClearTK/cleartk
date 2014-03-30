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
package org.cleartk.ml.multi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.resource.ConfigurationManager;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.Classifier;
import org.cleartk.ml.ClassifierFactory;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.DataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.util.CleartkInitializationException;
import org.cleartk.util.ReflectionUtil;

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

  public static final String PARAM_CLASSIFIER_FACTORY_CLASS_NAME = "classifierFactoryClassName";

  private static final String DEFAULT_CLASSIFIER_FACTORY_CLASS_NAME = "org.cleartk.ml.jar.JarClassifierFactory";

  @ConfigurationParameter(
      name = PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
      mandatory = false,
      description = "provides the full name of the ClassifierFactory class to be used.",
      defaultValue = DEFAULT_CLASSIFIER_FACTORY_CLASS_NAME)
  private String classifierFactoryClassName;

  public static final String PARAM_DATA_WRITER_FACTORY_CLASS_NAME = "dataWriterFactoryClassName";

  private static final String DEFAULT_DATA_WRITER_FACTORY_CLASS_NAME = "org.cleartk.ml.jar.DefaultDataWriterFactory";

  @ConfigurationParameter(
      name = PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
      mandatory = false,
      description = "provides the full name of the DataWriterFactory class to be used.",
      defaultValue = DEFAULT_DATA_WRITER_FACTORY_CLASS_NAME)
  private String dataWriterFactoryClassName;

  public static final String PARAM_IS_TRAINING = "isTraining";

  @ConfigurationParameter(
      name = PARAM_IS_TRAINING,
      mandatory = false,
      description = "determines whether this annotator is writing training data or using a classifier to annotate. Normally inferred automatically based on whether or not a DataWriterFactory class has been set.")
  private Boolean isTraining;

  private boolean primitiveIsTraining;

  protected ClassifierFactory<?> classifierFactory;

  protected DataWriterFactory<?> dataWriterFactory;

  protected Map<String, Classifier<OUTCOME_TYPE>> classifiers;

  protected Map<String, DataWriter<OUTCOME_TYPE>> dataWriters;

  private UimaContext uimaContext;

  protected File outputDirectoryRoot;

  protected File classifierJarPathRoot;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    if (this.dataWriterFactoryClassName == null && this.classifierFactoryClassName == null) {
      CleartkInitializationException.neitherParameterSet(
          PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
          this.dataWriterFactoryClassName,
          PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
          this.classifierFactoryClassName);
    }

    // determine whether we start out as training or predicting
    if (this.isTraining != null) {
      this.primitiveIsTraining = this.isTraining;
    } else if (!DEFAULT_DATA_WRITER_FACTORY_CLASS_NAME.equals(this.dataWriterFactoryClassName)) {
      this.primitiveIsTraining = true;
    } else if (context.getConfigParameterValue(DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY) != null) {
      this.primitiveIsTraining = true;
    } else if (!DEFAULT_CLASSIFIER_FACTORY_CLASS_NAME.equals(this.classifierFactoryClassName)) {
      this.primitiveIsTraining = false;
    } else if (context.getConfigParameterValue(GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH) != null) {
      this.primitiveIsTraining = false;
    } else {
      String message = "Please specify PARAM_IS_TRAINING - unable to infer it from context";
      throw new IllegalArgumentException(message);
    }

    this.uimaContext = context;
    UimaContextAdmin contextAdmin = (UimaContextAdmin) this.uimaContext;
    ConfigurationManager manager = contextAdmin.getConfigurationManager();

    if (this.isTraining()) {
      // Create the data writer factory and initialize a Map to hold the data writers
      // Individual data writers will be created dynamically with the getDataWriter method
      this.dataWriters = new HashMap<String, DataWriter<OUTCOME_TYPE>>();
      this.outputDirectoryRoot = (File) manager.getConfigParameterValue(DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY);
      this.dataWriterFactory = InitializableFactory.create(
          context,
          dataWriterFactoryClassName,
          DataWriterFactory.class);
    } else {

      // Create the classifier factory and initialize a map to hold the classifiers
      // Individual classifiers will be created dynamically with the getClassifier method
      this.classifiers = new HashMap<String, Classifier<OUTCOME_TYPE>>();
      this.classifierJarPathRoot = (File) manager.getConfigParameterValue(GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH);

      this.classifierFactory = InitializableFactory.create(
          context,
          classifierFactoryClassName,
          ClassifierFactory.class);
    }
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
   * {@link ClassifierFactory} specified at initialization to create a new one.
   * 
   * @param name
   *          The name of the classifier
   * @return The classifier associated with the name
   */
  protected Classifier<OUTCOME_TYPE> getClassifier(String name)
      throws ResourceInitializationException {
    if (classifiers.containsKey(name)) {
      return classifiers.get(name);
    }

    File classifierJarPath = new File(this.classifierJarPathRoot, name);
    UimaContextAdmin contextAdmin = (UimaContextAdmin) this.uimaContext;
    ConfigurationManager manager = contextAdmin.getConfigurationManager();
    manager.setConfigParameterValue(contextAdmin.getQualifiedContextName()
        + GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, classifierJarPath.getPath());

    // create the factory and instantiate the classifier
    ClassifierFactory<?> factory = InitializableFactory.create(
        uimaContext,
        classifierFactoryClassName,
        ClassifierFactory.class);
    Classifier<?> untypedClassifier;
    try {
      untypedClassifier = factory.createClassifier();
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
   * Gets the {@link DataWriter} associated with name. If it does not exist, this method will use
   * the {@link DataWriterFactory} specified during initialization to create a dataWriter associated
   * with the name parameter.
   * 
   * @param name
   *          The name of the {@link DataWriter}
   * @return The {@link DataWriter} associated with the name
   */
  protected DataWriter<OUTCOME_TYPE> getDataWriter(String name)
      throws ResourceInitializationException {
    if (dataWriters.containsKey(name)) {
      return dataWriters.get(name);
    }

    DataWriter<?> untypedDataWriter;
    File dataWriterPath = new File(this.outputDirectoryRoot, name);
    UimaContextAdmin contextAdmin = (UimaContextAdmin) this.uimaContext;
    ConfigurationManager manager = contextAdmin.getConfigurationManager();
    manager.setConfigParameterValue(contextAdmin.getQualifiedContextName()
        + DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, dataWriterPath);

    try {
      untypedDataWriter = this.dataWriterFactory.createDataWriter();
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

    InitializableFactory.initialize(untypedDataWriter, uimaContext);
    DataWriter<OUTCOME_TYPE> dataWriter = ReflectionUtil.uncheckedCast(untypedDataWriter);
    this.dataWriters.put(name, dataWriter);
    return dataWriter;
  }

}
