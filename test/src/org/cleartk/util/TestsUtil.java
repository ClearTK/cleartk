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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.syntax.treebank.type.TreebankNode;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public class TestsUtil {



	/**
	 * Initializes the instance producer with the AnalysisEngine's context, and
	 * returns all instances created by the producer for the given JCas. The
	 * value null will be returned for each instance.
	 * 
	 * @param producer
	 *            The object for producing ClassifierInstances from a JCas
	 * @param engine
	 *            The AnalysisEngine with the context for initialization.
	 * @param jCas
	 *            The JCas to be processed.
	 * @return The list of ClassifierInstances created by the producer.
	 * @throws UIMAException
	 */
	public static <T> List<Instance<T>> produceInstances(AnnotationHandler<T> producer, AnalysisEngine engine, JCas jCas)
			throws UIMAException {
		return produceInstances(producer, null, engine, jCas);
	}

	/**
	 * Initializes the instance producer with the AnalysisEngine's context, and
	 * returns all instances created by the producer for the given JCas.
	 * 
	 * @param producer
	 *            The object for producing ClassifierInstances from a JCas
	 * @param returnValue
	 *            The value that should be returned for each instance
	 * @param engine
	 *            The AnalysisEngine with the context for initialization.
	 * @param jCas
	 *            The JCas to be processed.
	 * @return The list of ClassifierInstances created by the producer.
	 * @throws UIMAException
	 */
	public static <T> List<Instance<T>> produceInstances(AnnotationHandler<T> producer, T returnValue,
			AnalysisEngine engine, JCas jCas) throws UIMAException {
		UimaContext context = engine.getUimaContext();
		AnnotatorConsumer<T> consumer = new AnnotatorConsumer<T>(returnValue);
		producer.initialize(context);
		producer.process(jCas, consumer);
		return consumer.instances;
	}

	/**
	 * A simple instance consumer that stores all instances in a public
	 * attribute. Intended primarily for testing.
	 */
	public static class AnnotatorConsumer<T> implements InstanceConsumer<T> {
		public List<Instance<T>> instances;

		public T returnValue;

		public AnnotatorConsumer(T returnValue) {
			this.instances = new ArrayList<Instance<T>>();
			this.returnValue = returnValue;
		}

		public T consume(Instance<T> instance) {
			this.instances.add(instance);
			return this.returnValue;
		}

		public List<T> consumeSequence(List<Instance<T>> instances) {
			this.instances.addAll(instances);
			List<T> result = null;
			if (this.returnValue != null) {
				result = new ArrayList<T>();
				for (int i = 0; i < instances.size(); i++) {
					result.add(this.returnValue);
				}
			}
			return result;
		}

		public boolean expectsOutcomes() {
			return this.returnValue == null;
		}
	}


	/**
	 * Create a leaf TreebankNode in a JCas.
	 * 
	 * @param jCas
	 *            The JCas which the annotation should be added to.
	 * @param begin
	 *            The begin offset of the node.
	 * @param end
	 *            The end offset of the node.
	 * @param nodeType
	 *            The part of speech tag of the node.
	 * @return The TreebankNode which was added to the JCas.
	 */
	public static TreebankNode newNode(JCas jCas, int begin, int end, String nodeType) {
		TreebankNode node = new TreebankNode(jCas, begin, end);
		node.setNodeType(nodeType);
		node.setChildren(new FSArray(jCas, 0));
		node.setLeaf(true);
		node.addToIndexes();
		return node;
	}

	/**
	 * Create a branch TreebankNode in a JCas. The offsets of this node will be
	 * determined by its children.
	 * 
	 * @param jCas
	 *            The JCas which the annotation should be added to.
	 * @param nodeType
	 *            The phrase type tag of the node.
	 * @param children
	 *            The TreebankNode children of the node.
	 * @return The TreebankNode which was added to the JCas.
	 */
	public static TreebankNode newNode(JCas jCas, String nodeType, TreebankNode... children) {
		int begin = children[0].getBegin();
		int end = children[children.length - 1].getEnd();
		TreebankNode node = new TreebankNode(jCas, begin, end);
		node.setNodeType(nodeType);
		node.addToIndexes();
		FSArray fsArray = new FSArray(jCas, children.length);
		fsArray.copyFromArray(children, 0, 0, children.length);
		node.setChildren(fsArray);
		for (TreebankNode child : children) {
			child.setParent(node);
		}
		return node;
	}



	/**
	 * A simple do-nothing AnnotationHandler that expects Boolean outcomes.
	 * Useful primarily for testing DataWriter objects which require some
	 * annotation handler to be specified.
	 */
	public static class EmptyBooleanHandler implements AnnotationHandler<Boolean> {
		public void initialize(UimaContext context) throws ResourceInitializationException {
		}

		public void process(JCas cas, InstanceConsumer<Boolean> consumer) throws AnalysisEngineProcessException {
		}
	}

	/**
	 * A simple do-nothing AnnotationHandler that expects String outcomes.
	 * Useful primarily for testing DataWriter objects which require some
	 * annotation handler to be specified.
	 */
	public static class EmptyStringHandler implements AnnotationHandler<String> {
		public void initialize(UimaContext context) throws ResourceInitializationException {
		}

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
		}
	}

	/**
	 * A random number generator for creating Instance objects
	 */
	private static final Random random = new Random(42);

	/**
	 * Create a number of random Instance objects that should be easy to
	 * classify. This is primarily useful for testing DataWriter and Classifier
	 * implementations.
	 * 
	 * @param n
	 *            The number of instances
	 * @return The list of newly-created instances
	 */
	public static List<Instance<Boolean>> generateBooleanInstances(int n) {
		List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
		for (int i = 0; i < n; i++) {
			Instance<Boolean> instance = new Instance<Boolean>();
			if (TestsUtil.random.nextInt(2) == 0) {
				instance.setOutcome(true);
				instance.add(new Feature("hello", TestsUtil.random.nextInt(1000) + 1000));
			}
			else {
				instance.setOutcome(false);
				instance.add(new Feature("hello", TestsUtil.random.nextInt(100)));
			}
			instances.add(instance);
		}
		return instances;
	}

	/**
	 * Create a number of random Instance objects that should be easy to
	 * classify. This is primarily useful for testing DataWriter and Classifier
	 * implementations.
	 * 
	 * @param n
	 *            The number of instances
	 * @return The list of newly-created instances
	 */
	public static List<Instance<String>> generateStringInstances(int n) {
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		for (int i = 0; i < n; i++) {
			Instance<String> instance = new Instance<String>();
			switch (TestsUtil.random.nextInt(3)) {
			case 0:
				instance.setOutcome("A");
				instance.add(new Feature("hello", -1050 + TestsUtil.random.nextInt(100)));
				break;
			case 1:
				instance.setOutcome("B");
				instance.add(new Feature("hello", -50 + TestsUtil.random.nextInt(100)));
				break;
			case 2:
				instance.setOutcome("C");
				instance.add(new Feature("hello", 950 + TestsUtil.random.nextInt(100)));
				break;
			}
			instances.add(instance);
		}
		return instances;
	}
}
