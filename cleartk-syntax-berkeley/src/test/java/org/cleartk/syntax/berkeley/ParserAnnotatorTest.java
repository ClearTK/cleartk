/** 
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
