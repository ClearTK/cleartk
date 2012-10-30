package org.cleartk.clearnlp;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;


import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

public class PosTaggerTest extends CleartkTestBase {
	protected TokenBuilder<Token, Sentence> tokenBuilder;
	protected AnalysisEngineDescription posTagger;

	protected void initLowMemModel() throws ResourceInitializationException {
			this.posTagger = AnalysisEngineFactory.createPrimitiveDescription(PosTagger.class,
			    PosTagger.PARAM_MODEL_URI,
			    new File("src/test/resources/models/sample-pos.jar").toURI());
	} 
	
	protected void initDefaultModel() throws ResourceInitializationException {
		  this.posTagger = PosTagger.getDescription();
	} 

  @Test
  public void posTaggerLowMemTest() throws Exception {
    initLowMemModel();
    
    
    this.jCas.reset();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The brown fox jumped quickly over the lazy dog .",
        "The brown fox jumped quickly over the lazy dog .");
    SimplePipeline.runPipeline(jCas, posTagger);
    
		List<String> expected = Arrays.asList("DT NN IN VBN RB IN DT NN NN .".split(" "));
    List<String> actual = new ArrayList<String>();
    for (Token token : JCasUtil.select(this.jCas, Token.class)) {
      actual.add(token.getPos());
    }
    Assert.assertEquals(expected, actual);
  }	
	
	

	@Test
	public void posTaggerTest() throws Exception {
    this.assumeBigMemoryTestsEnabled();
    this.logger.info(BIG_MEMORY_TEST_MESSAGE);
    
	  initDefaultModel();
		this.jCas.reset();
		tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

		this.tokenBuilder.buildTokens(
				this.jCas,
				"The brown fox jumped quickly over the lazy dog .",
				"The brown fox jumped quickly over the lazy dog .");
		SimplePipeline.runPipeline(jCas, posTagger);
		
    List<String> expected = Arrays.asList("DT JJ NN VBD RB IN DT JJ NN .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token : JCasUtil.select(this.jCas, Token.class)) {
			actual.add(token.getPos());
		}
		Assert.assertEquals(expected, actual);
	}
	
}
