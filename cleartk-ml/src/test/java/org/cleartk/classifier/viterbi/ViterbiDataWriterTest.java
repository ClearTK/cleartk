/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.classifier.viterbi;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.test.DefaultStringTestDataWriterFactory;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */

public class ViterbiDataWriterTest extends DefaultTestBase {

  public static class TestAnnotator extends CleartkSequenceAnnotator<String> {

    private SimpleFeatureExtractor extractor = new SpannedTextExtractor();

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
        List<Instance<String>> instances = new ArrayList<Instance<String>>();
        List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
        for (Token token : tokens) {
          Instance<String> instance = new Instance<String>();
          instance.addAll(this.extractor.extract(jCas, token));
          instance.setOutcome(token.getPos());
          instances.add(instance);
        }
        if (this.isTraining()) {
          this.dataWriter.write(instances);
        } else {
          this.classify(instances);
        }
      }
    }
  }

  @Test
  public void testConsumeAll() throws Exception {

    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        TestAnnotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        ViterbiDataWriterFactory.PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS,
        DefaultStringTestDataWriterFactory.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { "org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor" });

    String text = "Do I really have to come up with some creative text, or can I just write anything?";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "Do I really have to come up with some creative text , or can I just write anything ?",
        "D I R H T C U W S C T , O C I J W A ?");

    engine.process(jCas);
    engine.collectionProcessComplete();

    String expectedManifest = "Manifest-Version: 1.0\n"
        + "classifierBuilderClass: org.cleartk.classifier.viterbi.ViterbiClassifi\n" + " erBuilder";

    File manifestFile = new File(outputDirectoryName, "MANIFEST.MF");
    String actualManifest = FileUtils.readFileToString(manifestFile);
    Assert.assertEquals(expectedManifest, actualManifest.replaceAll("\r", "").trim());

    ViterbiClassifierBuilder<String> builder = new ViterbiClassifierBuilder<String>();
    File delegatedOutputDirectory = builder.getDelegatedModelDirectory(outputDirectory);
    String[] trainingData = FileUtil.loadListOfStrings(new File(
        delegatedOutputDirectory,
        "training-data.test"));
    testFeatures(trainingData[1], "PreviousOutcome_L1_D");
    testFeatures(
        trainingData[2],
        "PreviousOutcome_L1_I",
        "PreviousOutcome_L2_D",
        "PreviousOutcomes_L1_2gram_L2R_I_D");
    testFeatures(
        trainingData[3],
        "PreviousOutcome_L1_R",
        "PreviousOutcome_L2_I",
        "PreviousOutcome_L3_D",
        "PreviousOutcomes_L1_2gram_L2R_R_I",
        "PreviousOutcomes_L1_3gram_L2R_R_I_D");
    testFeatures(
        trainingData[4],
        "PreviousOutcome_L1_H",
        "PreviousOutcome_L2_R",
        "PreviousOutcome_L3_I",
        "PreviousOutcomes_L1_2gram_L2R_H_R",
        "PreviousOutcomes_L1_3gram_L2R_H_R_I");

    HideOutput hider = new HideOutput();
    Train.main(outputDirectoryName + "/", "10", "1");
    hider.restoreOutput();

    engine = AnalysisEngineFactory.createPrimitive(
        TestAnnotator.class,
        typeSystemDescription,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(outputDirectoryName, "model.jar").getPath());

    engine.process(jCas);
    engine.collectionProcessComplete();

  }

  private void testFeatures(String trainingDataLine, String... expectedFeatures) {
    Set<String> features = new HashSet<String>();
    features.addAll(Arrays.asList(trainingDataLine.split(" ")));
    for (String expectedFeature : expectedFeatures) {
      assertTrue(features.contains(expectedFeature));
    }
  }

}
