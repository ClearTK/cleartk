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
package org.cleartk.classifier.feature.extractor.annotationpair;

import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * 
 */

public class RelativePositionExtractor<T extends Annotation, U extends Annotation> implements
    FeatureExtractor2<T, U> {

  public static final String EQUALS = "EQUALS";

  public static final String CONTAINS = "CONTAINS";

  public static final String CONTAINEDBY = "CONTAINEDBY";

  public static final String OVERLAPS_LEFT = "OVERLAPS_LEFT";

  public static final String OVERLAPS_RIGHT = "OVERLAPS_RIGHT";

  public static final String LEFTOF = "LEFTOF";

  public static final String RIGHTOF = "RIGHTOF";

  public List<Feature> extract(JCas view, T annotation1, U annotation2) {
    String result;
    if (equals(annotation1, annotation2)) {
      result = EQUALS;
    } else if (contains(annotation1, annotation2)) {
      result = CONTAINS;
    } else if (contains(annotation2, annotation1)) {
      result = CONTAINEDBY;
    } else if (overlaps(annotation1, annotation2) && beginsFirst(annotation1, annotation2)) {
      result = OVERLAPS_LEFT;
    } else if (overlaps(annotation1, annotation2)) {
      result = OVERLAPS_RIGHT;
    } else if (beginsFirst(annotation1, annotation2)) {
      result = LEFTOF;
    } else {
      result = RIGHTOF;
    }

    return Collections.singletonList(new Feature("RelativePosition", result));
  }

  private boolean equals(Annotation a1, Annotation a2) {
    return a1.getBegin() == a2.getBegin() && a1.getEnd() == a2.getEnd();
  }

  private boolean contains(Annotation a1, Annotation a2) {
    return a1.getBegin() <= a2.getBegin() && a1.getEnd() >= a2.getEnd();
  }

  private boolean overlaps(Annotation a1, Annotation a2) {
    return !(a1.getBegin() >= a2.getEnd() || a1.getEnd() <= a2.getBegin());
  }

  private boolean beginsFirst(Annotation a1, Annotation a2) {
    return a1.getBegin() < a2.getBegin();
  }
}
