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
package org.cleartk.syntax.opennlp;

import java.util.logging.Level;

import opennlp.tools.cmdline.CLI;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.cleartk.syntax.SyntaxTestBase;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.testing.util.DisableLogging;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class ParserAnnotatorTest extends SyntaxTestBase {

	private static final String MODEL_PATH = "/data/parser/craft_test/craft_test.bin";
	@Test
	public void test() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				ParserAnnotator.class,
				typeSystemDescription,
				ParserAnnotator.PARAM_PARSER_MODEL_PATH, MODEL_PATH,
				ParserAnnotator.PARAM_USE_TAGS_FROM_CAS, true);
		tokenBuilder.buildTokens(jCas,
				"Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair.",  
				"Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair .",
				"DT JJ NN VBP JJ NN JJ IN DT NN IN DT NN NN NN NN NN IN NN NN ." //changed 1st tag from CD to DT and 3rd tag from NNS to NN
				);
		
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		/*
		 * (TOP (S (NP-SBJ (CD Two) (JJ recent) (NNS papers)) 
		 * 		(VP (VBP provide) (NP (NP (JJ new) (NN evidence)) (ADJP (JJ relevant) (PP (IN to) (NP (NP (DT the) (NN role)) (PP (IN of) 
		 * (NP (NP (DT the) (NML (NN breast) (NN cancer) (NN susceptibility)) (NN gene)) (NP (NN BRCA2))))) (PP (IN in) (NP (NN DNA) (NN repair))))))) (. .)) )
		 */
		Sentence sentence = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).get(0); 
		TopTreebankNode tree = AnnotationRetrieval.getContainingAnnotation(jCas, sentence, TopTreebankNode.class, false);
		
		Assert.assertNotNull(tree);
		Assert.assertEquals("TOP", tree.getNodeType());
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
		Assert.assertEquals("Two", twoNode.getNodeValue());
		Assert.assertEquals("recent", recentNode.getNodeValue());
		Assert.assertEquals("papers", papersNode.getNodeValue());

		TreebankNode provideNode = vpNode.getChildren(0);
		TreebankNode newEvidenceNode = vpNode.getChildren(1);
		Assert.assertEquals("VBP", provideNode.getNodeType());
		Assert.assertEquals("provide", provideNode.getNodeValue());
		Assert.assertEquals("NP", newEvidenceNode.getNodeType());
		Assert.assertEquals("new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair", newEvidenceNode.getCoveredText());

	}

	@Test
	public void test2() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				ParserAnnotator.class,
				typeSystemDescription,
				ParserAnnotator.PARAM_PARSER_MODEL_PATH, MODEL_PATH,
				ParserAnnotator.PARAM_USE_TAGS_FROM_CAS, false,
				ParserAnnotator.PARAM_INPUT_TYPES_HELPER_CLASS_NAME, TestInputTypesHelper.class.getName());
		TokenBuilder<org.cleartk.type.test.Token, org.cleartk.type.test.Sentence> tokBuilder = new TokenBuilder<org.cleartk.type.test.Token, org.cleartk.type.test.Sentence>(org.cleartk.type.test.Token.class, org.cleartk.type.test.Sentence.class);
		tokBuilder.buildTokens(jCas,
				"Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair.",  
				"Two recent papers provide new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair ."
				);
		
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		org.cleartk.type.test.Sentence sentence = AnnotationRetrieval.getAnnotations(jCas, org.cleartk.type.test.Sentence.class).get(0); 
		TopTreebankNode tree = AnnotationRetrieval.getContainingAnnotation(jCas, sentence, TopTreebankNode.class, false);
//		TreebankNodeUtility.print(System.out, tree);
		
		Assert.assertNotNull(tree);
		Assert.assertEquals("TOP", tree.getNodeType());
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
		Assert.assertEquals("Two", twoNode.getNodeValue());
		Assert.assertEquals("recent", recentNode.getNodeValue());
		Assert.assertEquals("papers", papersNode.getNodeValue());

		TreebankNode provideNode = vpNode.getChildren(0);
		TreebankNode newEvidenceNode = vpNode.getChildren(1);
		Assert.assertEquals("VBP", provideNode.getNodeType());
		Assert.assertEquals("provide", provideNode.getNodeValue());
		Assert.assertEquals("NP", newEvidenceNode.getNodeType());
		Assert.assertEquals("new evidence relevant to the role of the breast cancer susceptibility gene BRCA2 in DNA repair", newEvidenceNode.getCoveredText());

	}

	
	@Test
	public void testNoPos() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				ParserAnnotator.class,
				typeSystemDescription,
				ParserAnnotator.PARAM_PARSER_MODEL_PATH, MODEL_PATH,
				ParserAnnotator.PARAM_USE_TAGS_FROM_CAS, true);
		tokenBuilder.buildTokens(jCas,
				"The brown fox jumped quickly over the lazy dog.",
				"The brown fox jumped quickly over the lazy dog .");
		Level level = DisableLogging.disableLogging();
		try {
			engine.process(jCas);
		} catch (AnalysisEngineProcessException e) {
			if (!e.getCause().getMessage().contains("part of speech")) {
				Assert.fail("expected exception for no part of speech tags");
			}
		} finally {
			engine.collectionProcessComplete();
			DisableLogging.enableLogging(level);
		}
	}
	
	public static void main(String[] args) {
		CLI.main(new String[]{
				"ParserTrainer",
				"-lang", "en",
				"-encoding", "UTF-8",
				"-parserType", "CHUNKING",
				"-head-rules", "src/test/resources/data/parser/fox_dog_parser/en_head_rules",
				"-data", "src/test/resources/data/parser/fox_dog_parser/fox_dog.tree",
				"-model", "src/test/resources/data/parser/fox_dog_parser/fox_dog.bin"
		});

	}

}
