/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.ml.libsvm.tk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.TreeFeature;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.tksvmlight.TreeKernelSVMBooleanOutcomeClassifier;
import org.cleartk.classifier.tksvmlight.TreeKernelSVMStringOutcomeClassifier;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @author Tim Miller
 */
public class RunTKLIBSVMTest extends DefaultTestBase {

  protected String dataDirectory = "src/test/resources/data/libsvm-tk";

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
  public void testTKLIBSVM() throws Exception {
    // create the data writer
    EmptyAnnotator<Boolean> annotator = new EmptyAnnotator<Boolean>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        TKLIBSVMBooleanOutcomeDataWriter.class.getName()));

    // add a bunch of instances
    for (Instance<Boolean> instance : generateBooleanInstances(20)) {
      annotator.write(instance);
    }
    annotator.collectionProcessComplete();

    // check that the output file was written and is not empty
    BufferedReader reader = new BufferedReader(new FileReader(new File(
        this.outputDirectoryName,
        "training-data.libsvm")));
    Assert.assertTrue(reader.readLine().length() > 0);
    reader.close();

    // run the training command
    HideOutput hider = new HideOutput();
    Train.main(this.outputDirectoryName, "-t", "5", "-c", "1.0", "-C", "+");
    hider.restoreOutput();

    // read in the classifier and test it on new instances
    TKLIBSVMBooleanOutcomeClassifierBuilder builder = new TKLIBSVMBooleanOutcomeClassifierBuilder();
    TreeKernelSVMBooleanOutcomeClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<Boolean> instance : generateBooleanInstances(20)) {
      List<Feature> features = instance.getFeatures();
      Boolean outcome = instance.getOutcome();
      hider = new HideOutput();
      Assert.assertEquals(outcome, classifier.classify(features));
      hider.restoreOutput();
    }
    hider.close();
  }

  @Test
  public void testOVATKLIBSVM() throws Exception {
    // create the data writer
    EmptyAnnotator<String> annotator = new EmptyAnnotator<String>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        TKLIBSVMStringOutcomeDataWriter.class.getName()));

    // add a bunch of instances
    for (Instance<String> instance : generateStringInstances(20)) {
      annotator.write(instance);
    }
    annotator.collectionProcessComplete();

    // check that the output files were written for each class
    for (String fileName : new String[] {
        "training-data-1.libsvm",
        "training-data-2.libsvm",
        "training-data-3.libsvm" }) {
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
    TKLIBSVMStringOutcomeClassifierBuilder builder = new TKLIBSVMStringOutcomeClassifierBuilder();
    TreeKernelSVMStringOutcomeClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<String> instance : generateStringInstances(20)) {
      List<Feature> features = instance.getFeatures();
      String outcome = instance.getOutcome();
      Assert.assertEquals("Assert error with instance: " + instance.toString(), outcome, classifier.classify(features));
    }
    hider.close();
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
