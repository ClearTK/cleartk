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
package org.cleartk.util;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.uima.resource.ResourceInitializationException;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class CleartkInitializationException extends ResourceInitializationException {

  private static final String DEFAULT_RESOURCE_BUNDLE = "org.cleartk.util.CleartkExceptions";

  private static final String KEY_PREFIX = CleartkInitializationException.class.getName() + ".";

  private static final long serialVersionUID = 1L;

  public static CleartkInitializationException fileNotFound(File file) {
    String key = KEY_PREFIX + "fileNotFound";
    return new CleartkInitializationException(DEFAULT_RESOURCE_BUNDLE, key, file.getPath());
  }

  public static CleartkInitializationException incompatibleTypeParameterAndType(
      Object object1,
      String typeParamName1,
      Type typeParamValue1,
      Class<?> class2) {
    String key = KEY_PREFIX + "incompatibleTypeParameterAndType";
    String class1Name = object1.getClass().getName();
    return new CleartkInitializationException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        class1Name,
        typeParamName1,
        typeParamValue1,
        class2.getName());
  }

  public static CleartkInitializationException incompatibleTypeParameters(
      Object object1,
      String typeParamName1,
      Type typeParamValue1,
      Object object2,
      String typeParamName2,
      Type typeParamValue2) {
    String key = KEY_PREFIX + "incompatibleTypeParameters";
    String class1 = object1.getClass().getName();
    String class2 = object2.getClass().getName();
    return new CleartkInitializationException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        class1,
        typeParamName1,
        typeParamValue1 == null ? null : typeParamValue1.toString().replaceFirst("^class ", ""),
        class2,
        typeParamName2,
        typeParamValue2 == null ? null : typeParamValue2.toString().replaceFirst("^class ", ""));
  }

  public static CleartkInitializationException invalidParameterValueSelectFrom(
      String paramName,
      List<?> expectedValues,
      Object actualValue) {
    String key = KEY_PREFIX + "invalidParameterValueSelectFrom";
    return new CleartkInitializationException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        paramName,
        expectedValues,
        actualValue);
  }

  public static CleartkInitializationException neitherParameterSet(
      String param1,
      Object value1,
      String param2,
      Object value2) {
    String key = KEY_PREFIX + "neitherParameterSet";
    return new CleartkInitializationException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        param1,
        value1,
        param2,
        value2);
  }

  public static CleartkInitializationException notExactlyOneParameterSet(
      String param1,
      Object value1,
      String param2,
      Object value2) {
    String key = KEY_PREFIX + "notExactlyOneParameterSet";
    return new CleartkInitializationException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        param1,
        value1,
        param2,
        value2);
  }

  public static CleartkInitializationException notSingleCharacter(String paramName, Object value) {
    String key = KEY_PREFIX + "notSingleCharacter";
    return new CleartkInitializationException(DEFAULT_RESOURCE_BUNDLE, key, paramName, value);
  }

  public static CleartkInitializationException parameterLessThan(
      String paramName,
      Object minExpectedValue,
      Object actualValue) {
    String key = KEY_PREFIX + "parameterLessThan";
    return new CleartkInitializationException(
        DEFAULT_RESOURCE_BUNDLE,
        key,
        paramName,
        minExpectedValue,
        actualValue);
  }

  public CleartkInitializationException(
      String resourceBundleName,
      String messageKey,
      Object... arguments) {
    super(resourceBundleName, messageKey, arguments);
  }

  public CleartkInitializationException(
      Throwable cause,
      String resourceBundleName,
      String messageKey,
      Object... arguments) {
    super(resourceBundleName, messageKey, arguments, cause);
  }

}
