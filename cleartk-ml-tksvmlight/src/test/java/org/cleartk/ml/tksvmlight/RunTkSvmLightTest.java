/** 
 * Copyright (c) 2007-2013, Regents of the University of Colorado 
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
package org.cleartk.ml.tksvmlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.uima.jcas.JCas;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.TreeFeature;
import org.cleartk.ml.encoder.features.BooleanEncoder;
import org.cleartk.ml.encoder.features.NumberEncoder;
import org.cleartk.ml.encoder.features.StringEncoder;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.Train;
import org.cleartk.ml.tksvmlight.TkSvmLightBooleanOutcomeClassifierBuilder;
import org.cleartk.ml.tksvmlight.TkSvmLightBooleanOutcomeDataWriter;
import org.cleartk.ml.tksvmlight.TkSvmLightStringOutcomeClassifierBuilder;
import org.cleartk.ml.tksvmlight.TkSvmLightStringOutcomeDataWriter;
import org.cleartk.ml.tksvmlight.TreeFeatureVector;
import org.cleartk.ml.tksvmlight.TreeFeatureVectorFeaturesEncoder;
import org.cleartk.ml.tksvmlight.TreeKernelSvmBooleanOutcomeClassifier;
import org.cleartk.ml.tksvmlight.TreeKernelSvmStringOutcomeClassifier;
import org.cleartk.ml.tksvmlight.model.TreeKernel;
import org.cleartk.ml.tksvmlight.model.TreeKernel.ForestSumMethod;
import org.cleartk.ml.tksvmlight.model.TreeKernel.KernelType;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.uima.fit.factory.UimaContextFactory;
import org.apache.uima.fit.testing.util.HideOutput;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2007-2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 */
public class RunTkSvmLightTest extends DefaultTestBase {

  /**
   * Value for the {@link #SKIP_TESTS_PROPERTY} property that indicates that tests requiring the TK
   * SvmLight executables to be installed on your system's path should be disabled. Current value:
   * {@value #TK_SVMLIGHT_TESTS_PROPERTY_VALUE}.
   */
  public static final String TK_SVMLIGHT_TESTS_PROPERTY_VALUE = "tksvmlight";

  /**
   * Message that will be logged at the beginning of each test that requires the tree-kernel
   * SVMlight executables.
   */
  public static final String TK_SVMLIGHT_TESTS_ENABLED_MESSAGE = createTestEnabledMessage(
      TK_SVMLIGHT_TESTS_PROPERTY_VALUE,
      "This test requires installation of tree-kernel SVMlight executables");

  protected String dataDirectory = "src/test/resources/data/svmlight/tk";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  private static class EmptyAnnotator<T> extends CleartkAnnotator<T> {
    public EmptyAnnotator() {
    }

    @Override
    public void process(JCas aJCas) {
    }

    public void write(Instance<T> instance) throws CleartkProcessingException {
      this.dataWriter.write(instance);
    }
  }

  @Test
  public void testTKSim(){
    TreeKernel sst = new TreeKernel(TreeKernel.LAMBDA_DEFAULT, ForestSumMethod.SEQUENTIAL, KernelType.SUBSET, false);
    
    TreeFeatureVector tf1 = new TreeFeatureVector();
    String tree1 = "(S (NP i) (VP (VB eat) (NN cake)))";
    LinkedHashMap<String,String> tree1map = new LinkedHashMap<String,String>();
    tree1map.put("TK_1", tree1);
    tf1.setTrees(tree1map);
    
    TreeFeatureVector tf2 = new TreeFeatureVector();
    LinkedHashMap<String,String> tree2map = new LinkedHashMap<String,String>();
    tree2map.put("TK_1", tree1);
    tf2.setTrees(tree2map);
    
    double sim = sst.evaluate(tf1, tf2);
    
    Assert.assertEquals(2.983040, sim, 0.01);
    
    String tree2 = "(S (NP i) (VP (VBD ran) (NN home)))";
    tree2map.clear();
    tree2map.put("TK_1", tree2);
    tf2.setTrees(tree2map);
    
    sim = sst.evaluate(tf1, tf2);
    Assert.assertEquals(0.96, sim, 0.01);
    
    TreeKernel ptk = new TreeKernel(TreeKernel.LAMBDA_DEFAULT, ForestSumMethod.SEQUENTIAL, KernelType.PARTIAL, false);
    String tree3 = "(NP (DT the) (NN dog))";
    String tree4 = "(NP (DT the) (JJ big) (NN dog))";
    tree1map.clear();
    tree1map.put("TK1", tree3);
    tree2map.clear();
    tree2map.put("TK1", tree4);
    tf1.setTrees(tree1map);
    tf2.setTrees(tree2map);
    sim = ptk.evaluate(tf1, tf2);
    double expected = 0.337027; // output of moschitti's code
    Assert.assertEquals(expected, sim, 0.01);
   
    sim = ptk.evaluate(tf1, tf1);
    expected = 0.337205;
    Assert.assertEquals(expected, sim, 0.01);
    
    sim = ptk.evaluate(tf2, tf2);
    expected = 0.474024;
    Assert.assertEquals(expected, sim, 0.01);
    
    tree2map.clear();
    tree2map.put("TK1", tree2);
    tf2.setTrees(tree2map);
    sim = ptk.evaluate(tf1, tf2);
    expected = 0.128;
    Assert.assertEquals(expected, sim, 0.01);
    
    ptk.setUseCache(true);
    sim = ptk.evaluate(tf1, tf2);
    Assert.assertEquals(expected, sim, 0.01);
  }
  
  @Test
  public void testPTKSpeed() {
    TreeKernel sst = new TreeKernel(TreeKernel.LAMBDA_DEFAULT, ForestSumMethod.SEQUENTIAL, KernelType.SUBSET, false);
    TreeKernel ptk = new TreeKernel(TreeKernel.LAMBDA_DEFAULT, ForestSumMethod.SEQUENTIAL, KernelType.PARTIAL, false);
    
    TreeFeatureVector tf1 = new TreeFeatureVector();
    String tree1 = "(S (NP i) (VP (VB eat) (NN cake)))";
    LinkedHashMap<String,String> tree1map = new LinkedHashMap<String,String>();
    tree1map.put("TK_1", tree1);
    tf1.setTrees(tree1map);
    
    TreeFeatureVector tf2 = new TreeFeatureVector();
    LinkedHashMap<String,String> tree2map = new LinkedHashMap<String,String>();
    tree2map.put("TK_1", tree1);
    tf2.setTrees(tree2map);

    long start = System.currentTimeMillis();
    final int NUM_ITERATIONS = 1000000;
    @SuppressWarnings("unused")
    double sim = 0.0;
    for(int i = 0; i < NUM_ITERATIONS; i++){
      sim += sst.evaluate(tf1, tf2);
    }
    long end = System.currentTimeMillis();
    System.out.println("Test on sst takes: " + (end-start) + " ms");
    
    ptk.setUseCache(true);
    start = System.currentTimeMillis();
    sim = 0.0;
    for(int i = 0; i < NUM_ITERATIONS; i++){
      sim += ptk.evaluate(tf1, tf2);
    }
    end = System.currentTimeMillis();
    System.out.println("Test on ptk takes: " + (end - start) + " ms");
    
    ptk.setUseCache(false);
    start = System.currentTimeMillis();
    sim = 0.0;
    for(int i = 0; i < NUM_ITERATIONS; i++){
      sim += ptk.evaluate(tf1, tf2);
    }
    end = System.currentTimeMillis();
    System.out.println("Test on cached ptk takes: " + (end - start) + " ms");    
  }

  @Test
  public void testTreeFeatures() throws Exception {
    TreeFeatureVectorFeaturesEncoder encoder = new TreeFeatureVectorFeaturesEncoder();
    encoder.addEncoder(new NumberEncoder());
    encoder.addEncoder(new BooleanEncoder());
    encoder.addEncoder(new StringEncoder());
    
    Feature feat = new Feature("Feature1");
    List<Feature> singletonList = Lists.newArrayList();
    singletonList.add(feat);
    
    // test that the encoder can handle features without names
    try{
      encoder.encodeAll(singletonList);
    }catch(Exception e){
        Assert.assertTrue(false);
    }
    
    List<Instance<Boolean>> instances = generateTreeFeatureInstances(100);
    for(Instance<Boolean> instance : instances){
      TreeFeatureVector features = encoder.encodeAll(instance.getFeatures());
      Map<String,String> treeFeatures = features.getTrees();
      Assert.assertTrue(treeFeatures.size() > 0);
    }
  }
  
  @Test
  public void testTKSVMlight() throws Exception {
    this.assumeTestsEnabled(COMMON_TESTS_PROPERTY_VALUE, TK_SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(TK_SVMLIGHT_TESTS_ENABLED_MESSAGE);

    // create the data writer
    EmptyAnnotator<Boolean> annotator = new EmptyAnnotator<Boolean>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        TkSvmLightBooleanOutcomeDataWriter.class.getName()));

    // add a bunch of instances
    for (Instance<Boolean> instance : generateBooleanInstances(20)) {
      annotator.write(instance);
    }
    annotator.collectionProcessComplete();

    // check that the output file was written and is not empty
    BufferedReader reader = new BufferedReader(new FileReader(new File(
        this.outputDirectoryName,
        "training-data.svmlight")));
    Assert.assertTrue(reader.readLine().length() > 0);
    reader.close();

    // run the training command
    HideOutput hider = new HideOutput();
    Train.main(this.outputDirectoryName, "-t", "5", "-c", "1.0", "-C", "+");
    hider.restoreOutput();

    // read in the classifier and test it on new instances
    TkSvmLightBooleanOutcomeClassifierBuilder builder = new TkSvmLightBooleanOutcomeClassifierBuilder();
    TreeKernelSvmBooleanOutcomeClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<Boolean> instance : generateBooleanInstances(20)) {
      List<Feature> features = instance.getFeatures();
      Boolean outcome = instance.getOutcome();
      hider = new HideOutput();
      Assert.assertEquals(outcome, classifier.classify(features));
      hider.restoreOutput();
    }
  }

  @Test
  public void testOVATKSVMlight() throws Exception {
    this.assumeTestsEnabled(COMMON_TESTS_PROPERTY_VALUE, TK_SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(TK_SVMLIGHT_TESTS_ENABLED_MESSAGE);

    // create the data writer
    EmptyAnnotator<String> annotator = new EmptyAnnotator<String>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        TkSvmLightStringOutcomeDataWriter.class.getName()));

    // add a bunch of instances
    for (Instance<String> instance : generateStringInstances(20)) {
      annotator.write(instance);
    }
    annotator.collectionProcessComplete();

    // check that the output files were written for each class
    for (String fileName : new String[] {
        "training-data-1.svmlight",
        "training-data-2.svmlight",
        "training-data-3.svmlight" }) {
      BufferedReader reader = new BufferedReader(new FileReader(new File(
          this.outputDirectoryName,
          fileName)));
      Assert.assertTrue(reader.readLine().length() > 0);
      reader.close();
    }

    // run the training command
    HideOutput hider = new HideOutput();
    Train.main(this.outputDirectoryName, "-c", "0.01", "-t", "5", "-d", "2", "-C", "+");
    hider.restoreOutput();

    // read in the classifier and test it on new instances
    TkSvmLightStringOutcomeClassifierBuilder builder = new TkSvmLightStringOutcomeClassifierBuilder();
    TreeKernelSvmStringOutcomeClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<String> instance : generateStringInstances(20)) {
      List<Feature> features = instance.getFeatures();
      String outcome = instance.getOutcome();
      Assert.assertEquals("Assert error with instance: " + instance.toString(), outcome, classifier.classify(features));
    }
  }

  private static List<Instance<Boolean>> generateBooleanInstances(int n) {
    Random random = new Random(42);
    List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
    for (int i = 0; i < n; i++) {
      Instance<Boolean> instance = new Instance<Boolean>();
      if (random.nextInt(2) == 0) {
        instance.setOutcome(true);
        instance.add(new Feature("TK_tree", "(S (NP I) (VB ran) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100) + 1000));
        instance.add(new Feature("goodbye", 500));
      } else {
        instance.setOutcome(false);
        instance.add(new Feature("TK_tree", "(S (VB I) (NP ran) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", 500));
      }
      instances.add(instance);
    }
    return instances;
  }

  private static List<Instance<Boolean>> generateTreeFeatureInstances(int n) {
    Random random = new Random(42);
    List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
    for (int i = 0; i < n; i++) {
      Instance<Boolean> instance = new Instance<Boolean>();
      if (random.nextInt(2) == 0) {
        instance.setOutcome(true);
        instance.add(new TreeFeature("Tree", "(S (NP I) (VB ran) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100) + 1000));
        instance.add(new Feature("goodbye", 500));
      } else {
        instance.setOutcome(false);
        instance.add(new TreeFeature("Tree", "(S (VB I) (NP ran) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", 500));
      }
      instances.add(instance);
    }
    return instances;
  }

  private static List<Instance<String>> generateStringInstances(int n) {
    Random random = new Random(42);
    List<Instance<String>> instances = new ArrayList<Instance<String>>();
    for (int i = 0; i < n; i++) {
      Instance<String> instance = new Instance<String>();
      int c = random.nextInt(3);
      if (c == 0) {
        instance.setOutcome("A");
        instance.add(new TreeFeature("Tree", "(S (NP I) (VB ran) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100) + 950));
        instance.add(new Feature("goodbye", random.nextInt(100)));
        instance.add(new Feature("farewell", random.nextInt(100)));
      } else if (c == 1) {
        instance.setOutcome("B");
        instance.add(new TreeFeature("Tree", "(S (TT going) (ZZ gone) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", random.nextInt(100) + 950));
        instance.add(new Feature("farewell", random.nextInt(100)));
      } else {
        instance.setOutcome("C");
        instance.add(new TreeFeature("Tree", "(S (DET The) (PP Fox) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", random.nextInt(100)));
        instance.add(new Feature("farewell", random.nextInt(100) + 950));
      }
      instances.add(instance);
    }
    return instances;
  }
}
