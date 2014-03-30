/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.opennlp.tools.parser;

import java.util.List;

import opennlp.tools.postag.POSTagger;
import opennlp.tools.util.Sequence;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.opennlp.tools.ParserAnnotator;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philipp Wetzler, Phili Ogren
 * 
 *         <p>
 *         This class provides a simple implementation of the OpenNLP POSTagger interface. This
 *         implementation doesn't perform part-of-speech tagging, per se. Instead, it retrieves
 *         part-of-speech tags from the CAS using the {@link InputTypesHelper} that it is
 *         instantiated with. This class is not intended for use outside of the
 *         {@link ParserAnnotator}.
 */
@Beta
public class CasPosTagger<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation>
    implements POSTagger {

  private List<TOKEN_TYPE> tokens;

  InputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE> inputTypesHelper;

  public CasPosTagger(InputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE> inputTypesHelper) {
    this.inputTypesHelper = inputTypesHelper;
  }

  public void setTokens(List<TOKEN_TYPE> tokens) {
    this.tokens = tokens;
  }

  public Sequence[] topKSequences(List<String> sentence) {
    return topKSequences(sentence.toArray(new String[sentence.size()]));
  }

  /**
   * This method will only return a single sequence corresponding to the part-of-speech tags that
   * have already been assigned to the tokens in the CAS as part of some upstream processing that
   * precedes running of the {@link ParserAnnotator}.
   */
  public Sequence[] topKSequences(String[] sentence) {
    Sequence[] s = new Sequence[1];

    s[0] = new Sequence();
    for (TOKEN_TYPE token : this.tokens) {
      String pos = inputTypesHelper.getPosTag(token);
      if (pos == null) {
        throw new RuntimeException("no part of speech for token: " + token.getCoveredText());
      }
      s[0].add(pos, 1.0);
    }

    return s;
  }

  public List<String> tag(List<String> sentence) {
    throw new UnsupportedOperationException();
  }

  public String[] tag(String[] sentence) {
    throw new UnsupportedOperationException();
  }

  public String tag(String sentence) {
    throw new UnsupportedOperationException();
  }

  public String[] tag(String[] arg0, Object[] arg1) {
    throw new UnsupportedOperationException();
  }

  public Sequence[] topKSequences(String[] arg0, Object[] arg1) {
    throw new UnsupportedOperationException();
  }

}
