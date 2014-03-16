/** 
 * Copyright (c) 2007-2012, Regents of the University of Colorado 
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
package org.cleartk.ml.feature.function;

import java.util.Collections;
import java.util.List;

import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.util.CaseUtil;

/**
 * <br>
 * Copyright (c) 2007-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class CapitalTypeFeatureFunction implements FeatureFunction {

  public static final String DEFAULT_NAME = "CapitalType";

  public enum CapitalType {
    ALL_UPPERCASE, ALL_LOWERCASE, INITIAL_UPPERCASE, MIXED_CASE
  }

  /**
   * If the value of the feature is a StringValue and is determined to be one of ALL_UPPERCASE,
   * ALL_LOWERCASE, INITIAL_UPPERCASE, or MIXED_CASE, then a new feature containing one of those
   * four values is returned. If the value of the feature cannot be characterized by one of these
   * four values, then the empty list is returned (e.g. the value is an empty string, contains only
   * white space, or contains only digits, etc.)
   * 
   * <P>
   * This method was inspired by CapitalizationTypeTagger.py written by Steven Bethard.
   * 
   * @return a feature that has a value that is one of ALL_UPPERCASE, ALL_LOWERCASE,
   *         INITIAL_UPPERCASE, or MIXED_CASE. Otherwise the empty list is returned.
   */
  public List<Feature> apply(Feature feature) {
    String featureName = Feature.createName(DEFAULT_NAME, feature.getName());
    Object featureValue = feature.getValue();
    if (featureValue == null)
      return Collections.emptyList();
    else if (featureValue instanceof String) {
      String value = featureValue.toString();
      if (value == null || value.length() == 0)
        return Collections.emptyList();

      String lowerCaseValue = value.toLowerCase();
      String upperCaseValue = value.toUpperCase();
      if (lowerCaseValue.equals(upperCaseValue))
        return Collections.emptyList();

      if (value.equals(value.toLowerCase())) {
        return Collections.singletonList(new Feature(
            featureName,
            CapitalType.ALL_LOWERCASE.toString()));
      } else if (value.equals(value.toUpperCase())) {
        return Collections.singletonList(new Feature(
            featureName,
            CapitalType.ALL_UPPERCASE.toString()));
      }

      if (CaseUtil.isInitialUppercase(value)) {
        return Collections.singletonList(new Feature(
            featureName,
            CapitalType.INITIAL_UPPERCASE.toString()));
      }

      return Collections.singletonList(new Feature(featureName, CapitalType.MIXED_CASE.toString()));
    } else
      return Collections.emptyList();
  }

}
