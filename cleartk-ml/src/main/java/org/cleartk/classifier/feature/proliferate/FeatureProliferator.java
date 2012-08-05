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
package org.cleartk.classifier.feature.proliferate;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * The notion of a "feature proliferator" is to take existing features and create new features out
 * of them in some useful way to reduce work and complexity. For example, if you have a feature that
 * corresponds to the spanned text of a word and you want to a feature that corresponds to the
 * lowercased spanned text of the same word, then you could use the
 * LowerCaseProliferator.proliferate() method. The same feature proliferator could be used to get a
 * lowercase version of an feature that has a StringValue.
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 * @deprecated
 */
@Deprecated
public abstract class FeatureProliferator {
  /**
   * Creates a FeatureProliferator which will assign the given name to all features it creates.
   * 
   * @param featureName
   *          The name to assign to created Feature objects.
   */
  public FeatureProliferator(String featureName) {
    this.featureName = featureName;
  }

  /**
   * Creates a new Feature from the given one.
   * 
   * The feature passed in should not be modified. Instead, a new feature with the necessary
   * adjustments should be created.
   * 
   * @param feature
   *          The original Feature.
   * @return The newly created Feature.
   */
  public abstract List<Feature> proliferate(Feature feature);

  /**
   * Creates new Features from the given ones by repeatedly calling the proliferate(Feature) method.
   * 
   * @param features
   *          The original features.
   * @return The newly created features.
   */
  public List<Feature> proliferate(List<Feature> features) {
    List<Feature> returnValues = new ArrayList<Feature>();
    for (Feature feature : features) {
      returnValues.addAll(this.proliferate(feature));
    }
    return returnValues;
  }

  /**
   * Return the name that will be assigned to all Features created.
   * 
   * @return The Feature name.
   */
  public String getFeatureName() {
    return featureName;
  }

  /**
   * Set the name that will be assigned to all Features created.
   * 
   * @param featureName
   *          The Feature name.
   */
  public void setFeatureName(String featureName) {
    this.featureName = featureName;
  }

  private String featureName;
}
