package org.cleartk.clearnlp;

import java.net.URI;
import java.net.URL;

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
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.reader.AbstractReader;

public class MPAnalyzer extends JCasAnnotator_ImplBase {
	
	public static final String DEFAULT_DICTIONARY_FILE_NAME= "dictionary-1.2.0.zip";
	
    public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
    		MPAnalyzer.class,
    		"languageCode");
    
    @ConfigurationParameter(
    		description = "Language code (default value=en).",
    		defaultValue= AbstractReader.LANG_EN)
    private String languageCode;
	
	
	public static final String PARAM_DICTIONARY_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			MPAnalyzer.class, 
			"dictionaryUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the URI to the morphological analyzer dictionary used for lemmatizing.")
	private URI dictionaryUri;
	
	/**
	 * Convenience method to create Analysis Engine for ClearNLP's POSTagger + Lemmatizer using default English models and dictionaries.
	 */
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(MPAnalyzer.class);
	}
	
	public static AnalysisEngineDescription getDescription(String langCode, URI dictionaryUri) throws ResourceInitializationException {
	  return AnalysisEngineFactory.createPrimitiveDescription(MPAnalyzer.class, 
	      MPAnalyzer.PARAM_LANGUAGE_CODE, langCode,
	      MPAnalyzer.PARAM_DICTIONARY_URI, dictionaryUri);
	  
	}
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
		  URL mpAnalyzerDictionaryURL = (this.dictionaryUri == null) 
		      ? MPAnalyzer.class.getResource(DEFAULT_DICTIONARY_FILE_NAME).toURI().toURL()
		      : dictionaryUri.toURL();

		  this.mpAnalyzer = EngineGetter.getMPAnalyzer(this.languageCode, mpAnalyzerDictionaryURL.openStream());

		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
		  for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence))	{
		    token.setLemma(this.mpAnalyzer.getLemma(token.getCoveredText(), token.getPos()));
		  }
		}
	}
	
	private AbstractMPAnalyzer mpAnalyzer;

}
