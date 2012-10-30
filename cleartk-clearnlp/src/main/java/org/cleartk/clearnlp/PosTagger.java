package org.cleartk.clearnlp;

import java.net.URI;
import java.net.URL;
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
import com.googlecode.clearnlp.pos.POSNode;
import com.googlecode.clearnlp.pos.POSTagger;
import com.googlecode.clearnlp.util.pair.Pair;

public class PosTagger extends JCasAnnotator_ImplBase {
	
	public static final String DEFAULT_MODEL_FILE_NAME = "ontonotes-en-pos-1.1.0g.jar";
	
	public static final String PARAM_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			PosTagger.class,
			"modelUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the URI to the pos tagger model.")
	private URI modelUri;

	
	/**
	 * Convenience method to create Analysis Engine for ClearNLP's POSTagger + Lemmatizer using default English models and dictionaries.
	 */
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(PosTagger.class);
	}
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
		  URL modelURL = (this.modelUri == null) 
		      ? PosTagger.class.getResource(DEFAULT_MODEL_FILE_NAME).toURI().toURL()
		      : this.modelUri.toURL();
			this.taggers = EngineGetter.getPOSTaggers(modelURL.openStream());
			
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);	
			List<String> tokenStrings = JCasUtil.toText(tokens);
			
			POSNode[] posNodes = EngineProcess.getPOSNodes(this.taggers, tokenStrings);
					
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				POSNode posNode = posNodes[i];
				token.setPos(posNode.pos);
			}
		}
	}
	
	private Pair<POSTagger[], Double> taggers;

}
