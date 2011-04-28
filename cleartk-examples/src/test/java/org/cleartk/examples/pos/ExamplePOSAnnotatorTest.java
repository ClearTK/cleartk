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
package org.cleartk.examples.pos;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.util.PublicFieldSequenceDataWriter;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.examples.ExamplesTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */
public class ExamplePOSAnnotatorTest extends ExamplesTestBase {

  @Test
  public void testSimpleSentence() throws Exception {

    AnalysisEngineDescription desc = CleartkAnnotatorDescriptionFactory.createCleartkSequenceAnnotator(
        ExamplePOSAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        PublicFieldSequenceDataWriter.StringFactory.class,
        ".");
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(desc);

    // create some tokens, stems and part of speech tags
    tokenBuilder.buildTokens(
        jCas,
        "The Absurdis retreated in 2003.",
        "The Absurdis retreated in 2003 .",
        "DT NNP VBD IN CD .",
        "The Absurdi retreat in 2003 .");

    List<Instance<String>> instances = PublicFieldSequenceDataWriter.StringFactory.collectInstances(
        engine,
        jCas);

    List<String> featureValues;

    // check "The"
    featureValues = Arrays.asList("The", // word
        "The", // stem (thrown away if null)
        "the", // lower case
        "INITIAL_UPPERCASE", // capital type
        // numeric type
        "he", // last 2 chars
        "The", // last 3 chars
        "OOB2", // left 2 stems
        "OOB1",
        "Absurdi", // right 2 stems
        "retreat");
    Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(0)));
    Assert.assertEquals("DT", instances.get(0).getOutcome());

    // check "Absurdis"
    featureValues = Arrays.asList("Absurdi", // word
        "Absurdis", // stem (thrown away if null)
        "absurdis", // lower case
        "INITIAL_UPPERCASE", // capital type
        // numeric type
        "is", // last 2 chars
        "dis", // last 3 chars
        "OOB1", // left 2 stems
        "The",
        "retreat", // right 2 stems
        "in");
    Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(1)));
    Assert.assertEquals("NNP", instances.get(1).getOutcome());

    // check "retreated"
    featureValues = Arrays.asList("retreat", // word
        "retreated", // stem (thrown away if null)
        "retreated", // lower case
        "ALL_LOWERCASE", // capital type
        // numeric type
        "ed", // last 2 chars
        "ted", // last 3 chars
        "The", // left 2 stems
        "Absurdi", // right 2 stems
        "in",
        "2003");
    Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(2)));
    Assert.assertEquals("VBD", instances.get(2).getOutcome());

    // check "in"
    featureValues = Arrays.asList("in", // word
        "in", // stem (thrown away if null)
        "in", // lower case
        "ALL_LOWERCASE", // capital type
        // numeric type
        "in", // last 2 chars
        // last 3 chars
        "Absurdi", // left 2 stems
        "retreat",
        "2003", // right 2 stems
        ".");
    Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(3)));
    Assert.assertEquals("IN", instances.get(3).getOutcome());

    // check "2003"
    featureValues = Arrays.asList("2003", // word
        "2003", // stem (thrown away if null)
        "2003", // lower case
        // capital type
        "YEAR_DIGITS", // numeric type
        "03", // last 2 chars
        "003", // last 3 chars
        "retreat", // left 2 stems
        "in",
        ".", // right 2 stems
        "OOB1");
    Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(4)));
    Assert.assertEquals("CD", instances.get(4).getOutcome());

    // check "."
    featureValues = Arrays.asList(".", // word
        ".", // stem (thrown away if null)
        ".", // lower case
        // capital type
        // numeric type
        // last 2 chars
        // last 3 chars
        "in", // left 2 stems
        "2003",
        "OOB1", // right 2 stems
        "OOB2");
    Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(5)));
    Assert.assertEquals(".", instances.get(5).getOutcome());
  }

  @Test
  public void testAnnotatorDescriptor() throws Exception {
    HideOutput hider = new HideOutput();
    BuildTestExamplePosModel.main();
    hider.restoreOutput();

    AnalysisEngineDescription posTaggerDescription = ExamplePOSAnnotator.getClassifierDescription(ExamplePOSAnnotator.DEFAULT_MODEL);
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(posTaggerDescription);

    Object classifierJar = engine.getConfigParameterValue(GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH);
    Assert.assertEquals(ExamplePOSAnnotator.DEFAULT_MODEL, classifierJar);

    engine.collectionProcessComplete();
  }

  @Test
  public void testDataWriterDescriptor() throws UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(ExamplePOSAnnotator.getWriterDescription(ExamplePOSAnnotator.DEFAULT_OUTPUT_DIRECTORY));

    String outputDir = (String) engine.getConfigParameterValue(DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY);
    outputDir = outputDir.replace(File.separatorChar, '/');
    Assert.assertEquals(ExamplePOSAnnotator.DEFAULT_OUTPUT_DIRECTORY, outputDir);

    String expectedDataWriterFactory = (ViterbiDataWriterFactory.class.getName());
    Object dataWriter = engine.getConfigParameterValue(CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME);
    Assert.assertEquals(expectedDataWriterFactory, dataWriter);
    engine.collectionProcessComplete();
  }

  private List<String> getFeatureValues(Instance<String> instance) {
    List<String> values = new ArrayList<String>();
    for (Feature feature : instance.getFeatures()) {
      Object value = feature == null ? null : feature.getValue();
      values.add(value == null ? null : value.toString());
    }
    return values;
  }

}
