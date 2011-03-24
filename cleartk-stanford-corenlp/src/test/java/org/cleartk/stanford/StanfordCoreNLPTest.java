/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-stanford-corenlp project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.stanford;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class StanfordCoreNLPTest extends CleartkTestBase {

  protected TokenBuilder<Token, Sentence> tokenBuilder;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");
  }

  @Override
  public String[] getTypeSystemDescriptorNames() {
    return StanfordCoreNLPComponents.TYPE_SYSTEM_DESCRIPTOR_NAMES;
  }

  @Test
  public void test() throws Throwable {
    String sent1 = "The Stanford-based Dr. Smith bought \n milk for Martha.";
    String sent2 = "So she thanked him for it \n and put the milk into her bag.";
    this.jCas.setDocumentText(String.format("%s %s", sent1, sent2));
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(StanfordCoreNLPAnnotator
        .getDescription());
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // check tokens
    List<String> expected = Arrays.asList(
        "The",
        "Stanford-based",
        "Dr.",
        "Smith",
        "bought",
        "milk",
        "for",
        "Martha",
        ".",
        "So",
        "she",
        "thanked",
        "him",
        "for",
        "it",
        "and",
        "put",
        "the",
        "milk",
        "into",
        "her",
        "bag",
        ".");
    List<String> actual = JCasUtil.toText(JCasUtil.iterate(this.jCas, Token.class));
    Assert.assertEquals(expected, actual);

    // check sentences
    expected = Arrays.asList(sent1, sent2);
    actual = JCasUtil.toText(JCasUtil.iterate(this.jCas, Sentence.class));
    Assert.assertEquals(expected, actual);

    // check named entities
    Iterator<NamedEntityMention> nes;
    nes = JCasUtil.iterate(this.jCas, NamedEntityMention.class).iterator();
    NamedEntityMention ne = nes.next();
    Assert.assertEquals("Stanford-based", ne.getCoveredText());
    Assert.assertEquals("MISC", ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("Dr. Smith", ne.getCoveredText());
    Assert.assertEquals("PERSON", ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("milk", ne.getCoveredText());
    Assert.assertNull(ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("Martha", ne.getCoveredText());
    Assert.assertEquals("PERSON", ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("she", ne.getCoveredText());
    Assert.assertNull(ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("him", ne.getCoveredText());
    Assert.assertNull(ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("it", ne.getCoveredText());
    Assert.assertNull(ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("the milk", ne.getCoveredText());
    Assert.assertNull(ne.getMentionType());
    ne = nes.next();
    Assert.assertEquals("her", ne.getCoveredText());
    Assert.assertNull(ne.getMentionType());
    Assert.assertFalse(nes.hasNext());

    // check syntactic trees
    Iterator<TopTreebankNode> trees;
    trees = JCasUtil.iterate(this.jCas, TopTreebankNode.class).iterator();
    TopTreebankNode tree = trees.next();
    Assert.assertEquals(sent1, tree.getCoveredText());
    Assert.assertEquals("The Stanford-based Dr. Smith", tree.getChildren(0).getCoveredText());
    int smiths = 0;
    for (TreebankNode node : JCasUtil.iterate(jCas, TreebankNode.class)) {
      if (node.getCoveredText().equals("Smith")) {
        smiths += 1;
      }
    }
    Assert.assertEquals(1, smiths);
    tree = trees.next();
    Assert.assertEquals(sent2, tree.getCoveredText());
    Assert.assertEquals("IN", tree.getChildren(0).getNodeType());
    Assert.assertEquals("NP", tree.getChildren(1).getNodeType());
    Assert.assertEquals("VP", tree.getChildren(2).getNodeType());
    Assert.assertFalse(trees.hasNext());

    // check dependency trees
    Iterator<DependencyNode> nodes = JCasUtil.iterator(jCas, DependencyNode.class);
    DependencyNode the1 = nodes.next();
    Assert.assertEquals("The", the1.getCoveredText());
    DependencyNode stanfordBased = nodes.next();
    Assert.assertEquals("Stanford-based", stanfordBased.getCoveredText());
    DependencyNode dr = nodes.next();
    Assert.assertEquals("Dr.", dr.getCoveredText());
    DependencyNode smith = nodes.next();
    Assert.assertEquals("Smith", smith.getCoveredText());
    DependencyNode bought = nodes.next();
    Assert.assertEquals("bought", bought.getCoveredText());
    Assert.assertTrue(bought instanceof TopDependencyNode);
    DependencyNode milk1 = nodes.next();
    Assert.assertEquals("milk", milk1.getCoveredText());
    DependencyNode martha = nodes.next();
    Assert.assertEquals("Martha", martha.getCoveredText());
    DependencyNode so = nodes.next();
    Assert.assertEquals("So", so.getCoveredText());
    DependencyNode she = nodes.next();
    Assert.assertEquals("she", she.getCoveredText());
    DependencyNode thanked = nodes.next();
    Assert.assertEquals("thanked", thanked.getCoveredText());
    Assert.assertTrue(thanked instanceof TopDependencyNode);
    DependencyNode him = nodes.next();
    Assert.assertEquals("him", him.getCoveredText());
    DependencyNode it = nodes.next();
    Assert.assertEquals("it", it.getCoveredText());
    DependencyNode put = nodes.next();
    Assert.assertEquals("put", put.getCoveredText());
    DependencyNode the2 = nodes.next();
    Assert.assertEquals("the", the2.getCoveredText());
    DependencyNode milk2 = nodes.next();
    Assert.assertEquals("milk", milk2.getCoveredText());
    DependencyNode her = nodes.next();
    Assert.assertEquals("her", her.getCoveredText());
    DependencyNode bag = nodes.next();
    Assert.assertEquals("bag", bag.getCoveredText());
    Assert.assertFalse(nodes.hasNext());

    // check some dependency relations
    Assert.assertEquals(1, the1.getHeadRelations().size());
    Assert.assertEquals(smith, the1.getHeadRelations(0).getHead());
    Assert.assertEquals(the1, the1.getHeadRelations(0).getChild());
    Assert.assertEquals(3, smith.getChildRelations().size());
    Assert.assertEquals(the1, smith.getChildRelations(0).getChild());
    Assert.assertEquals(stanfordBased, smith.getChildRelations(1).getChild());
    Assert.assertEquals(dr, smith.getChildRelations(2).getChild());
    Assert.assertEquals(0, bought.getHeadRelations().size());
    Assert.assertEquals(3, bought.getChildRelations().size());
    Assert.assertEquals(smith, bought.getChildRelations(0).getChild());
    Assert.assertEquals(milk1, bought.getChildRelations(1).getChild());
    Assert.assertEquals("dobj", bought.getChildRelations(1).getRelation());
    Assert.assertEquals(martha, bought.getChildRelations(2).getChild());
    Assert.assertEquals("prep_for", bought.getChildRelations(2).getRelation());
    Assert.assertEquals(bought, bought.getChildRelations(2).getHead());
    Assert.assertEquals(0, martha.getChildRelations().size());
    Assert.assertEquals(1, martha.getHeadRelations().size());
    Assert.assertEquals(bought, martha.getHeadRelations(0).getHead());
    Assert.assertEquals(2, she.getHeadRelations().size());
    Assert.assertEquals(thanked, she.getHeadRelations(0).getHead());
    Assert.assertEquals(put, she.getHeadRelations(1).getHead());
    Assert.assertEquals("nsubj", she.getHeadRelations(0).getRelation());
    Assert.assertEquals("nsubj", she.getHeadRelations(1).getRelation());

    // check coreference
    List<List<String>> expectedEntities = new ArrayList<List<String>>();
    expectedEntities.add(Arrays.asList("Stanford-based"));
    expectedEntities.add(Arrays.asList("Dr. Smith", "him"));
    expectedEntities.add(Arrays.asList("milk", "it", "the milk"));
    expectedEntities.add(Arrays.asList("Martha", "she", "her"));
    List<List<String>> actualEntities = new ArrayList<List<String>>();
    for (NamedEntity entity : JCasUtil.iterate(this.jCas, NamedEntity.class)) {
      List<String> mentionTexts = new ArrayList<String>();
      for (NamedEntityMention mention : JCasUtil.iterate(
          entity.getMentions(),
          NamedEntityMention.class)) {
        mentionTexts.add(mention.getCoveredText());
      }
      actualEntities.add(mentionTexts);
    }
    for (NamedEntityMention mention : JCasUtil.iterate(this.jCas, NamedEntityMention.class)) {
      Assert.assertNotNull(mention.getMentionedEntity());
    }
    Assert.assertEquals(expectedEntities, actualEntities);
  }
}
