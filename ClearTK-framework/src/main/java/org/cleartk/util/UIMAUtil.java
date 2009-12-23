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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.Initializable;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * @author Philipp Wetzler
 * @author Steven Bethard
 * 
 */
public class UIMAUtil {
	public static FSArray toFSArray(JCas jCas, List<? extends FeatureStructure> fsList) {
		if (fsList == null) {
			return new FSArray(jCas, 0);
		}
		FSArray fsArray = new FSArray(jCas, fsList.size());
		fsArray.copyFromArray(fsList.toArray(new FeatureStructure[fsList.size()]), 0, 0, fsList.size());
		return fsArray;
	}

	public static StringArray toStringArray(JCas jCas, String[] sArray) {
		StringArray uimaSArray = new StringArray(jCas, sArray.length);
		uimaSArray.copyFromArray(sArray, 0, 0, sArray.length);
		return uimaSArray;
	}

	public static <T extends FeatureStructure> List<T> toList(FSArray fsArray, Class<T> cls) {
		List<T> list = new ArrayList<T>();

		if (fsArray == null) {
			return list;
		}

		for (FeatureStructure fs : fsArray.toArray()) {
			list.add(cls.cast(fs));
		}
		return list;

	}

	public static Type getCasType(JCas jCas, Class<? extends TOP> cls) {
		try {
			return jCas.getCasType(cls.getField("type").getInt(null));
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String readSofa(JCas view) throws IOException {
		InputStream in = view.getSofaDataStream();
		StringBuffer tmp = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			tmp.append(new String(b, 0, n));
		}
		return tmp.toString();
	}

	/**
	 * Get a configuration parameter value, raising an exception if it was not
	 * specified.
	 * 
	 * @param context
	 *            The UIMAContext where the parameter should be defined.
	 * @param paramName
	 *            The name of the parameter.
	 * @return The value of the named parameter.
	 * @throws ResourceInitializationException
	 */
	public static Object getRequiredConfigParameterValue(UimaContext context, String paramName)
			throws ResourceInitializationException {
		Object paramValue = context.getConfigParameterValue(paramName);
		if (paramValue == null) {
			String key = ResourceInitializationException.CONFIG_SETTING_ABSENT;
			throw new ResourceInitializationException(key, new Object[] { paramName });
		}
		else if (paramValue instanceof String) {
			String str = (String) paramValue;
			if (str.trim().equals("")) {
				String key = ResourceInitializationException.CONFIG_SETTING_ABSENT;
				throw new ResourceInitializationException(key, new Object[] { paramName });
			}
		}
		return paramValue;
	}

	/**
	 * Get a configuration parameter value, supplying a default if it was not
	 * specified.
	 * 
	 * @param context
	 *            The UIMAContext where the parameter should be defined.
	 * @param paramName
	 *            The name of the parameter.
	 * @param defaultValue
	 *            The value to use if the parameter was not specified.
	 * @return The value of the named parameter.
	 */
	public static Object getDefaultingConfigParameterValue(UimaContext context, String paramName, Object defaultValue) {
		Object paramValue = context.getConfigParameterValue(paramName);
		if (paramValue == null) {
			paramValue = defaultValue;
		}
		else if (paramValue instanceof String) {
			String str = (String) paramValue;
			if (str.trim().equals("")) paramValue = defaultValue;
		}
		else if (paramValue instanceof String[]) {
			String[] strs = (String[]) paramValue;
			if (strs.length == 0) paramValue = defaultValue;
			if (strs.length == 1 && strs[0].trim().equals("")) paramValue = defaultValue;
		}
		return paramValue;
	}

	/**
	 * Create a JCas view from a CAS for a given view name.
	 * 
	 * @param cas
	 *            The CAS object from which the JCas view should be derived.
	 * @param viewName
	 *            The name of the JCas view. If null, the default JCas view will
	 *            be returned instead. (No new view will be created.)
	 * @return The JCas view.
	 * @throws CollectionException
	 */
	public static JCas createJCasView(CAS cas, String viewName) throws CollectionException {
		try {
			if (viewName == null || viewName.trim().equals("")) {
				return cas.getJCas();
			}
			else {
				return cas.createView(viewName).getJCas();
			}
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}
	}

	/**
	 * Take the name of a Java class from the UimaContext, instantiate the
	 * class, and initialize it (if possible).
	 * <p>
	 * This method was enhanced to handle instantiation of declared (member)
	 * classes so that e.g. one could instantiate an annotation handler defined
	 * as a member class in a unit test using the
	 * InstanceConsumer_ImplBase.initialize method. While this is quite
	 * convenient, make note that the referring to MyUnitTest.this in your unit
	 * test code will not work correctly in that inner class that is the test
	 * annotation handler because an instance of your unit test will be created
	 * by JUnit to run the test and another instance will be created in this
	 * method. The latter is what is available inside your inner class and is,
	 * in general, not the "this" that you want.
	 * 
	 * @param <T>
	 *            The supertype of the class which should be instantiated.
	 * @param context
	 *            The UimaContext from which the parameter should be read.
	 * @param classParamName
	 *            The name of the the UimaContext parameter containing the class
	 *            name.
	 * @param superClass
	 *            The superclass of the class which should be instantiated.
	 * @return An instance of the requested class, as a subtype of the supplied
	 *         superclass. The instance's initialize method will be called if
	 *         possible, using {@link #initialize(Object, UimaContext)}
	 * @throws ResourceInitializationException
	 */
	public static <T> T create(UimaContext context, String classParamName, Class<T> superClass)
	throws ResourceInitializationException {
		return create(context, classParamName, superClass, null);
	}
	
	public static <T> T create(UimaContext context, String classParamName, Class<T> superClass, Class<? extends T> defaultClass)
			throws ResourceInitializationException {
		Class<? extends T> cls = getClass(context, classParamName, superClass, defaultClass);
		return create(context, cls);
	}

	public static <T> T create(String className, Class<T> superClass, UimaContext context)
	throws ResourceInitializationException {
		Class<? extends T> cls = getClass(className, superClass);
		return create(context, cls);
}

	
	public static <T> T create(UimaContext context, Class<? extends T> cls) throws ResourceInitializationException{
		// create a new instance
		T instance;
		try {
			if (cls.isMemberClass() && (cls.getModifiers() & Modifier.STATIC) == 0) {
				Class<?> declaringClass = cls.getDeclaringClass();
				Object declaringInstance = declaringClass.newInstance();
				instance = cls.getConstructor(new Class[] { declaringInstance.getClass() }).newInstance(
						new Object[] { declaringInstance });
			}
			else {
				instance = cls.newInstance();
			}
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

		// initialize and return the SequentialAnnotationHandler
		UIMAUtil.initialize(instance, context);
		return instance;
	}

	
	public static <T> Class<? extends T> getClass(UimaContext context, String classParamName, Class<T> superClass)
			throws ResourceInitializationException {
		return getClass(context, classParamName, superClass, null);
	}

	public static <T> Class<? extends T> getClass(UimaContext context, String classParamName, Class<T> superClass, Class<? extends T> defaultClass)
			throws ResourceInitializationException {
		// get the class name from the parameter
		String className;
		if(defaultClass != null) {
			className = (String) getDefaultingConfigParameterValue(context, classParamName, defaultClass.getName());
		} else { 
			className = (String) getRequiredConfigParameterValue(context, classParamName);
		}

		return getClass(className, superClass);
	}
	
	public static <T> Class<? extends T> getClass(String className, Class<T> superClass) throws ResourceInitializationException {
		try {
			Class<? extends T> cls = Class.forName(className).asSubclass(superClass);
			return cls;
		}
		catch (Exception e) {
			throw new ResourceInitializationException(new Throwable("classname = "+className+" superClass = "+superClass.getName(), e));
		}
	
	}
	

	/**
	 * Initialize the object using the UimaContext, if possible.
	 * 
	 * In particular, the current implementation will initialize any object
	 * which is either an Initializable instance or an AnalysisComponent
	 * instance.
	 * 
	 * @param object
	 *            The object to be initialized.
	 * @param context
	 *            The UimaContext used to initialize the object
	 * @throws ResourceInitializationException
	 */
	public static void initialize(Object object, UimaContext context) throws ResourceInitializationException {
		if (object instanceof Initializable) {
			((Initializable) object).initialize(context);
		}
	}

	/**
	 * Checks that the given type parameters of the given objects are
	 * compatible.
	 * 
	 * Type parameters are identified by providing the class in which the type
	 * parameter is defined, and the declared name of the type parameter.
	 * 
	 * Throws a ResourceInitializationException if the type parameters are not
	 * compatible.
	 * 
	 * @param <T>
	 *            Type of the class declaring the first type parameter
	 * @param <U>
	 *            Type of the class declaring the second type parameter
	 * @param paramDefiningClass1
	 *            The class declaring the first type parameter
	 * @param paramName1
	 *            The declared name of the first type parameter
	 * @param object1
	 *            The target object
	 * @param paramDefiningClass2
	 *            The class declaring the second type parameter
	 * @param paramName2
	 *            The declared name of the second type parameter
	 * @param object2
	 *            The source object
	 * @throws ResourceInitializationException
	 */
	public static <T, U> void checkTypeParameterIsAssignable(Class<T> paramDefiningClass1, String paramName1,
			T object1, Class<U> paramDefiningClass2, String paramName2, U object2)
			throws ResourceInitializationException {

		// get the type arguments from the objects
		java.lang.reflect.Type type1 = ReflectionUtil.getTypeArgument(paramDefiningClass1, paramName1, object1);
		java.lang.reflect.Type type2 = ReflectionUtil.getTypeArgument(paramDefiningClass2, paramName2, object2);
		
		// both arguments missing is compatible
		if (type1 == null && type2 == null) {
			return;
		}
		
		// if the second type is not assignable to the first, raise an exception
		if (type1 == null || type2 == null || !ReflectionUtil.isAssignableFrom(type1, type2)) {
			throw new ResourceInitializationException(new RuntimeException(String.format(
					"%s with %s %s is incompatible with %s with %s %s", object1.getClass().getSimpleName(), paramName1,
					type1, object2.getClass().getSimpleName(), paramName2, type2)));
		}
	}


}
