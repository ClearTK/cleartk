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
package org.cleartk.test.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Assert;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */

public class TypeTestUtil {

  public static void testType(JCas jcas, TOP top) throws Exception {
    Class<?> cls = top.getClass();
    if (top instanceof Annotation) {
      testAnnotationType(jcas, (Annotation) top);
    }
    Type type = jcas.getTypeSystem().getType(cls.getName());
    for (Object obj : type.getFeatures()) {
      Feature feature = (Feature) obj;
      if (feature.getDomain().equals(type)) {
        invokeMethods(cls, type, top, jcas, feature.getShortName());
      }
    }
  }

  private static void testAnnotationType(JCas jcas, Annotation annotation) throws Exception {
    Class<?> annotationClass = annotation.getClass();

    Constructor<?> constructor = annotationClass.getConstructor(JCas.class);
    annotation = (Annotation) constructor.newInstance(jcas);
    Assert.assertEquals(0, annotation.getBegin());
    Assert.assertEquals(0, annotation.getEnd());

    annotation.setBegin(15);
    Assert.assertEquals(15, annotation.getBegin());

    annotation.setEnd(12);
    Assert.assertEquals(12, annotation.getEnd());

    constructor = annotationClass.getConstructor(JCas.class, int.class, int.class);
    annotation = (Annotation) constructor.newInstance(jcas, 4, 6);
    Assert.assertEquals(4, annotation.getBegin());
    Assert.assertEquals(6, annotation.getEnd());
  }

  private static Class<?> findFSType(Class<?> annotationClass, String setterName) {
    for (Method method : annotationClass.getMethods()) {
      if (method.getName().equals(setterName)) {
        Class<?>[] types = method.getParameterTypes();
        if (types.length == 2) {
          return method.getParameterTypes()[1];
        }
      }
    }
    throw new RuntimeException("could not find FS type for setter:" + setterName);
  }

  private static void invokeMethods(Class<?> cls, Type type, TOP top, JCas jcas, String featureName)
          throws Exception {
    Map<Class<?>, Object> defaultValues = new HashMap<Class<?>, Object>();
    defaultValues.put(int.class, 0);
    defaultValues.put(boolean.class, false);
    defaultValues.put(double.class, 0.0);
    defaultValues.put(JCas.class, jcas);

    String suffix = featureName.substring(0, 1).toUpperCase() + featureName.substring(1);
    for (Method method : cls.getMethods()) {
      String name = method.getName();
      Class<?>[] types = method.getParameterTypes();
      if (name.endsWith(suffix) && types.length == 1) {
        if (types[0].equals(FSArray.class)) {
          FSArray value = new FSArray(jcas, 1);
          Class<?> fsType = findFSType(cls, name);
          Object fs = fsType.getConstructor(JCas.class).newInstance(jcas);
          value.set(0, (FeatureStructure) fs);
          method.invoke(top, new Object[] { value });
          method = top.jcasType.getClass().getMethod("set" + suffix, int.class, int.class);
          method.invoke(top.jcasType, new Object[] { top.getAddress(), value.getAddress() });
        }
        if (types[0].equals(StringArray.class)) {
          StringArray value = new StringArray(jcas, 1);
          value.set(0, "foo");
          method.invoke(top, new Object[] { value });
          method = top.jcasType.getClass().getMethod(name, int.class, int.class);
          method.invoke(top.jcasType, new Object[] { top.getAddress(), value.getAddress() });
        }
      }
    }
    for (Method method : cls.getMethods()) {
      String name = method.getName();
      Class<?>[] types = method.getParameterTypes();
      if (name.equals("get" + suffix) || name.equals("set" + suffix)) {
        if (types.length != 1
                || (!types[0].equals(FSArray.class) && !types[0].equals(StringArray.class))) {
          Class<?>[] jcasTypes = new Class<?>[types.length + 1];
          Object[] jcasTypeValues = new Object[types.length + 1];
          Object[] values = new Object[types.length];
          for (int i = 0; i < values.length; i++) {
            values[i] = defaultValues.get(types[i]);
            if (TOP.class.isAssignableFrom(types[i])) {
              jcasTypeValues[i + 1] = values[i] == null ? 0 : ((TOP) values[i]).getAddress();
              jcasTypes[i + 1] = int.class;
            } else {
              jcasTypeValues[i + 1] = values[i];
              jcasTypes[i + 1] = types[i];
            }

          }
          method.invoke(top, values);
          jcasTypes[0] = int.class;
          jcasTypeValues[0] = top.getAddress();
          method = top.jcasType.getClass().getMethod(name, jcasTypes);
          method.invoke(top.jcasType, jcasTypeValues);
        }
      }
    }
  }

  public static void testTypeSystem(JCas jCas) throws Exception {
    Iterator<Type> types = jCas.getTypeSystem().getTypeIterator();

    while (types.hasNext()) {
      try {
        Type type = types.next();
        @SuppressWarnings("unchecked")
        Class<? extends TOP> cls = (Class<? extends TOP>) Class.forName(type.getName());
        TOP top = cls.getConstructor(JCas.class).newInstance(jCas);
        testType(jCas, top);
      } catch (Exception e) {
        continue;
      }
    }

  }
}
