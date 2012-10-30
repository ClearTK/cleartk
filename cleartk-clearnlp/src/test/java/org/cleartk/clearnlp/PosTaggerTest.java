package org.cleartk.clearnlp;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;


import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

public class PosTaggerTest extends CleartkTestBase {
	protected TokenBuilder<Token, Sentence> tokenBuilder;
	protected static AnalysisEngine posTagger;

	static {
		try {
			posTagger = AnalysisEngineFactory.createPrimitive(PosTagger.class,
			    PosTagger.PARAM_MODEL_URI,
			    new File("src/test/resources/models/sample-pos.jar").toURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	


	@Test
	public void posTaggerTest() throws Exception {
		this.jCas.reset();
		//AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(PosTaggerAndMPAnalyzer.getDescription());
		tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

		this.tokenBuilder.buildTokens(
				this.jCas,
				"The brown fox jumped quickly over the lazy dog.",
				"The brown fox jumped quickly over the lazy dog .");
		posTagger.process(jCas);
		
		// Tags are wrong because using a dummy model file to conserve memory
		// Correct output should be the following commented out line.
		//List<String> expected = Arrays.asList("DT JJ NN VBD RB IN DT JJ NN .".split(" "));
		List<String> expected = Arrays.asList("DT NN IN VBN RB IN DT NN NN .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token : JCasUtil.select(this.jCas, Token.class)) {
			actual.add(token.getPos());
		}
		Assert.assertEquals(expected, actual);
	}
	
}
