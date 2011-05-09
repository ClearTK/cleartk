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
package org.cleartk.classifier.feature;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * 
 */

public class WindowFeature extends Feature {
  public static final String ORIENTATION_LEFT = "L";

  public static final String ORIENTATION_RIGHT = "R";

  public static final String ORIENTATION_MIDDLE = "M";

  public static final String ORIENTATION_MIDDLE_REVERSE = "MR";

  /**
   * This method returns the annotation at index 0 w.r.t. the orientation of the extractor, the
   * annotation we are extracting features for and the window the provides the boundaries for
   * feature extraction.
   * 
   * @param jCas
   *          The JCas containing the focus annotation
   * @param itemClass
   *          The type of Annotation around the focus annotation to select
   * @param focusAnnotation
   *          The annotation from which the orientation is defined.
   * @param orientation
   *          The orientation defining which annotation is the start.
   * @return the annotation at index 0:
   *         <ul>
   *         <li>for ORIENTATION_LEFT return the annotation adjacent and to the left of annotation
   *         <li>for ORIENTATION_RIGHT return the annotation adjacent and to the right of annotation
   *         <li>for ORIENTATION_MIDDLE return the left most annotation completely contained in
   *         annotation, null if does not exist
   *         <li>for ORIENTATION_MIDDLE_REVERSE return the right most annotation completely
   *         contained in annotation, null if does not exist
   *         </ul>
   */
  public static <T extends Annotation> T getStartAnnotation(
      JCas jCas,
      Class<T> itemClass,
      Annotation focusAnnotation,
      String orientation) {
    if (orientation.equals(WindowFeature.ORIENTATION_LEFT)) {
      List<T> anns = JCasUtil.selectPreceding(jCas, itemClass, focusAnnotation, 1);
      if (anns.size() > 0 && anns.get(0) == focusAnnotation) {
        anns = JCasUtil.selectPreceding(jCas, itemClass, focusAnnotation, 2).subList(0, 1);
      }
      return anns.size() == 0 ? null : anns.get(0);
    } else if (orientation.equals(WindowFeature.ORIENTATION_RIGHT)) {
      List<T> anns = JCasUtil.selectFollowing(jCas, itemClass, focusAnnotation, 1);
      if (anns.size() > 0 && anns.get(0) == focusAnnotation) {
        anns = JCasUtil.selectFollowing(jCas, itemClass, focusAnnotation, 2).subList(1, 2);
      }
      return anns.size() == 0 ? null : anns.get(0);
    } else if (orientation.equals(WindowFeature.ORIENTATION_MIDDLE)) {
      List<T> anns = JCasUtil.selectCovered(jCas, itemClass, focusAnnotation);
      if (anns.size() == 0) {
        return itemClass.isInstance(focusAnnotation) ? itemClass.cast(focusAnnotation) : null;
      } else {
        return anns.get(0);
      }
    } else if (orientation.equals(WindowFeature.ORIENTATION_MIDDLE_REVERSE)) {
      List<T> anns = JCasUtil.selectCovered(jCas, itemClass, focusAnnotation);
      if (anns.size() == 0) {
        return itemClass.isInstance(focusAnnotation) ? itemClass.cast(focusAnnotation) : null;
      } else {
        return anns.get(anns.size() - 1);
      }
    }
    throw new IllegalArgumentException("Unknown orientation: " + orientation);
  }

  private String orientation = null;

  private Integer position = null;

  private Feature windowedFeature = null;

  private Integer outOfBoundsDistance = 0;

  public WindowFeature(
      String name,
      Object value,
      String orientation,
      Integer position,
      Feature windowedFeature,
      Integer outOfBoundsDistance) {
    super(value);
    this.orientation = orientation;
    this.position = position;
    this.windowedFeature = windowedFeature;
    this.outOfBoundsDistance = outOfBoundsDistance;
    this.name = createName(name);
  }

  public WindowFeature(
      String name,
      Object value,
      String orientation,
      Integer position,
      Feature windowedFeature) {
    this(name, value, orientation, position, windowedFeature, null);
  }

  public WindowFeature(
      String name,
      Object value,
      String orientation,
      Integer position,
      Integer outOfBoundsDistance) {
    this(name, value, orientation, position, null, outOfBoundsDistance);
  }

  private String createName(String namePrefix) {
    if (namePrefix == null)
      namePrefix = "Window";

    StringBuffer sb = new StringBuffer();
    if (orientation != null)
      sb.append(orientation);
    if (position != null)
      sb.append(position);

    if (outOfBoundsDistance != null && outOfBoundsDistance > 0)
      sb.append("OOB" + outOfBoundsDistance);

    String windowedFeatureName = null;
    if (windowedFeature != null)
      windowedFeatureName = windowedFeature.getName();

    return Feature.createName(namePrefix, sb.toString(), windowedFeatureName);

  }

  public String getOrientation() {
    return orientation;
  }

  public int getOutOfBoundsDistance() {
    return outOfBoundsDistance;
  }

  public int getPosition() {
    return position;
  }

  public Feature getWindowedFeature() {
    return windowedFeature;
  }

}
