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
import org.cleartk.classifier.feature.function.FeatureFunctionExtractor;
import org.cleartk.classifier.feature.function.LowerCaseFeatureFunction;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Token;
import org.junit.Test;

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
        false,
        new LowerCaseFeatureFunction());
    features = extractor.extract(this.jCas, token);
    assertEquals(1, features.size());
    assertEquals("the", features.get(0).getValue());
  }
}
