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
package org.cleartk.classifier.feature.extractor;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.CleartkProcessingException;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class CleartkExtractorException extends CleartkProcessingException {

  private static final String KEY_PREFIX = CleartkExtractorException.class.getName() + ".";

  private static final long serialVersionUID = 1L;

  public static CleartkExtractorException invalidTypePath(String path, Type type) {
    String key = KEY_PREFIX + "invalidTypePath";
    return new CleartkExtractorException(DEFAULT_RESOURCE_BUNDLE, key, path, type);
  }

  public static CleartkExtractorException moreThanOneName(String name1, String name2) {
    String key = KEY_PREFIX + "moreThanOneName";
    return new CleartkExtractorException(DEFAULT_RESOURCE_BUNDLE, key, name1, name2);
  }

  public static CleartkExtractorException noAnnotationInWindow(
      Class<? extends Annotation> expectedType,
      Annotation window) {
    String key = KEY_PREFIX + "noAnnotationInWindow";
    return new CleartkExtractorException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        expectedType.getName(),
        window);
  }

  public static CleartkExtractorException noAnnotationMatchingWindow(
      Class<? extends Annotation> expectedType,
      Annotation window) {
    String key = KEY_PREFIX + "noAnnotationMatchingWindow";
    return new CleartkExtractorException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        expectedType.getName(),
        window);
  }

  public static CleartkExtractorException notPrimitive(Feature feature) {
    String key = KEY_PREFIX + "notPrimitive";
    return new CleartkExtractorException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        feature.getDomain(),
        feature.getRange());
  }

  public static CleartkExtractorException notPrimitiveArray(Feature feature) {
    String key = KEY_PREFIX + "notPrimitiveArray";
    return new CleartkExtractorException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        feature.getDomain(),
        feature.getRange());
  }

  public static CleartkExtractorException wrongAnnotationType(
      Class<? extends Annotation> expectedType,
      Annotation actualAnnotation) {
    String key = KEY_PREFIX + "wrongAnnotationType";
    return new CleartkExtractorException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        expectedType.getName(),
        actualAnnotation);
  }

  public static CleartkExtractorException wrongNumberOfAnnotations(
      Class<? extends Annotation> expectedType,
      int expectedNumber,
      int actualNumber) {
    String key = KEY_PREFIX + "wrongNumberOfAnnotations";
    return new CleartkExtractorException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        expectedType.getName(),
        expectedNumber,
        actualNumber);
  }

  public CleartkExtractorException(
      Throwable cause,
      String resourceBundleName,
      String messageKey,
      Object... arguments) {
    super(resourceBundleName, messageKey, arguments, cause);
  }

  public CleartkExtractorException(
      String resourceBundleName,
      String messageKey,
      Object... arguments) {
    super(resourceBundleName, messageKey, arguments);
  }

  public CleartkExtractorException(Throwable cause) {
    super(cause);
  }
}
