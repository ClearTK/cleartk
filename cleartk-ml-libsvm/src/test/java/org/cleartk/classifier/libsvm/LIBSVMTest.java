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
package org.cleartk.classifier.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.libsvm.ExampleInstanceFactory.BooleanAnnotator;
import org.cleartk.classifier.libsvm.ExampleInstanceFactory.StringAnnotator;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class LIBSVMTest extends DefaultTestBase {

  @Test
  public void testBinaryLIBSVM() throws Exception {
    // create the data writer
    BooleanAnnotator annotator = new BooleanAnnotator();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        LIBSVMBooleanOutcomeDataWriter.class.getName()));

    // run process to produce a bunch of instances
    annotator.process(null);

    annotator.collectionProcessComplete();

    // check that the output file was written and is not empty
    BufferedReader reader = new BufferedReader(new FileReader(new File(
        this.outputDirectoryName,
        "training-data.libsvm")));
    Assert.assertTrue(reader.readLine().length() > 0);
    reader.close();

    // run the training command
    HideOutput hider = new HideOutput();
    Train.main(this.outputDirectoryName, "-c", "1.0", "-s", "0", "-t", "0");
    hider.restoreOutput();

    // read in the classifier and test it on new instances
    LIBSVMBooleanOutcomeClassifierBuilder builder = new LIBSVMBooleanOutcomeClassifierBuilder();
    LIBSVMBooleanOutcomeClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<Boolean> instance : ExampleInstanceFactory.generateBooleanInstances(1000)) {
      List<Feature> features = instance.getFeatures();
      Boolean outcome = instance.getOutcome();
      Assert.assertEquals(outcome, classifier.classify(features));

      List<ScoredOutcome<Boolean>> scoredOutcomes = classifier.score(features, 1);
      Assert.assertEquals(1, scoredOutcomes.size());
      Assert.assertEquals(outcome, scoredOutcomes.get(0).getOutcome());
      scoredOutcomes = classifier.score(features, 2);
      Assert.assertEquals(2, scoredOutcomes.size());
      Assert.assertTrue(scoredOutcomes.get(0).getScore() > scoredOutcomes.get(1).getScore());
      scoredOutcomes = classifier.score(features, Integer.MAX_VALUE);
    }
  }

  @Test
  public void testMultiClassLIBSVM() throws Exception {
    // create the data writer
    StringAnnotator annotator = new StringAnnotator();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        LIBSVMStringOutcomeDataWriter.class.getName()));

    // run process to produce a bunch of instances
    annotator.process(null);

    annotator.collectionProcessComplete();

    // check that the output files were written for each class
    BufferedReader reader = new BufferedReader(new FileReader(new File(
        this.outputDirectoryName,
        "training-data.libsvm")));
    Assert.assertTrue(reader.readLine().length() > 0);
    reader.close();

    // run the training command
    HideOutput hider = new HideOutput();
    Train.main(this.outputDirectoryName, "-c", "10", "-t", "2");
    hider.restoreOutput();

    // read in the classifier and test it on new instances
    LIBSVMStringOutcomeClassifierBuilder builder = new LIBSVMStringOutcomeClassifierBuilder();
    LIBSVMStringOutcomeClassifier classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (Instance<String> instance : ExampleInstanceFactory.generateStringInstances(1000)) {
      List<Feature> features = instance.getFeatures();
      String outcome = instance.getOutcome();
      Assert.assertEquals(outcome, classifier.classify(features));

      List<ScoredOutcome<String>> scoredOutcomes = classifier.score(features, 1);
      Assert.assertEquals(1, scoredOutcomes.size());
      Assert.assertEquals(outcome, scoredOutcomes.get(0).getOutcome());
      scoredOutcomes = classifier.score(features, 2);
      Assert.assertEquals(2, scoredOutcomes.size());
      Assert.assertTrue(scoredOutcomes.get(0).getScore() > scoredOutcomes.get(1).getScore());
      scoredOutcomes = classifier.score(features, Integer.MAX_VALUE);
    }
  }
}
