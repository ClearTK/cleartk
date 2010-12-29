package org.cleartk.token.breakit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.token.TokenTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Test;
import org.uimafit.pipeline.SimplePipeline;

public class BreakIteratorAnnotatorTest extends TokenTestBase {

	@Test
	public void testTokenAnnotator() throws Exception {
		AnalysisEngineDescription tokenAnnotator = BreakIteratorAnnotatorFactory.createTokenAnnotator(Locale.US);
		String text = "  : ;) Hey there!  I am going to the store.  Would you like to come with me?";
		String expectedText = ": ; ) Hey there ! I am going to the store . Would you like to come with me ?";
		test(tokenAnnotator, Token.class, text, expectedText);
	}

	@Test
	public void testSentenceAnnotator() throws Exception {
		AnalysisEngineDescription sentenceAnnotator = BreakIteratorAnnotatorFactory.createSentenceAnnotator(Locale.US);
		String text = "  : ;) Hey there!  I am going to the store.  Would you like to come with me?";
		String[] expectedAnnotations = new String[] {"  : ;) Hey there!  ", "I am going to the store.  ", "Would you like to come with me?"};
		test(sentenceAnnotator, Sentence.class, text, expectedAnnotations);
	}

	private void test(AnalysisEngineDescription annotator, Class<? extends Annotation> annotationCls, String text, String[] expectedAnnotations) throws UIMAException, IOException {
		jCas.setDocumentText(text);
		SimplePipeline.runPipeline(jCas, annotator);
		List<? extends Annotation> actualAnnotations = AnnotationRetrieval.getAnnotations(jCas, annotationCls);
		assertEquals(expectedAnnotations.length, actualAnnotations.size());
		for(int i=0; i<expectedAnnotations.length; i++) {
			assertEquals(expectedAnnotations[i], actualAnnotations.get(i).getCoveredText());
		}
	}

	private void test(AnalysisEngineDescription annotator, Class<? extends Annotation> annotationCls, String text, String expectedText) throws UIMAException, IOException {
		String[] expectedAnnotations = expectedText.split(" ");
		test(annotator, annotationCls, text, expectedAnnotations);
	}


}
