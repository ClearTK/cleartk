package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.annotations.Beta;

/**
 * Defines common set of token data type operations used to convert to and query from 
 * the target type system
 * 
 * @author Lee Becker
 *
 */
@Beta
public interface TokenOps<TOKEN_TYPE> {
  
  TOKEN_TYPE createToken(JCas jCas, int begin, int end);
  
  List<TOKEN_TYPE> selectTokens(JCas jCas, Annotation coveringAnnotation);
  
  String getPos(JCas jCas, TOKEN_TYPE token);
  
  void setPos(JCas jCas, TOKEN_TYPE token, String posTag);
  
  String getLemma(JCas jCas, TOKEN_TYPE token);
  
  void setLemma(JCas jCas, TOKEN_TYPE token, String lemma);
  
}
