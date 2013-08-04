/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Bag;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Count;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Covered;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.FirstCovered;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Focus;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.LastCovered;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Ngram;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Ngrams;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Chunk;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.junit.Test;
import org.uimafit.factory.JCasFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CleartkExtractorTest extends DefaultTestBase {

  @Test
  public void testBasic() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Preceding(2),
        new Preceding(3, 6),
        new Covered(),
        new FirstCovered(1),
        new FirstCovered(1, 3),
        new LastCovered(1),
        new LastCovered(1, 3),
        new Following(1, 3),
        new Following(3, 5),
        new Preceding(5, 6),
        new Following(5, 6));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(19, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "brown", iter.next());
    this.assertFeature("Preceding_0_2_0", "fox", iter.next());
    this.assertFeature("Preceding_3_6_5", "OOB2", iter.next());
    this.assertFeature("Preceding_3_6_4", "OOB1", iter.next());
    this.assertFeature("Preceding_3_6_3", "The", iter.next());
    this.assertFeature("Covered_0", "jumped", iter.next());
    this.assertFeature("Covered_1", "over", iter.next());
    this.assertFeature("FirstCovered_0_1_0", "jumped", iter.next());
    this.assertFeature("FirstCovered_1_3_1", "over", iter.next());
    this.assertFeature("FirstCovered_1_3_2", "OOB1", iter.next());
    this.assertFeature("LastCovered_0_1_0", "over", iter.next());
    this.assertFeature("LastCovered_1_3_2", "OOB1", iter.next());
    this.assertFeature("LastCovered_1_3_1", "jumped", iter.next());
    this.assertFeature("Following_1_3_1", "lazy", iter.next());
    this.assertFeature("Following_1_3_2", "dog", iter.next());
    this.assertFeature("Following_3_5_3", ".", iter.next());
    this.assertFeature("Following_3_5_4", "OOB1", iter.next());
    this.assertFeature("Preceding_5_6_5", "OOB2", iter.next());
    this.assertFeature("Following_5_6_5", "OOB2", iter.next());
  }

  @Test
  public void testBag() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(Token.class, new TypePathExtractor(
        Token.class,
        "pos"), new Bag(new Preceding(2)), new Bag(new Preceding(3, 6)), new Bag(
        new FirstCovered(1),
        new LastCovered(1)), new Bag(new Following(1, 3)), new Bag(new Following(3, 5)), new Bag(
        new Preceding(1),
        new Following(1)));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(13, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Bag_Preceding_0_2_TypePath(Pos)", "JJ", iter.next());
    this.assertFeature("Bag_Preceding_0_2_TypePath(Pos)", "NN", iter.next());
    this.assertFeature("Bag_Preceding_3_6_TypePath(Pos)", "OOB2", iter.next());
    this.assertFeature("Bag_Preceding_3_6_TypePath(Pos)", "OOB1", iter.next());
    this.assertFeature("Bag_Preceding_3_6_TypePath(Pos)", "DT", iter.next());
    this.assertFeature("Bag_FirstCovered_0_1_LastCovered_0_1_TypePath(Pos)", "VBD", iter.next());
    this.assertFeature("Bag_FirstCovered_0_1_LastCovered_0_1_TypePath(Pos)", "IN", iter.next());
    this.assertFeature("Bag_Following_1_3_TypePath(Pos)", "JJ", iter.next());
    this.assertFeature("Bag_Following_1_3_TypePath(Pos)", "NN", iter.next());
    this.assertFeature("Bag_Following_3_5_TypePath(Pos)", ".", iter.next());
    this.assertFeature("Bag_Following_3_5_TypePath(Pos)", "OOB1", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Following_0_1_TypePath(Pos)", "NN", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Following_0_1_TypePath(Pos)", "DT", iter.next());
  }

  @Test
  public void testCounts() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Count(new Preceding(2)),
        new Count(new Covered()),
        new Count(new Following(1, 5)),
        new Count(new Preceding(3), new Following(4)),
        new Count(new Ngrams(2, new Preceding(3), new Following(4))),
        new Count(new Ngram(new Preceding(2), new Following(2))), // silly!
        new Count(new Bag(new Preceding(2))), // weird!
        new Count(new Count(new Preceding(2))), // weird & silly!
        new Count(new Count(new Count(new Preceding(2))))); // weird & silly!

    this.tokenBuilder.buildTokens(this.jCas, "aa bb cc bb aa cc bb cc aa");
    Chunk chunk = new Chunk(this.jCas, 9, 11);
    chunk.addToIndexes();
    Assert.assertEquals("bb", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    // Assert.assertEquals(19, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Count_Preceding_0_2_bb", 1, iter.next());
    this.assertFeature("Count_Preceding_0_2_cc", 1, iter.next());
    this.assertFeature("Count_Covered_bb", 1, iter.next());
    this.assertFeature("Count_Following_1_5_cc", 2, iter.next());
    this.assertFeature("Count_Following_1_5_bb", 1, iter.next());
    this.assertFeature("Count_Following_1_5_aa", 1, iter.next());
    this.assertFeature("Count_Preceding_0_3_Following_0_4_aa", 2, iter.next());
    this.assertFeature("Count_Preceding_0_3_Following_0_4_bb", 2, iter.next());
    this.assertFeature("Count_Preceding_0_3_Following_0_4_cc", 3, iter.next());
    this.assertFeature("Count_2grams_Preceding_0_3_Following_0_4_aa_bb", 1, iter.next());
    this.assertFeature("Count_2grams_Preceding_0_3_Following_0_4_bb_cc", 2, iter.next());
    this.assertFeature("Count_2grams_Preceding_0_3_Following_0_4_cc_aa", 1, iter.next());
    this.assertFeature("Count_2grams_Preceding_0_3_Following_0_4_aa_cc", 1, iter.next());
    this.assertFeature("Count_2grams_Preceding_0_3_Following_0_4_cc_bb", 1, iter.next());
    this.assertFeature("Count_Ngram_Preceding_0_2_Following_0_2_bb_cc_aa_cc", 1, iter.next());
    this.assertFeature("Count_Bag_Preceding_0_2_bb", 1, iter.next());
    this.assertFeature("Count_Bag_Preceding_0_2_cc", 1, iter.next());
    this.assertFeature("Count_Count_Preceding_0_2_bb_1", 1, iter.next());
    this.assertFeature("Count_Count_Preceding_0_2_cc_1", 1, iter.next());
    this.assertFeature("Count_Count_Count_Preceding_0_2_bb_1", 1, iter.next());
    this.assertFeature("Count_Count_Count_Preceding_0_2_cc_1", 1, iter.next());
  }

  @Test
  public void testCounts2() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(Token.class, new TypePathExtractor(
        Token.class,
        "pos"), new Count(new Preceding(2)), new Count(new Covered()), new Count(
        new Following(1, 5)), new Count(new Preceding(3), new Following(4)), new Count(new Ngrams(
        2,
        new Preceding(3),
        new Following(4))), new Count(new Ngram(new Preceding(2), new Following(2))), // silly!
        new Count(new Bag(new Preceding(2)))); // weird!
    // new Count(new Count(new Preceding(2)))); // weird & silly!

    this.tokenBuilder.buildTokens(
        this.jCas,
        "aa bb cc bb aa cc bb cc aa",
        "aa bb cc bb aa cc bb cc aa",
        "p1 p2 p2 p3 p3 p3 p3 p4 p5");
    Chunk chunk = new Chunk(this.jCas, 9, 11);
    chunk.addToIndexes();
    Assert.assertEquals("bb", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(16, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Count_Preceding_0_2_TypePath(Pos)_p2", 2, iter.next());
    this.assertFeature("Count_Covered_TypePath(Pos)_p3", 1, iter.next());
    this.assertFeature("Count_Following_1_5_TypePath(Pos)_p3", 2, iter.next());
    this.assertFeature("Count_Following_1_5_TypePath(Pos)_p4", 1, iter.next());
    this.assertFeature("Count_Following_1_5_TypePath(Pos)_p5", 1, iter.next());
    this.assertFeature("Count_Preceding_0_3_Following_0_4_TypePath(Pos)_p1", 1, iter.next());
    this.assertFeature("Count_Preceding_0_3_Following_0_4_TypePath(Pos)_p2", 2, iter.next());
    this.assertFeature("Count_Preceding_0_3_Following_0_4_TypePath(Pos)_p3", 3, iter.next());
    this.assertFeature("Count_Preceding_0_3_Following_0_4_TypePath(Pos)_p4", 1, iter.next());
    this.assertFeature(
        "Count_2grams_Preceding_0_3_Following_0_4_TypePath(Pos)_p1_p2",
        1,
        iter.next());
    this.assertFeature(
        "Count_2grams_Preceding_0_3_Following_0_4_TypePath(Pos)_p2_p2",
        1,
        iter.next());
    this.assertFeature(
        "Count_2grams_Preceding_0_3_Following_0_4_TypePath(Pos)_p2_p3",
        1,
        iter.next());
    this.assertFeature(
        "Count_2grams_Preceding_0_3_Following_0_4_TypePath(Pos)_p3_p3",
        2,
        iter.next());
    this.assertFeature(
        "Count_2grams_Preceding_0_3_Following_0_4_TypePath(Pos)_p3_p4",
        1,
        iter.next());
    this.assertFeature(
        "Count_Ngram_Preceding_0_2_Following_0_2_TypePath(Pos)_p2_p2_p3_p3",
        1,
        iter.next());
    this.assertFeature("Count_Bag_Preceding_0_2_TypePath(Pos)_p2", 2, iter.next());
  }

  @Test
  public void testNgram() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Ngram(new Preceding(2)),
        new Ngram(new Preceding(3, 6)),
        new Ngram(new Preceding(1), new FirstCovered(1), new LastCovered(1)),
        new Ngram(new Following(1, 3)),
        new Ngram(new Following(3, 5)),
        new Ngram(new Preceding(2), new Following(1, 2)));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(6, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Ngram_Preceding_0_2", "brown_fox", iter.next());
    this.assertFeature("Ngram_Preceding_3_6", "OOB2_OOB1_The", iter.next());
    this.assertFeature(
        "Ngram_Preceding_0_1_FirstCovered_0_1_LastCovered_0_1",
        "fox_jumped_over",
        iter.next());
    this.assertFeature("Ngram_Following_1_3", "lazy_dog", iter.next());
    this.assertFeature("Ngram_Following_3_5", "._OOB1", iter.next());
    this.assertFeature("Ngram_Preceding_0_2_Following_1_2", "brown_fox_lazy", iter.next());

    extractor = new CleartkExtractor(Token.class, new CoveredTextExtractor(), new Ngram(
        new Preceding(2),
        new Following(2)));

    jCas = JCasFactory.createJCas();
    this.tokenBuilder.buildTokens(this.jCas, "A B C D E");
    chunk = new Chunk(this.jCas, 4, 5);
    chunk.addToIndexes();
    Assert.assertEquals("C", chunk.getCoveredText());
    features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(1, features.size());
    this.assertFeature("Ngram_Preceding_0_2_Following_0_2", "A_B_D_E", features.get(0));

  }

  @Test
  public void testNgrams() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Ngrams(2, new Preceding(3)),
        new Ngrams(2, new Following(3)),
        new Ngrams(4, new Preceding(3), new Following(3)),
        new Ngrams(3, new Preceding(1, 5)),
        new Ngrams(2, new Covered()),
        new Ngrams(3, new Covered()));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(10, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("2grams_Preceding_0_3", "quick_brown", iter.next());
    this.assertFeature("2grams_Preceding_0_3", "brown_fox", iter.next());
    this.assertFeature("2grams_Following_0_3", "the_lazy", iter.next());
    this.assertFeature("2grams_Following_0_3", "lazy_dog", iter.next());
    this.assertFeature("4grams_Preceding_0_3_Following_0_3", "quick_brown_fox_the", iter.next());
    this.assertFeature("4grams_Preceding_0_3_Following_0_3", "brown_fox_the_lazy", iter.next());
    this.assertFeature("4grams_Preceding_0_3_Following_0_3", "fox_the_lazy_dog", iter.next());
    this.assertFeature("3grams_Preceding_1_5", "OOB1_The_quick", iter.next());
    this.assertFeature("3grams_Preceding_1_5", "The_quick_brown", iter.next());
    this.assertFeature("2grams_Covered", "jumped_over", iter.next());
  }

  @Test
  public void testFocus() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Focus(),
        new Bag(new Preceding(1), new Focus()),
        new Ngram(new Following(2), new Focus()));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Token jumped = JCasUtil.selectByIndex(this.jCas, Token.class, 4);
    Assert.assertEquals("jumped", jumped.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, jumped);
    Assert.assertEquals(4, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Focus", "jumped", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Focus", "fox", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Focus", "jumped", iter.next());
    this.assertFeature("Ngram_Following_0_2_Focus", "over_the_jumped", iter.next());

    CleartkExtractor chunkExtractor = new CleartkExtractor(
        Chunk.class,
        new CoveredTextExtractor(),
        new Focus());
    try {
      chunkExtractor.extract(this.jCas, jumped);
      Assert.fail("Expected exception from Focus of wrong type");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testBounds() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Preceding(2),
        new LastCovered(1),
        new Following(3));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "She bought milk.\nHe sold oranges.",
        "She bought milk .\nHe sold oranges .");
    Chunk boughMilk = new Chunk(this.jCas, 4, 15);
    boughMilk.addToIndexes();
    Assert.assertEquals("bought milk", boughMilk.getCoveredText());
    Chunk soldOranges = new Chunk(this.jCas, 20, 32);
    soldOranges.addToIndexes();
    Assert.assertEquals("sold oranges", soldOranges.getCoveredText());
    Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
    Assert.assertEquals(2, sentences.size());
    Iterator<Sentence> sentIter = sentences.iterator();
    Sentence sent1 = sentIter.next();
    Sentence sent2 = sentIter.next();

    List<Feature> features = extractor.extractWithin(this.jCas, boughMilk, sent1);
    Assert.assertEquals(6, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "OOB1", iter.next());
    this.assertFeature("Preceding_0_2_0", "She", iter.next());
    this.assertFeature("LastCovered_0_1_0", "milk", iter.next());
    this.assertFeature("Following_0_3_0", ".", iter.next());
    this.assertFeature("Following_0_3_1", "OOB1", iter.next());
    this.assertFeature("Following_0_3_2", "OOB2", iter.next());

    features = extractor.extractWithin(this.jCas, boughMilk, sent2);
    Assert.assertEquals(6, features.size());
    iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "OOB2", iter.next());
    this.assertFeature("Preceding_0_2_0", "OOB1", iter.next());
    this.assertFeature("LastCovered_0_1_0", "OOB1", iter.next());
    this.assertFeature("Following_0_3_0", "OOB1", iter.next());
    this.assertFeature("Following_0_3_1", "He", iter.next());
    this.assertFeature("Following_0_3_2", "sold", iter.next());

    features = extractor.extractWithin(this.jCas, soldOranges, sent2);
    Assert.assertEquals(6, features.size());
    iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "OOB1", iter.next());
    this.assertFeature("Preceding_0_2_0", "He", iter.next());
    this.assertFeature("LastCovered_0_1_0", "oranges", iter.next());
    this.assertFeature("Following_0_3_0", ".", iter.next());
    this.assertFeature("Following_0_3_1", "OOB1", iter.next());
    this.assertFeature("Following_0_3_2", "OOB2", iter.next());
  }

  @Test
  public void testExtractBetween() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Bag(new Preceding(2)),
        new Covered(),
        new Ngram(new Following(3)));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "She bought milk.\nHe sold oranges.",
        "She bought milk .\nHe sold oranges .");
    Chunk boughMilk = new Chunk(this.jCas, 4, 15);
    boughMilk.addToIndexes();
    Assert.assertEquals("bought milk", boughMilk.getCoveredText());
    Chunk soldOranges = new Chunk(this.jCas, 20, 32);
    soldOranges.addToIndexes();
    Assert.assertEquals("sold oranges", soldOranges.getCoveredText());

    List<Feature> features = extractor.extractBetween(this.jCas, boughMilk, soldOranges);
    Assert.assertEquals(5, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Bag_Preceding_0_2", "bought", iter.next());
    this.assertFeature("Bag_Preceding_0_2", "milk", iter.next());
    this.assertFeature("Covered_0", ".", iter.next());
    this.assertFeature("Covered_1", "He", iter.next());
    this.assertFeature("Ngram_Following_0_3", "sold_oranges_.", iter.next());
  }

  @Test
  public void testNestedNames() throws Exception {
    CleartkExtractor extractor = new CleartkExtractor(
        Token.class,
        new TypePathExtractor(Token.class, "pos"),
        new Count(new Preceding(1, 5), new Covered()),
        new Bag(new Preceding(3)),
        new Ngram(new Following(2)),
        new Ngrams(3, new Following(1, 6)));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Count_Preceding_1_5_Covered_TypePath(Pos)_OOB1", 1, iter.next());
    this.assertFeature("Count_Preceding_1_5_Covered_TypePath(Pos)_DT", 1, iter.next());
    this.assertFeature("Count_Preceding_1_5_Covered_TypePath(Pos)_JJ", 2, iter.next());
    this.assertFeature("Count_Preceding_1_5_Covered_TypePath(Pos)_VBD", 1, iter.next());
    this.assertFeature("Count_Preceding_1_5_Covered_TypePath(Pos)_IN", 1, iter.next());
    this.assertFeature("Bag_Preceding_0_3_TypePath(Pos)", "JJ", iter.next());
    this.assertFeature("Bag_Preceding_0_3_TypePath(Pos)", "JJ", iter.next());
    this.assertFeature("Bag_Preceding_0_3_TypePath(Pos)", "NN", iter.next());
    this.assertFeature("Ngram_Following_0_2_TypePath(Pos)", "DT_JJ", iter.next());
    this.assertFeature("3grams_Following_1_6_TypePath(Pos)", "JJ_NN_.", iter.next());
    this.assertFeature("3grams_Following_1_6_TypePath(Pos)", "NN_._OOB1", iter.next());
    this.assertFeature("3grams_Following_1_6_TypePath(Pos)", "._OOB1_OOB2", iter.next());
    Assert.assertFalse(iter.hasNext());
  }

  private void assertFeature(String expectedName, Object expectedValue, Feature actualFeature) {
    Assert.assertNotNull(actualFeature);
    Assert.assertEquals(expectedName, actualFeature.getName());
    Assert.assertEquals(expectedValue, actualFeature.getValue());
  }
}
