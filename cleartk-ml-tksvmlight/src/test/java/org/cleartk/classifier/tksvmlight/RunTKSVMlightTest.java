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
package org.cleartk.classifier.tksvmlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.Train;
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
 */
public class RunTKSVMlightTest extends DefaultTestBase {

  protected String dataDirectory = "src/test/resources/data/svmlight/tk";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  protected boolean testPath() throws Exception {
    String[] command = new String[] { "tk_svm_learn" };

    try {
      Process process = Runtime.getRuntime().exec(command);
      process.getOutputStream().write('\n');
      process.getOutputStream().write('\n');
      process.getOutputStream().close();
      slurp(process.getInputStream());
      slurp(process.getErrorStream());
      process.waitFor();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  // @Test
  // public void testTreeKernel() throws Exception {
  /*
   * File dir = new File(dataDirectory, "nonlinear"); File trainingFile = new File(dir,
   * "training-data.svmlight"); File testFile = new File(dir, "test-data.svmlight");
   * 
   * trainAndTest(trainingFile, testFile, new String[] { "-t", "5" }, "tree kernel");
   */
  // }
  /*
   * private void trainAndTest(File trainingFile, File testFile, String[] args, String name) throws
   * Exception { File modelFile = new File(this.outputDirectoryName, "model.svmlight");
   * 
   * String[] command = new String[3 + args.length]; command[0] = "svm_learn"; for (int i = 0; i <
   * args.length; i++) command[i + 1] = args[i]; command[command.length - 2] =
   * trainingFile.getPath(); command[command.length - 1] = modelFile.getPath();
   * 
   * Process process = Runtime.getRuntime().exec(command); slurp(process.getInputStream());
   * output(process.getErrorStream(), System.err); process.waitFor();
   * 
   * SVMlightModel model = SVMlightModel.fromFile(modelFile); BufferedReader r = new
   * BufferedReader(new FileReader(testFile)); float total = 0; float correct = 0; String line;
   * while ((line = r.readLine()) != null) { String[] fields = line.split(" ");
   * 
   * boolean expectedResult = fields[0].equals("+1");
   * 
   * FeatureVector fv = new SparseFeatureVector(); for (int i = 1; i < fields.length; i++) {
   * String[] parts = fields[i].split(":"); int featureIndex = Integer.valueOf(parts[0]); double
   * featureValue = Double.valueOf(parts[1]); fv.set(featureIndex, featureValue); }
   * 
   * boolean actualResult = model.evaluate(fv) > 0;
   * 
   * total += 1; if (expectedResult == actualResult) correct += 1; } r.close();
   * 
   * if (correct < (total * 0.95)) Assert.fail("model accuracy using " + name + " is below 95%"); }
   * 
   * private static void output(InputStream input, PrintStream output) throws IOException { byte[]
   * buffer = new byte[128]; int count = input.read(buffer); while (count != -1) {
   * output.write(buffer, 0, count); count = input.read(buffer); } }
   */
  private static void slurp(InputStream input) throws IOException {
    byte[] buffer = new byte[128];
    int count = input.read();
    while (count != -1) {
      count = input.read(buffer);
    }
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
  public void testTKSVMlight() throws Exception {
    assumeTkSvmLightEnabled();
    this.logger.info(TK_SVMLIGHT_TEST_MESSAGE);

    // create the data writer
    EmptyAnnotator<Boolean> annotator = new EmptyAnnotator<Boolean>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        TKSVMlightDataWriter.class.getName()));

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
    TKSVMlightClassifierBuilder builder = new TKSVMlightClassifierBuilder();
    TKSVMlightClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<Boolean> instance : generateBooleanInstances(20)) {
      List<Feature> features = instance.getFeatures();
      Boolean outcome = instance.getOutcome();
      Assert.assertEquals(outcome, classifier.classify(features));
    }
  }

  @Test
  public void testOVATKSVMlight() throws Exception {
    assumeTkSvmLightEnabled();
    this.logger.info(TK_SVMLIGHT_TEST_MESSAGE);

    // create the data writer
    EmptyAnnotator<String> annotator = new EmptyAnnotator<String>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        OVATKSVMlightDataWriter.class.getName()));

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
    OVATKSVMlightClassifierBuilder builder = new OVATKSVMlightClassifierBuilder();
    OVATKSVMlightClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<String> instance : generateStringInstances(20)) {
      List<Feature> features = instance.getFeatures();
      String outcome = instance.getOutcome();
      Assert.assertEquals(outcome, classifier.classify(features));
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

  private static List<Instance<String>> generateStringInstances(int n) {
    Random random = new Random(42);
    List<Instance<String>> instances = new ArrayList<Instance<String>>();
    for (int i = 0; i < n; i++) {
      Instance<String> instance = new Instance<String>();
      int c = random.nextInt(3);
      if (c == 0) {
        instance.setOutcome("A");
        instance.add(new Feature("TK_tree", "(S (NP I) (VB ran) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100) + 950));
        instance.add(new Feature("goodbye", random.nextInt(100)));
        instance.add(new Feature("farewell", random.nextInt(100)));
      } else if (c == 1) {
        instance.setOutcome("B");
        instance.add(new Feature("TK_tree", "(S (TT going) (ZZ gone) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", random.nextInt(100) + 950));
        instance.add(new Feature("farewell", random.nextInt(100)));
      } else {
        instance.setOutcome("C");
        instance.add(new Feature("TK_tree", "(S (DET The) (PP Fox) (. .))"));
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", random.nextInt(100)));
        instance.add(new Feature("farewell", random.nextInt(100) + 950));
      }
      instances.add(instance);
    }
    return instances;
  }

  @Test
  public void testname() throws Exception {
    String skipTests = "long,bigMem,tkSvm";
    String[] values = skipTests.split("\\s*[,]\\s*");
    for (String string : values) {
      System.out.println(string);
    }
  }
}
