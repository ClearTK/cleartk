package org.cleartk.berkeleyparser;

import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;

public interface Tokenizer<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> {

  public List<TOKEN_TYPE> tokenize(SENTENCE_TYPE sent);
}
