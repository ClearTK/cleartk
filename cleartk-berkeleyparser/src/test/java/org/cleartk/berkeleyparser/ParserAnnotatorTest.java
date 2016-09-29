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
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */

package org.cleartk.berkeleyparser;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.constituent.type.TerminalTreebankNode;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Assert;
import org.junit.Test;

import edu.berkeley.nlp.PCFGLA.GrammarTrainer;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class ParserAnnotatorTest extends BerkeleyTestBase {

  private static final String MODEL_PATH = "/models/11597317.gr";
  public static String SAMPLE_SENT = "Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair.";
  public static String SAMPLE_SENT_TOKEN = "Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair .";
  public static String SAMPLE_SENT_POSES = "DT JJ NN VBP JJ NN JJ IN DT NN IN DT NN NN NN NN NN IN NN NN ."; // changed 1st tag from CD to DT and
  // 3rd tag from NNS to NN
  @Test
  public void givenASentenceAndPosesWhenParsingThenSyntaxTreeOfTheSentenceIsConstructed() throws UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(
        ParserAnnotator.getDescription(MODEL_PATH, false));

    tokenBuilder.buildTokens(
        jCas,
        SAMPLE_SENT,
        SAMPLE_SENT_TOKEN,
        SAMPLE_SENT_POSES 
        );

    engine.process(jCas);
    engine.collectionProcessComplete();

    /*
     * (TOP (S (NP-SBJ (CD Two) (JJ recent) (NNS papers)) (VP (VBP provide) (NP (NP (JJ new) (NN
     * evidence)) (ADJP (JJ relevant) (PP (IN to) (NP (NP (DT the) (NN role)) (PP (IN of) (NP (NP
     * (DT the) (NML (NN breast) (NN cancer) (NN susceptibility)) (NN gene)) (NP (NN BRCA2))))) (PP
     * (IN in) (NP (NN DNA) (NN repair))))))) (. .)) )
     */
    TopTreebankNode tree = JCasUtil.selectByIndex(jCas, TopTreebankNode.class, 0);
    Assert.assertNotNull(tree);
    Assert.assertEquals("ROOT", tree.getNodeType());
    Assert.assertEquals(1, tree.getChildren().size());

    TreebankNode sNode = tree.getChildren(0);
    Assert.assertEquals("S", sNode.getNodeType());
    Assert.assertEquals(3, sNode.getChildren().size());

    TreebankNode npNode = sNode.getChildren(0);
    TreebankNode vpNode = sNode.getChildren(1);
    TreebankNode periodNode = sNode.getChildren(2);
    Assert.assertEquals("NP", npNode.getNodeType());
    Assert.assertEquals("VP", vpNode.getNodeType());
    Assert.assertEquals(".", periodNode.getNodeType());
    Assert.assertEquals(3, npNode.getChildren().size());

    TreebankNode twoNode = npNode.getChildren(0);
    TreebankNode recentNode = npNode.getChildren(1);
    TreebankNode papersNode = npNode.getChildren(2);
    Assert.assertEquals("DT", twoNode.getNodeType());
    Assert.assertEquals("JJ", recentNode.getNodeType());
    Assert.assertEquals("NN", papersNode.getNodeType());

    TreebankNode provideNode = vpNode.getChildren(0);
    Assert.assertEquals("VBP", provideNode.getNodeType());
    TreebankNode newEvidenceNode = vpNode.getChildren(1);
    Assert.assertEquals("NP", newEvidenceNode.getNodeType());
    Assert.assertEquals(
        "new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair",
        newEvidenceNode.getCoveredText());

  }


  @Test
  public void givenASentenceWithoutPosesWhenParsingThenSyntaxTreeOfTheSentenceIsConstructed() throws ResourceInitializationException, AnalysisEngineProcessException{
    jCas.setDocumentText(SAMPLE_SENT);
    new Sentence(jCas, 0 , SAMPLE_SENT.length()).addToIndexes();

    SimplePipeline.runPipeline(jCas, DefaultBerkeleyTokenizer.getDescription(), 
        ParserAnnotator.getDescription(MODEL_PATH, true), 
        ParseTreePosTagSetter.getDescription());

    /*
     * (TOP (S (NP-SBJ (CD Two) (JJ recent) (NNS papers)) (VP (VBP provide) (NP (NP (JJ new) (NN
     * evidence)) (ADJP (JJ relevant) (PP (IN to) (NP (NP (DT the) (NN role)) (PP (IN of) (NP (NP
     * (DT the) (NML (NN breast) (NN cancer) (NN susceptibility)) (NN gene)) (NP (NN BRCA2))))) (PP
     * (IN in) (NP (NN DNA) (NN repair))))))) (. .)) )
     */
    TopTreebankNode tree = JCasUtil.selectByIndex(jCas, TopTreebankNode.class, 0);
    Assert.assertNotNull(tree);
    Assert.assertEquals("ROOT", tree.getNodeType());
    Assert.assertEquals(1, tree.getChildren().size());

    TreebankNode sNode = tree.getChildren(0);
    Assert.assertEquals("S", sNode.getNodeType());
    Assert.assertEquals(3, sNode.getChildren().size());

    TreebankNode npNode = sNode.getChildren(0);
    TreebankNode vpNode = sNode.getChildren(1);
    TreebankNode periodNode = sNode.getChildren(2);
    Assert.assertEquals("NP", npNode.getNodeType());
    Assert.assertEquals("VP", vpNode.getNodeType());
    Assert.assertEquals(".", periodNode.getNodeType());
    Assert.assertEquals(3, npNode.getChildren().size());

    TreebankNode twoNode = npNode.getChildren(0);
    TreebankNode recentNode = npNode.getChildren(1);
    TreebankNode papersNode = npNode.getChildren(2);
    Assert.assertEquals("CD", twoNode.getNodeType());   
    Assert.assertEquals("JJ", recentNode.getNodeType());
    Assert.assertEquals("NNS", papersNode.getNodeType());

    TreebankNode provideNode = vpNode.getChildren(0);
    Assert.assertEquals("VBP", provideNode.getNodeType());
    TreebankNode newEvidenceNode = vpNode.getChildren(1);
    Assert.assertEquals("NP", newEvidenceNode.getNodeType());
    Assert.assertEquals(
        "new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair",
        newEvidenceNode.getCoveredText());

    List<Token> tokens = new ArrayList<Token>(JCasUtil.select(jCas, Token.class));
    String posTagString = "CD JJ NNS VBP JJ NN JJ IN DT NN IN DT NN NN NN NN NN IN NN NN .";
    String[] posTags = posTagString.split(" ");
    Assert.assertEquals(posTags.length, tokens.size());
    for (int i = 0; i < tokens.size(); i++){
      Assert.assertEquals(posTags[i], tokens.get(i).getPos());
    }
  }

  @Test
  public void givenASentenceWithShortFormOfToBeWhenParsingThenApostropheDoesNotChange() throws ResourceInitializationException, AnalysisEngineProcessException{
    String sent = "I've provided new evidence.";
    jCas.setDocumentText(sent);
    new Sentence(jCas, 0 , sent.length()).addToIndexes();

    SimplePipeline.runPipeline(jCas, DefaultBerkeleyTokenizer.getDescription(), 
        ParserAnnotator.getDescription(MODEL_PATH, true));
    
    Assert.assertEquals(1, JCasUtil.select(jCas, TopTreebankNode.class).size());
    TopTreebankNode tree = JCasUtil.selectByIndex(jCas, TopTreebankNode.class, 0);
    Assert.assertNotNull(tree);
    Assert.assertEquals("ROOT", tree.getNodeType());
    Assert.assertEquals(1, tree.getChildren().size());

    TreebankNode sNode = tree.getChildren(0);
    Assert.assertEquals("S", sNode.getNodeType());
    Assert.assertEquals(3, sNode.getChildren().size());
    
    TreebankNode npNode = sNode.getChildren(0);
    TreebankNode vpNode = sNode.getChildren(1);
    TreebankNode periodNode = sNode.getChildren(2);
    Assert.assertEquals("NP", npNode.getNodeType());
    Assert.assertEquals("VP", vpNode.getNodeType());
    Assert.assertEquals(".", periodNode.getNodeType());
    Assert.assertEquals(2, vpNode.getChildren().size());
   
    TreebankNode vbnNode = vpNode.getChildren(0);
    Assert.assertEquals("VBN", vbnNode.getNodeType()); 
    Assert.assertEquals("'ve", vbnNode.getNodeValue());
  }
  
  @Test
  public void whenParsingASentenceThenNoDuplicateTreeNodeIsGenerated() throws ResourceInitializationException, AnalysisEngineProcessException{
    String sent = "I've provided new evidence.";
    //(ROOT (S (@S (NP (NN I)) (VP (VBN 've) (S (VP (VBN provided) (S (NP (JJ new) (NN evidence))))))) (. .)))
    jCas.setDocumentText(sent);
    new Sentence(jCas, 0 , sent.length()).addToIndexes();

    SimplePipeline.runPipeline(jCas, DefaultBerkeleyTokenizer.getDescription(), 
        ParserAnnotator.getDescription(MODEL_PATH, true));
    Assert.assertEquals(1, JCasUtil.select(jCas, TopTreebankNode.class).size());
    Assert.assertEquals(6, JCasUtil.select(jCas, TerminalTreebankNode.class).size());

    Assert.assertEquals(14, JCasUtil.select(jCas, TreebankNode.class).size());
  }
    
  public static void main(String[] args) {
    GrammarTrainer.main(new String[] {
        "-path",
        "src/test/resources/data/treebank/11597317.tree",
        "-out",
        "src/test/resources/models/11597317.gr",
        "-treebank",
    "SINGLEFILE" });

  }
}
