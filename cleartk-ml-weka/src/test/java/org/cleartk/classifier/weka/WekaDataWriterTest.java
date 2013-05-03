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
package org.cleartk.classifier.weka;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class WekaDataWriterTest extends DefaultTestBase {

  public static class Test1Annotator extends CleartkAnnotator<String> {

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

  // @Ignore
  @Test
  public void test1() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test1Annotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        WekaStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectory);
    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();
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
  public void test2() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test2Annotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        WekaStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.outputDirectory);
    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();
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
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
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
