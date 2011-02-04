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

package org.cleartk.classifier.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.mallet.factory.ClassifierTrainerFactory;
import org.cleartk.classifier.util.InstanceFactory;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class MalletDataWriterTest extends DefaultTestBase {

  public static class Test1Annotator extends CleartkAnnotator<String> {

    public void process(JCas cas) throws AnalysisEngineProcessException {
      List<Feature> features = Arrays.asList(
          new Feature("pos", "NN"),
          new Feature("distance", 3.0),
          new Feature("precision", 1.234));
      Instance<String> instance = new Instance<String>("A", features);
      this.dataWriter.write(instance);

      features = Arrays.asList(new Feature("name", "2PO"), new Feature("p's", 2));
      instance = new Instance<String>("B", features);
      this.dataWriter.write(instance);

      instance = new Instance<String>("Z");
      this.dataWriter.write(instance);

      features = Arrays.asList(new Feature("A_B", "AB"));
      instance = new Instance<String>("A", features);
      this.dataWriter.write(instance);
    }
  }

  @Test
  public void test1() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test1Annotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMalletDataWriterFactory.class.getName());

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MalletClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("pos_NN:1.0 distance:3.0 precision:1.234 A", lines[0]);
    assertEquals("name_2PO:1.0 p's:2 B", lines[1]);
    assertEquals("null:0 Z", lines[2]);
    assertEquals("A_B_AB:1.0 A", lines[3]);

    // simply train four different models where each one writes over the previous
    HideOutput hider = new HideOutput();
    for (String classifierName : ClassifierTrainerFactory.NAMES) {
      Train.main(outputDirectoryName, classifierName);
    }
    hider.restoreOutput();

    IllegalArgumentException iae = null;
    try {
      Train.main(outputDirectoryName, "AutoTrophic");
    } catch (IllegalArgumentException e) {
      iae = e;
    }
    assertNotNull(iae);
    hider.restoreOutput();
  }

  /**
   * This test is identical to test1 except that the features are compressed by
   * NameNumberFeaturesEncoder.
   * 
   * @throws Exception
   */
  @Test
  public void test2() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test1Annotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMalletDataWriterFactory.class.getName(),
        MalletDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true);

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MalletClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("0:1.0 1:3.0 2:1.234 A", lines[0]);
    assertEquals("3:1.0 4:2 B", lines[1]);
    assertEquals("null:0 Z", lines[2]);
    assertEquals("5:1.0 A", lines[3]);

    lines = FileUtil.loadListOfStrings(new File(
        outputDirectoryName,
        NameNumberFeaturesEncoder.LOOKUP_FILE_NAME));
    Set<String> lineSet = new HashSet<String>();
    for (int i = 0; i < lines.length; i++)
      lineSet.add(lines[i]);
    assertEquals("6", lines[0]);
    assertTrue(lineSet.contains("6"));
    assertTrue(lineSet.contains("name_2PO\t3"));
    assertTrue(lineSet.contains("precision\t2"));
    assertTrue(lineSet.contains("distance\t1"));
    assertTrue(lineSet.contains("pos_NN\t0"));
    assertTrue(lineSet.contains("A_B_AB\t5"));
    assertTrue(lineSet.contains("p's\t4"));
    assertEquals(7, lineSet.size());

    HideOutput hider = new HideOutput();
    for (String classifierName : ClassifierTrainerFactory.NAMES) {
      Train.main(outputDirectoryName, classifierName);
    }
    hider.restoreOutput();
  }

  /**
   * This test is identical to test2 except that the feature lookup file is sorted by
   * NameNumberFeaturesEncoder.
   * 
   * @throws Exception
   */

  @Test
  public void test3() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test1Annotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMalletDataWriterFactory.class.getName(),
        MalletDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true,
        MalletDataWriterFactory_ImplBase.PARAM_SORT,
        true);

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MalletClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("0:1.0 1:3.0 2:1.234 A", lines[0]);
    assertEquals("3:1.0 4:2 B", lines[1]);
    assertEquals("null:0 Z", lines[2]);
    assertEquals("5:1.0 A", lines[3]);

    lines = FileUtil.loadListOfStrings(new File(
        outputDirectoryName,
        NameNumberFeaturesEncoder.LOOKUP_FILE_NAME));
    int i = 0;
    assertEquals("6", lines[i++]);
    assertEquals("A_B_AB	5", lines[i++]);
    assertEquals("distance	1", lines[i++]);
    assertEquals("name_2PO	3", lines[i++]);
    assertEquals("p's	4", lines[i++]);
    assertEquals("pos_NN	0", lines[i++]);
    assertEquals("precision	2", lines[i++]);

    HideOutput hider = new HideOutput();
    for (String classifierName : ClassifierTrainerFactory.NAMES) {
      Train.main(outputDirectoryName, classifierName);
    }
    hider.restoreOutput();
  }

  public static class Test4Annotator extends CleartkAnnotator<String> {
    public void process(JCas cas) throws AnalysisEngineProcessException {
      List<Feature> features = Arrays.asList(
          new Feature("pos", "NN"),
          new Feature("distance", 3.0),
          new Feature("precision", 1.234));
      Instance<String> instance = new Instance<String>(features);
      this.dataWriter.write(instance);
    }

  }

  /**
   * Here we test that an exception is thrown if an instance with no outcome
   * 
   * @throws Exception
   */
  @Test
  public void test4() throws Exception {

    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test4Annotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMalletDataWriterFactory.class.getName(),
        MalletDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true,
        MalletDataWriterFactory_ImplBase.PARAM_SORT,
        true);

    AnalysisEngineProcessException aepe = null;
    try {
      dataWriterAnnotator.process(jCas);
    } catch (AnalysisEngineProcessException e) {
      aepe = e;
    }
    dataWriterAnnotator.collectionProcessComplete();
    assertNotNull(aepe);
  }

  public static class Test5Annotator extends CleartkAnnotator<String> {
    public void process(JCas cas) throws AnalysisEngineProcessException {
      Instance<String> instance = InstanceFactory.createInstance("a", "b c d");
      this.dataWriter.write(instance);
    }
  }

  /**
   * This test is identical to test1 except that the features are compressed by
   * NameNumberFeaturesEncoder.
   * 
   * @throws Exception
   */
  @Test
  public void test5() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test5Annotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMalletDataWriterFactory.class.getName(),
        MalletDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true);

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MalletClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("0:1.0 1:1.0 2:1.0 a", lines[0]);
  }

  public static class TestAnnotator extends CleartkSequenceAnnotator<String> {

    private SimpleFeatureExtractor extractor = new SpannedTextExtractor();

    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
    }

    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (Sentence sentence : AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
        List<Instance<String>> instances = new ArrayList<Instance<String>>();
        List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class);
        for (Token token : tokens) {
          Instance<String> instance = new Instance<String>();
          instance.addAll(this.extractor.extract(jCas, token));
          instance.setOutcome(token.getPos());
          instances.add(instance);
        }
        this.dataWriter.write(instances);
      }
    }

  }

  @Test
  public void testSequenceDataWriterAnnotator() throws IOException, UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        TestAnnotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMalletCRFDataWriterFactory.class.getName());

    // create some tokens and sentences
    // add part-of-speech and stems to tokens

    String text = "What if we built a large\r\n, wooden badger?";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "What if we built a large \n, wooden badger ?",
        "WDT TO PRP VBN DT JJ , JJ NN .");
    engine.process(jCas);
    engine.collectionProcessComplete();

    File trainFile = new MalletCRFClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    BufferedReader input = new BufferedReader(new FileReader(trainFile));
    String line = input.readLine();
    assertNotNull(line);
    assertTrue(line.endsWith(" WDT"));
    assertTrue(line.startsWith("What "));
    line = input.readLine();
    assertNotNull(line);
    assertTrue(line.endsWith(" TO"));
    assertTrue(line.startsWith("if "));
    line = input.readLine();
    assertNotNull(line);
    assertTrue(line.endsWith(" PRP"));
    assertTrue(line.startsWith("we "));
    line = input.readLine();
    assertNotNull(line);
    assertTrue(line.endsWith(" VBN"));
    assertTrue(line.startsWith("built "));
    line = input.readLine();
    assertNotNull(line);
    assertTrue(line.endsWith(" DT"));
    assertTrue(line.startsWith("a "));
    line = input.readLine();
    assertNotNull(line);
    assertTrue(line.endsWith(" JJ"));
    assertTrue(line.startsWith("large "));
    line = input.readLine();
    assertNotNull(line);
    assertEquals("", line.trim());
    line = input.readLine();
    assertNotNull(line);
    assertTrue(line.endsWith(" ,"));
    assertTrue(line.startsWith(", "));
    input.close();
  }

}
