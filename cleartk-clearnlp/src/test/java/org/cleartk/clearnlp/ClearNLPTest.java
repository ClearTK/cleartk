package org.cleartk.clearnlp;

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

public class ClearNLPTest extends CleartkTestBase {
	protected TokenBuilder<Token, Sentence> tokenBuilder;
	
	@Test
	public void tokenizerTest() throws Exception {
	    this.jCas.setDocumentText("\"John & Mary's dog,\" Jane thought (to herself).\n"
	            + "\"What a #$%!\n" + "a- ``I like AT&T''.\"");
	    new Sentence(this.jCas, 0, 47).addToIndexes();
	    new Sentence(this.jCas, 48, 60).addToIndexes();
	    new Sentence(this.jCas, 61, 81).addToIndexes();
	    
	    AnalysisEngine tokenizer = AnalysisEngineFactory.createPrimitive(Tokenizer.getDescription());
	    tokenizer.process(this.jCas);
	    
	    List<String> expected = Arrays.asList(
	            "\"",
	            "John",
	            "&",
	            "Mary",
	            "'s",
	            "dog",
	            ",",
	            "\"",
	            "Jane",
	            "thought",
	            "(",
	            "to",
	            "herself",
	            ")",
	            ".",
	            "\"",
	            "What",
	            "a",
	            "#",
	            "$",
	            "%",
	            "!",
	            "a",
	            "-",
	            "``",
	            "I",
	            "like",
	            "AT&T",
	            "''",
	            ".",
	            "\"");
	        List<String> actual = JCasUtil.toText(JCasUtil.select(this.jCas, Token.class));
	        Assert.assertEquals(expected, actual);
	}

}
