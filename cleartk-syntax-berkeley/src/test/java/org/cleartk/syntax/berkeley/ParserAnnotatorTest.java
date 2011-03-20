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

package org.cleartk.syntax.berkeley;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.syntax.constituent.ParserWrapper_ImplBase;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import edu.berkeley.nlp.PCFGLA.GrammarTrainer;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class ParserAnnotatorTest extends BerkeleyTestBase{

  private static final String MODEL_PATH = "/models/11597317.gr";

  @Test
  public void test() throws UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        ParserAnnotator.class,
        typeSystemDescription,
        ParserAnnotator.PARAM_PARSER_MODEL_PATH,
        MODEL_PATH,
        ParserWrapper_ImplBase.PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME,
        DefaultOutputTypesHelper.class.getName());
    tokenBuilder
        .buildTokens(
            jCas,
            "Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair.",
            "Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair .",
            "DT JJ NN VBP JJ NN JJ IN DT NN IN DT NN NN NN NN NN IN NN NN ." // changed 1st
                                                                             // tag from CD
                                                                             // to DT and
                                                                             // 3rd tag from
                                                                             // NNS to NN
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
    Assert
        .assertEquals(
            "new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair",
            newEvidenceNode.getCoveredText());

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
