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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.classifier.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.classifier.feature.function.FeatureFunctionExtractor;
import org.cleartk.classifier.feature.function.FeatureFunctionExtractor.BaseFeatures;
import org.cleartk.classifier.feature.function.LowerCaseFeatureFunction;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Token;
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
    assertEquals("part-of-speech", feature.getName());
    assertEquals("VBZ", feature.getValue());
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

}
