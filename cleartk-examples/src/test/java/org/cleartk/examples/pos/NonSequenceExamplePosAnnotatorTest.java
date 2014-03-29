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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.corpus.penntreebank.TreebankGoldAnnotator;
import org.cleartk.examples.ExamplesTestBase;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.libsvm.LibSvmStringOutcomeDataWriter;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;
import org.cleartk.ml.svmlight.SvmLightStringOutcomeDataWriter;
import org.cleartk.snowball.DefaultSnowballStemmer;
import org.cleartk.token.breakit.BreakIteratorAnnotatorFactory;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.cr.FilesCollectionReader;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class NonSequenceExamplePosAnnotatorTest extends ExamplesTestBase {

  public static String firstLineGold = "2008 Sichuan earthquake From Wikipedia , the free encyclopedia The 2008 Sichuan earthquake occurred at 14:28 : 01.42 CST ( 06:28 : 01.42 UTC ) on 12 May 2008 , with its epicenter in Wenchuan County ( Chinese : ? ? ?";

  private void checkPOS(String posTaggedLine) {
    List<String> goldTokens = Arrays.asList(firstLineGold.split(" "));
    List<String> sysTokens = Arrays.asList(posTaggedLine.replaceAll("\\/[A-Z,.$-]{1,5}", "").split(
        " "));
    List<String> diffTokens = new ArrayList<String>(sysTokens);

    // Check that tokens match
    diffTokens.removeAll(goldTokens);
    assertTrue(diffTokens.isEmpty());

    // Check POS tags
    assertTrue(posTaggedLine.contains("the/DT"));
    assertTrue(posTaggedLine.contains("From/IN"));
    assertTrue(posTaggedLine.contains("earthquake/NN"));
    assertTrue(posTaggedLine.contains("The/DT"));
    assertTrue(posTaggedLine.contains("at/IN"));
    assertTrue(posTaggedLine.contains("12/CD"));
    assertTrue(posTaggedLine.contains("with/IN"));
    assertTrue(posTaggedLine.contains("in/IN"));
    assertTrue(posTaggedLine.contains("(/-LRB"));
    assertTrue(posTaggedLine.contains(")/-RRB-"));
  }

  @Test
  public void testLibsvm() throws Exception {
    String libsvmDirectoryName = outputDirectory + "/libsvm";
    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        NonSequenceExamplePosAnnotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        LibSvmStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        libsvmDirectoryName);
    testClassifier(dataWriter, libsvmDirectoryName, "-t", "0", "-c", "0.1");

    String firstLine = FileUtil.loadListOfStrings(new File(libsvmDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    checkPOS(firstLine);
  }

  @Test
  public void testMaxent() throws Exception {
    String maxentDirectoryName = outputDirectoryName + "/maxent";
    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        NonSequenceExamplePosAnnotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MaxentStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        maxentDirectoryName);
    testClassifier(dataWriter, maxentDirectoryName);

    // Not sure why the _SPLIT is here, but we will throw it out for good measure
    String firstLine = FileUtil.loadListOfStrings(new File(maxentDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0].trim().replace("_SPLIT", "");
    checkPOS(firstLine);
  }

  @Test
  public void testSVMLIGHT() throws Exception {
    this.assumeTestsEnabled(ExamplePosClassifierTest.SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(ExamplePosClassifierTest.SVMLIGHT_TESTS_ENABLED_MESSAGE);

    String svmlightDirectoryName = outputDirectoryName + "/svmlight";
    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createEngineDescription(
        NonSequenceExamplePosAnnotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        SvmLightStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        svmlightDirectoryName);
    testClassifier(dataWriter, svmlightDirectoryName, "-c", "0.1");

    String firstLine = FileUtil.loadListOfStrings(new File(svmlightDirectoryName
        + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    checkPOS(firstLine);
  }

  private void testClassifier(
      AnalysisEngineDescription dataWriter,
      String outDirectoryName,
      String... trainingArgs) throws Exception {
    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReaderWithView(
            "src/test/resources/data/treebank/11597317.tree",
            PennTreebankReader.TREEBANK_VIEW),
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
    org.cleartk.ml.jar.Train.main(args);
    hider.restoreOutput();

    AnalysisEngineDescription taggerDescription = AnalysisEngineFactory.createEngineDescription(
        NonSequenceExamplePosAnnotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        JarClassifierBuilder.getModelJarFile(outDirectoryName));

    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReader("src/test/resources/data/2008_Sichuan_earthquake.txt"),
        BreakIteratorAnnotatorFactory.createSentenceAnnotator(Locale.US),
        TokenAnnotator.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        taggerDescription,
        AnalysisEngineFactory.createEngineDescription(
            ExamplePosPlainTextWriter.class,
            ExamplePosPlainTextWriter.PARAM_OUTPUT_DIRECTORY_NAME,
            outDirectoryName));

  }
}
