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
package org.cleartk.srl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.ClassifierAnnotator;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.svmlight.DefaultSVMlightDataWriterFactory;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.TreebankTestsUtil;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationHandlerTestUtil2;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.EmptyAnnotator;
import org.cleartk.util.UIMAUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.TearDownUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class PredicateArgumentHandlerTest {
	
	private final File predicateOutputDir = new File("test/data/srl/predicate-output");
	private final File argumentOutputDir = new File("test/data/srl/argument-output");
	
	@After
	public void tearDown() throws Exception {
		TearDownUtil.removeDirectory(predicateOutputDir);
		Assert.assertFalse(predicateOutputDir.exists());
		TearDownUtil.removeDirectory(argumentOutputDir);
		Assert.assertFalse(argumentOutputDir.exists());
	}

	@Test
	public void testArgumentAnnotationNoPredicate() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setTrees(jCas);
		
		// make sure the handler produces no instances
		List<Instance<String>> instances = AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentAnnotationHandler(), engine, jCas);
		Assert.assertEquals(0, instances.size());
	}
		
	@Test
	public void testArgumentIdentificationNoPredicate() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setTrees(jCas);
		
		// make sure the handler produces no instances
		List<Instance<Boolean>> instances = AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentIdentificationHandler(), engine, jCas);
		Assert.assertEquals(0, instances.size());
	}
		
	@Test
	public void testArgumentClassificationNoPredicate() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setTrees(jCas);
		
		// make sure the handler produces no instances
		List<Instance<String>> instances = AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentClassificationHandler(), engine, jCas);
		Assert.assertEquals(0, instances.size());
	}
		
	@Test
	public void testArgumentAnnotationNoTree() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setPredicates(jCas);
		
		// make sure the handler produces an exception
		try {
			AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentAnnotationHandler(), engine, jCas);
			Assert.fail("expected exception for missing TopTreebankNode");
		} catch (IllegalArgumentException e) {}
	}
		
	@Test
	public void testArgumentIdentificationNoTree() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setPredicates(jCas);
		
		// make sure the handler produces an exception
		try {
			AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentIdentificationHandler(), engine, jCas);
			Assert.fail("expected exception for missing TopTreebankNode");
		} catch (IllegalArgumentException e) {}
	}
		
	@Test
	public void testArgumentClassificationNoTree() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setPredicates(jCas);
		
		// make sure the handler produces an exception
		try {
			AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentClassificationHandler(), engine, jCas);
			Assert.fail("expected exception for missing TopTreebankNode");
		} catch (IllegalArgumentException e) {}
	}
		
	@Test
	public void testPredicateAnnotation() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setTrees(jCas);
		this.setPredicates(jCas);
		
		// get the instances produced by the handler
		List<Instance<Boolean>> instances = AnnotationHandlerTestUtil2.produceInstances(
				new PredicateAnnotationHandler(), engine, jCas);
		Assert.assertEquals(5, instances.size());
		Object[] featureValues;
		
		// check "broke"
		Instance<Boolean> brokeInstance = instances.get(1);
		featureValues = new Object[] {
				"broke", "break", "VBD",
				"John", "John", "NNP",
				null,
				"the", "the", "DT",
				"lamp", "lamp", "NN"};
		Assert.assertEquals(
				Arrays.asList(featureValues),
				this.getFeatureValues(brokeInstance));
		Assert.assertEquals(true, brokeInstance.getOutcome());

		// check "lamp"
		Instance<Boolean> lampInstance = instances.get(3);
		featureValues = new Object[] {
				"lamp", "lamp", "NN",
				"the", "the", "DT",
				"broke", "break", "VBD",
				".", ".", ".",
				null};
		Assert.assertEquals(
				Arrays.asList(featureValues),
				this.getFeatureValues(lampInstance));
		Assert.assertEquals(false, lampInstance.getOutcome());
	}
		
	@Test
	public void testArgumentAnnotation() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setTrees(jCas);
		this.setPredicates(jCas);
		
		// get the instances produced by the handler 
		List<Instance<String>> instances = AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentAnnotationHandler(), engine, jCas);
		String featuresString;
		Assert.assertEquals(9, instances.size());
		
		// check "John"
		Instance<String> johnInstance = instances.get(2);
		featuresString = (
				"broke break VBD " +
				"VP->VBD-NP " +
				"NNP John John NNP " +
				"John John NNP " +
				"John John NNP " +
				"NP John John NNP " +
				"LEFTOF " +
				"NNP::NP::S;;VP;;VBD 5 " +
				"NNP::NP::S 3 " +
				"0");
		Assert.assertEquals(
				Arrays.asList(featuresString.split(" ")),
				this.getFeatureValues(johnInstance));
		/* 
		 * This is testing the NNP node of the John token,
		 * not the NP. The NP is an argument, the token is not.
		 */
		Assert.assertEquals("NULL", johnInstance.getOutcome());

		// check "the lamp"
		Instance<String> theLampInstance = instances.get(5);
		featuresString = (
				"broke break VBD " +
				"VP->VBD-NP " +
				"NP lamp lamp NN " +
				"the the DT " +
				"lamp lamp NN " +
				"VP broke break VBD " +
				"VBD broke break VBD " +
				"RIGHTOF " +
				"NP::VP;;VBD 3 " +
				"NP::VP 2 " +
				"0");
		Assert.assertEquals(
				Arrays.asList(featuresString.split(" ")),
				this.getFeatureValues(theLampInstance));
		Assert.assertEquals("ARG1", theLampInstance.getOutcome());
	}
	
	@Test
	public void testArgumentIdentification() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setTrees(jCas);
		this.setPredicates(jCas);
		
		// get the instances produced by the handler 
		List<Instance<Boolean>> instances = AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentIdentificationHandler(), engine, jCas);
		String featuresString;
		Assert.assertEquals(9, instances.size());
		
		// check "John"
		Instance<Boolean> johnInstance = instances.get(2);
		featuresString = (
				"LEFTOF " +
				"NNP::NP::S;;VP;;VBD 5 " +
				"NNP John John NNP " +
				"John John NNP " +
				"John John NNP " +
				"broke break VBD " +
				"VP->VBD-NP");
		Assert.assertEquals(
				Arrays.asList(featuresString.split(" ")),
				this.getFeatureValues(johnInstance));
		/* 
		 * This is testing the NNP node of the John token,
		 * not the NP. The NP is an argument, the token is not.
		 */
		Assert.assertEquals(false, johnInstance.getOutcome());

		// check "the lamp"
		Instance<Boolean> theLampInstance = instances.get(5);
		featuresString = (
				"RIGHTOF " +
				"NP::VP;;VBD 3 " +
				"NP lamp lamp NN " +
				"the the DT " +
				"lamp lamp NN " +
				"broke break VBD " +
				"VP->VBD-NP");
		Assert.assertEquals(
				Arrays.asList(featuresString.split(" ")),
				this.getFeatureValues(theLampInstance));
		Assert.assertEquals(true, theLampInstance.getOutcome());
	}
	
	@Test
	public void testArgumentClassification() throws UIMAException, CleartkException {
		// create the document
		AnalysisEngine engine = this.getEngine();
		JCas jCas = engine.newJCas();
		this.setTokens(jCas);
		this.setTrees(jCas);
		this.setPredicates(jCas);
		
		// get the instances produced by the handler 
		List<Instance<String>> instances = AnnotationHandlerTestUtil2.produceInstances(
				new ArgumentClassificationHandler(), engine, jCas);
		String featuresString;
		Assert.assertEquals(2, instances.size());
		
		// check "John"
		Instance<String> johnInstance = instances.get(0);
		featuresString = (
				"broke break VBD " +
				"VP->VBD-NP " +
				"NP John John NNP " + 
				"S broke break VBD " +
				"VP broke break VBD " +
				"LEFTOF " +
				"NP::S;;VP;;VBD 4");
		Assert.assertEquals(
				Arrays.asList(featuresString.split(" ")),
				this.getFeatureValues(johnInstance));
		Assert.assertEquals("ARG0-XXX", johnInstance.getOutcome());

		// check "the lamp"
		Instance<String> theLampInstance = instances.get(1);
		featuresString = (
				"broke break VBD " +
				"VP->VBD-NP " +
				"NP lamp lamp NN " +
				"VP broke break VBD " +
				"VBD broke break VBD " +
				"RIGHTOF " +
				"NP::VP;;VBD 3 ");
		Assert.assertEquals(
				Arrays.asList(featuresString.split(" ")),
				this.getFeatureValues(theLampInstance));
		Assert.assertEquals("ARG1", theLampInstance.getOutcome());
	}
	
	@Test
	public void testPredicateDataWriterDescriptor() throws UIMAException, IOException {
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.srl.PredicateDataWriter");
			Assert.fail("expected exception with missing output directory");
		} catch (ResourceInitializationException e) {}
			
		String outputPath = this.predicateOutputDir.getPath();
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.srl.PredicateDataWriter",
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputPath);
		
		Object handler = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(PredicateAnnotationHandler.class.getName(), handler);
		
		Object dataWriterFactory = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS);
		Assert.assertEquals(DefaultSVMlightDataWriterFactory.class.getName(), dataWriterFactory);
		
		Object outputDir = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
		Assert.assertEquals(outputPath, outputDir);
		
		engine.collectionProcessComplete();
	}
	
	@Test
	public void testPredicateAnnotationDescriptor() throws UIMAException, IOException {
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.srl.PredicateAnnotator");
			Assert.fail("expected exception with missing classifier jar");
		} catch (ResourceInitializationException e) {}
			
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.srl.PredicateAnnotator",
				ClassifierAnnotator.PARAM_CLASSIFIER_JAR, "test/data/srl/predicate/model.jar");
		Object handler = engine.getConfigParameterValue(
				ClassifierAnnotator.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(PredicateAnnotationHandler.class.getName(), handler);
		
		engine.collectionProcessComplete();
	}

	@Test
	public void testArgumentDataWriterDescriptor() throws UIMAException, IOException {
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.srl.ArgumentDataWriter");
			Assert.fail("expected exception with missing output directory");
		} catch (ResourceInitializationException e) {}
			
		String outputPath = this.argumentOutputDir.getPath();
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.srl.ArgumentDataWriter",
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputPath);
		
		Object handler = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(ArgumentAnnotationHandler.class.getName(), handler);
		
		Object dataWriter = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS);
		Assert.assertEquals(DefaultMaxentDataWriterFactory.class.getName(), dataWriter);
		
		Object outputDir = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
		Assert.assertEquals(outputPath, outputDir);
		
		engine.collectionProcessComplete();
	}

	@Test
	public void testArgumentAnnotationDescriptor() throws UIMAException, IOException {
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.srl.ArgumentAnnotator");
			Assert.fail("expected exception with missing classifier jar");
		} catch (ResourceInitializationException e) {}
			
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.srl.ArgumentAnnotator",
				ClassifierAnnotator.PARAM_CLASSIFIER_JAR, "test/data/srl/argument/model.jar");
		Object handler = engine.getConfigParameterValue(
				ClassifierAnnotator.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(ArgumentAnnotationHandler.class.getName(), handler);
		
		engine.collectionProcessComplete();
	}

	private AnalysisEngine getEngine() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitive(
			EmptyAnnotator.class,
			TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));	
	}
	
	private void setTokens(JCas jCas) throws UIMAException {
		TokenFactory.createTokens(jCas,  
				"John broke the lamp.",Token.class, Sentence.class,
				"John broke the lamp .",
				"NNP VBD DT NN .",
				"John break the lamp .", "org.cleartk.type.Token:pos", "org.cleartk.type.Token:stem");
	}
	
	private void setTrees(JCas jCas) {
		TreebankNode sNode = TreebankTestsUtil.newNode(jCas, "S",
				TreebankTestsUtil.newNode(jCas, "NP",
						TreebankTestsUtil.newNode(jCas, 0, 4, "NNP")),
						TreebankTestsUtil.newNode(jCas, "VP",
								TreebankTestsUtil.newNode(jCas, 5, 10, "VBD"),
								TreebankTestsUtil.newNode(jCas, "NP",
										TreebankTestsUtil.newNode(jCas, 11, 14, "DT"),
										TreebankTestsUtil.newNode(jCas, 15, 19, "NN"))),
										TreebankTestsUtil.newNode(jCas, 19, 20, "."));

		TopTreebankNode topNode = new TopTreebankNode(jCas, sNode.getBegin(), sNode.getEnd());
		topNode.setNodeType("TOP");
		topNode.setChildren(UIMAUtil.toFSArray(jCas, Collections.singletonList(sNode)));
		topNode.addToIndexes();
	}
	
	private void setPredicates(JCas jCas) {
		List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, Token.class);
		Token predToken = tokens.get(1);
		Predicate predicate = new Predicate(jCas, predToken.getBegin(), predToken.getEnd());
		predicate.setAnnotation(predToken);
		predicate.addToIndexes();
		
		List<TreebankNode> nodes = AnnotationRetrieval.getAnnotations(jCas, TreebankNode.class);
		SemanticArgument arg0 = new SemanticArgument(jCas, 0, 4);
		arg0.setLabel("ARG0");
		arg0.setFeature("XXX");
		arg0.addToIndexes();
		SemanticArgument arg1 = new SemanticArgument(jCas, 11, 19);
		arg1.setLabel("ARG1");
		arg1.addToIndexes();
		for (TreebankNode node: nodes) {
			if (node.getNodeType().equals("NP") && node.getCoveredText().equals("John")) {
				arg0.setAnnotation(node);
			}
			if (node.getCoveredText().equals("the lamp")) {
				arg1.setAnnotation(node);
			}
		}
		predicate.setArguments(new FSArray(jCas, 2));
		predicate.setArguments(0, arg0);
		predicate.setArguments(1, arg1);
	}
	
	private List<String> getFeatureValues(Instance<?> instance) {
		List<String> values = new ArrayList<String>();
		for (Feature feature: instance.getFeatures()) {
			Object value = feature == null ? null : feature.getValue(); 
			values.add(value == null ? null : value.toString());
		}
		return values;
	}

}
