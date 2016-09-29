/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.berkeleyparser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Majid Laali
 */
public class DefaultBerkeleyTokenizerTest extends BerkeleyTestBase {

  @Test
  public void givenASentencWhenTokenizingThenAllTokensAreReturned() throws UIMAException{
    setupJCas(ParserAnnotatorTest.SAMPLE_SENT, ParserAnnotatorTest.SAMPLE_SENT_TOKEN);

    AnalysisEngineDescription tokenizer = DefaultBerkeleyTokenizer.getDescription();
    SimplePipeline.runPipeline(jCas, tokenizer);
    
    Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
    List<String> goldTokens = Arrays.asList(ParserAnnotatorTest.SAMPLE_SENT_TOKEN.split(" "));

    List<String> strTokens = new ArrayList<>();
    for (Token token: tokens){
      strTokens.add(token.getCoveredText());
    }

    assertThat(strTokens).isEqualTo(goldTokens);
  }

  @Test
  public void givenALeftBracketWhenTokenizingThenItIsConvertedToLRB() throws UIMAException{
    String testSent = "(Test)";
    String testToken = "( Test )";
    setupJCas(testSent, testToken);

    AnalysisEngineDescription tokenizer = DefaultBerkeleyTokenizer.getDescription();
    SimplePipeline.runPipeline(jCas, tokenizer);
    
    Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
    List<String> goldTokens = Arrays.asList(testToken.split(" "));

    List<String> strTokens = new ArrayList<>();
    for (Token token: tokens){
      strTokens.add(token.getCoveredText());
    }

    assertThat(strTokens).isEqualTo(goldTokens);
  }

  @Test
  public void givenBritishWordWhenTokenizingThenTokenizerDoesntConvertToAmerican() throws UIMAException{
    String testSent = "colour";
    String testToken = "colour";
    setupJCas(testSent, testToken);

    AnalysisEngineDescription tokenizer = DefaultBerkeleyTokenizer.getDescription();
    SimplePipeline.runPipeline(jCas, tokenizer);
    
    Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
    List<String> goldTokens = Arrays.asList(testToken.split(" "));
    List<String> strTokens = new ArrayList<>();
    
    for (Token token: tokens){
      strTokens.add(token.getCoveredText());
    }

    assertThat(strTokens).isEqualTo(goldTokens);
  }

  @Test
  public void givenTwoSentencesWhenTokenizingTheSecondSentenceThenTokensBoundariesAreCorrectlySet() throws UIMAException{
    String testSent = "It is a test.I like it.";
    String testToken = "It is a test . \n I like it .";

    setupJCas(testSent, testToken);

    AnalysisEngineDescription tokenizer = DefaultBerkeleyTokenizer.getDescription();
    SimplePipeline.runPipeline(jCas, tokenizer);
    

    String[] sentTokens = testToken.split("\n");
    int idx = 0;
    for (Sentence sent :JCasUtil.select(jCas, Sentence.class)){
      List<Token> tokens = JCasUtil.selectCovered(Token.class, sent);
      List<String> goldTokens = Arrays.asList(sentTokens[idx++].trim().split(" "));

      List<String> strTokens = new ArrayList<>();
      for (Token token: tokens){
        strTokens.add(token.getCoveredText());
      }

      assertThat(strTokens).isEqualTo(goldTokens);
    }
  }

  private void setupJCas(String sents, String tokens) {
    jCas.setDocumentText(sents);
    
    int begin = 0;
    for (String aSent: tokens.split("\n")){
      int end = begin;
      for (String aToken: aSent.split(" ")){
        int wordIdx = sents.indexOf(aToken, end);
        if (wordIdx == -1)
          throw new RuntimeException(String.format("A token <%s> does not exist in sent <%s>.", aToken, aSent));
        end = wordIdx + aToken.length();
      }
      new Sentence(jCas, begin, end).addToIndexes();
      begin = end;
    }
  }

  /**
   * TODO: resolve this bug
   */
  @Test
  public void givenTheSentenceWhenTokenizingTheOutputIsIncorrect() throws UIMAException{
    String testSent = "I show it to my friends, and they all say 'wow.";
    String testToken = "I show it to my friends , and they all say 'wo .";
    setupJCas(testSent, testToken);

    AnalysisEngineDescription tokenizer = DefaultBerkeleyTokenizer.getDescription();
    SimplePipeline.runPipeline(jCas, tokenizer);
    
    Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
    List<String> goldTokens = Arrays.asList(testToken.split(" "));

    List<String> strTokens = new ArrayList<>();
    for (Token token: tokens){
      strTokens.add(token.getCoveredText());
    }

    assertThat(strTokens).isEqualTo(goldTokens); 
  }

}
