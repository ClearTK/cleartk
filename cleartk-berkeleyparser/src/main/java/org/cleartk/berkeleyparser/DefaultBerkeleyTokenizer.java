package org.cleartk.berkeleyparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CASException;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

import edu.berkeley.nlp.io.LineLexer;

public class DefaultBerkeleyTokenizer implements Tokenizer<Token, Sentence> {
  private LineLexer tokenizer = new LineLexer();

  public List<Token> tokenize(Sentence sent) {
    try {
      String strSent = sent.getCoveredText();
      List<String> strTokens = tokenizer.tokenizeLine(strSent);
      List<Token> tokens = new ArrayList<>();
      
      int index = 0;
      for (String strToken: strTokens){
        index = strSent.indexOf(strToken, index);
        if (index == -1)
          throw new RuntimeException(String.format("Cannot find token <%s> in the sentence <%s>: ", 
              strToken, strSent));
        Token token = new Token(sent.getCAS().getJCas(), index, index + strToken.length());
        token.addToIndexes();
        tokens.add(token);
        index += strToken.length();
      }
      return tokens;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (CASException e) {
      e.printStackTrace();
    }
    return null;
    
  }
}
