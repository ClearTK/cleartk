package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.token.type.Sentence;

/**
 * Implements SentenceOps for org.cleartk.token.type.Sentence
 * 
 * @author Lee Becker
 *
 */
public class CleartkSentenceOps implements SentenceOps<Sentence>{

  public Sentence createSentence(JCas jCas, int begin, int end) {
    Sentence sentence = new Sentence(jCas, begin, end);
    sentence.addToIndexes();
    return sentence;
  }

  public List<Sentence> selectSentences(JCas jCas, Annotation coveringAnnotation) {
    return JCasUtil.selectCovered(Sentence.class, coveringAnnotation);
  }

}
