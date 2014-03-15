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
package org.cleartk.classifier.feature.function;

import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.test.util.type.Token;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public class FeatureFunctionsTest extends DefaultTestBase {

  private void testOne(
      FeatureFunction featureFunction,
      String origName,
      String origValue,
      String newName,
      String newValue) {
    Feature origFeature = new Feature(origName, origValue);
    List<Feature> newFeatures = featureFunction.apply(origFeature);
    String value = newFeatures.size() == 0 ? null : newFeatures.get(0).getValue().toString();
    String name = newFeatures.size() == 0 ? null : newFeatures.get(0).getName();
    Assert.assertTrue(newFeatures.size() >= 0 && newFeatures.size() <= 1);
    Assert.assertEquals(newValue, value);
    Assert.assertEquals(newName, name);
  }

  @Test
  public void testLowerCaseFeatureFunction() {
    this.testOne(new LowerCaseFeatureFunction(), null, "HI", "LowerCase", "hi");
    this.testOne(new LowerCaseFeatureFunction(), "OrigName", "", "LowerCase_OrigName", "");
  }

  @Test
  public void testCapitalTypeFeatureFunction() {
    FeatureFunction caps = new CapitalTypeFeatureFunction();

    this.testOne(
        caps,
        null,
        "HI",
        "CapitalType",
        CapitalTypeFeatureFunction.CapitalType.ALL_UPPERCASE.toString());
    this.testOne(
        caps,
        "OrigName",
        "hi",
        "CapitalType_OrigName",
        CapitalTypeFeatureFunction.CapitalType.ALL_LOWERCASE.toString());
    this.testOne(
        caps,
        null,
        "hi",
        "CapitalType",
        CapitalTypeFeatureFunction.CapitalType.ALL_LOWERCASE.toString());
    this.testOne(
        caps,
        "OrigName",
        "Hi",
        "CapitalType_OrigName",
        CapitalTypeFeatureFunction.CapitalType.INITIAL_UPPERCASE.toString());
    this.testOne(
        caps,
        null,
        "HigH",
        "CapitalType",
        CapitalTypeFeatureFunction.CapitalType.MIXED_CASE.toString());
    this.testOne(caps, "OrigName", "!234", null, null);
    this.testOne(
        caps,
        "OrigName",
        "!@#a@#\\$",
        "CapitalType_OrigName",
        CapitalTypeFeatureFunction.CapitalType.ALL_LOWERCASE.toString());
    this.testOne(caps, null, "\t\n", null, null);

  }

  @Test
  public void testNumericTypeFeatureFunction() {
    FeatureFunction nums = new NumericTypeFeatureFunction();

    this.testOne(nums, null, "HI", null, null);
    this.testOne(nums, "OrigName", "", null, null);
    this.testOne(nums, null, "\t\t", null, null);
    this.testOne(
        nums,
        "OrigName",
        "HI2",
        "NumericType_OrigName",
        NumericTypeFeatureFunction.NumericType.ALPHANUMERIC.toString());
    this.testOne(
        nums,
        null,
        "222",
        "NumericType",
        NumericTypeFeatureFunction.NumericType.DIGITS.toString());
    this.testOne(
        nums,
        "OrigName",
        "2222",
        "NumericType_OrigName",
        NumericTypeFeatureFunction.NumericType.DIGITS.toString());
    this.testOne(
        nums,
        null,
        "2122",
        "NumericType",
        NumericTypeFeatureFunction.NumericType.YEAR_DIGITS.toString());
    this.testOne(
        nums,
        "OrigName",
        "2022",
        "NumericType_OrigName",
        NumericTypeFeatureFunction.NumericType.YEAR_DIGITS.toString());
    this.testOne(
        nums,
        null,
        "1022",
        "NumericType",
        NumericTypeFeatureFunction.NumericType.YEAR_DIGITS.toString());
    this.testOne(
        nums,
        "OrigName",
        "0022",
        "NumericType_OrigName",
        NumericTypeFeatureFunction.NumericType.DIGITS.toString());
    this.testOne(
        nums,
        null,
        "0",
        "NumericType",
        NumericTypeFeatureFunction.NumericType.DIGITS.toString());
    this.testOne(
        nums,
        "OrigName",
        "asdfASDF1234",
        "NumericType_OrigName",
        NumericTypeFeatureFunction.NumericType.ALPHANUMERIC.toString());
    this.testOne(
        nums,
        null,
        "1F1234",
        "NumericType",
        NumericTypeFeatureFunction.NumericType.ALPHANUMERIC.toString());
    this.testOne(
        nums,
        "OrigName",
        "10-1234",
        "NumericType_OrigName",
        NumericTypeFeatureFunction.NumericType.SOME_DIGITS.toString());
    this.testOne(
        nums,
        null,
        "1F1234!",
        "NumericType",
        NumericTypeFeatureFunction.NumericType.SOME_DIGITS.toString());
    this.testOne(
        nums,
        "OrigName",
        "!!12!",
        "NumericType_OrigName",
        NumericTypeFeatureFunction.NumericType.SOME_DIGITS.toString());
    this.testOne(
        nums,
        null,
        "10,000",
        "NumericType",
        NumericTypeFeatureFunction.NumericType.SOME_DIGITS.toString());
  }

  @Test
  public void testCharacterNGramFeatureFunction() {
    FeatureFunction triSuff = new CharacterNgramFeatureFunction(
        CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT,
        0,
        3,
        7,
        false);
    this.testOne(triSuff, "OrigName", "emotion", "NGram_Right_0_3_7_OrigName", "ion");
    this.testOne(triSuff, null, "motion", null, null);
    this.testOne(triSuff, "OrigName", "locomotive", "NGram_Right_0_3_7_OrigName", "ive");

    FeatureFunction triPre = new CharacterNgramFeatureFunction(
        "TriPre",
        CharacterNgramFeatureFunction.Orientation.LEFT_TO_RIGHT,
        0,
        3,
        3,
        false);
    this.testOne(triPre, "OrigName", "LOCOMOTIVE", "NGram_Left_0_3_3_TriPre_OrigName", "LOC");
    this.testOne(triPre, null, "LOC", "NGram_Left_0_3_3_TriPre", "LOC");
    this.testOne(triPre, "OrigName", "lo", null, null);

    FeatureFunction left12 = new CharacterNgramFeatureFunction(
        CharacterNgramFeatureFunction.Orientation.LEFT_TO_RIGHT,
        1,
        3,
        8,
        false);
    this.testOne(left12, null, "locomotive", "NGram_Left_1_3_8", "oc");

    FeatureFunction left5 = new CharacterNgramFeatureFunction(
        "FooBar",
        CharacterNgramFeatureFunction.Orientation.LEFT_TO_RIGHT,
        5,
        6,
        8,
        false);
    this.testOne(left5, "OrigName", "abcdefghi", "NGram_Left_5_6_8_FooBar_OrigName", "f");

    FeatureFunction right46lower = new CharacterNgramFeatureFunction(
        CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT,
        2,
        4,
        6,
        true);
    this.testOne(right46lower, null, "abcdefghi", "NGram_Right_2_4_6_lower", "fg");

    FeatureFunction right3 = new CharacterNgramFeatureFunction(
        CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT,
        0,
        3);
    this.testOne(right3, null, "Foo", "NGram_Right_0_3_3", "Foo");
    this.testOne(right3, null, "Fo", null, null);
    this.testOne(right3, "OrigName", "Food", "NGram_Right_0_3_3_OrigName", "ood");
  }

  @Test
  public void testFeatureFunctionExtractor() throws Throwable {
    jCas.setDocumentText("Hello World 2008!");
    Token hello = new Token(jCas, 0, 5);
    Token year = new Token(jCas, 12, 16);

    FeatureExtractor1<Token> textAndLower = new FeatureFunctionExtractor<Token>(
        new CoveredTextExtractor<Token>(),
        new LowerCaseFeatureFunction());
    List<Feature> features = textAndLower.extract(jCas, hello);
    Assert.assertEquals(2, features.size());
    Assert.assertEquals(null, features.get(0).getName());
    Assert.assertEquals("Hello", features.get(0).getValue());
    Assert.assertEquals("LowerCase", features.get(1).getName());
    Assert.assertEquals("hello", features.get(1).getValue());

    String yearDigits = NumericTypeFeatureFunction.NumericType.YEAR_DIGITS.toString();
    FeatureExtractor1<Token> textAndCapsAndNums = new FeatureFunctionExtractor<Token>(
        new CoveredTextExtractor<Token>(),
        new CapitalTypeFeatureFunction(),
        new NumericTypeFeatureFunction());
    features = textAndCapsAndNums.extract(jCas, year);
    Assert.assertEquals(2, features.size());
    Assert.assertEquals(null, features.get(0).getName());
    Assert.assertEquals("2008", features.get(0).getValue());
    Assert.assertEquals("NumericType", features.get(1).getName());
    Assert.assertEquals(yearDigits, features.get(1).getValue());

    String initialUpper = CapitalTypeFeatureFunction.CapitalType.INITIAL_UPPERCASE.toString();
    FeatureExtractor1<Token> textAndCapsAndLower = new FeatureFunctionExtractor<Token>(
        new CoveredTextExtractor<Token>(),
        new CapitalTypeFeatureFunction(),
        new LowerCaseFeatureFunction());
    features = textAndCapsAndLower.extract(jCas, hello);
    Assert.assertEquals(3, features.size());
    Assert.assertEquals(null, features.get(0).getName());
    Assert.assertEquals("Hello", features.get(0).getValue());
    Assert.assertEquals("CapitalType", features.get(1).getName());
    Assert.assertEquals(initialUpper, features.get(1).getValue());
    Assert.assertEquals("LowerCase", features.get(2).getName());
    Assert.assertEquals("hello", features.get(2).getValue());
  }

}
