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
package org.cleartk.ml.feature.selection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Bag;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Covered;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.selection.MutualInformationFeatureSelectionExtractor.MutualInformationStats;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.test.util.type.Sentence;
import org.cleartk.test.util.type.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * <br>
 * Copyright (c) 2007-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Lee Becker
 */
public class MutualInformationTest extends DefaultTestBase {

  @Before
  public void localSetUp() throws Throwable {
    this.engine = AnalysisEngineFactory.createEngine(NoOpAnnotator.class);
    this.jCasObjects = new ArrayList<JCas>();
  }

  @Test
  public void testMutualInformationStats() {
    MutualInformationStats<String> stats = new MutualInformationStats<String>(0.0);
    stats.update("export", "poultry", 25);
    stats.update("export", "poultry", 24);
    stats.update("export", "not_poultry", 141);
    stats.update("not_export", "poultry", 27652);
    stats.update("not_export", "not_poultry", 774106);

    Assert.assertEquals((int) stats.classConditionalCounts.get("export", "poultry"), 49);
    Assert.assertEquals((int) stats.classConditionalCounts.get("export", "not_poultry"), 141);
    Assert.assertEquals((int) stats.classConditionalCounts.get("not_export", "poultry"), 27652);
    Assert.assertEquals(
        (int) stats.classConditionalCounts.get("not_export", "not_poultry"),
        774106);

    Assert.assertEquals(stats.classCounts.count("poultry"), 27701);
    Assert.assertEquals(stats.classCounts.count("not_poultry"), 774247);

    Assert.assertEquals(stats.mutualInformation("export", "poultry"), 0.0000766, 0.00000005);
    Assert.assertEquals(stats.mutualInformation("export", "not_poultry"), 0.0000766, 0.00000005);
    Assert.assertEquals(stats.mutualInformation("not_export", "poultry"), 0.0000766, 0.00000005);
    Assert.assertEquals(
        stats.mutualInformation("not_export", "not_poultry"),
        0.0000766,
        0.00000005);
  }

  @Test
  public void testMutualInformationFeatureSelection() throws Exception {

    CleartkExtractor<Sentence, Token> extractor = new CleartkExtractor<Sentence, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new Bag(new Covered()));

    MutualInformationFeatureSelectionExtractor<String, Sentence> miExtractor = new MutualInformationFeatureSelectionExtractor<String, Sentence>(
        "miExtractor",
        extractor,
        5);

    List<Instance<String>> instances = Lists.newArrayList();

    this.jCasObjects = Arrays.asList(
        this.buildJCas(CLASS_A_TEXT.toLowerCase(), "ClassA"),
        this.buildJCas(CLASS_B_TEXT.toLowerCase(), "ClassB"),
        this.buildJCas(CLASS_C_TEXT.toLowerCase(), "ClassC"));

    for (JCas jcas : jCasObjects) {
      JCas classView = jcas.getView("ClassView");
      for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
        Instance<String> inst = new Instance<String>(miExtractor.extract(jcas, s));
        inst.setOutcome(classView.getDocumentText());
        instances.add(inst);
      }
    }

    // Train and save extractor model
    miExtractor.train(instances);
    File tmpDir = Files.createTempDir();
    File miFile = new File(tmpDir, "mi.txt");
    miExtractor.save(miFile.toURI());

    // Load and check selected features
    miExtractor.load(miFile.toURI());
    List<String> selectedFeatures = miExtractor.getSelectedFeatures();
    Assert.assertEquals(5, selectedFeatures.size());
    Assert.assertEquals("Bag_Covered:monkey", selectedFeatures.get(0));
    Assert.assertEquals("Bag_Covered:girl", selectedFeatures.get(1));
    Assert.assertEquals("Bag_Covered:pig", selectedFeatures.get(2));
    Assert.assertEquals("Bag_Covered:wolf", selectedFeatures.get(3));
    Assert.assertEquals("Bag_Covered:boy", selectedFeatures.get(4));

    // Extract features using selected features model
    List<Feature> features = Lists.newArrayList();
    for (Sentence s : JCasUtil.select(this.jCasObjects.get(0), Sentence.class)) {
      features.addAll(miExtractor.extract(this.jCasObjects.get(0), s));
    }

    // Check that values are as specified
    Assert.assertTrue(features.contains(new Feature("Bag_Covered", "boy")));
    Assert.assertTrue(features.contains(new Feature("Bag_Covered", "pig")));
    Assert.assertTrue(features.contains(new Feature("Bag_Covered", "wolf")));
    Assert.assertFalse(features.contains(new Feature("Bag_Covered", "and")));
    Assert.assertFalse(features.contains(new Feature("Bag_Covered", "the")));
    Assert.assertFalse(features.contains(new Feature("Bag_Covered", ".")));
    Assert.assertFalse(features.contains(new Feature("Bag_Covered", "girl")));
  }

  public List<Instance<String>> createInstances(int n, String featureName, String outcome) {
    Instance<String> instance = new Instance<String>();
    instance.add(new Feature(featureName));
    instance.setOutcome(outcome);

    List<Instance<String>> instances = Lists.newArrayList();
    for (int i = 0; i < n; i++) {
      instances.add(instance);
    }
    return instances;
  }

  private static final String CLASS_A_TEXT = "The boy and the cat . \n" + "The boy and the dog . \n"
      + "The boy and the wolf . \n" + "The boy and the pig . \n";

  private static final String CLASS_B_TEXT = "The girl and the cat . \n"
      + "The girl and the dog . \n" + "The girl and the boy . \n" + "The girl and the chicken . \n";

  private static final String CLASS_C_TEXT = "The monkey and the cat . \n"
      + "The girl and the monkey . \n" + "The monkey and the boy . \n"
      + "The monkey and the chicken . \n";

  private JCas buildJCas(String text, String outcome) throws UIMAException {
    JCas newJCas = this.engine.newJCas();
    this.tokenBuilder.buildTokens(newJCas, text);
    JCas classView = newJCas.createView("ClassView");
    classView.setDocumentText(outcome);
    return newJCas;
  }

  private AnalysisEngine engine;

  private List<JCas> jCasObjects;

}
