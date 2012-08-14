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
package org.cleartk.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.resource.ResourceInitializationException;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class ReflectionUtil {

  /**
   * Perform an unchecked cast based on a type parameter.
   * 
   * @param <T>
   *          The type to which the object should be cast.
   * @param o
   *          The object.
   * @return The object, cast to the given type.
   */
  @SuppressWarnings("unchecked")
  public static <T> T uncheckedCast(Object o) {
    return (T) o;
  }

  public static interface TypeArgumentDelegator {
    public Map<String, Type> getTypeArguments(Class<?> genericType);
  }

  public static <T> Type getTypeArgument(Class<T> genericType, String typeParameterName, T obj) {
    Map<String, Type> typeArguments = getTypeArguments(genericType, obj);
    return typeArguments == null ? null : typeArguments.get(typeParameterName);
  }

  /**
   * Try to find the instantiation of all of genericTypes type parameters in objs class.
   * 
   * @param genericType
   *          the generic supertype of objs class
   * @param obj
   *          an instantiation of a subclass of genericType. All of genericTypes type parameters
   *          must have been instantiated in the inheritance hierarchy.
   * @return a map of genericTypes type parameters (their name in the source code) to the type they
   *         are instantiated as in obj
   */
  public static Map<String, Type> getTypeArguments(Class<?> genericType, Object obj) {
    if (obj instanceof TypeArgumentDelegator) {
      return ((TypeArgumentDelegator) obj).getTypeArguments(genericType);
    }
    Map<String, Type> typeMap = new TreeMap<String, Type>();
    return getTypeArguments(genericType, obj.getClass(), typeMap);
  }

  public static boolean isAssignableFrom(Type type1, Type type2) {
    if (type1 instanceof Class<?> && type2 instanceof Class<?>) {
      return ((Class<?>) type1).isAssignableFrom((Class<?>) type2);
    } else {
      return type1.equals(type2);
    }
  }

  private static Map<String, Type> getTypeArguments(
      Class<?> genericType,
      Type type,
      Map<String, Type> typeMap) {
    if (type instanceof ParameterizedType) {
      return getTypeArguments(genericType, (ParameterizedType) type, typeMap);
    } else if (type instanceof Class<?>) {
      return getTypeArguments(genericType, (Class<?>) type, typeMap);
    } else {
      throw new IllegalArgumentException("type must be a ParameterizedType or Class");
    }
  }

  private static Map<String, Type> getTypeArguments(
      Class<?> genericType,
      Class<?> classType,
      Map<String, Type> typeMap) {
    if (genericType.isInterface()) {
      for (Type interfaceType : classType.getGenericInterfaces()) {
        Map<String, Type> result = getTypeArguments(genericType, interfaceType, typeMap);
        if (result != null)
          return result;
      }
    }

    Type superType = classType.getGenericSuperclass();
    if (superType != null) {
      return getTypeArguments(genericType, superType, typeMap);
    }

    return null;
  }

  private static Map<String, Type> getTypeArguments(
      Class<?> genericType,
      ParameterizedType paramType,
      Map<String, Type> typeMap) {
    Class<?> rawType = (Class<?>) paramType.getRawType();
    if (rawType == genericType) {
      // found it!
      TypeVariable<?> typeVars[] = rawType.getTypeParameters();
      Type actualTypes[] = paramType.getActualTypeArguments();
      Map<String, Type> result = new TreeMap<String, Type>();
      for (int i = 0; i < actualTypes.length; i++) {
        while (actualTypes[i] != null && actualTypes[i] instanceof TypeVariable<?>) {
          String key = typevarString((TypeVariable<?>) actualTypes[i]);
          if (typeMap.containsKey(key))
            actualTypes[i] = typeMap.get(key);
          else
            actualTypes[i] = null;
        }
        result.put(typeVars[i].getName(), actualTypes[i]);
      }
      return result;
    } else {
      TypeVariable<?> typeVars[] = rawType.getTypeParameters();
      Type actualTypes[] = paramType.getActualTypeArguments();
      for (int i = 0; i < typeVars.length; i++)
        typeMap.put(typevarString(typeVars[i]), actualTypes[i]);
      return getTypeArguments(genericType, paramType.getRawType(), typeMap);
    }
  }

  private static String typevarString(TypeVariable<?> tv) {
    return tv.getGenericDeclaration().toString() + " " + tv.getName();
  }

  /**
   * Checks that the given type parameters of the given objects are compatible.
   * 
   * Type parameters are identified by providing the class in which the type parameter is defined,
   * and the declared name of the type parameter.
   * 
   * Throws a ResourceInitializationException if the type parameters are not compatible.
   * 
   * @param <T>
   *          Type of the class declaring the first type parameter
   * @param <U>
   *          Type of the class declaring the second type parameter
   * @param paramDefiningClass1
   *          The class declaring the first type parameter
   * @param paramName1
   *          The declared name of the first type parameter
   * @param object1
   *          The target object
   * @param paramDefiningClass2
   *          The class declaring the second type parameter
   * @param paramName2
   *          The declared name of the second type parameter
   * @param object2
   *          The source object
   */
  public static <T, U> void checkTypeParameterIsAssignable(
      Class<T> paramDefiningClass1,
      String paramName1,
      T object1,
      Class<U> paramDefiningClass2,
      String paramName2,
      U object2) throws ResourceInitializationException {

    // get the type arguments from the objects
    java.lang.reflect.Type type1 = ReflectionUtil.getTypeArgument(
        paramDefiningClass1,
        paramName1,
        object1);
    java.lang.reflect.Type type2 = ReflectionUtil.getTypeArgument(
        paramDefiningClass2,
        paramName2,
        object2);

    // both arguments missing is compatible
    if (type1 == null && type2 == null) {
      return;
    }

    // if the second type is not assignable to the first, raise an exception
    if (type1 == null || type2 == null || !ReflectionUtil.isAssignableFrom(type1, type2)) {
      throw CleartkInitializationException.incompatibleTypeParameters(
          object1,
          paramName1,
          type1,
          object2,
          paramName2,
          type2);
    }
  }

  /**
   * Checks that the given type parameters of the given objects are exactly equal.
   * 
   * Type parameters are identified by providing the class in which the type parameter is defined,
   * and the declared name of the type parameter.
   * 
   * Throws an instance of the given exception class if type parameters are not exactly equal.
   * 
   * @param <T>
   *          Type of the class declaring the first type parameter
   * @param <U>
   *          Type of the class declaring the second type parameter
   * @param paramDefiningClass1
   *          The class declaring the first type parameter
   * @param paramName1
   *          The declared name of the first type parameter
   * @param object1
   *          The target object
   * @param paramDefiningClass2
   *          The class declaring the second type parameter
   * @param paramName2
   *          The declared name of the second type parameter
   * @param object2
   *          The source object
   */
  public static <T, U, E extends Exception> void checkTypeParametersAreEqual(
      Class<T> paramDefiningClass1,
      String paramName1,
      T object1,
      Class<U> paramDefiningClass2,
      String paramName2,
      U object2,
      Class<E> exceptionClass) throws E {

    // get the type arguments from the objects
    java.lang.reflect.Type type1 = ReflectionUtil.getTypeArgument(
        paramDefiningClass1,
        paramName1,
        object1);
    java.lang.reflect.Type type2 = ReflectionUtil.getTypeArgument(
        paramDefiningClass2,
        paramName2,
        object2);

    // both arguments missing is equal
    if (type1 == null && type2 == null) {
      return;
    }

    // if the second type is not equal to the first, raise an exception
    if (type1 == null || type2 == null || !type1.equals(type2)) {
      try {
        throw exceptionClass.getConstructor(String.class).newInstance(
            String.format(
                "%s with %s %s is not equal to %s with %s %s",
                object1.getClass().getSimpleName(),
                paramName1,
                type1,
                object2.getClass().getSimpleName(),
                paramName2,
                type2));
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
