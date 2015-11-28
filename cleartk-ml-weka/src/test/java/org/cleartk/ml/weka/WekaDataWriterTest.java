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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * @author Majid Laali
 */

public class WekaDataWriterTest extends DefaultTestBase {
  private File getFile(String testName){
    String fileName = getClass().getName().replace('.', '/') + "-" + testName;
    return new File(getClass().getClassLoader().getResource(fileName).getFile());
  }
  
  public void runTest(Class<? extends CleartkAnnotator<String>> annotatorClass, String expectedFileName) throws Exception{
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createEngine(
        annotatorClass,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        WekaStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectory);
    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    assertThat(outputDirectory).exists();
    String actual = FileUtils.readFileToString(new File(outputDirectory, WekaStringOutcomeClassifierBuilder.TRAINING_FILE_NAME));
    String expected = FileUtils.readFileToString(getFile(expectedFileName));
    assertThat(actual).isEqualTo(expected);
  }

  public static class NumericFeaturesAnnotator extends CleartkAnnotator<String> {

    public void process(JCas cas) throws AnalysisEngineProcessException {
      List<Feature> features = Arrays.asList(
          new Feature("A", 1.1),
          new Feature("B", 3.0),
          new Feature("C", 1.234));
      Instance<String> instance = new Instance<String>("yes", features);
      this.dataWriter.write(instance);

      features = Arrays.asList(
          new Feature("A", 2.1),
          new Feature("B", 2.0),
          new Feature("C", 2.234));
      instance = new Instance<String>("no", features);
      this.dataWriter.write(instance);

      features = Arrays.asList(
          new Feature("A", 5.1),
          new Feature("B", 5.0),
          new Feature("C", 5.234));
      instance = new Instance<String>("yes", features);
      this.dataWriter.write(instance);
      
    }
  }

  @Test
  public void whenWritingNumuricFeaturesThenGeneratedArffFilesIsFine() throws Exception {
    runTest(NumericFeaturesAnnotator.class, "numericFeatures.arff");
  }

  public static class StringFeaturesAnnotator extends CleartkAnnotator<String> {

    public void process(JCas cas) throws AnalysisEngineProcessException {
      List<Feature> features = Arrays.asList(
          new Feature("normal", "a")
          , new Feature("space", "space")
          , new Feature("apostrophe", "apostrophe")
          );
      Instance<String> instance = new Instance<String>("yes", features);
      this.dataWriter.write(instance);

      features = Arrays.asList(
          new Feature("normal", "b")
          , new Feature("space", "space ")
          , new Feature("apostrophe", "apostrophe=")
          );
      instance = new Instance<String>("no", features);
      this.dataWriter.write(instance);

      features = Arrays.asList(
          new Feature("normal", "c")
          , new Feature("space", "space= ")
          , new Feature("apostrophe", "apostrophe='")
          );
      instance = new Instance<String>("yes", features);
      this.dataWriter.write(instance);
      
    }
  }

  
  /*
   * WEKA does not support string features very well and most classifier cannot handle features with String type.
   * Therefore, it is better to convert them to Enum type.
   */
  @Test
  public void whenWritingStringFeaturesThenTheyAreSavedAsEnumType() throws Exception {
    runTest(StringFeaturesAnnotator.class, "stringFeatures.arff");
  }
  


  
  public static class Test2Annotator extends CleartkAnnotator<String> {

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
  public void test2() throws Exception{
    runTest(Test2Annotator.class, "test2.arff");
  }

  public static class TestIssue339Annotator extends CleartkAnnotator<String> {

    public void process(JCas cas) throws AnalysisEngineProcessException {
      List<Feature> features = Arrays.asList(new Feature("pos", "NN"));
      Instance<String> instance = new Instance<String>("A", features);
      this.dataWriter.write(instance);
    }
  }

  @Test
  public void testIssue339() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createEngine(
        TestIssue339Annotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        WekaStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectory);
    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();
    File outputFile = new File(this.outputDirectory, "training-data.arff");
    String output = Files.toString(outputFile, Charsets.US_ASCII);
    
    // make sure that at least one instance was written
    Pattern emptyData = Pattern.compile("@data\\s*\\{\\}");
    boolean hasEmptyData = emptyData.matcher(output).find();
    Assert.assertFalse(hasEmptyData);
    
    // make sure that the "NN" value shows up
    Assert.assertTrue(output.contains("0 NN"));
  }
}
