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

import java.util.Collections;
import java.util.List;

import org.cleartk.classifier.Feature;

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

public class WindowNGramFeature extends Feature {
  private static final long serialVersionUID = 1L;

  public static final String ORIENTATION_LEFT = "L";

  public static final String ORIENTATION_RIGHT = "R";

  public static final String ORIENTATION_MIDDLE = "M";

  public static final String ORIENTATION_MIDDLE_REVERSE = "MR";

  public static final String DIRECTION_LEFT_TO_RIGHT = "L2R";

  public static final String DIRECTION_RIGHT_TO_LEFT = "R2L";

  private String orientation = null;

  private String direction = null;

  private String separator;

  private Integer size = null;

  private Integer index = null;

  private List<Feature> windowedFeatures = null;

  public WindowNGramFeature(
      String name,
      Object value,
      String orientation,
      String direction,
      String separator,
      Integer size,
      Integer index,
      List<Feature> windowedFeatures) {
    super(value);
    this.orientation = orientation;
    this.direction = direction;
    this.separator = separator;
    this.size = size;
    this.index = index;
    this.windowedFeatures = windowedFeatures;
    this.name = createName(name);
  }

  public Integer getIndex() {
    return index;
  }

  private String createName(String namePrefix) {
    if (namePrefix == null)
      namePrefix = "WindowNGram";

    StringBuffer returnValue = new StringBuffer();
    returnValue.append(orientation + index + "_" + size + "gram_" + direction);

    String windowedFeatureName = null;
    if (windowedFeatures != null && windowedFeatures.size() > 0)
      windowedFeatureName = windowedFeatures.get(0).getName();

    return Feature.createName(namePrefix, returnValue.toString(), windowedFeatureName);

  }

  public String getOrientation() {
    return orientation;
  }

  public int getSize() {
    return size;
  }

  public String getDirection() {
    return direction;
  }

  public String getSeparator() {
    return separator;
  }

  public List<Feature> getWindowedFeatures() {
    return Collections.unmodifiableList(windowedFeatures);
  }

}
