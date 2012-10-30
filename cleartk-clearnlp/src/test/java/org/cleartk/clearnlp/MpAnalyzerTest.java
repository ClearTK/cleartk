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

public class MpAnalyzerTest extends CleartkTestBase {
  protected TokenBuilder<Token, Sentence> tokenBuilder;
  protected static AnalysisEngine posTagger;

  static {
    try {
      posTagger = AnalysisEngineFactory.createPrimitive(MPAnalyzer.getDescription()); 
      //PosTagger.PARAM_POS_TAGGER_MODEL_URI,
      //new File("src/test/resources/models/sample-pos.jar").toURI());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }	


  @Test
  public void mpAnalyzerTest() throws Exception {
    this.jCas.reset();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

    this.tokenBuilder.buildTokens(jCas, 
        "jump jumping jumped jumper happy happier happiest", 
        "jump jumping jumped jumper happy happier happiest", 
        "VBP VBG VBP NN JJ JJ JJ"
        );

    posTagger.process(jCas);

    List<String> expected = Arrays.asList("jump jump jump jumper happy happy happy".split(" "));
    List<String> actual = new ArrayList<String>();
    for (Token token : JCasUtil.select(this.jCas, Token.class)) {
      actual.add(token.getLemma());
    }
    Assert.assertEquals(expected, actual);
  }

}
