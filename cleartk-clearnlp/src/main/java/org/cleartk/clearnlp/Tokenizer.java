package org.cleartk.clearnlp;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;

public class Tokenizer extends JCasAnnotator_ImplBase{
	public static final String DEFAULT_DICTIONARY_FILE_NAME= "dictionary-1.1.0.zip";

    public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
    		Tokenizer.class,
    		"languageCode");
    
    @ConfigurationParameter(
    		description = "Language code for the tokenizer (default value=en).",
    		defaultValue= AbstractReader.LANG_EN)
    private String languageCode;
    
    public static final String PARAM_TOKENIZER_DICTIONARY_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			Tokenizer.class,
			"tokenizerDictionaryUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the URI of the tokenizer dictionary file.")
	private URI tokenizerDictionaryUri;

	
    private AbstractTokenizer tokenizer;
    
    
    /**
     * Convenience method for getting an Analysis Engine for ClearNLP's English tokenizer.
     * 
     * @return
     * @throws ResourceInitializationException
     */
    public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    	return AnalysisEngineFactory.createPrimitiveDescription(Tokenizer.class); 
    }
    
      
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			File dictionaryFile = (this.tokenizerDictionaryUri == null)
				? new File(Tokenizer.class.getResource(DEFAULT_DICTIONARY_FILE_NAME).toURI())
				: new File(this.tokenizerDictionaryUri.toURL().getPath());
				
			this.tokenizer = EngineGetter.getTokenizer(languageCode, dictionaryFile.getPath());
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}


	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			String sentenceText = sentence.getCoveredText();
			int sentenceOffset = sentence.getBegin();
			List<String> tokens = tokenizer.getTokens(sentence.getCoveredText());
			int offset = 0;
			for (String token : tokens) {
				int tokenBegin = sentenceText.indexOf(token, offset);
				int tokenEnd = tokenBegin + token.length();
				Token cleartkToken = new Token(jCas, sentenceOffset + tokenBegin, sentenceOffset + tokenEnd);
				cleartkToken.addToIndexes();
				offset += tokenEnd;
			}
		}
	}

}
