/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.ml.encoder;

import java.util.List;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.features.FeatureEncoder;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class CleartkEncoderException extends CleartkProcessingException {

  private static final String KEY_PREFIX = CleartkEncoderException.class.getName() + ".";

  private static final long serialVersionUID = 1L;

  public static CleartkEncoderException invalidFeatureVectorValue(int index, double value) {
    String key = KEY_PREFIX + "invalidFeatureVectorValue";
    return new CleartkEncoderException(
        CleartkProcessingException.DEFAULT_RESOURCE_BUNDLE,
        key,
        index,
        value);
  }

  public static CleartkEncoderException noMatchingEncoder(
      Feature feature,
      List<? extends FeatureEncoder<?>> encoders) {
    String key = KEY_PREFIX + "noMatchingEncoder";
    return new CleartkEncoderException(DEFAULT_RESOURCE_BUNDLE, key, feature, encoders);
  }

  public CleartkEncoderException(
      Throwable cause,
      String resourceBundleName,
      String messageKey,
      Object... arguments) {
    super(resourceBundleName, messageKey, arguments, cause);
  }

  public CleartkEncoderException(String resourceBundleName, String messageKey, Object... arguments) {
    super(resourceBundleName, messageKey, arguments);
  }

  public CleartkEncoderException(Throwable cause) {
    super(cause);
  }
}
