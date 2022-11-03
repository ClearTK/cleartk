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
package org.cleartk.stanford.corenlp;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.test.util.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

/**
 *
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 *
 * @author Steven Bethard
 */
public class StanfordCoreNlpTest
    extends CleartkTestBase
{

    protected TokenBuilder<Token, Sentence> tokenBuilder;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos",
                "stem");
    }

    @Test
    public void test() throws Throwable
    {
        this.assumeBigMemoryTestsEnabled();
        this.logger.info(BIG_MEMORY_TEST_MESSAGE);

        String sent1 = "The Stanford-based Dr. Smith bought \n milk for Martha.";
        String sent2 = "So she thanked him for it \n and put the milk into her bag.";
        this.jCas.setDocumentText(String.format("%s %s", sent1, sent2));
        AnalysisEngine engine = AnalysisEngineFactory
                .createEngine(StanfordCoreNlpAnnotator.getDescription());
        engine.process(this.jCas);
        engine.collectionProcessComplete();

        // check tokens
        List<String> actual = JCasUtil.toText(JCasUtil.select(this.jCas, Token.class));
        assertThat(actual).containsExactly("The", "Stanford", "-", "based", "Dr.", "Smith", "bought",
                "milk", "for", "Martha", ".", "So", "she", "thanked", "him", "for", "it", "and",
                "put", "the", "milk", "into", "her", "bag", ".");

        // check sentences
        actual = JCasUtil.toText(JCasUtil.select(this.jCas, Sentence.class));
        assertThat(actual).containsExactly(sent1, sent2);

        // check named entities
        Collection<NamedEntityMention> nes = JCasUtil.select(this.jCas, NamedEntityMention.class);
        Iterator<NamedEntityMention> nesIter = nes.iterator();
        NamedEntityMention ne = nesIter.next();
        Assert.assertEquals("The Stanford-based Dr. Smith", ne.getCoveredText());
        Assert.assertNull(ne.getMentionType());
        ne = nesIter.next();
        Assert.assertEquals("Stanford", ne.getCoveredText());
        Assert.assertEquals("ORGANIZATION", ne.getMentionType());
        ne = nesIter.next();
        // bug in Stanford coref - should be "Dr. Smith"
        Assert.assertEquals("Smith", ne.getCoveredText());
        Assert.assertEquals("PERSON", ne.getMentionType());
        ne = nesIter.next();
        // bug in Stanford coref - the first "milk" is not found
        // Assert.assertEquals("milk", ne.getCoveredText());
        // Assert.assertNull(ne.getMentionType());
        // ne = nesIter.next();
        Assert.assertEquals("Martha", ne.getCoveredText());
        Assert.assertEquals("PERSON", ne.getMentionType());
        ne = nesIter.next();
        Assert.assertEquals("she", ne.getCoveredText());
        Assert.assertNull(ne.getMentionType());
        ne = nesIter.next();
        Assert.assertEquals("him", ne.getCoveredText());
        Assert.assertNull(ne.getMentionType());
        ne = nesIter.next();
        Assert.assertEquals("her", ne.getCoveredText());
        Assert.assertNull(ne.getMentionType());
        Assert.assertFalse(nesIter.hasNext());

        // check syntactic trees
        Iterator<TopTreebankNode> trees;
        trees = JCasUtil.select(this.jCas, TopTreebankNode.class).iterator();
        TopTreebankNode tree = trees.next();
        Assert.assertEquals(sent1, tree.getCoveredText());
        Assert.assertEquals("The Stanford-based Dr. Smith", tree.getChildren(0).getCoveredText());
        int smiths = 0;
        for (TreebankNode node : JCasUtil.select(jCas, TreebankNode.class)) {
            if (node.getCoveredText().equals("Smith")) {
                smiths += 1;
            }
        }
        Assert.assertEquals(1, smiths);
        tree = trees.next();
        Assert.assertEquals(sent2, tree.getCoveredText());
        Assert.assertEquals("ADVP", tree.getChildren(0).getNodeType());
        Assert.assertEquals("NP", tree.getChildren(1).getNodeType());
        Assert.assertEquals("VP", tree.getChildren(2).getNodeType());
        Assert.assertFalse(trees.hasNext());

        // check dependency trees
        Iterator<DependencyNode> nodes = JCasUtil.iterator(jCas, DependencyNode.class);
        DependencyNode the1 = nodes.next();
        Assert.assertEquals("The", the1.getCoveredText());
        DependencyNode stanford = nodes.next();
        Assert.assertEquals("Stanford", stanford.getCoveredText());
        DependencyNode dash = nodes.next();
        Assert.assertEquals("-", dash.getCoveredText());
        DependencyNode based = nodes.next();
        Assert.assertEquals("based", based.getCoveredText());
        DependencyNode dr = nodes.next();
        Assert.assertEquals("Dr.", dr.getCoveredText());
        DependencyNode smith = nodes.next();
        Assert.assertEquals("Smith", smith.getCoveredText());
        DependencyNode bought = nodes.next();
        Assert.assertEquals("bought", bought.getCoveredText());
        Assert.assertTrue(bought instanceof TopDependencyNode);
        DependencyNode milk1 = nodes.next();
        Assert.assertEquals("milk", milk1.getCoveredText());
        Assert.assertEquals("for", nodes.next().getCoveredText());
        DependencyNode martha = nodes.next();
        Assert.assertEquals("Martha", martha.getCoveredText());
        Assert.assertEquals(".", nodes.next().getCoveredText());
        Assert.assertEquals("So", nodes.next().getCoveredText());
        DependencyNode she = nodes.next();
        Assert.assertEquals("she", she.getCoveredText());
        DependencyNode thanked = nodes.next();
        Assert.assertEquals("thanked", thanked.getCoveredText());
        Assert.assertTrue(thanked instanceof TopDependencyNode);
        Assert.assertEquals("him", nodes.next().getCoveredText());
        Assert.assertEquals("for", nodes.next().getCoveredText());
        Assert.assertEquals("it", nodes.next().getCoveredText());
        Assert.assertEquals("and", nodes.next().getCoveredText());
        Assert.assertEquals("put", nodes.next().getCoveredText());
        Assert.assertEquals("the", nodes.next().getCoveredText());
        Assert.assertEquals("milk", nodes.next().getCoveredText());
        Assert.assertEquals("into", nodes.next().getCoveredText());
        Assert.assertEquals("her", nodes.next().getCoveredText());
        Assert.assertEquals("bag", nodes.next().getCoveredText());
        Assert.assertEquals(".", nodes.next().getCoveredText());
        Assert.assertFalse(nodes.hasNext());

        // check some dependency relations
        Assert.assertEquals(1, the1.getHeadRelations().size());
        Assert.assertEquals(smith, the1.getHeadRelations(0).getHead());
        Assert.assertEquals(the1, the1.getHeadRelations(0).getChild());
        Assert.assertEquals(3, smith.getChildRelations().size());
        Assert.assertEquals(the1, smith.getChildRelations(0).getChild());
        Assert.assertEquals(based, smith.getChildRelations(1).getChild());
        Assert.assertEquals(dr, smith.getChildRelations(2).getChild());
        Assert.assertEquals(0, bought.getHeadRelations().size());
        Assert.assertEquals(4, bought.getChildRelations().size());
        Assert.assertEquals(smith, bought.getChildRelations(0).getChild());
        Assert.assertEquals(milk1, bought.getChildRelations(1).getChild());
        Assert.assertEquals("obj", bought.getChildRelations(1).getRelation());
        Assert.assertEquals(martha, bought.getChildRelations(2).getChild());
        Assert.assertEquals("obl:for", bought.getChildRelations(2).getRelation());
        Assert.assertEquals(bought, bought.getChildRelations(2).getHead());
        Assert.assertEquals(1, martha.getChildRelations().size());
        Assert.assertEquals(1, martha.getHeadRelations().size());
        Assert.assertEquals(bought, martha.getHeadRelations(0).getHead());
        Assert.assertEquals(2, she.getHeadRelations().size());
        Assert.assertEquals(thanked, she.getHeadRelations(0).getHead());
        Assert.assertEquals("nsubj", she.getHeadRelations(0).getRelation());
        Assert.assertEquals("nsubj", she.getHeadRelations(1).getRelation());

        // check coreference
        // current Stanford coref is buggy in several ways; correct answers are commented out
        List<List<String>> expectedEntities = new ArrayList<List<String>>();
        // expectedEntities.add(Arrays.asList("The Stanford-based Dr. Smith", "him"));
        // expectedEntities.add(Arrays.asList("Stanford-based"));
        // expectedEntities.add(Arrays.asList("milk", "it", "the milk"));
        // expectedEntities.add(Arrays.asList("Martha", "she", "her"));


        expectedEntities.add(Arrays.asList("The Stanford-based Dr. Smith", "him"));
        expectedEntities.add(Arrays.asList("Martha", "she", "her"));
        expectedEntities.add(Arrays.asList("Stanford"));
        expectedEntities.add(Arrays.asList("Smith"));
        List<List<String>> actualEntities = new ArrayList<List<String>>();
        for (NamedEntity entity : JCasUtil.select(this.jCas, NamedEntity.class)) {
            List<String> mentionTexts = new ArrayList<String>();
            for (NamedEntityMention mention : JCasUtil.select(entity.getMentions(),
                    NamedEntityMention.class)) {
                mentionTexts.add(mention.getCoveredText());
            }
            actualEntities.add(mentionTexts);
        }
        
        for (NamedEntityMention mention : JCasUtil.select(this.jCas, NamedEntityMention.class)) {
            Assert.assertNotNull(mention.getMentionedEntity());
        }
        
        assertThat(actualEntities).containsExactlyInAnyOrderElementsOf(expectedEntities);
    }
}
