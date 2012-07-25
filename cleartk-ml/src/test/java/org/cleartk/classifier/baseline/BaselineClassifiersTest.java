/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.classifier.baseline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

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
import org.junit.Test;
import org.uimafit.factory.UimaContextFactory;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class BaselineClassifiersTest extends DefaultTestBase {

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
  public void testMostFrequentStringClassifier() throws Exception {
    // create the data writer
    EmptyAnnotator<String> annotator = new EmptyAnnotator<String>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MostFrequentStringDataWriter.class.getName()));

    // add a bunch of instances, labels are about 1/3 "Three" and 2/3 "Not-Three"
    for (int i = 0; i < 100; ++i) {
      List<Feature> features = Arrays.asList(new Feature("x"));
      annotator.write(new Instance<String>(i % 3 == 0 ? "Three" : "Not-Three", features));
    }
    annotator.collectionProcessComplete();

    // check that the output file was written and is not empty
    MostFrequentStringClassifierBuilder builder;
    builder = new MostFrequentStringClassifierBuilder();
    BufferedReader reader = new BufferedReader(new FileReader(
        builder.getTrainingDataFile(this.outputDirectory)));
    Assert.assertTrue(reader.readLine().length() > 0);
    reader.close();

    // run the training command
    Train.main(this.outputDirectoryName);

    // read in the classifier and test it on new instances
    SingleOutcomeClassifier<String> classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (int i = 0; i < 100; ++i) {
      List<Feature> features = Arrays.asList(new Feature("x", i));
      Assert.assertEquals("Not-Three", classifier.classify(features));
    }
  }

  @Test
  public void testMostFrequentBooleanClassifier() throws Exception {
    // create the data writer
    EmptyAnnotator<Boolean> annotator = new EmptyAnnotator<Boolean>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MostFrequentBooleanDataWriter.class.getName()));

    // add a bunch of instances, labels are about 1/3 true and 2/3 false
    for (int i = 0; i < 100; ++i) {
      annotator.write(new Instance<Boolean>(i % 3 == 0, Arrays.asList(new Feature("x"))));
    }
    annotator.collectionProcessComplete();

    // check that the output file was written and is not empty
    MostFrequentBooleanClassifierBuilder builder;
    builder = new MostFrequentBooleanClassifierBuilder();
    BufferedReader reader = new BufferedReader(new FileReader(
        builder.getTrainingDataFile(this.outputDirectory)));
    Assert.assertTrue(reader.readLine().length() > 0);
    reader.close();

    // run the training command
    Train.main(this.outputDirectoryName);

    // read in the classifier and test it on new instances
    SingleOutcomeClassifier<Boolean> classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (int i = 0; i < 100; ++i) {
      List<Feature> features = Arrays.asList(new Feature("x", i));
      Assert.assertEquals(false, classifier.classify(features));
    }
  }

  @Test
  public void testMeanValueClassifier() throws Exception {
    // create the data writer
    EmptyAnnotator<Double> annotator = new EmptyAnnotator<Double>();
    annotator.initialize(UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MeanValueDataWriter.class.getName()));

    // add a bunch of instances, labels are equally 0, 1, 2 and 3
    for (double i = 0.0; i < 100.0; ++i) {
      annotator.write(new Instance<Double>(i % 4, Arrays.asList(new Feature("x"))));
    }
    annotator.collectionProcessComplete();

    // check that the output file was written and is not empty
    MeanValueClassifierBuilder builder;
    builder = new MeanValueClassifierBuilder();
    BufferedReader reader = new BufferedReader(new FileReader(
        builder.getTrainingDataFile(this.outputDirectory)));
    Assert.assertTrue(reader.readLine().length() > 0);
    reader.close();

    // run the training command
    Train.main(this.outputDirectoryName);

    // read in the classifier and test it on new instances
    SingleOutcomeClassifier<Double> classifier;
    classifier = builder.loadClassifierFromTrainingDirectory(this.outputDirectory);
    for (int i = 0; i < 100; ++i) {
      List<Feature> features = Arrays.asList(new Feature("x", i));
      Assert.assertEquals(1.5, classifier.classify(features), 0.0);
    }
  }
}
