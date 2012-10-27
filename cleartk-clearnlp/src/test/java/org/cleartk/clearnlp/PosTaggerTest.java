package org.cleartk.clearnlp;


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
	protected static AnalysisEngine tokenizer;

	static {
		try {
			tokenizer = AnalysisEngineFactory.createPrimitive(Tokenizer.getDescription());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	


	@Test
	public void posTaggerTest() throws Exception {
		this.jCas.reset();
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(PosTaggerAndMPAnalyzer.getDescription());
		this.tokenBuilder.buildTokens(
				this.jCas,
				"The brown fox jumped quickly over the lazy dog.",
				"The brown fox jumped quickly over the lazy dog .");
		engine.process(this.jCas);

		List<String> expected = Arrays.asList("DT JJ NN VBD RB IN DT JJ NN .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token : JCasUtil.select(this.jCas, Token.class)) {
			actual.add(token.getPos());
		}
		System.out.println(actual);
		System.out.println(expected);
		Assert.assertEquals(expected, actual);
	}
}
