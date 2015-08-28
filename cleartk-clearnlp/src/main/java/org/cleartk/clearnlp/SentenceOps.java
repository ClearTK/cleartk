package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Defines common set of sentence data type operations used to convert to and query from 
 * the target type system
 * 
 * @author Lee Becker
 *
 */
public interface SentenceOps<SENTENCE_TYPE> {
  
  SENTENCE_TYPE createSentence(JCas jCas, int begin, int end);
  
  List<SENTENCE_TYPE> selectSentences(JCas jCas, Annotation coveringAnnotation);
  
}
