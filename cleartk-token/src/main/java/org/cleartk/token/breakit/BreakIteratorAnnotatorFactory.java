package org.cleartk.token.breakit;

import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.TokenComponents;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;


public class BreakIteratorAnnotatorFactory {

	public static AnalysisEngineDescription createSentenceAnnotator(Locale locale) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(BreakIteratorAnnotator.class, TokenComponents.TYPE_SYSTEM_DESCRIPTION,
				BreakIteratorAnnotator.PARAM_ANNOTATION_TYPE_NAME, Sentence.class.getName(),
				BreakIteratorAnnotator.PARAM_LOCALE_NAME, locale.toString()
				);
	}
	
	public static AnalysisEngineDescription createTokenAnnotator(Locale locale) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(BreakIteratorAnnotator.class, TokenComponents.TYPE_SYSTEM_DESCRIPTION,
				BreakIteratorAnnotator.PARAM_BREAK_ITERATOR_TYPE, "WORD",
				BreakIteratorAnnotator.PARAM_ANNOTATION_TYPE_NAME, Token.class.getName(),
				BreakIteratorAnnotator.PARAM_LOCALE_NAME, locale.toString());
	}

}
