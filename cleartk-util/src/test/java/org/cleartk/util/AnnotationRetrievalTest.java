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
package org.cleartk.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Chunk;
import org.cleartk.type.test.NamedEntityMention;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.junit.Assert;
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
 */
public class AnnotationRetrievalTest extends DefaultTestBase {

  @Test
  public void testGet() throws UIMAException, IOException {
    String text = FileUtils.readFileToString(new File(
        "src/test/resources/docs/youthful-precocity.txt"));
    tokenBuilder.buildTokens(jCas, text);

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 20);
    Assert.assertEquals("genius,", token.getCoveredText());
    Assert.assertEquals("a", AnnotationRetrieval.get(jCas, token, -1).getCoveredText());
    Assert.assertEquals("and", AnnotationRetrieval.get(jCas, token, 1).getCoveredText());
    Assert.assertEquals(null, AnnotationRetrieval.get(jCas, token, -40));
    Assert.assertEquals("genius,", AnnotationRetrieval.get(jCas, token, 0).getCoveredText());

    Assert.assertEquals("The", JCasUtil.selectByIndex(jCas, Token.class, 0).getCoveredText());
    Assert.assertEquals("precocity", JCasUtil.selectByIndex(jCas, Token.class, 1).getCoveredText());

    Assert.assertEquals(
        "http://www.gutenberg.org/wiki/Gutenberg:The_Project_Gutenberg_License",
        JCasUtil.selectByIndex(jCas, Token.class, -1).getCoveredText());
    Assert.assertEquals("from", JCasUtil.selectByIndex(jCas, Token.class, -5).getCoveredText());
    Assert.assertEquals(null, JCasUtil.selectByIndex(jCas, Token.class, -500));

  }

  @Test
  public void testTypedGet() throws UIMAException {
    tokenBuilder.buildTokens(jCas, "A B C D E F G H I J");

    Annotation ca = new Annotation(jCas, 10, 13);
    Assert.assertEquals("F G", ca.getCoveredText());
    Token token = AnnotationRetrieval.get(jCas, ca, Token.class, 0);
    Assert.assertEquals("F", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -1);
    Assert.assertEquals("E", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -2);
    Assert.assertEquals("D", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -3);
    Assert.assertEquals("C", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -4);
    Assert.assertEquals("B", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -5);
    Assert.assertEquals("A", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -6);
    Assert.assertNull(token);
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 1);
    Assert.assertEquals("H", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 2);
    Assert.assertEquals("I", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 3);
    Assert.assertEquals("J", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 4);
    Assert.assertNull(token);

    jCas.reset();
    tokenBuilder.buildTokens(jCas, "AAA BBB CCC DDDD EEEE FFFF");
    ca = new Annotation(jCas, 6, 9);
    Assert.assertEquals("B C", ca.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 0);
    Assert.assertNull(token);
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 1);
    Assert.assertEquals("DDDD", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -1);
    Assert.assertEquals("AAA", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, -2);
    Assert.assertNull(token);
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 2);
    Assert.assertEquals("EEEE", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 3);
    Assert.assertEquals("FFFF", token.getCoveredText());
    token = AnnotationRetrieval.get(jCas, ca, Token.class, 4);
    Assert.assertNull(token);

    jCas.reset();
    tokenBuilder.buildTokens(jCas, "AAA BBB CCC DDDD EEEE FFFF");

    Annotation sa = new Annotation(jCas, 8, 11);
    sa.addToIndexes();
    assertEquals("CCC", sa.getCoveredText());
    assertEquals("DDDD", AnnotationRetrieval
        .getAdjacentAnnotation(jCas, sa, Token.class, false)
        .getCoveredText());
    assertEquals("DDDD", AnnotationRetrieval.get(jCas, sa, Token.class, 1).getCoveredText());

  }

  @Test
  public void testGetContainingAnnotation() throws UIMAException, IOException {
    tokenBuilder.buildTokens(
        jCas,
        "Were your just trippin', just ego tripping.",
        "Were your just trippin ' , just ego tripping .");
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 3);
    Assert.assertEquals("trippin", token.getCoveredText());
    Sentence sentence = AnnotationRetrieval.getContainingAnnotation(jCas, token, Sentence.class);
    Assert.assertEquals("Were your just trippin', just ego tripping.", sentence.getCoveredText());

    jCas.reset();
    tokenBuilder.buildTokens(
        jCas,
        "Ffff's a pppppppp ttttt, bbbb ii ccc tttt yyyy hhhhh bbbbb yyy ccc ttttt. \n"
            + "It'll tttt yyyy ggg ffffff ssss aaa ffff tt wwww ddddls nnd ddst.",
        "Ffff 's a pppppppp ttttt, bbbb ii ccc tttt yyyy hhhhh bbbbb yyy ccc ttttt . \n"
            + "It ' ll tttt yyyy ggg ffffff ssss aaa ffff tt wwww ddddls nnd ddst .");

    token = JCasUtil.selectByIndex(jCas, Token.class, 28);
    Assert.assertEquals("ddddls", token.getCoveredText());
    sentence = AnnotationRetrieval.getContainingAnnotation(jCas, token, Sentence.class);
    Assert.assertTrue(sentence.getCoveredText().startsWith("It'll"));

    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("Ffff", token.getCoveredText());
    sentence = AnnotationRetrieval.getContainingAnnotation(jCas, token, Sentence.class);
    Assert.assertTrue(sentence.getCoveredText().startsWith("Ffff's a pppppppp ttttt"));

    token = JCasUtil.selectByIndex(jCas, Token.class, -1);
    Assert.assertEquals(".", token.getCoveredText());
    sentence = AnnotationRetrieval.getContainingAnnotation(jCas, token, Sentence.class);
    Assert.assertTrue(sentence.getCoveredText().startsWith("It'll tttt yyyy"));

    token = JCasUtil.selectByIndex(jCas, Token.class, 28);
    Assert.assertEquals("ddddls", token.getCoveredText());
    token = AnnotationRetrieval.getContainingAnnotation(jCas, token, Token.class);
    Assert.assertEquals("ddddls", token.getCoveredText());

    token = JCasUtil.selectByIndex(jCas, Token.class, 28);
    Assert.assertEquals("ddddls", token.getCoveredText());
    token = AnnotationRetrieval.getContainingAnnotation(jCas, token, Token.class, true);
    Assert.assertNull(token);

    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("Ffff", token.getCoveredText());
    token = AnnotationRetrieval.getContainingAnnotation(jCas, token, Token.class);
    Assert.assertEquals("Ffff", token.getCoveredText());

    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("Ffff", token.getCoveredText());
    token = AnnotationRetrieval.getContainingAnnotation(jCas, token, Token.class, true);
    Assert.assertNull(token);

    token = JCasUtil.selectByIndex(jCas, Token.class, -1);
    Assert.assertEquals(".", token.getCoveredText());
    token = AnnotationRetrieval.getContainingAnnotation(jCas, token, Token.class);
    Assert.assertEquals(".", token.getCoveredText());

    jCas.reset();
    AnnotationUtilTest.Annotator.getProcessedJCas(jCas, typeSystemDescription);

    Token token1 = JCasUtil.selectByIndex(jCas, Token.class, 1);
    Token token2 = JCasUtil.selectByIndex(jCas, Token.class, 2);
    Token token3 = JCasUtil.selectByIndex(jCas, Token.class, 3);
    Token token4 = JCasUtil.selectByIndex(jCas, Token.class, 4);
    Token token5 = JCasUtil.selectByIndex(jCas, Token.class, 5);
    JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 2);
    NamedEntityMention nem3 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 3);
    NamedEntityMention nem4 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 4);
    JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 5);
    NamedEntityMention nem6 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 6);
    NamedEntityMention nem7 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 7);

    NamedEntityMention nem = AnnotationRetrieval.getContainingAnnotation(
        jCas,
        token1,
        NamedEntityMention.class);
    Assert.assertEquals(nem, nem3);

    nem = AnnotationRetrieval.getContainingAnnotation(jCas, token2, NamedEntityMention.class);
    Assert.assertEquals(nem, nem4);

    nem = AnnotationRetrieval.getContainingAnnotation(jCas, token3, NamedEntityMention.class);
    Assert.assertEquals(nem, nem6);

    nem = AnnotationRetrieval.getContainingAnnotation(jCas, token4, NamedEntityMention.class);
    Assert.assertEquals(nem, nem7);

    nem = AnnotationRetrieval.getContainingAnnotation(jCas, token5, NamedEntityMention.class);
    Assert.assertEquals(nem, nem3);

    String text = "word";
    jCas.reset();
    tokenBuilder.buildTokens(jCas, text);
    token1 = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("word", token1.getCoveredText());
    sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    Assert.assertEquals("word", sentence.getCoveredText());
    sentence = AnnotationRetrieval.getContainingAnnotation(jCas, token1, Sentence.class);
    Assert.assertEquals("word", sentence.getCoveredText());

    jCas.reset();
    text = "Materials and Methods ";
    tokenBuilder.buildTokens(jCas, text);
    nem = new NamedEntityMention(jCas, 14, 21);
    nem.addToIndexes();
    token1 = JCasUtil.selectByIndex(jCas, Token.class, 2);
    Assert.assertEquals("Methods", token1.getCoveredText());
    nem = AnnotationRetrieval.getContainingAnnotation(jCas, token1, NamedEntityMention.class);
    Assert.assertEquals("Methods", nem.getCoveredText());

    jCas.reset();
    text = "Materials and Methods ";
    jCas.setDocumentText(text);
    token1 = new Token(jCas, 14, 21);
    token1.addToIndexes();
    nem = new NamedEntityMention(jCas, 14, 21);
    nem.addToIndexes();

    nem = AnnotationRetrieval.getContainingAnnotation(jCas, token1, NamedEntityMention.class);
    Assert.assertEquals("Methods", nem.getCoveredText());

    token3 = AnnotationRetrieval.getContainingAnnotation(jCas, nem, Token.class);
    Assert.assertEquals("Methods", token3.getCoveredText());

  }

  @Test
  public void testGetContainingAnnotationExclusive() throws UIMAException {
    String text = "What if we built a rocket ship made of cheese?\n"
        + "We could fly it to the moon for repairs.";
    tokenBuilder.buildTokens(jCas, text);
    Token containingToken = new Token(jCas, 0, 10);
    containingToken.addToIndexes();

    Token token1 = JCasUtil.selectByIndex(jCas, Token.class, 1);
    Token token = AnnotationRetrieval.getContainingAnnotation(jCas, token1, Token.class);
    Assert.assertEquals("What", token.getCoveredText());
    token = AnnotationRetrieval.getContainingAnnotation(jCas, token1, Token.class, true);
    Assert.assertEquals("What if we", token.getCoveredText());

  }

  /**
   * The following is the text of a bug reported by a user:
   * 
   * <p>
   * we are using your UIMAUtils in our applications and it works well. But I have a trouble with
   * getContainingAnnotation (...) for the case of the sequence "Alls" where "lls" is an entity and
   * "Alls" is a token. I call getContainingAnnotation (jcas, entity, token.class) and receive a
   * null.
   * 
   * @throws UIMAException
   * @throws IOException
   */
  @Test
  public void testTokenBug() throws UIMAException, IOException {
    jCas.setDocumentText("This is text that supports the above bug report: Alls lls. ");

    NamedEntityMention entity = new NamedEntityMention(jCas, 50, 53);
    entity.addToIndexes();
    Assert.assertEquals("lls", entity.getCoveredText());

    Token token = new Token(jCas, 49, 53);
    token.addToIndexes();

    Assert.assertEquals("Alls", token.getCoveredText());

    Token containingToken = AnnotationRetrieval.getContainingAnnotation(jCas, entity, Token.class);

    Assert.assertNotNull(containingToken);
    Assert.assertEquals("Alls", containingToken.getCoveredText());
    Assert.assertEquals(token, containingToken);
  }

  @Test
  public void testGetAdjacentAnnotation() throws UIMAException, IOException {
    tokenBuilder.buildTokens(jCas, "Swwww thh sii , biiiii thh taaaa in my moooo . \n"
        + "I see seeee tooooo , buu I onnn see onn waa ouu . \n"
        + "Yoo goooo crr wiiiiii weeeeee , taaa wiiiiii sppppppp "
        + "Sccccc wiiiiii raaaaaa yooo voooo . \n"
        + "Yoo knnn I tooo thh pooooo , frrr thh pooooo sttttt Thhh I fllllll ouu of heee .  \n"
        + "Siiiiii Ah la la la de daa Ah la la la de daa . ");

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 27);
    Assert.assertEquals("wiiiiii", token.getCoveredText());
    Token adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Token.class, true);
    Assert.assertEquals("crr", adjacentToken.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Token.class, false);
    Assert.assertEquals("weeeeee", adjacentToken.getCoveredText());
    Sentence adjacentSentence = AnnotationRetrieval.getAdjacentAnnotation(
        jCas,
        token,
        Sentence.class,
        true);
    Assert.assertTrue(adjacentSentence.getCoveredText().startsWith("I see"));
    adjacentSentence = AnnotationRetrieval
        .getAdjacentAnnotation(jCas, token, Sentence.class, false);
    Assert.assertTrue(adjacentSentence.getCoveredText().startsWith("Yoo knnn"));

    Sentence sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 3);
    Assert.assertEquals(
        "Yoo knnn I tooo thh pooooo , frrr thh pooooo sttttt Thhh I fllllll ouu of heee .",
        sentence.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, sentence, Token.class, true);
    Assert.assertEquals(".", adjacentToken.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, sentence, Token.class, false);
    Assert.assertEquals("Siiiiii", adjacentToken.getCoveredText());
    adjacentSentence = AnnotationRetrieval.getAdjacentAnnotation(
        jCas,
        sentence,
        Sentence.class,
        true);
    Assert.assertTrue(adjacentSentence.getCoveredText().startsWith("Yoo goooo"));
    adjacentSentence = AnnotationRetrieval.getAdjacentAnnotation(
        jCas,
        sentence,
        Sentence.class,
        false);
    Assert.assertTrue(adjacentSentence.getCoveredText().startsWith("Siiiiii"));

    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("Swwww", token.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Token.class, true);
    Assert.assertEquals(null, adjacentToken);
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Token.class, false);
    Assert.assertEquals("thh", adjacentToken.getCoveredText());
    adjacentSentence = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Sentence.class, true);
    Assert.assertEquals(null, adjacentSentence);
    adjacentSentence = AnnotationRetrieval
        .getAdjacentAnnotation(jCas, token, Sentence.class, false);
    Assert.assertTrue(adjacentSentence.getCoveredText().startsWith("I see"));

    token = JCasUtil.selectByIndex(jCas, Token.class, -1);
    Assert.assertEquals(".", token.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Token.class, true);
    Assert.assertEquals("daa", adjacentToken.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Token.class, false);
    Assert.assertEquals(null, adjacentToken);
    adjacentSentence = AnnotationRetrieval.getAdjacentAnnotation(jCas, token, Sentence.class, true);
    Assert.assertEquals(
        "Yoo knnn I tooo thh pooooo , frrr thh pooooo sttttt Thhh I fllllll ouu of heee .",
        adjacentSentence.getCoveredText());
    adjacentSentence = AnnotationRetrieval
        .getAdjacentAnnotation(jCas, token, Sentence.class, false);
    Assert.assertEquals(null, adjacentSentence);

    sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    Assert
        .assertEquals("Swwww thh sii , biiiii thh taaaa in my moooo .", sentence.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, sentence, Token.class, true);
    Assert.assertEquals(null, adjacentToken);
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, sentence, Token.class, false);
    Assert.assertEquals("I", adjacentToken.getCoveredText());
    adjacentSentence = AnnotationRetrieval.getAdjacentAnnotation(
        jCas,
        sentence,
        Sentence.class,
        true);
    Assert.assertEquals(null, adjacentSentence);
    adjacentSentence = AnnotationRetrieval.getAdjacentAnnotation(
        jCas,
        sentence,
        Sentence.class,
        false);
    Assert.assertTrue(adjacentSentence.getCoveredText().startsWith("I see"));

    jCas.reset();
    String text = FileUtils.readFileToString(new File("src/test/resources/docs/huckfinn.txt"));
    tokenBuilder.buildTokens(jCas, text);

    Annotation annotation = new Annotation(jCas, 404, 449);
    Assert.assertEquals(
        "top, the sides was so steep and the bushes so",
        annotation.getCoveredText());
    adjacentToken = AnnotationRetrieval.getAdjacentAnnotation(jCas, annotation, Token.class, false);
    Assert.assertEquals("thick.", adjacentToken.getCoveredText());

    jCas.reset();
    tokenBuilder.buildTokens(jCas, "AAA BBB CCC DDDD EEEE FFFF");

    Annotation sa = new Annotation(jCas, 8, 11);
    sa.addToIndexes();
    assertEquals("CCC", sa.getCoveredText());
    assertEquals("BBB", AnnotationRetrieval
        .getAdjacentAnnotation(jCas, sa, Token.class, true)
        .getCoveredText());
    assertEquals("DDDD", AnnotationRetrieval
        .getAdjacentAnnotation(jCas, sa, Token.class, false)
        .getCoveredText());

  }

  @Test
  public void testGetAnnotationsWithBeginEnd() throws UIMAException, IOException {
    String text = FileUtils.readFileToString(new File("src/test/resources/docs/huckfinn.txt"));
    tokenBuilder.buildTokens(jCas, text);

    System.out.println(text.substring(1200, 1500));
    List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, 1200, 1500, Token.class);
    Assert.assertEquals(tokens.size(), 60);

    tokens = AnnotationRetrieval.getAnnotations(jCas, 1778, 2500, Token.class);
    Assert.assertEquals(tokens.size(), 0);
  }

  @Test
  public void testGetAnnotationsWithWindow() throws UIMAException, IOException {

    AnnotationUtilTest.Annotator.getProcessedJCas(jCas, typeSystemDescription);

    Token token0 = JCasUtil.selectByIndex(jCas, Token.class, 0);
    NamedEntityMention nem0 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 0);
    NamedEntityMention nem1 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 1);

    List<Token> annotations = AnnotationRetrieval.getAnnotations(jCas, nem0, Token.class);
    Assert.assertEquals(1, annotations.size());
    Assert.assertEquals("text", annotations.get(0).getCoveredText());

    List<NamedEntityMention> neAnnotations = AnnotationRetrieval.getAnnotations(
        jCas,
        token0,
        NamedEntityMention.class);
    Assert.assertEquals(1, neAnnotations.size());
    Assert.assertEquals("text", neAnnotations.get(0).getCoveredText());

    annotations = AnnotationRetrieval.getAnnotations(jCas, nem1, Token.class);
    Assert.assertEquals(1, annotations.size());
    Assert.assertEquals("text", annotations.get(0).getCoveredText());

    annotations = AnnotationRetrieval.getAnnotations(jCas, token0, Token.class);
    Assert.assertEquals(1, annotations.size());
    Assert.assertEquals("text", annotations.get(0).getCoveredText());

    JCasUtil.selectByIndex(jCas, Token.class, 1);
    JCasUtil.selectByIndex(jCas, Token.class, 2);
    JCasUtil.selectByIndex(jCas, Token.class, 3);
    JCasUtil.selectByIndex(jCas, Token.class, 4);
    JCasUtil.selectByIndex(jCas, Token.class, 5);
    NamedEntityMention nem2 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 2);
    NamedEntityMention nem3 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 3);
    NamedEntityMention nem4 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 4);
    NamedEntityMention nem5 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 5);
    NamedEntityMention nem6 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 6);
    NamedEntityMention nem7 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 7);

    annotations = AnnotationRetrieval.getAnnotations(jCas, nem2, Token.class);
    Assert.assertEquals(5, annotations.size());
    Assert.assertEquals(20, annotations.get(0).getBegin());
    Assert.assertEquals(26, annotations.get(4).getBegin());

    annotations = AnnotationRetrieval.getAnnotations(jCas, nem3, Token.class);
    Assert.assertEquals(5, annotations.size());
    Assert.assertEquals(20, annotations.get(0).getBegin());
    Assert.assertEquals(26, annotations.get(4).getBegin());

    annotations = AnnotationRetrieval.getAnnotations(jCas, nem4, Token.class);
    Assert.assertEquals(3, annotations.size());
    Assert.assertEquals(21, annotations.get(0).getBegin());
    Assert.assertEquals(25, annotations.get(2).getBegin());

    annotations = AnnotationRetrieval.getAnnotations(jCas, nem5, Token.class);
    Assert.assertEquals(0, annotations.size());

    annotations = AnnotationRetrieval.getAnnotations(jCas, nem6, Token.class);
    Assert.assertEquals(2, annotations.size());
    Assert.assertEquals(24, annotations.get(0).getBegin());
    Assert.assertEquals(25, annotations.get(1).getBegin());

    annotations = AnnotationRetrieval.getAnnotations(jCas, nem7, Token.class);
    Assert.assertEquals(1, annotations.size());
    Assert.assertEquals(25, annotations.get(0).getBegin());

    NamedEntityMention nem8 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 8);
    NamedEntityMention nem9 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 9);
    NamedEntityMention nem10 = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 10);
    JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 11);

    neAnnotations = AnnotationRetrieval.getAnnotations(jCas, nem8, NamedEntityMention.class);
    Assert.assertEquals(4, neAnnotations.size());
    Assert.assertEquals(49, neAnnotations.get(0).getBegin());
    Assert.assertEquals(55, neAnnotations.get(3).getBegin());

    neAnnotations = AnnotationRetrieval.getAnnotations(jCas, nem9, NamedEntityMention.class);
    Assert.assertEquals(3, neAnnotations.size());
    Assert.assertEquals(50, neAnnotations.get(0).getBegin());
    Assert.assertEquals(55, neAnnotations.get(2).getBegin());

    neAnnotations = AnnotationRetrieval.getAnnotations(jCas, nem10, NamedEntityMention.class);
    Assert.assertEquals(1, neAnnotations.size());
    Assert.assertEquals(50, neAnnotations.get(0).getBegin());
  }

  @Test
  public void testGetAnnotations() throws UIMAException, IOException {

    AnnotationUtilTest.Annotator.getProcessedJCas(jCas, typeSystemDescription);

    int[][] tokenOffsets = {
        { 0, 4 },
        { 20, 25 },
        { 21, 25 },
        { 24, 25 },
        { 25, 28 },
        { 26, 30 },
        { 100, 104 },
        { 105, 109 },
        { 110, 114 },
        { 115, 119 },
        { 120, 124 }, };
    List<Token> tokenAnns = AnnotationRetrieval.getAnnotations(jCas, Token.class);
    this.testOneGetAnnotations(tokenOffsets, tokenAnns);

    int[][] neOffsets = {
        { 0, 13 },
        { 0, 4 },
        { 19, 31 },
        { 20, 30 },
        { 21, 29 },
        { 21, 24 },
        { 24, 28 },
        { 25, 28 },
        { 49, 61 },
        { 50, 60 },
        { 50, 55 },
        { 55, 60 }, };
    List<NamedEntityMention> neAnns = AnnotationRetrieval.getAnnotations(
        jCas,
        NamedEntityMention.class);
    this.testOneGetAnnotations(neOffsets, neAnns);

    int[][] chunkOffsets = { { 100, 109 }, { 115, 124 } };
    List<Chunk> chunkAnns = AnnotationRetrieval.getAnnotations(jCas, Chunk.class);
    this.testOneGetAnnotations(chunkOffsets, chunkAnns);

  }

  private <T extends Annotation> void testOneGetAnnotations(
      int[][] expectedOffsetPairs,
      List<T> actualAnnotations) {

    Assert.assertEquals(expectedOffsetPairs.length, actualAnnotations.size());
    Iterator<T> annotationsIter = actualAnnotations.iterator();
    for (int[] offsetPair : expectedOffsetPairs) {
      Annotation annotation = annotationsIter.next();
      Assert.assertEquals(annotation.getBegin(), offsetPair[0]);
      Assert.assertEquals(annotation.getEnd(), offsetPair[1]);
    }
  }

  @Test
  public void testGetAnnotationIndex() throws UIMAException, IOException {
    // original joke by Philip Ogren
    String text = "Police Officer: Put down that gun!\n"
        + "Hooligan (turning toward his gun): Stupid gun!";
    tokenBuilder.buildTokens(jCas, text);

    AnnotationIndex<Annotation> tokenIndex = AnnotationRetrieval.getAnnotationIndex(
        jCas,
        Token.class);
    Assert.assertEquals(13, tokenIndex.size());
    FSIterator<Annotation> iterator = tokenIndex.iterator();
    while (iterator.hasNext()) {
      Token token = (Token) iterator.next();
      Assert.assertTrue(token instanceof Token);
    }

    AnnotationIndex<Annotation> sentenceIndex = AnnotationRetrieval.getAnnotationIndex(
        jCas,
        Sentence.class);
    Assert.assertEquals(2, sentenceIndex.size());

  }

  @Test
  public void testGetAtIndex() throws ResourceInitializationException {
    // original joke by Philip Ogren
    String text = "Police Officer: Put down that gun!\n"
        + "Hooligan (turning toward his gun): Stupid gun!";
    jCas.setDocumentText(text);
    Token token = new Token(jCas, 7, 14);
    token.addToIndexes();

    token = new Token(jCas, 0, 6);
    token.addToIndexes();

    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("Police", token.getCoveredText());
    token = JCasUtil.selectByIndex(jCas, Token.class, 1);
    Assert.assertEquals("Officer", token.getCoveredText());
  }

  @Test
  public void testGetAnnotationsExact() throws Exception {
    // from http://www.gutenberg.org/files/17192/17192-h/17192-h.htm
    jCas.setDocumentText("Quoth the Raven, \"Nevermore.\"");
    Token token = new Token(jCas, 10, 15);
    token.addToIndexes();
    Assert.assertEquals("Raven", token.getCoveredText());

    List<Token> exactTokens = AnnotationRetrieval.getAnnotations(jCas, 10, 15, Token.class, true);
    Assert.assertEquals(1, exactTokens.size());
    Assert.assertEquals("Raven", exactTokens.get(0).getCoveredText());

    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 9, 15, Token.class, true);
    Assert.assertEquals(0, exactTokens.size());
    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 10, 16, Token.class, true);
    Assert.assertEquals(0, exactTokens.size());
    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 11, 15, Token.class, true);
    Assert.assertEquals(0, exactTokens.size());
    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 10, 14, Token.class, true);
    Assert.assertEquals(0, exactTokens.size());
    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 11, 14, Token.class, true);
    Assert.assertEquals(0, exactTokens.size());
    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 9, 16, Token.class, true);
    Assert.assertEquals(0, exactTokens.size());

    token = new Token(jCas, 10, 15);
    token.addToIndexes();

    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 10, 15, Token.class, true);
    Assert.assertEquals(2, exactTokens.size());
    Assert.assertEquals("Raven", exactTokens.get(0).getCoveredText());
    Assert.assertEquals("Raven", exactTokens.get(1).getCoveredText());

    token = new Token(jCas, 0, 5);
    token.addToIndexes();
    Assert.assertEquals("Quoth", token.getCoveredText());

    exactTokens = AnnotationRetrieval.getAnnotations(jCas, 0, 5, Token.class, true);
    Assert.assertEquals(1, exactTokens.size());
    Assert.assertEquals("Quoth", exactTokens.get(0).getCoveredText());

  }

  @Test
  public void testGetAnnotationsExact2() throws Exception {
    JCas myView = jCas.createView("MyView");
    tokenBuilder.buildTokens(myView, "red and blue cars and tipsy motorcycles");
    Token token = JCasUtil.selectByIndex(myView, Token.class, 6);
    assertEquals("motorcycles", token.getCoveredText());
    assertEquals(28, token.getBegin());
    assertEquals(39, token.getEnd());
    token = AnnotationRetrieval.getAnnotations(myView, 22, 27, Token.class, true).get(0);
    assertEquals("tipsy", token.getCoveredText());
    token = AnnotationRetrieval.getAnnotations(myView, 28, 39, Token.class, true).get(0);
    assertEquals("motorcycles", token.getCoveredText());
  }

}
