/*
 * Copyright (c) 2013, Regents of the University of Colorado 
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

package org.cleartk.classifier.feature;

import static org.cleartk.classifier.ClassifierTestUtil.assertFeature;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Bag;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Focus;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Ngram;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.classifier.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.classifier.feature.function.FeatureFunctionExtractor;
import org.cleartk.classifier.feature.function.FeatureFunctionExtractor.BaseFeatures;
import org.cleartk.classifier.feature.function.LowerCaseFeatureFunction;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Chunk;
import org.cleartk.type.test.Lemma;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 */

public class FeatureExtractionTutorialTest extends DefaultTestBase {

  @Test
  public void test1() throws Exception {
    Feature feature = new Feature("part-of-speech", "VBZ");
    assertFeature("part-of-speech", "VBZ", feature);
  }

  // text from http://www.gutenberg.org/files/3177/3177-h/3177-h.htm#linkch05
  @Test
  public void test2() throws Exception {
    this.tokenBuilder.buildTokens(
        this.jCas,
        "The cayote is a long , slim , sick and sorry-looking skeleton , with a gray wolf-skin stretched over it , a tolerably bushy tail that forever sags down with a despairing expression of forsakenness and misery , a furtive and evil eye , and a long , sharp face , with slightly lifted lip and exposed teeth .");

    List<Feature> features = new ArrayList<Feature>();
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 1);
    assertEquals("cayote", token.getCoveredText());

    features.add(new Feature("token length", token.getCoveredText().length()));
    assertEquals(6, features.get(0).getValue());
    features.add(new Feature("vowel groups", vowelGroupings(token.getCoveredText())));
    assertEquals(3, features.get(1).getValue());
    features.add(new Feature("consonant groups", consonantGroupings(token.getCoveredText())));
    assertEquals(3, features.get(2).getValue());
    features.add(new Feature("is first letter 'c'?", token.getCoveredText().charAt(0) == 'c'));
    assertTrue((Boolean) (features.get(3).getValue()));
  }

  public static int vowelGroupings(String s) {
    Pattern pattern = Pattern.compile("[aeiou]+");
    Matcher matcher = pattern.matcher(s);
    int vowelGroups = 0;
    while (matcher.find()) {
      vowelGroups++;
    }
    return vowelGroups;
  }

  public static int consonantGroupings(String s) {
    Pattern pattern = Pattern.compile("[bcdfghjklmnpqrstvwxyz]+");
    Matcher matcher = pattern.matcher(s);
    int vowelGroups = 0;
    while (matcher.find()) {
      vowelGroups++;
    }
    return vowelGroups;
  }

  @Test
  public void test3() throws Exception {
    this.tokenBuilder.buildTokens(
        this.jCas,
        "The cayote is a long , slim , sick and sorry-looking skeleton , with a gray wolf-skin stretched over it , a tolerably bushy tail that forever sags down with a despairing expression of forsakenness and misery , a furtive and evil eye , and a long , sharp face , with slightly lifted lip and exposed teeth .");

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 1);
    FeatureExtractor1<Token> extractor = new CoveredTextExtractor<Token>();
    List<Feature> features = extractor.extract(this.jCas, token);
    assertEquals(1, features.size());
    assertEquals("cayote", features.get(0).getValue());

    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    extractor = new FeatureFunctionExtractor<Token>(
        extractor,
        BaseFeatures.EXCLUDE,
        new LowerCaseFeatureFunction());
    features = extractor.extract(this.jCas, token);
    assertEquals(1, features.size());
    assertEquals("the", features.get(0).getValue());
  }

  @Test
  public void test4() throws Exception {
    this.tokenBuilder.buildTokens(
        this.jCas,
        "This is a short sentence.",
        "This is a short sentence . ",
        "DT VB DT JJ NN .");
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 3);

    FeatureExtractor1<Token> extractor = new TypePathExtractor<Token>(Token.class, "pos");
    List<Feature> features = extractor.extract(this.jCas, token);
    assertEquals(1, features.size());
    assertEquals("JJ", features.get(0).getValue());
    // System.out.println(features.get(0).getName());
  }

  @Test
  public void test5() throws Exception {
    this.tokenBuilder.buildTokens(this.jCas, "This is a short sentence.");
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);

    FeatureExtractor1<Token> extractor = new FeatureFunctionExtractor<Token>(
        new CoveredTextExtractor<Token>(),
        new CapitalTypeFeatureFunction());
    List<Feature> features = extractor.extract(this.jCas, token);
    assertEquals(2, features.size());
    assertEquals("This", features.get(0).getValue());
    assertEquals(
        CapitalTypeFeatureFunction.CapitalType.INITIAL_UPPERCASE.toString(),
        features.get(1).getValue());
  }

  // Get the text of the 2 tokens before a focus token
  @Test
  public void testCleartkExtractor1() throws Exception {
    CleartkExtractor<Token, Token> extractor = new CleartkExtractor<Token, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new Preceding(2));
    this.tokenBuilder.buildTokens(this.jCas, "The quick brown fox jumped over the lazy dog.");

    Token fox = JCasUtil.selectByIndex(jCas, Token.class, 3);

    List<Feature> features = extractor.extract(this.jCas, fox);
    assertEquals(2, features.size());
    assertFeature("Preceding_0_2_1", "quick", features.get(0));
    assertFeature("Preceding_0_2_0", "brown", features.get(1));
  }

  // Get the part-of-speech tags of the 3 tokens after a focus annotation
  @Test
  public void testCleartkExtractor2() throws Exception {
    CleartkExtractor<Chunk, Token> extractor = new CleartkExtractor<Chunk, Token>(
        Token.class,
        new TypePathExtractor<Token>(Token.class, "pos"),
        new Following(3));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");

    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    assertEquals(3, features.size());

    assertFeature("Following_0_3_0_TypePath(Pos)", "DT", features.get(0));
    assertFeature("Following_0_3_1_TypePath(Pos)", "JJ", features.get(1));
    assertFeature("Following_0_3_2_TypePath(Pos)", "NN", features.get(2));
  }

  // Get the tokens after a focus annotation, beginning 2 after and ending 5 after, as a bag of
  // words

  @Test
  public void testCleartkExtractor3() throws Exception {
    CleartkExtractor<Chunk, Token> extractor = new CleartkExtractor<Chunk, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new Bag(new Following(2, 5)));

    this.tokenBuilder.buildTokens(this.jCas, "The quick brown fox jumped over the lazy dog .");

    Chunk chunk = new Chunk(this.jCas, 4, 9);
    chunk.addToIndexes();
    Assert.assertEquals("quick", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    assertEquals(3, features.size());

    assertEquals("Bag_Following_2_5", features.get(0).getName());
    assertEquals("jumped", features.get(0).getValue());
    assertEquals("Bag_Following_2_5", features.get(1).getName());
    assertEquals("over", features.get(1).getValue());
    assertEquals("Bag_Following_2_5", features.get(2).getName());
    assertEquals("the", features.get(2).getValue());

    // I considered adding the following description of the out-of-bounds case to the tutorial but
    // decided it was too arcane and describes behavior that is perhaps not correct.
    // If the specified range goes past the last token in the JCas, then "out-of-bounds" features
    // will be generated. Such features have names whose prefix is {{{OOB}}} followed by a digit
    // corresponding to how far out of the range the feature is. You may choose to filter out this
    // features.

    chunk = new Chunk(this.jCas, 20, 35);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over the", chunk.getCoveredText());

    features = extractor.extract(this.jCas, chunk);
    assertEquals(3, features.size());

    assertEquals("Bag_Following_2_5", features.get(0).getName());
    assertEquals(".", features.get(0).getValue());
    assertEquals("Bag_Following_2_5", features.get(1).getName());
    assertEquals("OOB1", features.get(1).getValue());
    assertEquals("Bag_Following_2_5", features.get(2).getName());
    assertEquals("OOB2", features.get(2).getValue());

    // you get the same behavior with extractWithin
    Sentence sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    features = extractor.extractWithin(this.jCas, chunk, sentence);
    assertEquals("Bag_Following_2_5", features.get(0).getName());
    assertEquals(".", features.get(0).getValue());
    assertEquals("Bag_Following_2_5", features.get(1).getName());
    assertEquals("OOB1", features.get(1).getValue());
    assertEquals("Bag_Following_2_5", features.get(2).getName());
    assertEquals("OOB2", features.get(2).getValue());

  }

  // Get an ngram concatenating the stem of the first word before a focus annotation and the first
  // word contained in the focus annotation
  @Test
  public void testCleartkExtractor4() throws Exception {
    CleartkExtractor<Token, Token> extractor = new CleartkExtractor<Token, Token>(
        Token.class,
        new TypePathExtractor<Token>(Token.class, "lemma/value"),
        new Ngram(new Preceding(1), new Focus()));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");

    // not a best practices lemmatizer!
    JCasUtil.selectByIndex(this.jCas, Token.class, 0).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 0).getLemma().setValue("the");
    JCasUtil.selectByIndex(this.jCas, Token.class, 1).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 1).getLemma().setValue("quick");
    JCasUtil.selectByIndex(this.jCas, Token.class, 2).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 2).getLemma().setValue("brown");
    JCasUtil.selectByIndex(this.jCas, Token.class, 3).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 3).getLemma().setValue("fox");
    JCasUtil.selectByIndex(this.jCas, Token.class, 4).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 4).getLemma().setValue("jump");
    JCasUtil.selectByIndex(this.jCas, Token.class, 5).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 5).getLemma().setValue("over");
    JCasUtil.selectByIndex(this.jCas, Token.class, 6).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 6).getLemma().setValue("the");
    JCasUtil.selectByIndex(this.jCas, Token.class, 7).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 7).getLemma().setValue("lazy");
    JCasUtil.selectByIndex(this.jCas, Token.class, 8).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 8).getLemma().setValue("dog");
    JCasUtil.selectByIndex(this.jCas, Token.class, 9).setLemma(new Lemma(jCas));
    JCasUtil.selectByIndex(this.jCas, Token.class, 9).getLemma().setValue(".");

    Token jumped = JCasUtil.selectByIndex(this.jCas, Token.class, 4);
    Assert.assertEquals("jumped", jumped.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, jumped);
    assertEquals(1, features.size());

    assertEquals("Ngram_Preceding_0_1_Focus_TypePath(LemmaValue)", features.get(0).getName());
    assertEquals("fox_jump", features.get(0).getValue());

  }
}

// for (Feature feature : features) {
// System.out.println(feature.getName());
// System.out.println(feature.getValue());
// }
