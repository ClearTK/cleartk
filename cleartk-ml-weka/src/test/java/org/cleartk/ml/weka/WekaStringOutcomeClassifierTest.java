/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.ml.weka;

import static org.cleartk.ml.util.InstanceFactory.createInstance;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.jar.Train;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;

import weka.classifiers.functions.Logistic;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Majid Laali
 */
public class WekaStringOutcomeClassifierTest extends DefaultTestBase {
  public static class Test1Annotator extends CleartkAnnotator<String> {

    @Override
    public void process(JCas cas) throws AnalysisEngineProcessException {
      if (this.isTraining()) {
        this.dataWriter.write(createInstance("A", "hello", 1234));
        this.dataWriter.write(createInstance("A", "hello", 1000));
        this.dataWriter.write(createInstance("A", "hello", 900));
        this.dataWriter.write(createInstance("A", "hello", 1500));
        this.dataWriter.write(createInstance("A", "hello", 2000));
        this.dataWriter.write(createInstance("A", "hello", 1235));
        this.dataWriter.write(createInstance("A", "hello", 1001));
        this.dataWriter.write(createInstance("A", "hello", 901));
        this.dataWriter.write(createInstance("A", "hello", 1501));
        this.dataWriter.write(createInstance("A", "hello", 2001));
        this.dataWriter.write(createInstance("A", "hello", 1502));
        this.dataWriter.write(createInstance("A", "hello", 2003));

        this.dataWriter.write(createInstance("B", "hello", 10));
        this.dataWriter.write(createInstance("B", "hello", 8));
        this.dataWriter.write(createInstance("B", "hello", 60));
        this.dataWriter.write(createInstance("B", "hello", 80));
        this.dataWriter.write(createInstance("B", "hello", 4));
        this.dataWriter.write(createInstance("B", "hello", 11));
        this.dataWriter.write(createInstance("B", "hello", 5));
        this.dataWriter.write(createInstance("B", "hello", 61));
        this.dataWriter.write(createInstance("B", "hello", 81));
        this.dataWriter.write(createInstance("B", "hello", 7));
        this.dataWriter.write(createInstance("B", "hello", 82));
        this.dataWriter.write(createInstance("B", "hello", 3));

        this.dataWriter.write(createInstance("C", "goodbye", 1));
        this.dataWriter.write(createInstance("C", "goodbye", 8));
        this.dataWriter.write(createInstance("C", "goodbye", 60));
        this.dataWriter.write(createInstance("C", "goodbye", 81));
        this.dataWriter.write(createInstance("C", "goodbye", 4));
        this.dataWriter.write(createInstance("C", "goodbye", 11));
        this.dataWriter.write(createInstance("C", "goodbye", 7));
        this.dataWriter.write(createInstance("C", "goodbye", 61));
        this.dataWriter.write(createInstance("C", "goodbye", 82));
        this.dataWriter.write(createInstance("C", "goodbye", 2));
        this.dataWriter.write(createInstance("C", "goodbye", 83));
        this.dataWriter.write(createInstance("C", "goodbye", 1));
      } else {
        String classification = this.classifier.classify(
            createInstance("", "hello", 1000).getFeatures());
        assertEquals("A", classification);
        classification = this.classifier.classify(createInstance("", "hello", 1).getFeatures());
        assertEquals("B", classification);
        classification = this.classifier.classify(createInstance("", "goodbye", 1).getFeatures());
        assertEquals("C", classification);
      }
    }
  }

  @Test
  public void test1() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createEngine(
        Test1Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        WekaStringOutcomeDataWriter.class.getName());

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new WekaStringOutcomeClassifierBuilder().getTrainingDataFile(
        this.outputDirectory);
    BufferedReader reader = new BufferedReader(new FileReader(trainFile));
    String line = reader.readLine();
    assertEquals("@relation cleartk-generated", line);
    reader.close();

    HideOutput hider = new HideOutput();
    Train.main(outputDirectoryName, Logistic.class.getName(), "-R 1.0E-8 -M -1");
    hider.restoreOutput();
    WekaStringOutcomeClassifierBuilder builder = new WekaStringOutcomeClassifierBuilder();
    WekaStringOutcomeClassifier classifier = builder.loadClassifierFromTrainingDirectory(
        this.outputDirectory);
    String classification = classifier.classify(createInstance(null, "hello", 1000).getFeatures());
    assertEquals("A", classification);
    classification = classifier.classify(createInstance(null, "hello", 1).getFeatures());
    assertEquals("B", classification);
    classification = classifier.classify(createInstance(null, "goodbye", 1).getFeatures());
    assertEquals("C", classification);

    AnalysisEngine classifierAnnotator = AnalysisEngineFactory.createEngine(
        Test1Annotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        JarClassifierBuilder.getModelJarFile(outputDirectoryName));
    jCas.reset();
    classifierAnnotator.process(jCas);
    classifierAnnotator.collectionProcessComplete();
  }

  public static class Test2Annotator extends CleartkAnnotator<String> {

    @Override
    public void process(JCas cas) throws AnalysisEngineProcessException {
      if (this.isTraining()) {
        this.dataWriter.write(createInstance("A", "hello", "1234"));
        this.dataWriter.write(createInstance("A", "hello", "1000"));
        this.dataWriter.write(createInstance("A", "hello", "900"));
        this.dataWriter.write(createInstance("A", "hello", "1500"));
        this.dataWriter.write(createInstance("A", "hello", "2000"));
        this.dataWriter.write(createInstance("A", "hello", "1235"));
        this.dataWriter.write(createInstance("A", "hello", "1001"));
        this.dataWriter.write(createInstance("A", "hello", "901"));
        this.dataWriter.write(createInstance("A", "hello", "1501"));
        this.dataWriter.write(createInstance("A", "hello", "2001"));
        this.dataWriter.write(createInstance("A", "hello", "1502"));
        this.dataWriter.write(createInstance("A", "hello", "2003"));

        this.dataWriter.write(createInstance("B", "hello", "10"));
        this.dataWriter.write(createInstance("B", "hello", "8"));
        this.dataWriter.write(createInstance("B", "hello", "60"));
        this.dataWriter.write(createInstance("B", "hello", "80"));
        this.dataWriter.write(createInstance("B", "hello", "4"));
        this.dataWriter.write(createInstance("B", "hello", "11"));
        this.dataWriter.write(createInstance("B", "hello", "5"));
        this.dataWriter.write(createInstance("B", "hello", "61"));
        this.dataWriter.write(createInstance("B", "hello", "81"));
        this.dataWriter.write(createInstance("B", "hello", "7"));
        this.dataWriter.write(createInstance("B", "hello", "82"));
        this.dataWriter.write(createInstance("B", "hello", "3"));

        this.dataWriter.write(createInstance("C", "goodbye", "1"));
        this.dataWriter.write(createInstance("C", "goodbye", "8"));
        this.dataWriter.write(createInstance("C", "goodbye", "60"));
        this.dataWriter.write(createInstance("C", "goodbye", "81"));
        this.dataWriter.write(createInstance("C", "goodbye", "4"));
        this.dataWriter.write(createInstance("C", "goodbye", "11"));
        this.dataWriter.write(createInstance("C", "goodbye", "7"));
        this.dataWriter.write(createInstance("C", "goodbye", "61"));
        this.dataWriter.write(createInstance("C", "goodbye", "82"));
        this.dataWriter.write(createInstance("C", "goodbye", "2"));
        this.dataWriter.write(createInstance("C", "goodbye", "83"));
        this.dataWriter.write(createInstance("C", "goodbye", "1"));
      } else {
        String classification = this.classifier.classify(
            createInstance("", "hello", "1000").getFeatures());
        assertEquals("A", classification);
        classification = this.classifier.classify(createInstance("", "hello", "1").getFeatures());
        assertEquals("B", classification);
        classification = this.classifier.classify(createInstance("", "goodbye", "1").getFeatures());
        assertEquals("C", classification);
      }
    }
  }

  @Test
  public void test2() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createEngine(
        Test2Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        WekaStringOutcomeDataWriter.class.getName());

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new WekaStringOutcomeClassifierBuilder().getTrainingDataFile(
        this.outputDirectory);
    BufferedReader reader = new BufferedReader(new FileReader(trainFile));
    String line = reader.readLine();
    assertEquals("@relation cleartk-generated", line);
    reader.close();

    HideOutput hider = new HideOutput();
    Train.main(outputDirectoryName, Logistic.class.getName(), "-R 1.0E-8 -M -1");
    hider.restoreOutput();
    WekaStringOutcomeClassifierBuilder builder = new WekaStringOutcomeClassifierBuilder();
    WekaStringOutcomeClassifier classifier = builder.loadClassifierFromTrainingDirectory(
        this.outputDirectory);
    String classification = classifier.classify(
        createInstance(null, "hello", "1000").getFeatures());
    assertEquals("A", classification);
    classification = classifier.classify(createInstance(null, "hello", "1").getFeatures());
    assertEquals("B", classification);
    classification = classifier.classify(createInstance(null, "goodbye", "1").getFeatures());
    assertEquals("C", classification);

    AnalysisEngine classifierAnnotator = AnalysisEngineFactory.createEngine(
        Test1Annotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        JarClassifierBuilder.getModelJarFile(outputDirectoryName));
    jCas.reset();
    classifierAnnotator.process(jCas);
    classifierAnnotator.collectionProcessComplete();
  }
}
