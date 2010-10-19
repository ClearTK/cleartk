/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
*/
package org.cleartk.temporal.timeml.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;

/**
 * Extract the text of Tokens preceding the focus annotation, filtering Tokens
 * by part of speech tags.
 * 
 * @author Steven Bethard
 */
public class PrecedingTokenTextBagExtractor implements SimpleFeatureExtractor {
  
  private int nTokens;
  private Set<String> acceptablePOSTags;

  /**
   * Create an extractor for preceding Token texts.
   * 
   * @param nTokens           Number of Tokens before the focus annotation to
   *                          consider.
   * @param acceptablePOSTags 2-character part of speech tags (e.g. NN, VB)
   *                          that are acceptable parts of speech for Tokens.
   *                          Tokens with other parts of speech will be omitted
   *                          from the output (though they still count against
   *                          the nTokens limit).
   */
  public PrecedingTokenTextBagExtractor(int nTokens, String ... acceptablePOSTags) {
    this.nTokens = nTokens;
    this.acceptablePOSTags = new HashSet<String>(Arrays.asList(acceptablePOSTags));
  }
  
  public String getFeatureName(String pos, int index) {
    return "PrecedingToken";
  }

  public List<Feature> extract(JCas jCas, Annotation focusAnnotation) throws CleartkException {
    List<Feature> features = new ArrayList<Feature>();
    Token firstTokenBefore = AnnotationRetrieval.getAdjacentAnnotation(jCas, focusAnnotation, Token.class, true);
    if (firstTokenBefore != null) {
      FSIterator<Annotation> iterator = jCas.getAnnotationIndex(UIMAUtil.getCasType(jCas, Token.class)).iterator();
      iterator.moveTo(firstTokenBefore);
      for (int i = 0; i < this.nTokens && iterator.isValid(); ++i) {
        Token token = (Token)iterator.get();
        String pos = token.getPos();
        if (pos.length() > 2) {
          pos = pos.substring(0, 2);
        }
        if (this.acceptablePOSTags.contains(pos)) {
          features.add(new Feature(this.getFeatureName(pos, i), token.getCoveredText()));
        }
        iterator.moveToPrevious();
      }
    }
    return features;
  }
}