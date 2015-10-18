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

public class DefaultBerkeleyTokenizerTest extends BerkeleyTestBase {

  @Test
  public void givenASentenctWhenTokenizingThenAllTokenAreReturned() throws UIMAException{
    tokenBuilder.buildTokens(
        jCas,
        ParserAnnotatorTest.SAMPLE_SENT,
        ParserAnnotatorTest.SAMPLE_SENT_TOKEN,
        ParserAnnotatorTest.SAMPLE_SENT_POSES 
    );
    
    Sentence sent = JCasUtil.select(jCas, Sentence.class).iterator().next();
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
    String testPos = "X X X";
    
    tokenBuilder.buildTokens(
        jCas,
        testSent,
        testToken,
        testPos 
    );
    
    Sentence sent = JCasUtil.select(jCas, Sentence.class).iterator().next();
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
    String testPos = "X";
    
    tokenBuilder.buildTokens(
        jCas,
        testSent,
        testToken,
        testPos 
    );
    
    Sentence sent = JCasUtil.select(jCas, Sentence.class).iterator().next();
    DefaultBerkeleyTokenizer tokenizer = new DefaultBerkeleyTokenizer();
    List<Token> tokens = tokenizer.tokenize(sent);
    List<String> goldTokens = Arrays.asList(testToken.split(" "));
    
    List<String> strTokens = new ArrayList<>();
    for (Token token: tokens){
      strTokens.add(token.getCoveredText());
    }
    
    assertThat(strTokens).isEqualTo(goldTokens);
  }
  
 
}
