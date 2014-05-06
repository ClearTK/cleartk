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

package org.cleartk.examples.pos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ResourceCreationSpecifierFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.testing.util.DisableLogging;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.corpus.penntreebank.TreebankGoldAnnotator;
import org.cleartk.examples.ExamplesTestBase;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.libsvm.LibSvmStringOutcomeDataWriter;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;
import org.cleartk.ml.mallet.MalletStringOutcomeDataWriter;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;
import org.cleartk.ml.svmlight.SvmLightStringOutcomeDataWriter;
import org.cleartk.ml.viterbi.DefaultOutcomeFeatureExtractor;
import org.cleartk.ml.viterbi.ViterbiClassifier;
import org.cleartk.ml.viterbi.ViterbiDataWriterFactory;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class ExamplePosClassifierTest extends ExamplesTestBase {

  /**
   * Value for the {@link #SKIP_TESTS_PROPERTY} property that indicates that tests requiring the
   * SVMlight executables to be installed on your system's path should be disabled. Current value:
   * {@value #SVMLIGHT_TESTS_PROPERTY_VALUE}.
   */
  public static final String SVMLIGHT_TESTS_PROPERTY_VALUE = "svmlight";

  /**
   * Message that will be logged at the beginning of each test that requires the SVMlight
   * executables.
   */
  public static final String SVMLIGHT_TESTS_ENABLED_MESSAGE = createTestEnabledMessage(
      SVMLIGHT_TESTS_PROPERTY_VALUE,
      "This test requires installation of SVMlight executables");

  @Test
  public void testLibsvm() throws Exception {
    String outDirectoryName = outputDirectoryName + "/libsvm";

    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        ExamplePosAnnotator.class,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        LibSvmStringOutcomeDataWriter.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });

    testClassifier(dataWriter, outDirectoryName, 1, "-t", "0"); // MultiClassLIBSVMClassifier.score
                                                                // is not implemented so we cannot
                                                                // have a stack size greater than 1.
    String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    boolean badTags = firstLine.equals("2008/NN Sichuan/NN earthquake/NN From/NN Wikipedia/NN ,/NN the/NN free/NN encyclopedia/NN");
    assertFalse(badTags);
    assertEquals(
        "2008/NN Sichuan/NN earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN",
        firstLine);

  }

  @Test
  public void testMalletCRF() throws Exception {
    this.assumeLongTestsEnabled();
    this.logger.info(LONG_TEST_MESSAGE);

    String outDirectoryName = outputDirectoryName + "/malletcrf";
    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        ExamplePosAnnotator.class,
        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MalletCrfStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outDirectoryName);
    testClassifier(dataWriter, outDirectoryName, -1); // viterbi stack size is meaningless here so
                                                      // pass in an invalid value to make sure it is
                                                      // ignored.

    String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    assertEquals(
        "2008/NN Sichuan/CD earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN",
        firstLine);

  }

  @Test
  public void testMaxent() throws Exception {
    String outDirectoryName = outputDirectoryName + "/maxent";

    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        ExamplePosAnnotator.class,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MaxentStringOutcomeDataWriter.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });
    testClassifier(dataWriter, outDirectoryName, 10);

    String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0];
    assertEquals(
        "2008/CD Sichuan/JJ earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN",
        firstLine);
  }

  @Test
  public void testMalletMaxent() throws Exception {
    String outDirectoryName = outputDirectoryName + "/mallet-maxent";

    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        ExamplePosAnnotator.class,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MalletStringOutcomeDataWriter.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });
    testClassifier(dataWriter, outDirectoryName, 10, "MaxEnt");

    String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0];
    assertEquals(
        "2008/DT Sichuan/JJ earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN",
        firstLine);
  }

  @Test
  public void testMalletNaiveBayes() throws Exception {
    String outDirectoryName = outputDirectoryName + "/mallet-naive-bayes";

    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        ExamplePosAnnotator.class,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MalletStringOutcomeDataWriter.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });
    testClassifier(dataWriter, outDirectoryName, 10, "NaiveBayes");

    String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0];
    assertEquals(
        "2008/DT Sichuan/JJ earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN",
        firstLine);
  }

  @Test
  public void testMalletC45() throws Exception {
    this.assumeLongTestsEnabled();
    this.logger.info(LONG_TEST_MESSAGE);

    String outDirectoryName = outputDirectoryName + "/mallet-c45";

    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        ExamplePosAnnotator.class,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MalletStringOutcomeDataWriter.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });
    testClassifier(dataWriter, outDirectoryName, 10, "C45");

    String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0];
    assertEquals(
        "2008/CD Sichuan/JJ earthquake/NN From/NN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN",
        firstLine);
  }

  @Test
  public void testSVMLIGHT() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    String outDirectoryName = outputDirectoryName + "/svmlight";
    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        ExamplePosAnnotator.class,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        SvmLightStringOutcomeDataWriter.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });
    testClassifier(dataWriter, outDirectoryName, 1);

    String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    boolean badTags = firstLine.equals("2008/NN Sichuan/NN earthquake/NN From/NN Wikipedia/NN ,/NN the/NN free/NN encyclopedia/NN");
    assertFalse(badTags);

    assertEquals(
        "2008/CD Sichuan/JJ earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN",
        firstLine);
  }

  private void testClassifier(
      AnalysisEngineDescription dataWriter,
      String outDirectoryName,
      int stackSize,
      String... trainingArgs) throws Exception {

    SimplePipeline.runPipeline(
        UriCollectionReader.getCollectionReaderFromFiles(Arrays.asList(new File(
            "src/test/resources/data/treebank/11597317.tree"))),
        UriToDocumentTextAnnotator.getDescriptionForView(PennTreebankReader.TREEBANK_VIEW),
        TreebankGoldAnnotator.getDescriptionPOSTagsOnly(),
        DefaultSnowballStemmer.getDescription("English"),
        dataWriter);

    String[] args;
    if (trainingArgs != null && trainingArgs.length > 0) {
      args = new String[trainingArgs.length + 1];
      args[0] = outDirectoryName;
      System.arraycopy(trainingArgs, 0, args, 1, trainingArgs.length);
    } else {
      args = new String[] { outDirectoryName };
    }

    HideOutput hider = new HideOutput();
    Level level = DisableLogging.disableLogging();
    try {
      org.cleartk.ml.jar.Train.main(args);
    } finally {
      DisableLogging.enableLogging(level);
      hider.restoreOutput();
    }

    AnalysisEngineDescription taggerDescription = ExamplePosAnnotator.getClassifierDescription(JarClassifierBuilder.getModelJarFile(
        outDirectoryName).getPath());
    ResourceCreationSpecifierFactory.setConfigurationParameters(
        taggerDescription,
        ViterbiClassifier.PARAM_STACK_SIZE,
        stackSize);

    SimplePipeline.runPipeline(
        UriCollectionReader.getCollectionReaderFromFiles(Arrays.asList(new File(
            "src/test/resources/data/2008_Sichuan_earthquake.txt"))),
        UriToDocumentTextAnnotator.getDescription(),
        SentenceAnnotator.getDescription(),
        TokenAnnotator.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        taggerDescription,
        AnalysisEngineFactory.createEngineDescription(
            ExamplePosPlainTextWriter.class,
            ExamplePosPlainTextWriter.PARAM_OUTPUT_DIRECTORY_NAME,
            outDirectoryName));
  }
}
