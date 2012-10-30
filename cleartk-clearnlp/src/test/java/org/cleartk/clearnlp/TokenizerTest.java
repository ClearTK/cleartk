package org.cleartk.clearnlp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;


import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

public class TokenizerTest extends CleartkTestBase {
	protected static AnalysisEngine tokenizer;

	static {
		try {
			tokenizer = AnalysisEngineFactory.createPrimitive(Tokenizer.getDescription());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	



	@Test
	public void testMarysDog() throws UIMAException, IOException {
		String text = FileUtils.readFileToString(new File("src/test/resources/token/marysdog.txt"));
		jCas.setDocumentText(text);
		new Sentence(jCas, 0, 52).addToIndexes();
		new Sentence(jCas, 54, 68).addToIndexes();
		new Sentence(jCas, 70, 91).addToIndexes();
		SimplePipeline.runPipeline(jCas, tokenizer);
		
		System.out.println(JCasUtil.toText(JCasUtil.select(jCas, Token.class)));
		FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(37, tokenIndex.size());

		int index = 0;
		assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("John", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("&", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("Mary", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'s", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("dog", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("...", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(",", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("Jane", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("thought", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("(", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("to", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("herself", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(")", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("What", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("a", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("@", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("#", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("$", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("%", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("*", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("!", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("a", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("-", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("``", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("like", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("AT&T", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'''", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		//assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		//assertEquals("''", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		
	}



	@Test
	public void testWatcha() throws UIMAException, IOException {
		String text = FileUtils.readFileToString(new File("src/test/resources/token/watcha.txt"));
		jCas.setDocumentText(text);

		new Sentence(jCas, 0, 45).addToIndexes();
		new Sentence(jCas, 47, 73).addToIndexes();
		new Sentence(jCas, 75, 109).addToIndexes();

		SimplePipeline.runPipeline(jCas, tokenizer);
		FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(30, tokenIndex.size());

		int index = 0;
		assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("ca", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("n't", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("believe", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("they", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("wan", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("na", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("keep", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("40", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("%", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("of", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("that", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("``", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("What", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("cha", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("think", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("?", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("''", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("do", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("n't", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("---", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("think", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("so", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("...", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(",", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());

	}


	@Test
	public void testTimes() throws UIMAException, IOException {
		String text = FileUtils.readFileToString(new File("src/test/resources/token/times.txt"));
		jCas.setDocumentText(text);
		new Sentence(jCas, 0, 17).addToIndexes();
		new Sentence(jCas, 19, 59).addToIndexes();
		SimplePipeline.runPipeline(jCas, tokenizer);

		FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(16, tokenIndex.size());

		int index = 0;
		assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("said", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("at", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("4:45", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("pm", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("was", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("born", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("in", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'80", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(",", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("not", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("the", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'70s", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
	}
	@Test
	public void testDollars() throws UIMAException, IOException {
		String text = FileUtils.readFileToString(new File("src/test/resources/token/dollars.txt"));
		jCas.setDocumentText(text);
		new Sentence(jCas, 9, 33).addToIndexes();
		new Sentence(jCas, 34, 73).addToIndexes();
		SimplePipeline.runPipeline(jCas, tokenizer);
		FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(15, tokenIndex.size());


		int index = 0;
		assertEquals("You", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("`", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("paid", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("US$", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("170,000", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("?!", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("You", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("should", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("'ve", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("paid", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("only", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("$", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("16.75", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
	}

	@Test
	public void testPercents() throws UIMAException, IOException {

		jCas.setDocumentText(" 1. Buy a new Chevrolet (37%-owned in the U.S..) . 15%");
		new Sentence(jCas, 0, 54).addToIndexes();
		SimplePipeline.runPipeline(jCas, tokenizer);
		FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(19, tokenIndex.size());

		int index = 0;
		assertEquals("1", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("Buy", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("a", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("new", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("Chevrolet", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("(", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("37", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("%", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("-", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("owned", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("in", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("the", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("U.S.", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(")", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("15", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
		assertEquals("%", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
	}

	@Test
	public void testPeriod() throws UIMAException, IOException {
		String text = "The sides was so steep and the bushes so thick. We tramped and clumb. ";
		jCas.setDocumentText(text);
		new Sentence(jCas, 0, 47).addToIndexes();
		new Sentence(jCas, 48, 70).addToIndexes();
		SimplePipeline.runPipeline(jCas, tokenizer);
		int i = 0;
		assertEquals("The", getToken(i++).getCoveredText());
		assertEquals("sides", getToken(i++).getCoveredText());
		assertEquals("was", getToken(i++).getCoveredText());
		assertEquals("so", getToken(i++).getCoveredText());
		assertEquals("steep", getToken(i++).getCoveredText());
		assertEquals("and", getToken(i++).getCoveredText());
		assertEquals("the", getToken(i++).getCoveredText());
		assertEquals("bushes", getToken(i++).getCoveredText());
		assertEquals("so", getToken(i++).getCoveredText());
		assertEquals("thick", getToken(i++).getCoveredText());
		assertEquals(".", getToken(i++).getCoveredText());
		assertEquals("We", getToken(i++).getCoveredText());
		assertEquals("tramped", getToken(i++).getCoveredText());
		assertEquals("and", getToken(i++).getCoveredText());
		assertEquals("clumb", getToken(i++).getCoveredText());
		assertEquals(".", getToken(i++).getCoveredText());

	}

	private Token getToken(int i) {
		return JCasUtil.selectByIndex(jCas, Token.class, i);
	}

}
