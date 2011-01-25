/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.util;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class InstanceFactory {

  /**
   * @param <T>
   * @param outcome
   *          the outcome of the returned instance
   * @param featureData
   *          an even number of elements corresponding to name/value pairs used to create features
   * @return a single instance with the provided outcome and features corresponding to the
   *         featureData provided
   */
  public static <T> Instance<T> createInstance(T outcome, Object... featureData) {
    if (featureData.length % 2 != 0) {
      throw new IllegalArgumentException(
          "feature data must consist of an even number of elements corresponding to name/value pairs used to create features. ");
    }
    Instance<T> instance = new Instance<T>(outcome);
    for (int i = 0; i < featureData.length;) {
      instance.add(new Feature(featureData[i].toString(), featureData[i + 1]));
      i += 2;
    }
    return instance;
  }

  /**
   * 
   * @param <T>
   * @param outcome
   *          the outcome of the returned instance
   * @param featureData
   *          space delimited features. Here the features only have names (no values) corresponding
   *          to the space delimited strings.
   * @return a single instance with the provided outcome and name-only string features found in the
   *         provided featureData
   */
  public static <T> Instance<T> createInstance(T outcome, String featureData) {
    Instance<T> instance = new Instance<T>(outcome);
    String[] columns = featureData.split(" ");
    for (int i = 0; i < columns.length; i++) {
      Feature feature = new Feature();
      feature.setName(columns[i]);
      instance.add(feature);
    }
    return instance;
  }

}
