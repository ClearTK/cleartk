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
import java.util.List;

import org.apache.uima.UIMAException;
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
  public void givenASentenctWhenTokenizingThenAllTokenAreReturned() throws UIMAException{
    setupJCas(ParserAnnotatorTest.SAMPLE_SENT, ParserAnnotatorTest.SAMPLE_SENT_TOKEN);

    Sentence sent = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    DefaultBerkeleyTokenizer tokenizer = new DefaultBerkeleyTokenizer();
    List<Token> tokens = tokenizer.tokenize(sent);
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

    Sentence sent = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    DefaultBerkeleyTokenizer tokenizer = new DefaultBerkeleyTokenizer();
    List<Token> tokens = tokenizer.tokenize(sent);
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

    Sentence sent = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    DefaultBerkeleyTokenizer tokenizer = new DefaultBerkeleyTokenizer();
    List<String> goldTokens = Arrays.asList(testToken.split(" "));

    List<Token> tokens = tokenizer.tokenize(sent);
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

    String[] sentTokens = testToken.split("\n");
    int idx = 0;
    for (Sentence sent :JCasUtil.select(jCas, Sentence.class)){
      DefaultBerkeleyTokenizer tokenizer = new DefaultBerkeleyTokenizer();
      List<Token> tokens = tokenizer.tokenize(sent);
      tokens = tokenizer.tokenize(sent);
      List<String> goldTokens = Arrays.asList(sentTokens[idx++].trim().split(" "));

      List<String> strTokens = new ArrayList<>();
      for (Token token: tokens){
        strTokens.add(token.getCoveredText());
      }

      assertThat(strTokens).isEqualTo(goldTokens);
    }
  }

  private void setupJCas(String sent, String token) throws UIMAException {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < token.split(" ").length; i++){
      if (i != 0)
        sb.append(" ");
      sb.append("X");
    }
    String testPos = sb.toString();

    tokenBuilder.buildTokens(
        jCas,
        sent,
        token,
        testPos 
        );
  }
  
  @Test
  public void givenTheSentenceWhenTokenizingTheOutputIsIncorrect() throws UIMAException{
    String testSent = "I show it to my friends, and they all say 'wow.";
    String testToken = "I show it to my friends , and they all say 'wow .";
    setupJCas(testSent, testToken);
    
    Sentence sent = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    DefaultBerkeleyTokenizer tokenizer = new DefaultBerkeleyTokenizer();
    List<String> goldTokens = Arrays.asList("I show it to my friends , and they all say 'wo .".split(" "));

    List<Token> tokens = tokenizer.tokenize(sent);
    List<String> strTokens = new ArrayList<>();
    for (Token token: tokens){
      strTokens.add(token.getCoveredText());
    }

    assertThat(strTokens).isEqualTo(goldTokens); 
  }

}
