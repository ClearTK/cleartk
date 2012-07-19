/** 
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
package org.cleartk.classifier.svmlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.svmlight.rank.QidInstance;
import org.cleartk.classifier.svmlight.rank.SVMlightRank;
import org.cleartk.classifier.svmlight.rank.SVMlightRankBuilder;
import org.cleartk.classifier.svmlight.rank.SVMlightRankDataWriter;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.SparseFeatureVector;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard, Philipp Wetzler
 */
public class RunSVMlightTest extends DefaultTestBase {

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

  protected String dataDirectory = "src/test/resources/data/svmlight";

  @Test
  public void testPath() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    String[] command = new String[] { "svm_learn" };

    try {
      Process process = Runtime.getRuntime().exec(command);
      process.getOutputStream().write('\n');
      process.getOutputStream().write('\n');
      process.getOutputStream().close();
      slurp(process.getInputStream());
      slurp(process.getErrorStream());
      process.waitFor();
    } catch (IOException e) {
      throw e;
    }
  }

  @Test
  public void testLinearKernel() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    File dir = new File(dataDirectory, "linear");
    File trainingFile = new File(dir, "training-data.svmlight");
    File testFile = new File(dir, "test-data.svmlight");

    trainAndTest(trainingFile, testFile, new String[] { "-t", "0" }, "linear kernel");
  }

  @Test
  public void testPolynomialKernel() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    File dir = new File(dataDirectory, "nonlinear");
    File trainingFile = new File(dir, "training-data.svmlight");
    File testFile = new File(dir, "test-data.svmlight");

    trainAndTest(trainingFile, testFile, new String[] { "-t", "1", "-d", "2" }, "quadratic kernel");
    trainAndTest(trainingFile, testFile, new String[] { "-t", "1", "-d", "3" }, "cubic kernel");
  }

  @Test
  public void testRBFKernel() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    File dir = new File(dataDirectory, "nonlinear");
    File trainingFile = new File(dir, "training-data.svmlight");
    File testFile = new File(dir, "test-data.svmlight");

    trainAndTest(trainingFile, testFile, new String[] { "-t", "2" }, "RBF kernel");
  }

  // @Test
  public void testSigmoidKernel() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);
    File dir = new File(dataDirectory, "nonlinear");
    File trainingFile = new File(dir, "training-data.svmlight");
    File testFile = new File(dir, "test-data.svmlight");

    trainAndTest(trainingFile, testFile, new String[] { "-t", "3" }, "sigmoid kernel");
  }

  private void trainAndTest(File trainingFile, File testFile, String[] args, String name)
      throws Exception {
    File modelFile = new File(this.outputDirectoryName, "model.svmlight");

    String[] command = new String[3 + args.length];
    command[0] = "svm_learn";
    for (int i = 0; i < args.length; i++)
      command[i + 1] = args[i];
    command[command.length - 2] = trainingFile.getPath();
    command[command.length - 1] = modelFile.getPath();

    Process process = Runtime.getRuntime().exec(command);
    slurp(process.getInputStream());
    output(process.getErrorStream(), System.err);
    process.waitFor();

    SVMlightModel model = SVMlightModel.fromFile(modelFile);
    BufferedReader r = new BufferedReader(new FileReader(testFile));
    float total = 0;
    float correct = 0;
    String line;
    while ((line = r.readLine()) != null) {
      String[] fields = line.split(" ");

      boolean expectedResult = fields[0].equals("+1");

      FeatureVector fv = new SparseFeatureVector();
      for (int i = 1; i < fields.length; i++) {
        String[] parts = fields[i].split(":");
        int featureIndex = Integer.valueOf(parts[0]);
        double featureValue = Double.valueOf(parts[1]);
        fv.set(featureIndex, featureValue);
      }

      boolean actualResult = model.evaluate(fv) > 0;

      total += 1;
      if (expectedResult == actualResult)
        correct += 1;
    }
    r.close();

    if (correct < (total * 0.95))
      Assert.fail("model accuracy using " + name + " is below 95%");
  }

  private static void output(InputStream input, PrintStream output) throws IOException {
    byte[] buffer = new byte[128];
    int count = input.read(buffer);
    while (count != -1) {
      output.write(buffer, 0, count);
      count = input.read(buffer);
    }
  }

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
  public void testSVMlight() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    // create the data writer
    EmptyAnnotator<Boolean> annotator = new EmptyAnnotator<Boolean>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        SVMlightDataWriter.class.getName()));

    // add a bunch of instances
    for (Instance<Boolean> instance : generateBooleanInstances(1000)) {
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
    Train.main(this.outputDirectoryName, "-c", "1.0");
    hider.restoreOutput();

    // read in the classifier and test it on new instances
    SVMlightClassifierBuilder builder = new SVMlightClassifierBuilder();
    SVMlightClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<Boolean> instance : generateBooleanInstances(1000)) {
      List<Feature> features = instance.getFeatures();
      Boolean outcome = instance.getOutcome();
      Assert.assertEquals(outcome, classifier.classify(features));
    }
  }

  @Test
  public void testOVASVMlight() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    // create the data writer
    EmptyAnnotator<String> annotator = new EmptyAnnotator<String>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        OVASVMlightDataWriter.class.getName()));

    // add a bunch of instances
    for (Instance<String> instance : generateStringInstances(1000)) {
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
    Train.main(this.outputDirectoryName, "-c", "0.01", "-t", "1", "-d", "2");
    hider.restoreOutput();

    // read in the classifier and test it on new instances
    OVASVMlightClassifierBuilder builder = new OVASVMlightClassifierBuilder();
    OVASVMlightClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<String> instance : generateStringInstances(1000)) {
      List<Feature> features = instance.getFeatures();
      String outcome = instance.getOutcome();
      Assert.assertEquals(outcome, classifier.classify(features));
    }
  }

  @Test
  public void testSVMlightRegression() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    // create the data writer
    EmptyAnnotator<Double> annotator = new EmptyAnnotator<Double>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        SVMlightRegressionDataWriter.class.getName()));

    // add instances
    for (double i = 0.0; i < 100; i += 2) {
      annotator.write(new Instance<Double>(2.0 * i + 5.0, Arrays.asList(new Feature("x", i))));
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
    Train.main(this.outputDirectoryName, "-c", "1", "-w", "0.0001");
    hider.restoreOutput();

    // read in the regression and test it on new instances
    SVMlightRegressionBuilder builder = new SVMlightRegressionBuilder();
    SVMlightRegression regression;
    regression = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (double i = 1.0; i < 100; i += 2) {
      double prediction = regression.classify(Arrays.asList(new Feature("x", i)));
      Assert.assertEquals(2.0 * i + 5.0, prediction, 0.0005);
    }
  }

  @Test
  public void testSVMlightRank() throws Exception {
    this.assumeTestsEnabled(SVMLIGHT_TESTS_PROPERTY_VALUE);
    this.logger.info(SVMLIGHT_TESTS_ENABLED_MESSAGE);

    // create the data writer
    EmptyAnnotator<Double> annotator = new EmptyAnnotator<Double>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        SVMlightRankDataWriter.class.getName()));

    System.out.println(outputDirectoryName);
    // add instances
    for (int qid = 10; qid < 15; qid++) {
      for (double i = 0.0; i < 10; i += 2) {
        QidInstance<Double> inst = new QidInstance<Double>();
        inst.setQid(Integer.toString(qid));
        inst.addAll(Arrays.asList(new Feature("x", i)));
        if (i >= 0.0 && i < 3) {
          inst.setOutcome(3.0);
        } else if (i >= 3 && i < 6) {
          inst.setOutcome(2.0);
        } else {
          inst.setOutcome(1.0);
        }
        annotator.write(inst);
      }

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
    Train.main(this.outputDirectoryName, "-c", "1", "-w", "0.0001");
    hider.restoreOutput();

    // read in the ranking SVM model and test it on new instances
    SVMlightRankBuilder builder = new SVMlightRankBuilder();
    SVMlightRank rank;
    rank = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (double i = 1.0; i < 100; i += 2) {
      double prediction = rank.classify(Arrays.asList(new Feature("x", i)));
      Assert.assertEquals(i / -2.0, prediction, 0.0005);
    }
  }

  private static List<Instance<Boolean>> generateBooleanInstances(int n) {
    Random random = new Random(42);
    List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
    for (int i = 0; i < n; i++) {
      Instance<Boolean> instance = new Instance<Boolean>();
      if (random.nextInt(2) == 0) {
        instance.setOutcome(true);
        instance.add(new Feature("hello", random.nextInt(100) + 1000));
        instance.add(new Feature("goodbye", 500));
      } else {
        instance.setOutcome(false);
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
        instance.add(new Feature("hello", random.nextInt(100) + 950));
        instance.add(new Feature("goodbye", random.nextInt(100)));
        instance.add(new Feature("farewell", random.nextInt(100)));
      } else if (c == 1) {
        instance.setOutcome("B");
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", random.nextInt(100) + 950));
        instance.add(new Feature("farewell", random.nextInt(100)));
      } else {
        instance.setOutcome("C");
        instance.add(new Feature("hello", random.nextInt(100)));
        instance.add(new Feature("goodbye", random.nextInt(100)));
        instance.add(new Feature("farewell", random.nextInt(100) + 950));
      }
      instances.add(instance);
    }
    return instances;
  }

}
