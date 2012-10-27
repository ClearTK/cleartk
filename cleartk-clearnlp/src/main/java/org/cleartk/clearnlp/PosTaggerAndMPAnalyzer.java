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
import com.googlecode.clearnlp.engine.EngineProcess;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.pos.POSNode;
import com.googlecode.clearnlp.pos.POSTagger;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.util.pair.Pair;

public class PosTaggerAndMPAnalyzer extends JCasAnnotator_ImplBase {
	
	public static final String DEFAULT_POS_TAGGER_MODEL_FILE_NAME = "ontonotes-en-pos-1.1.0g.jar";
	public static final String DEFAULT_MP_ANALYZER_DICTIONARY_FILE_NAME= "dictionary-1.1.0.zip";
	
	
    public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
    		PosTaggerAndMPAnalyzer.class,
    		"languageCode");
    
    @ConfigurationParameter(
    		description = "Language code (default value=en).",
    		defaultValue= AbstractReader.LANG_EN)
    private String languageCode;
	
	public static final String PARAM_POS_TAGGER_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			PosTaggerAndMPAnalyzer.class,
			"posTaggerModelUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the URI to the pos tagger model.")
	private URI posTaggerModelUri;
	
	public static final String PARAM_MP_ANALYZER_DICTIONARY_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			PosTaggerAndMPAnalyzer.class, 
			"mpAnalyzerDictionaryUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the URI to the morphological analyzer dictionary used for lemmatizing.")
	private URI mpAnalyzerDictionaryUri;
	
	public static final String PARAM_LEMMATIZE = ConfigurationParameterFactory.createConfigurationParameterName(
			PosTaggerAndMPAnalyzer.class,
			"lemmatize");
	
	@ConfigurationParameter(
			description = "This boolean parameter indicates whether or not to produce lemma forms on the token (default=true).",
			defaultValue = "true")
	private Boolean lemmatize;

	
	/**
	 * Convenience method to create Analysis Engine for ClearNLP's POSTagger + Lemmatizer using default English models and dictionaries.
	 */
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(PosTaggerAndMPAnalyzer.class);
	}
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
			File posTaggerModelFile = (this.posTaggerModelUri == null) 
				? new File(PosTaggerAndMPAnalyzer.class.getResource(DEFAULT_POS_TAGGER_MODEL_FILE_NAME).toURI())
				: new File(posTaggerModelUri);

			this.taggers = EngineGetter.getPOSTaggers(posTaggerModelFile.getPath());
			
			if (this.lemmatize) {
				File mpAnalyzerDictionaryFile = (this.mpAnalyzerDictionaryUri == null) 
					? new File(PosTaggerAndMPAnalyzer.class.getResource(DEFAULT_MP_ANALYZER_DICTIONARY_FILE_NAME).toURI())
					: new File(mpAnalyzerDictionaryUri);

				this.mpAnalyzer = EngineGetter.getMPAnalyzer(this.languageCode, mpAnalyzerDictionaryFile.getPath());
			}
			
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);	
			List<String> tokenStrings = JCasUtil.toText(tokens);
			
			POSNode[] posNodes =  (this.lemmatize)
					? EngineProcess.getPOSNodesWithLemmas(this.taggers, this.mpAnalyzer, tokenStrings)
					: EngineProcess.getPOSNodes(this.taggers, tokenStrings);
			
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				POSNode posNode = posNodes[i];
				token.setPos(posNode.pos);
				token.setLemma(posNode.lemma);
			}
		}
	}
	
	private Pair<POSTagger[], Double> taggers;
	private AbstractMPAnalyzer mpAnalyzer;

}
