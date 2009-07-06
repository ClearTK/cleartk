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
package org.cleartk.temporal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.ClassifierAnnotator;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.svmlight.DefaultOVASVMlightDataWriterFactory;
import org.cleartk.corpus.timeml.type.Event;
import org.cleartk.corpus.timeml.type.TemporalLink;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.EmptyAnnotator;
import org.cleartk.util.TestsUtil;
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

 *
 *
 * @author Steven Bethard
 */
public class VerbClauseTemporalHandlerTests {
	
	private final File outputDirectory = new File("test/data/temporal");
	
	@Before
	public void setUp() {
		if (!this.outputDirectory.exists()) {
			this.outputDirectory.mkdirs();
		}
	}
	
	@After
	public void tearDown() {
		TearDownUtil.emptyDirectory(this.outputDirectory);
	}
	
	@Test
	public void test() throws UIMAException, CleartkException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				EmptyAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"He said she bought milk.",
				Token.class, Sentence.class, 
				"He said she bought milk .", 
				"PRP VBD PRP VBD NN .",
				"he say she buy milk .", "org.cleartk.type.Token:pos", "org.cleartk.type.Token:stem");
		List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, Token.class);
		
		// create the Event and TemporalLink annotations
		Event source = new Event(jCas, tokens.get(1).getBegin(), tokens.get(1).getEnd());
		Event target = new Event(jCas, tokens.get(3).getBegin(), tokens.get(3).getEnd());
		TemporalLink tlink = new TemporalLink(jCas);
		tlink.setSource(source);
		tlink.setTarget(target);
		tlink.setRelationType("AFTER");
		Annotation[] timemlAnnotations = new Annotation[]{source, target, tlink};
		for (Annotation annotation: timemlAnnotations) {
			annotation.addToIndexes();
		}
		
		// create the TreebankNode annotations
		TreebankNode root = TestsUtil.newNode(jCas, "S",
				TestsUtil.newNode(jCas, "NP",
						this.newNode(jCas, tokens.get(0))),
				TestsUtil.newNode(jCas, "VP", 
						this.newNode(jCas, tokens.get(1)),
						TestsUtil.newNode(jCas, "SBAR", 
								TestsUtil.newNode(jCas, "NP",
										this.newNode(jCas, tokens.get(2))),
								TestsUtil.newNode(jCas, "VP", 
										this.newNode(jCas, tokens.get(3)),
										TestsUtil.newNode(jCas, "NP",
												this.newNode(jCas, tokens.get(4)))))));
		
		Sentence sentence = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).get(0);

		// set the Sentence's constitutentParse feature
		TopTreebankNode tree = new TopTreebankNode(jCas, sentence.getBegin(), sentence.getEnd());
		tree.setNodeType("TOP");
		tree.setChildren(new FSArray(jCas, 1));
		tree.setChildren(0, root);
		tree.addToIndexes();
//		sentence.setConstituentParse(tree);
		
		// collect the single instance from the handler
		List<Instance<String>> instances;
		instances = TestsUtil.produceInstances(
				new VerbClauseTemporalHandler(), engine, jCas);
		Assert.assertEquals(1, instances.size());
		
		// check the outcome
		Assert.assertEquals("AFTER", instances.get(0).getOutcome());

		// check the feature values
		List<Object> expectedFeatureValues = Arrays.asList(new Object[]{
				"said",						// source token
				"bought",					// target token
				"VBD",						// source pos
				"VBD",						// target pos
				"say",						// source stem
				"buy",						// target stem
				"VBD::VP;;SBAR;;VP;;VBD",	// path
				5L,							// path length
		});
		List<Object> actualFeatureValues = new ArrayList<Object>();
		for (Feature feature: instances.get(0).getFeatures()) {
			actualFeatureValues.add(feature.getValue());
		}
		Assert.assertEquals(expectedFeatureValues, actualFeatureValues);
		
		// now remove all TimeML annotations
		List<Event> events;
		List<TemporalLink> tlinks;
		for (Annotation annotation: timemlAnnotations) {
			annotation.removeFromIndexes();
		}
		events = AnnotationRetrieval.getAnnotations(jCas, Event.class);
		tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class);
		Assert.assertEquals(0, events.size());
		Assert.assertEquals(0, tlinks.size());
		
		// and run the handler again, asking it to annotate this time
		instances = TestsUtil.produceInstances(
				new VerbClauseTemporalHandler(), "AFTER-NEW", engine, jCas);
		Assert.assertEquals(1, instances.size());
		
		// check the resulting TimeML annotations
		events = AnnotationRetrieval.getAnnotations(jCas, Event.class);
		tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class);
		Assert.assertEquals(2, events.size());
		Assert.assertEquals(1, tlinks.size());
		Assert.assertEquals("said", events.get(0).getCoveredText());
		Assert.assertEquals("bought", events.get(1).getCoveredText());
		Assert.assertEquals(events.get(0), tlinks.get(0).getSource());
		Assert.assertEquals(events.get(1), tlinks.get(0).getTarget());
		Assert.assertEquals("AFTER-NEW", tlinks.get(0).getRelationType());
	}
	
	private TreebankNode newNode(JCas jCas, Token token) {
		return TestsUtil.newNode(jCas, token.getBegin(), token.getEnd(), token.getPos());
	}
	
	@Test
	public void testDataWriterDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.temporal.VerbClauseTemporalDataWriter");
		
		Object handler = engine.getConfigParameterValue(
				InstanceConsumer.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(VerbClauseTemporalHandler.class.getName(), handler);
		
		Object dataWriter = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS);
		Assert.assertEquals(DefaultOVASVMlightDataWriterFactory.class.getName(), dataWriter);
		
		Object outputDir = engine.getConfigParameterValue(
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
		Assert.assertEquals("test/data/temporal", outputDir);
		
		engine.collectionProcessComplete();
	}

	@Test
	public void testAnnotationDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.temporal.VerbClauseTemporalAnnotator");
		
		Object handler = engine.getConfigParameterValue(
				InstanceConsumer.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(VerbClauseTemporalHandler.class.getName(), handler);
		
		Object modelJar = engine.getConfigParameterValue(
				ClassifierAnnotator.PARAM_CLASSIFIER_JAR);
		Assert.assertEquals("resources/models/verb-clause-temporal-model.jar", modelJar);
		
		engine.collectionProcessComplete();
	}


}
