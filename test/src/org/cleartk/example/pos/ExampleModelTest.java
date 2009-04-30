package org.cleartk.example.pos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.TestsUtil;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.util.AnnotationRetrieval;

public class ExampleModelTest {

	@Test
	public void testModel() throws Exception {
		AnalysisEngine posTagger = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.pos.ExamplePOSAnnotator", 
				SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR, "example/model/model.jar");
		
		JCas jCas = TestsUtil.getJCas();
		
		TokenFactory.createTokens(jCas,
				"What would you do if I sang in tune?  Would you listen then?", Token.class, Sentence.class, 
				"What would you do if I sang in tune ?\n  Would you listen then ?");
		
		Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		assertNull(token.getPos());
		token = AnnotationRetrieval.get(jCas, Token.class, 5);
		assertNull(token.getPos());

		posTagger.process(jCas);
		token = AnnotationRetrieval.get(jCas, Token.class, 0);
		assertNotNull(token.getPos());
		assertEquals("WP", token.getPos());
		token = AnnotationRetrieval.get(jCas, Token.class, 5);
		assertNotNull(token.getPos());
		assertEquals("PRP", token.getPos());
	}
}
