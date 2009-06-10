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

import java.io.IOException;
import java.util.logging.Level;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.DisableLogging;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class OpenNLPTreebankParserTests {

	@Test
	public void testMissingParameters() throws UIMAException {
		try {
			AnalysisEngineFactory.createPrimitive(
					OpenNLPTreebankParser.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
			Assert.fail("expected error for missing parser parameters");
		} catch (ResourceInitializationException e) {}

		try {
			AnalysisEngineFactory.createPrimitive(
					OpenNLPTreebankParser.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					"buildModelFile", "resources/test/models/fox_dog_parser/build.bin.gz");
			Assert.fail("expected error for missing parser parameters");
		} catch (ResourceInitializationException e) {}

		try {
			AnalysisEngineFactory.createPrimitive(
					OpenNLPTreebankParser.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					"buildModelFile", "resources/test/models/fox_dog_parser/build.bin.gz",
					"checkModelFile", "resources/test/models/fox_dog_parser/check.bin.gz");
			Assert.fail("expected error for missing parser parameters");
		} catch (ResourceInitializationException e) {}

		try {
			AnalysisEngineFactory.createPrimitive(
					OpenNLPTreebankParser.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					"buildModelFile", "resources/test/models/fox_dog_parser/build.bin.gz",
					"checkModelFile", "resources/test/models/fox_dog_parser/check.bin.gz",
					"chunkModelFile", "resources/test/models/fox_dog_parser/chunk.bin.gz");
			Assert.fail("expected error for missing parser parameters");
		} catch (ResourceInitializationException e) {}
	}
	
	@Test
	public void test() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				OpenNLPTreebankParser.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE, "test/models/fox_dog_parser/build.bin.gz",
				OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE, "test/models/fox_dog_parser/check.bin.gz",
				OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE, "test/models/fox_dog_parser/chunk.bin.gz",
				OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE, "test/models/fox_dog_parser/head_rules");
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"The brown fox jumped quickly over the lazy dog.", Token.class, Sentence.class, 
				"The brown fox jumped quickly over the lazy dog .",
				"DT JJ NN VBZ RB IN DT JJ NN .",
				null, "org.cleartk.type.Token:pos", null);
		
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		/*  Expected tree structure
		(TOP
		  (S
		    (NP
		      (DT The)
		      (JJ brown)
		      (NN fox))
		    (VP
		      (VBD jumped)
		      (ADVP
		        (RB quickly))
		      (PP
		        (IN over)
		        (NP
		          (DT the)
		          (JJ lazy)
		          (NN dog))))
		    (. .)))
		*/
		Sentence sentence = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).get(0); 
		TopTreebankNode tree = sentence.getConstituentParse();
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
		
		TreebankNode theNode = npNode.getChildren(0);
		TreebankNode brownNode = npNode.getChildren(1);
		TreebankNode foxNode = npNode.getChildren(2);
		Assert.assertEquals("DT", theNode.getNodeType());
		Assert.assertEquals("JJ", brownNode.getNodeType());
		Assert.assertEquals("NN", foxNode.getNodeType());
		Assert.assertEquals("The", theNode.getNodeValue());
		Assert.assertEquals("brown", brownNode.getNodeValue());
		Assert.assertEquals("fox", foxNode.getNodeValue());
		
	}
	
	@Test
	public void testNoPos() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				OpenNLPTreebankParser.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE, "test/models/fox_dog_parser/build.bin.gz",
				OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE, "test/models/fox_dog_parser/check.bin.gz",
				OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE, "test/models/fox_dog_parser/chunk.bin.gz",
				OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE, "test/models/fox_dog_parser/head_rules");
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"The brown fox jumped quickly over the lazy dog.",
				Token.class, Sentence.class, 
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
	
	@Test
	public void testLongParse() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				OpenNLPTreebankParser.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE, "test/models/fox_dog_parser/build.bin.gz",
				OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE, "test/models/fox_dog_parser/check.bin.gz",
				OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE, "test/models/fox_dog_parser/chunk.bin.gz",
				OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE, "test/models/fox_dog_parser/head_rules");
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"The brown fox jumped quickly over the lazy dog " +
				"who jumped quickly over the brown fox " +
				"who jumped quickly over the lazy dog " +
				"who jumped quickly over the brown fox " +
				"who jumped quickly over the lazy dog.",
				Token.class, Sentence.class, 
				"The brown fox jumped quickly over the lazy dog " +
				"who jumped quickly over the brown fox " +
				"who jumped quickly over the lazy dog " +
				"who jumped quickly over the brown fox " +
				"who jumped quickly over the lazy dog .",
				"DT JJ NN VBZ RB IN DT JJ NN " +
				"WDT VBZ RB IN DT JJ NN " +
				"WDT VBZ RB IN DT JJ NN " +
				"WDT VBZ RB IN DT JJ NN " +
				"WDT VBZ RB IN DT JJ NN .",
				null, "org.cleartk.type.Token:pos", null);
		engine.process(jCas);
		engine.collectionProcessComplete();
		Sentence sentence = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).get(0);
		Assert.assertNotNull(sentence.getConstituentParse());
	}
	
	@Test
	public void testDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.syntax.opennlp.OpenNLPTreebankParser");
		
		Object buildModelFile = engine.getConfigParameterValue(
				OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE);
		Assert.assertEquals("resources/models/OpenNLP.Parser.English.Build.bin.gz", buildModelFile);
		
		Object checkModelFile = engine.getConfigParameterValue(
				OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE);
		Assert.assertEquals("resources/models/OpenNLP.Parser.English.Check.bin.gz", checkModelFile);
		
		Object chunkModelFile = engine.getConfigParameterValue(
				OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE);
		Assert.assertEquals("resources/models/OpenNLP.Chunker.English.bin.gz", chunkModelFile);
		
		Object headRulesFile = engine.getConfigParameterValue(
				OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE);
		Assert.assertEquals("resources/models/OpenNLP.HeadRules.txt", headRulesFile);
		
		Object beamSize = engine.getConfigParameterValue(OpenNLPTreebankParser.PARAM_BEAM_SIZE);
		Assert.assertEquals(null, beamSize);
		
		Object advancePercentage = engine.getConfigParameterValue(
				OpenNLPTreebankParser.PARAM_ADVANCE_PERCENTAGE);
		Assert.assertEquals(null, advancePercentage);
		
		engine.collectionProcessComplete();
	}
}
