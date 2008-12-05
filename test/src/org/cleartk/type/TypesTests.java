package org.cleartk.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.timeml.type.Anchor;
import org.cleartk.corpus.timeml.type.Event;
import org.cleartk.corpus.timeml.type.TemporalLink;
import org.cleartk.corpus.timeml.type.Time;
import org.cleartk.ne.type.GazetteerNamedEntityMention;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankAnnotation;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.token.chunk.type.Subtoken;
import org.cleartk.type.Chunk;
import org.cleartk.type.ContiguousAnnotation;
import org.cleartk.type.Document;
import org.cleartk.type.Sentence;
import org.cleartk.type.SimpleAnnotation;
import org.cleartk.type.SplitAnnotation;
import org.cleartk.type.Token;
import org.cleartk.util.TestsUtil;
import org.junit.Assert;
import org.junit.Test;


/**
 * Copyright 2007-2008 Regents of the University of Colorado.  
 * All Rights Reserved.  This software is provided under the terms of the 
 * <a href="https://www.cusys.edu/techtransfer/downloads/Bulletin-ResearchLicenses.pdf">
 * CU Research License Agreement</a><p>
 */
public class TypesTests {
	
	@Test
	public void testTypes() throws Exception {
		JCas jCas = TestsUtil.newJCas();
		this.testType(jCas, new Chunk(jCas));
		this.testType(jCas, new ContiguousAnnotation(jCas));
		this.testType(jCas, new Document(jCas));
		this.testType(jCas, new Sentence(jCas));
		this.testType(jCas, new SimpleAnnotation(jCas));
		this.testType(jCas, new SplitAnnotation(jCas));
		this.testType(jCas, new Token(jCas));
		// ace2005
		this.testType(jCas, new org.cleartk.corpus.ace2005.type.Document(jCas));
		// chunk
		this.testType(jCas, new Subtoken(jCas));
		// ne
		this.testType(jCas, new GazetteerNamedEntityMention(jCas));
		this.testType(jCas, new NamedEntity(jCas));
		this.testType(jCas, new NamedEntityMention(jCas));
		// srl
		this.testType(jCas, new Argument(jCas));
		this.testType(jCas, new Predicate(jCas));
		this.testType(jCas, new SemanticArgument(jCas));
		// timeml
		this.testType(jCas, new Anchor(jCas));
		this.testType(jCas, new Event(jCas));
		this.testType(jCas, new Time(jCas));
		this.testType(jCas, new TemporalLink(jCas));
		// treebank
		this.testType(jCas, new TopTreebankNode(jCas));
		this.testType(jCas, new TreebankNode(jCas));
		this.testType(jCas, new TreebankAnnotation(jCas));
	}
	
	private void testType(JCas jCas, TOP top) throws Exception {
		Class<?> cls = top.getClass();
		if (top instanceof Annotation) {
			this.testAnnotationType(jCas, (Annotation)top);
		}
		Type type = jCas.getTypeSystem().getType(cls.getName());
		for (Object obj: type.getFeatures()) {
			Feature feature = (Feature)obj;
			if (feature.getDomain().equals(type)) {
				this.invokeMethods(cls, type, top, jCas, feature.getShortName());
			}
		}
	}

	private void testAnnotationType(JCas jCas, Annotation annotation) throws Exception {
		Class<?> annotationClass = annotation.getClass();
		
		Constructor<?> constructor = annotationClass.getConstructor(JCas.class);
		annotation = (Annotation)constructor.newInstance(jCas);
		Assert.assertEquals(0, annotation.getBegin());
		Assert.assertEquals(0, annotation.getEnd());
		
		annotation.setBegin(15);
		Assert.assertEquals(15, annotation.getBegin());

		annotation.setEnd(12);
		Assert.assertEquals(12, annotation.getEnd());
		
		constructor = annotationClass.getConstructor(JCas.class, int.class, int.class);
		annotation = (Annotation)constructor.newInstance(jCas, 4, 6);
		Assert.assertEquals(4, annotation.getBegin());
		Assert.assertEquals(6, annotation.getEnd());
	}
	
	private Class<?> findFSType(Class<?> annotationClass, String setterName) {
		for (Method method: annotationClass.getMethods()) {
			if (method.getName().equals(setterName)) {
				Class<?>[] types = method.getParameterTypes();
				if (types.length == 2) {
					return method.getParameterTypes()[1];
				}
			}
		}
		throw new RuntimeException("could not find FS type for setter:" + setterName);
	}
	
	private void invokeMethods(Class<?> cls, Type type, TOP top, JCas jCas, String featureName) throws Exception {
		Map<Class<?>, Object> defaultValues = new HashMap<Class<?>, Object>();
		defaultValues.put(int.class, 0);
		defaultValues.put(boolean.class, false);
		defaultValues.put(double.class, 0.0);
		defaultValues.put(JCas.class, jCas);

		String suffix = featureName.substring(0, 1).toUpperCase() + featureName.substring(1);
		for (Method method: cls.getMethods()) {
			String name = method.getName();
			Class<?>[] types = method.getParameterTypes();
			if (name.endsWith(suffix) && types.length == 1) {
				if (types[0].equals(FSArray.class)) {
					FSArray value = new FSArray(jCas, 1);
					Class<?> fsType = this.findFSType(cls, name);
					Object fs = fsType.getConstructor(JCas.class).newInstance(jCas);
					value.set(0, (FeatureStructure)fs);
					method.invoke(top, new Object[]{value});
					method = top.jcasType.getClass().getMethod("set" + suffix, int.class, int.class);
					method.invoke(top.jcasType, new Object[]{top.getAddress(), value.getAddress()});
				}
				if (types[0].equals(StringArray.class)) {
					StringArray value = new StringArray(jCas, 1);
					value.set(0, "foo");
					method.invoke(top, new Object[]{value});
					method = top.jcasType.getClass().getMethod(name, int.class, int.class);
					method.invoke(top.jcasType, new Object[]{top.getAddress(), value.getAddress()});
				}
			}
		}
		for (Method method: cls.getMethods()) {
			String name = method.getName();
			Class<?>[] types = method.getParameterTypes();
			if (name.equals("get" + suffix) || name.equals("set" + suffix)) {
				if (types.length != 1 || !types[0].equals(FSArray.class)) {
					Class<?>[] jcasTypes = new Class<?>[types.length + 1];
					Object[] jcasTypeValues = new Object[types.length + 1];
					Object[] values = new Object[types.length];
					for (int i = 0; i < values.length; i++) {
						values[i] = defaultValues.get(types[i]);
						if (TOP.class.isAssignableFrom(types[i])) {
							jcasTypeValues[i + 1] = values[i] ==  null ? 0 : ((TOP)values[i]).getAddress();
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
}
