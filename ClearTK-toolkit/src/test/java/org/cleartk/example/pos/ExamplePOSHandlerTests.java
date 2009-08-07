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
package org.cleartk.example.pos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.SequentialInstanceConsumer;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.EmptyAnnotator;
import org.cleartk.util.AnnotationHandlerTestUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class ExamplePOSHandlerTests {
	
	@Test
	public void testSimpleSentence() throws Exception {
		
		// create the engine and the cas
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				EmptyAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription(Token.class, Sentence.class));
		JCas jCas = engine.newJCas();
		
		// create some tokens, stems and part of speech tags
		TokenFactory.createTokens(jCas,
				"The Absurdis retreated in 2003.", Token.class, Sentence.class, 
				"The Absurdis retreated in 2003 .",
				"DT NNP VBD IN CD .",
				"The Absurdi retreat in 2003 .", "org.cleartk.type.Token:pos", "org.cleartk.type.Token:stem");

		List<Instance<String>> instances = AnnotationHandlerTestUtil.produceInstances(
				new ExamplePOSAnnotationHandler(), engine, jCas);
		List<String> featureValues;
		
		// check "The"
		featureValues = Arrays.asList(new String[]{
				"The",					// word
				"the",					// lower case
				"INITIAL_UPPERCASE",	// capital type
										// numeric type
				"he",					// last 2 chars
				"The",					// last 3 chars
				"The",					// stem (thrown away if null)
				null, null,				// left 2 stems
				"Absurdi", "retreat",	// right 2 stems
		});
		Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(0)));
		Assert.assertEquals("DT", instances.get(0).getOutcome());

		// check "Absurdis"
		featureValues = Arrays.asList(new String[]{
				"Absurdis",				// word
				"absurdis",				// lower case
				"INITIAL_UPPERCASE",	// capital type
										// numeric type
				"is",					// last 2 chars
				"dis",					// last 3 chars
				"Absurdi",				// stem (thrown away if null)
				"The", null,			// left 2 stems
				"retreat", "in",		// right 2 stems
		});
		Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(1)));
		Assert.assertEquals("NNP", instances.get(1).getOutcome());

		// check "retreated"
		featureValues = Arrays.asList(new String[]{
				"retreated",			// word
				"retreated",			// lower case
				"ALL_LOWERCASE",		// capital type
										// numeric type
				"ed",					// last 2 chars
				"ted",					// last 3 chars
				"retreat",				// stem (thrown away if null)
				"Absurdi", "The",		// left 2 stems
				"in", "2003",			// right 2 stems
		});
		Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(2)));
		Assert.assertEquals("VBD", instances.get(2).getOutcome());

		// check "in"
		featureValues = Arrays.asList(new String[]{
				"in",					// word
				"in",					// lower case
				"ALL_LOWERCASE",		// capital type
										// numeric type
				"in",					// last 2 chars
										// last 3 chars
				"in",					// stem (thrown away if null)
				"retreat", "Absurdi",	// left 2 stems
				"2003", ".",			// right 2 stems
		});
		Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(3)));
		Assert.assertEquals("IN", instances.get(3).getOutcome());

		// check "2003"
		featureValues = Arrays.asList(new String[]{
				"2003",					// word
				"2003",					// lower case
										// capital type
				"YEAR_DIGITS",			// numeric type
				"03",					// last 2 chars
				"003",					// last 3 chars
				"2003",					// stem (thrown away if null)
				"in", "retreat",		// left 2 stems
				".", null,			// right 2 stems
		});
		Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(4)));
		Assert.assertEquals("CD", instances.get(4).getOutcome());

		// check "."
		featureValues = Arrays.asList(new String[]{
				".",					// word
				".",					// lower case
										// capital type
										// numeric type
										// last 2 chars
										// last 3 chars
				".",					// stem (thrown away if null)
				"2003", "in",			// left 2 stems
				null, null,				// right 2 stems
		});
		Assert.assertEquals(featureValues, this.getFeatureValues(instances.get(5)));
		Assert.assertEquals(".", instances.get(5).getOutcome());
	}
	
	@Test
	public void testAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngineDescription posTaggerDescription = ExamplePOSAnnotationHandler.getClassifierDescription(ExamplePOSAnnotationHandler.DEFAULT_MODEL);
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(posTaggerDescription);
		
		String expectedName = ExamplePOSAnnotationHandler.class.getName();
		Object annotationHandler = engine.getConfigParameterValue(
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(expectedName, annotationHandler);

		Object classifierJar = engine.getConfigParameterValue(
				SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR);
		Assert.assertEquals(ExamplePOSAnnotationHandler.DEFAULT_MODEL, classifierJar);
		
		engine.collectionProcessComplete();
	}
	
	@Test
	public void testDataWriterDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				ExamplePOSAnnotationHandler.getWriterDescription(ExamplePOSAnnotationHandler.DEFAULT_OUTPUT_DIRECTORY));
		
		String expectedName = ExamplePOSAnnotationHandler.class.getName();
		Object annotationHandler = engine.getConfigParameterValue(
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(expectedName, annotationHandler);
		
		Object outputDir = engine.getConfigParameterValue(
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
		Assert.assertEquals(ExamplePOSAnnotationHandler.DEFAULT_OUTPUT_DIRECTORY, outputDir);
		
		String expectedDataWriterFactory = (
				ViterbiDataWriterFactory.class.getName());
		Object dataWriter = engine.getConfigParameterValue(
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS);
		Assert.assertEquals(expectedDataWriterFactory, dataWriter);
		engine.collectionProcessComplete();
	}
	
	private List<String> getFeatureValues(Instance<String> instance) {
		List<String> values = new ArrayList<String>();
		for (Feature feature: instance.getFeatures()) {
			Object value = feature == null ? null : feature.getValue(); 
			values.add(value == null ? null : value.toString());
		}
		return values;
	}

}
