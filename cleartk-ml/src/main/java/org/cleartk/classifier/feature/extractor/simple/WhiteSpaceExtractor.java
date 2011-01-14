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
package org.cleartk.classifier.feature.extractor.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class WhiteSpaceExtractor implements SimpleFeatureExtractor {

  public static final String WhiteSpaceRegex = "\\s";

  public static final Pattern WhiteSpacePattern = Pattern.compile(WhiteSpaceRegex);

  public static final String ORIENTATION_LEFT = "L";

  public static final String ORIENTATION_RIGHT = "R";

  public List<Feature> extract(JCas view, Annotation focusAnnotation) throws CleartkException {
    List<Feature> features = new ArrayList<Feature>();

    String text = view.getDocumentText();
    int begin = focusAnnotation.getBegin();
    int end = focusAnnotation.getEnd();

    // white space to the left of the focusAnnotation
    if (begin == 0) {
      Feature feature = new Feature("whitespace", ORIENTATION_LEFT);
      features.add(feature);
    } else {
      char leftChar = text.charAt(begin - 1);
      if (isWhiteSpace(leftChar)) {
        Feature feature = new Feature("whitespace", ORIENTATION_LEFT);
        features.add(feature);
      }
    }

    // white space to the right of the focusAnnotation
    if (end == text.length()) {
      Feature feature = new Feature("whitespace", ORIENTATION_RIGHT);
      features.add(feature);
    } else {
      char rightChar = text.charAt(end);
      if (isWhiteSpace(rightChar)) {
        Feature feature = new Feature("whitespace", ORIENTATION_RIGHT);
        features.add(feature);
      }
    }
    return features;
  }

  /**
   * List of whitespace characters comes from java.util.regex.Pattern
   * 
   * @param chr
   */
  private boolean isWhiteSpace(char chr) {
    if (chr == ' ' || chr == '\t' || chr == '\n' || chr == '\f' || chr == '\u000B' || chr == '\r')
      return true;
    return false;
  }
}
