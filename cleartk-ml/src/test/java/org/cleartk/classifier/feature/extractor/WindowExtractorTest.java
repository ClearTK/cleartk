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
package org.cleartk.classifier.feature.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Header;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.junit.Test;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * 
 */
@SuppressWarnings("deprecation")
public class WindowExtractorTest extends DefaultTestBase {

  @Test
  public void testGetStartAnnotation() throws UIMAException {
    // TODO think about what the behavior should be if window annotation
    // boundary does not fall at boundary
    // of featureAnnotation
    WindowExtractor leftEx = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_LEFT,
        0,
        3);
    WindowExtractor rightEx = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_RIGHT,
        0,
        3);
    WindowExtractor middleEx = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_MIDDLE,
        0,
        3);
    WindowExtractor middleRevEx = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_MIDDLE_REVERSE,
        0,
        3);

    tokenBuilder.buildTokens(jCas, "because the island was only");
    Annotation focusAnnotation = JCasUtil.selectByIndex(jCas, Token.class, 2);
    assertEquals("island", focusAnnotation.getCoveredText());
    Annotation startAnnotation = leftEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("the", startAnnotation.getCoveredText());
    startAnnotation = rightEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("was", startAnnotation.getCoveredText());
    startAnnotation = middleEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("island", startAnnotation.getCoveredText());
    startAnnotation = middleRevEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("island", startAnnotation.getCoveredText());

    jCas.reset();
    tokenBuilder.buildTokens(jCas, "to the top , the sides was so steep and the bushes so thick .");
    focusAnnotation = new Annotation(jCas, 7, 53);
    assertEquals("top , the sides was so steep and the bushes so", focusAnnotation.getCoveredText());
    startAnnotation = leftEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("the", startAnnotation.getCoveredText());
    startAnnotation = rightEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("thick", startAnnotation.getCoveredText());
    startAnnotation = middleEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("top", startAnnotation.getCoveredText());
    startAnnotation = middleRevEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("so", startAnnotation.getCoveredText());

    jCas.reset();
    tokenBuilder.buildTokens(
        jCas,
        "the side towards Illinois .\n"
            + "The cavern was as big as two or three rooms bunched together , and Jim could stand up straight in it .\n"
            + "It was cool in there .");
    focusAnnotation = JCasUtil.selectByIndex(jCas, Sentence.class, 1);
    assertEquals(
        "The cavern was as big as two or three rooms bunched together , and Jim could stand up straight in it .",
        focusAnnotation.getCoveredText());
    startAnnotation = leftEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals(".", startAnnotation.getCoveredText());
    startAnnotation = rightEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("It", startAnnotation.getCoveredText());
    startAnnotation = middleEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("The", startAnnotation.getCoveredText());
    startAnnotation = middleRevEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals(".", startAnnotation.getCoveredText());

    jCas.reset();
    tokenBuilder.buildTokens(jCas, "text obtained from");
    focusAnnotation = JCasUtil.selectByIndex(jCas, Token.class, 0);
    assertEquals("text", focusAnnotation.getCoveredText());
    startAnnotation = leftEx.getStartAnnotation(jCas, focusAnnotation);
    assertNull(startAnnotation);
    startAnnotation = rightEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("obtained", startAnnotation.getCoveredText());
    startAnnotation = middleEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("text", startAnnotation.getCoveredText());
    startAnnotation = middleRevEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("text", startAnnotation.getCoveredText());

    jCas.reset();
    tokenBuilder.buildTokens(jCas, "and cooked dinner .");
    focusAnnotation = JCasUtil.selectByIndex(jCas, Token.class, -1);
    assertEquals(".", focusAnnotation.getCoveredText());
    startAnnotation = leftEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals("dinner", startAnnotation.getCoveredText());
    startAnnotation = rightEx.getStartAnnotation(jCas, focusAnnotation);
    assertNull(startAnnotation);
    startAnnotation = middleEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals(".", startAnnotation.getCoveredText());
    startAnnotation = middleRevEx.getStartAnnotation(jCas, focusAnnotation);
    assertEquals(".", startAnnotation.getCoveredText());

  }

  @Test
  public void testExtractLeft() throws Throwable {
    WindowExtractor leftEx03 = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_LEFT,
        0,
        3);
    WindowExtractor leftEx24 = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_LEFT,
        2,
        4);

    // feature extraction on "island" in "...because the island was only..."
    tokenBuilder.buildTokens(jCas, "it , because the island was only");
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 4);
    assertEquals("island", token.getCoveredText());
    List<Feature> features = leftEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    WindowFeature feature = (WindowFeature) features.get(0);
    assertEquals(WindowFeature.ORIENTATION_LEFT, feature.getOrientation());
    assertEquals(0, feature.getPosition());
    assertEquals("the", feature.getValue().toString().toString());
    assertEquals("Window_L0", feature.getName());
    feature = (WindowFeature) features.get(1);
    assertEquals(WindowFeature.ORIENTATION_LEFT, feature.getOrientation());
    assertEquals(1, feature.getPosition());
    assertEquals("because", feature.getValue().toString());
    assertEquals("Window_L1", feature.getName());
    feature = (WindowFeature) features.get(2);
    assertEquals(WindowFeature.ORIENTATION_LEFT, feature.getOrientation());
    assertEquals(2, feature.getPosition());
    assertEquals(",", feature.getValue().toString());
    assertEquals("Window_L2", feature.getName());

    features = leftEx24.extract(jCas, token, Sentence.class);
    assertEquals(2, features.size());
    feature = (WindowFeature) features.get(0);
    assertEquals(",", feature.getValue().toString());
    assertEquals(WindowFeature.ORIENTATION_LEFT, feature.getOrientation());
    assertEquals(2, feature.getPosition());
    feature = (WindowFeature) features.get(1);
    assertEquals("it", feature.getValue().toString());

    // Feature extraction on "place" in "...This place was a tolerable..."
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "a mile wide .\nThis place was a tolerable long");
    token = JCasUtil.selectByIndex(jCas, Token.class, 5);
    assertEquals("place", token.getCoveredText());
    features = leftEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    feature = (WindowFeature) features.get(0);
    assertEquals(WindowFeature.ORIENTATION_LEFT, feature.getOrientation());
    assertEquals(0, feature.getPosition());
    assertEquals("This", feature.getValue().toString());
    feature = (WindowFeature) features.get(1);
    assertEquals(WindowFeature.ORIENTATION_LEFT, feature.getOrientation());
    assertEquals(1, feature.getPosition());
    assertNull(feature.getWindowedFeature());
    assertEquals(1, feature.getOutOfBoundsDistance());
    feature = (WindowFeature) features.get(2);
    assertEquals(WindowFeature.ORIENTATION_LEFT, feature.getOrientation());
    assertEquals(2, feature.getPosition());
    assertEquals(2, feature.getOutOfBoundsDistance());

    features = leftEx24.extract(jCas, token, Sentence.class);
    assertEquals(2, features.size());
    feature = (WindowFeature) features.get(0);
    assertEquals(2, feature.getOutOfBoundsDistance());
    feature = (WindowFeature) features.get(1);
    assertEquals(3, feature.getOutOfBoundsDistance());

    // Feature extraction on the first word of the file
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "text obtained from");
    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    assertEquals("text", token.getCoveredText());
    features = leftEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    assertEquals(1, ((WindowFeature) features.get(0)).getOutOfBoundsDistance());
    assertEquals(2, ((WindowFeature) features.get(1)).getOutOfBoundsDistance());
    assertEquals(3, ((WindowFeature) features.get(2)).getOutOfBoundsDistance());

    features = leftEx24.extract(jCas, token, Sentence.class);
    assertEquals(2, features.size());
    assertEquals(3, ((WindowFeature) features.get(0)).getOutOfBoundsDistance());
    assertEquals(4, ((WindowFeature) features.get(1)).getOutOfBoundsDistance());

    // Feature extraction on the last word of the file
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "and cooked dinner .");
    token = JCasUtil.selectByIndex(jCas, Token.class, -1);
    assertEquals(".", token.getCoveredText());
    features = leftEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    assertEquals("dinner", features.get(0).getValue().toString());
    assertEquals("cooked", features.get(1).getValue().toString());
    assertEquals("and", features.get(2).getValue().toString());

    int end = jCas.getDocumentText().length();
    token = new Token(jCas, end, end);
    token.addToIndexes();
    assertEquals("", token.getCoveredText());
    features = leftEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    assertEquals(".", features.get(0).getValue().toString());
    assertEquals("dinner", features.get(1).getValue().toString());
    assertEquals("cooked", features.get(2).getValue().toString());

  }

  @Test
  public void testExtractRight() throws Throwable {
    WindowExtractor rightEx03 = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_RIGHT,
        0,
        3);
    WindowExtractor rightEx1030 = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_RIGHT,
        10,
        30);

    // feature extraction on "island" in "...because the island was only..."
    tokenBuilder.buildTokens(
        jCas,
        "because the island was only three miles long and a quarter of a mile wide .\n"
            + "This place was a tolerable long, steep hill or ridge about forty foot high .");
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 2);
    assertEquals("island", token.getCoveredText());
    List<Feature> features = rightEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    WindowFeature feature = (WindowFeature) features.get(0);
    assertEquals(WindowFeature.ORIENTATION_RIGHT, feature.getOrientation());
    assertEquals(0, feature.getPosition());
    assertEquals("was", feature.getValue().toString());
    feature = (WindowFeature) features.get(1);
    assertEquals(WindowFeature.ORIENTATION_RIGHT, feature.getOrientation());
    assertEquals(1, feature.getPosition());
    assertEquals("only", feature.getValue().toString());
    feature = (WindowFeature) features.get(2);
    assertEquals(WindowFeature.ORIENTATION_RIGHT, feature.getOrientation());
    assertEquals(2, feature.getPosition());
    assertEquals("three", feature.getValue().toString());

    features = rightEx1030.extract(jCas, token, Sentence.class);
    assertEquals(20, features.size());
    feature = (WindowFeature) features.get(0);
    assertEquals("mile", feature.getValue().toString());
    feature = (WindowFeature) features.get(1);
    assertEquals(17, ((WindowFeature) features.get(19)).getOutOfBoundsDistance());

    // Feature extraction on "mile" in "...and a quarter of a mile wide...."
    token = JCasUtil.selectByIndex(jCas, Token.class, 13);
    assertEquals("mile", token.getCoveredText());
    features = rightEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    feature = (WindowFeature) features.get(0);
    assertEquals(WindowFeature.ORIENTATION_RIGHT, feature.getOrientation());
    assertEquals(0, feature.getPosition());
    assertEquals("wide", feature.getValue().toString());
    feature = (WindowFeature) features.get(1);
    assertEquals(WindowFeature.ORIENTATION_RIGHT, feature.getOrientation());
    assertEquals(1, feature.getPosition());
    assertEquals(".", feature.getValue().toString());
    feature = (WindowFeature) features.get(2);
    assertEquals(WindowFeature.ORIENTATION_RIGHT, feature.getOrientation());
    assertEquals(2, feature.getPosition());
    assertEquals(1, feature.getOutOfBoundsDistance());

    features = rightEx1030.extract(jCas, token, Sentence.class);
    assertEquals(20, features.size());
    feature = (WindowFeature) features.get(0);
    assertEquals(9, feature.getOutOfBoundsDistance());
    assertEquals(28, ((WindowFeature) features.get(19)).getOutOfBoundsDistance());

    // Feature extraction on the first word of the file
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "text obtained from gutenberg\n" + "I WANTED to go and");
    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    assertEquals("text", token.getCoveredText());
    features = rightEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    assertEquals("obtained", features.get(0).getValue().toString());
    assertEquals("from", features.get(1).getValue().toString());
    assertEquals("gutenberg", features.get(2).getValue().toString());

    features = rightEx1030.extract(jCas, token, Sentence.class);
    assertEquals(20, features.size());
    assertEquals(8, ((WindowFeature) features.get(0)).getOutOfBoundsDistance());

    // feature extraction on spanless annotation at end of file
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "and cooked dinner .");
    int end = jCas.getDocumentText().length();
    token = new Token(jCas, end, end);
    assertEquals("", token.getCoveredText());
    features = rightEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    assertEquals(1, ((WindowFeature) features.get(0)).getOutOfBoundsDistance());
    assertEquals(2, ((WindowFeature) features.get(1)).getOutOfBoundsDistance());
    assertEquals(3, ((WindowFeature) features.get(2)).getOutOfBoundsDistance());
    features = rightEx1030.extract(jCas, token, Sentence.class);
    assertEquals(20, features.size());
    assertEquals(30, ((WindowFeature) features.get(19)).getOutOfBoundsDistance());

    // Feature extraction on the last word
    token = JCasUtil.selectByIndex(jCas, Token.class, -2);
    assertEquals("dinner", token.getCoveredText());
    features = rightEx03.extract(jCas, token, Sentence.class);
    assertEquals(3, features.size());
    assertEquals(".", features.get(0).getValue().toString());
    assertEquals(1, ((WindowFeature) features.get(1)).getOutOfBoundsDistance());
    assertEquals(2, ((WindowFeature) features.get(2)).getOutOfBoundsDistance());
  }

  @Test
  public void testExtractMiddle() throws Throwable {
    tokenBuilder.buildTokens(jCas, "because the island was only");
    Annotation spanningToken = new Annotation(jCas);
    spanningToken.setBegin(JCasUtil.selectByIndex(jCas, Token.class, 1).getBegin());
    spanningToken.setEnd(JCasUtil.selectByIndex(jCas, Token.class, 3).getEnd());
    assertEquals("the island was", spanningToken.getCoveredText());
    WindowExtractor windowExtractor = new WindowExtractor(
        Token.class,
        new SpannedTextExtractor(),
        WindowFeature.ORIENTATION_MIDDLE,
        0,
        2);

    Annotation startAnnotation = windowExtractor.getStartAnnotation(jCas, spanningToken);
    assertEquals("the", startAnnotation.getCoveredText());

    Sentence sentence = JCasUtil.selectSingle(jCas, Sentence.class);
    List<Feature> features = windowExtractor.extract(jCas, spanningToken, sentence);
    assertEquals(2, features.size());
    WindowFeature feature = (WindowFeature) features.get(0);
    assertEquals(WindowFeature.ORIENTATION_MIDDLE, feature.getOrientation());
    assertEquals(0, feature.getPosition());
    assertEquals("the", feature.getValue().toString());
    feature = (WindowFeature) features.get(1);
    assertEquals(WindowFeature.ORIENTATION_MIDDLE, feature.getOrientation());
    assertEquals(1, feature.getPosition());
    assertEquals("island", feature.getValue().toString());
  }

  @Test
  public void testTicket23() throws Throwable {
    // token "place" in "wide. This place was a tolerable long,"
    tokenBuilder.buildTokens(jCas, "a mile wide .\nThis place was a tolerable long ,");
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 5);
    assertEquals("place", token.getCoveredText());
    token.setPos("A");
    Token tokenL0 = JCasUtil.selectByIndex(jCas, Token.class, 4);
    tokenL0.setPos("B");
    Token tokenL1 = JCasUtil.selectByIndex(jCas, Token.class, 3);
    tokenL1.setPos("C");
    Token tokenL2 = JCasUtil.selectByIndex(jCas, Token.class, 2);
    tokenL2.setPos("D");
    Token tokenL3 = JCasUtil.selectByIndex(jCas, Token.class, 1);
    tokenL3.setPos("E");
    Token tokenL4 = JCasUtil.selectByIndex(jCas, Token.class, 0);
    tokenL4.setPos("F");

    WindowExtractor leftPosExtractor = new WindowExtractor(Token.class, new TypePathExtractor(
        Token.class,
        "pos"), WindowFeature.ORIENTATION_LEFT, 0, 5);

    List<Feature> features = leftPosExtractor.extract(jCas, token, Sentence.class);
    assertEquals("B", features.get(0).getValue().toString());
    assertEquals(0, ((WindowFeature) features.get(0)).getOutOfBoundsDistance());
    assertEquals("Window_L0_TypePath(Pos)", features.get(0).getName());
    assertEquals(1, ((WindowFeature) features.get(1)).getOutOfBoundsDistance());
    assertEquals("Window_L1OOB1", features.get(1).getName());
    assertEquals(2, ((WindowFeature) features.get(2)).getOutOfBoundsDistance());
    assertEquals("Window_L2OOB2", ((WindowFeature) features.get(2)).getName());
  }

  /**
   * The assertions in this test demonstrate that WindowExtractor works consistently. They are
   * provided to contrast with what's in WindowNGramExtractorTest which demonstrates that the
   * WindowNGramExtractor does not work consistently.
   * 
   * @throws UIMAException
   * @throws CleartkException
   */
  @Test
  public void testIssue158() throws Throwable {
    tokenBuilder.buildTokens(jCas, "1 2 3 4 5 6 7 8 9 10");

    Token token5 = JCasUtil.selectByIndex(jCas, Token.class, 4);
    assertEquals("5", token5.getCoveredText());
    Token token6 = JCasUtil.selectByIndex(jCas, Token.class, 5);
    assertEquals("6", token6.getCoveredText());

    Header header = new Header(jCas, 8, 11);
    header.addToIndexes();
    assertEquals("5 6", header.getCoveredText());

    SpannedTextExtractor wordExtractor = new SpannedTextExtractor();

    WindowExtractor windowExtractor = new WindowExtractor(
        Token.class,
        wordExtractor,
        WindowFeature.ORIENTATION_RIGHT,
        0,
        2);
    List<Feature> features = windowExtractor.extract(jCas, header, Sentence.class);
    assertEquals(2, features.size());
    assertEquals("Window_R0", features.get(0).getName());
    assertEquals("7", features.get(0).getValue().toString());
    assertEquals("Window_R1", features.get(1).getName());
    assertEquals("8", features.get(1).getValue().toString());

    features = windowExtractor.extract(jCas, token6, Sentence.class);
    assertEquals(2, features.size());
    assertEquals("Window_R0", features.get(0).getName());
    assertEquals("7", features.get(0).getValue().toString());
    assertEquals("Window_R1", features.get(1).getName());
    assertEquals("8", features.get(1).getValue().toString());

    windowExtractor = new WindowExtractor(
        Token.class,
        wordExtractor,
        WindowFeature.ORIENTATION_LEFT,
        0,
        2);
    features = windowExtractor.extract(jCas, header, Sentence.class);

    assertEquals("Window_L0", features.get(0).getName());
    assertEquals("4", features.get(0).getValue().toString());
    assertEquals("Window_L1", features.get(1).getName());
    assertEquals("3", features.get(1).getValue().toString());

    features = windowExtractor.extract(jCas, token5, Sentence.class);

    assertEquals("Window_L0", features.get(0).getName());
    assertEquals("4", features.get(0).getValue().toString());
    assertEquals("Window_L1", features.get(1).getName());
    assertEquals("3", features.get(1).getValue().toString());

  }

}
