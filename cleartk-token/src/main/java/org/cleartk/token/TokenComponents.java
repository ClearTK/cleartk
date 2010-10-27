package org.cleartk.token;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.token.tokenizer.PennTreebankTokenizer;
import org.cleartk.token.tokenizer.Subtokenizer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.token.type.Subtoken;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;

public class TokenComponents {

	public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
	.createTypeSystemDescription("org.cleartk.token.TypeSystem");
	
	public static AnalysisEngineDescription createPennTokenizer() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(TokenAnnotator.class, TYPE_SYSTEM_DESCRIPTION, 
				TokenAnnotator.PARAM_TOKEN_TYPE_NAME, Token.class.getName(), 
				TokenAnnotator.PARAM_TOKENIZER_NAME, PennTreebankTokenizer.class.getName());
	}
	
	public static AnalysisEngineDescription createSubtokenizer() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(TokenAnnotator.class, TYPE_SYSTEM_DESCRIPTION, 
				TokenAnnotator.PARAM_TOKEN_TYPE_NAME, Subtoken.class.getName(), 
				TokenAnnotator.PARAM_TOKENIZER_NAME, Subtokenizer.class.getName());
	}
}
