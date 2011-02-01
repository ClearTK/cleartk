/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.viterbi.ViterbiClassifier;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * A factory class that simplifies the creation of descriptors for {@link CleartkAnnotator} and
 * {@link CleartkSequentialAnnotator} classes. The factory methods here wrap common patterns of
 * calls to {@link AnalysisEngineFactory#createPrimitiveDescription} and
 * {@link ConfigurationParameterFactory#addConfigurationParameter} for setting classifier classes,
 * output directories, etc. that are necessary when creating a CleartkAnnotator or
 * CleartkSequentialAnnotator.
 * 
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CleartkAnnotatorDescriptionFactory {

  /**
   * Create an {@link AnalysisEngineDescription} for using a {@link CleartkSequentialAnnotator} to
   * write training data for a {@link ViterbiClassifier}. Note that data is written with a
   * {@link DataWriterFactory}, not a {@link SequentialDataWriterFactory} - A ViterbiClassifier
   * wraps a non-sequential classifier to work as a sequential classifier.
   * 
   * @param <OUTCOME_TYPE>
   *          The outcome type of the classifier (must be the same for both the
   *          CleartkSequentialAnnotator and the DataWriterFactory).
   * @param annotatorClass
   *          The main annotator for the AnalysisEngineDescription.
   * @param typeSystemDescription
   *          The type system used by the annotator.
   * @param delegatedDataWriterFactoryClass
   *          The non-sequential DataWriterFactory that will be used to write the training data (and
   *          indicate the type of classifier that will be trained from this data).
   * @param outputDir
   *          The directory where the training data should be written.
   * @return An AnalysisEngineDescription for the CleartkSequentialAnnotator.
   */
  public static <OUTCOME_TYPE> AnalysisEngineDescription createViterbiAnnotator(
      Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> annotatorClass,
      TypeSystemDescription typeSystemDescription,
      Class<? extends DataWriterFactory<OUTCOME_TYPE>> delegatedDataWriterFactoryClass,
      String outputDir) throws ResourceInitializationException {

    return AnalysisEngineFactory.createPrimitiveDescription(
        annotatorClass,
        typeSystemDescription,
        CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDir,
        ViterbiDataWriterFactory.PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS,
        delegatedDataWriterFactoryClass.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });
  }

  /**
   * Create an {@link AnalysisEngineDescription} for using a {@link CleartkAnnotator} to create new
   * annotations based on the predictions of a classifier.
   * 
   * @param cleartkAnnotatorClass
   *          The main annotator for the AnalysisEngineDescription.
   * @param typeSystemDescription
   *          The type system used by the annotator.
   * @param classifierJar
   *          The jar file containing the classifier that will make predictions.
   * @return An AnalysisEngineDescription for the CleartkAnnotator.
   */
  public static AnalysisEngineDescription createCleartkAnnotator(
      Class<? extends CleartkAnnotator<?>> cleartkAnnotatorClass,
      TypeSystemDescription typeSystemDescription,
      String classifierJar) throws ResourceInitializationException {

    AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
        cleartkAnnotatorClass,
        typeSystemDescription);
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJar);
    return aed;
  }

  /**
   * Create an {@link AnalysisEngineDescription} for using a {@link CleartkAnnotator} to write
   * training data.
   * 
   * @param <OUTCOME_TYPE>
   *          The outcome type of the classifier (must be the same for both the CleartkAnnotator and
   *          the DataWriterFactory).
   * @param cleartkAnnotatorClass
   *          The main annotator for the AnalysisEngineDescription.
   * @param typeSystemDescription
   *          The type system used by the annotator.
   * @param dataWriterFactoryClass
   *          The DataWriterFactory that will be used to write the training data (and indicate the
   *          type of classifier that will be trained from this data).
   * @param outputDir
   *          The directory where the training data should be written.
   * @return An AnalysisEngineDescription for the CleartkAnnotator.
   */
  public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
      Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass,
      TypeSystemDescription typeSystemDescription,
      Class<? extends DataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass,
      String outputDir) throws ResourceInitializationException {
    AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
        cleartkAnnotatorClass,
        typeSystemDescription);

    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        dataWriterFactoryClass.getName());
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDir);
    return aed;
  }

  /**
   * Create an {@link AnalysisEngineDescription} for using a {@link CleartkSequentialAnnotator} to
   * create new annotations based on the predictions of a classifier.
   * 
   * @param sequentialClassifierAnnotatorClass
   *          The main annotator for the AnalysisEngineDescription.
   * @param typeSystemDescription
   *          The type system used by the annotator.
   * @param classifierJar
   *          The jar file containing the classifier that will make predictions.
   * @return An AnalysisEngineDescription for the CleartkSequentialAnnotator.
   */
  public static AnalysisEngineDescription createCleartkSequentialAnnotator(
      Class<? extends CleartkSequentialAnnotator<?>> sequentialClassifierAnnotatorClass,
      TypeSystemDescription typeSystemDescription,
      String classifierJar) throws ResourceInitializationException {

    AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
        sequentialClassifierAnnotatorClass,
        typeSystemDescription);
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJar);
    return aed;
  }

  /**
   * Create an {@link AnalysisEngineDescription} for using a {@link CleartkSequentialAnnotator} to
   * write training data.
   * 
   * @param <OUTCOME_TYPE>
   *          The outcome type of the classifier (must be the same for both the
   *          CleartkSequentialAnnotator and the SequentialDataWriterFactory).
   * @param sequentialClassifierAnnotatorClass
   *          The main annotator for the AnalysisEngineDescription.
   * @param typeSystemDescription
   *          The type system used by the annotator.
   * @param dataWriterFactoryClass
   *          The SequentialDataWriterFactory that will be used to write the training data (and
   *          indicate the type of classifier that will be trained from this data).
   * @param outputDir
   *          The directory where the training data should be written.
   * @return An AnalysisEngineDescription for the CleartkSequentialAnnotator.
   */
  public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkSequentialAnnotator(
      Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> sequentialClassifierAnnotatorClass,
      TypeSystemDescription typeSystemDescription,
      Class<? extends SequentialDataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass,
      String outputDir) throws ResourceInitializationException {

    AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
        sequentialClassifierAnnotatorClass,
        typeSystemDescription);
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME,
        dataWriterFactoryClass.getName());
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDir);
    return aed;
  }
}
