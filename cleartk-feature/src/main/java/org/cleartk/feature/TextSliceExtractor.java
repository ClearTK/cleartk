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
package org.cleartk.feature;

import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.SimpleNamedFeatureExtractor;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Extract a slice of the text covered by the annotation. Handles negative slice indices to make it
 * easy to slice from the end of the string.
 * 
 * @author Steven Bethard
 */
public class TextSliceExtractor<T extends Annotation> implements SimpleNamedFeatureExtractor<T> {

  private int start;

  private int stop;
  
  private String featureName;

  /**
   * Create an extractor for a given slice of the text. E.g.
   * <code>new TextSliceExtractor(1, -1)</code> would extract all of the text but its first and last
   * characters.
   * 
   * @param start
   *          The first character offset of the slice. If negative, it is assumed to count backwards
   *          from the end of the string. If the offset falls before the start of the string, the
   *          start of the string will be used instead.
   * @param stop
   *          The last character offset of the slice. If negative, it is assumed to count backwards
   *          from the end of the string. If the offset falls after the end of the string, the end
   *          of the string will be used instead.
   */
  public TextSliceExtractor(int start, int stop) {
    this.start = start;
    this.stop = stop;
    this.featureName = "Suffix";
  }

  /**
   * Create an extractor for a slice of text from a single offset to the end of the string. E.g.
   * <code>new TextSliceExtractor(-2)</code> would extract a suffix of length 2 from the text.
   * 
   * @param start
   *          The first character offset of the slice. If negative, it is assumed to count backwards
   *          from the end of the string. If the offset falls before the start of the string, the
   *          start of the string will be used instead.
   */
  public TextSliceExtractor(int start) {
    this(start, Integer.MAX_VALUE);
  }
  
  @Override
  public String getFeatureName() {
    return this.featureName;
  }

  public List<Feature> extract(JCas view, T focusAnnotation) {
    String text = focusAnnotation.getCoveredText();
    int startOffset = this.start;
    if (startOffset < 0) {
      startOffset += text.length();
    }
    if (startOffset < 0) {
      startOffset = 0;
    }
    int stopOffset = this.stop;
    if (stopOffset < 0) {
      stopOffset += text.length();
    }
    if (stopOffset > text.length()) {
      stopOffset = text.length();
    }
    text = text.substring(startOffset, stopOffset);
    return Collections.singletonList(new Feature(this.featureName, text));
  }

}