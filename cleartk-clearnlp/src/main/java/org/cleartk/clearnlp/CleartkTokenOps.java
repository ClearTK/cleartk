package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;


/**
 * Defines common set of token data type operations used to convert output from tokenizers, pos taggers, and lemmatizers into
 * the annotations and properties of {@link org.cleartk.token.type.Token}
 * 
 * @author Lee Becker
 *
 */
@Beta
public class CleartkTokenOps implements TokenOps<Token>{

  @Override
  public Token createToken(JCas jCas, int begin, int end) {
    Token token = (new Token(jCas, begin, end));
    token.addToIndexes();
    return token;
  }

  @Override
  public List<Token> selectTokens(JCas jCas, Annotation coveringAnnotation) {
    return JCasUtil.selectCovered(jCas, Token.class, coveringAnnotation);
  }

  @Override
  public String getPos(JCas jCas, Token token) {
    return token.getPos();
  }

  @Override
  public void setPos(JCas jCas, Token token, String posTag) {
    token.setPos(posTag);
  }

  @Override
  public String getLemma(JCas jCas, Token token) {
    return token.getLemma();
  }

  @Override
  public void setLemma(JCas jCas, Token token, String lemma) {
    token.setLemma(lemma);
  }

}
