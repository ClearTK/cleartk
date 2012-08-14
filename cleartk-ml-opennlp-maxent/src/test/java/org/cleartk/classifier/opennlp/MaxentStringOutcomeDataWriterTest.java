/** 
 * Copyright (c) 2009-2011, Regents of the University of Colorado 
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

package org.cleartk.classifier.opennlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.util.InstanceFactory;
import org.cleartk.test.DefaultTestBase;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2009-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class MaxentStringOutcomeDataWriterTest extends DefaultTestBase {

  public static class Test1Annotator extends CleartkAnnotator<String> {

    @Override
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
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MaxentStringOutcomeDataWriter.class.getName());

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MaxentStringOutcomeClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("A pos_NN distance=3.0 precision=1.234", lines[0]);
    assertEquals("B name_2PO p's=2", lines[1]);
    assertEquals("Z null=0", lines[2]);
    assertEquals("A A_B_AB", lines[3]);

    // simply train four different models where each one writes over the previous
    HideOutput hider = new HideOutput();
    Train.main(outputDirectoryName, "10", "1");
    hider.restoreOutput();

  }

  /**
   * This test is identical to test1 except that the features are compressed by
   * NameNumberFeaturesEncoder.
   */
  @Test
  @Deprecated
  public void test2() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test1Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMaxentDataWriterFactory.class.getName(),
        MaxentDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true);

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MaxentStringOutcomeClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("A 0 1=3.0 2=1.234", lines[0]);
    assertEquals("B 3 4=2", lines[1]);
    assertEquals("Z null=0", lines[2]);
    assertEquals("A 5", lines[3]);

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
    Train.main(outputDirectoryName, "10", "1");
    hider.restoreOutput();
  }

  /**
   * This test is identical to test2 except that the feature lookup file is sorted by
   * NameNumberFeaturesEncoder.
   */

  @Test
  @Deprecated
  public void test3() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test1Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMaxentDataWriterFactory.class.getName(),
        MaxentDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true,
        MaxentDataWriterFactory_ImplBase.PARAM_SORT,
        true);

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MaxentStringOutcomeClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("A 0 1=3.0 2=1.234", lines[0]);
    assertEquals("B 3 4=2", lines[1]);
    assertEquals("Z null=0", lines[2]);
    assertEquals("A 5", lines[3]);

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
    Train.main(outputDirectoryName, "10", "1");
    hider.restoreOutput();
  }

  public static class Test4Annotator extends CleartkAnnotator<String> {

    @Override
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
   */
  @Test
  public void test4() throws Exception {

    HideOutput hider = new HideOutput();

    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test4Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MaxentStringOutcomeDataWriter.class.getName());

    AnalysisEngineProcessException aepe = null;
    try {
      dataWriterAnnotator.process(jCas);
    } catch (AnalysisEngineProcessException e) {
      aepe = e;
    }
    dataWriterAnnotator.collectionProcessComplete();
    assertNotNull(aepe);
    hider.restoreOutput();

  }

  public static class Test5Annotator extends CleartkAnnotator<String> {

    @Override
    public void process(JCas cas) throws AnalysisEngineProcessException {
      Instance<String> instance = InstanceFactory.createInstance("a", "b c d");
      this.dataWriter.write(instance);
    }
  }

  /**
   * This test is identical to test1 except that the features are compressed by
   * NameNumberFeaturesEncoder.
   */
  @Test
  @Deprecated
  public void test5() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test5Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMaxentDataWriterFactory.class.getName(),
        MaxentDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true);

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new MaxentStringOutcomeClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("a 0 1 2", lines[0]);
  }

}
