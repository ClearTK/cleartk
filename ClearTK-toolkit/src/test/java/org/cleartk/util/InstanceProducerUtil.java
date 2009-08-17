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

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.SequentialAnnotationHandler;
import org.cleartk.classifier.SequentialInstanceConsumer;

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
public class InstanceProducerUtil {

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
			throws UIMAException, CleartkException {
		return produceInstances(producer, null, engine, jCas);
	}

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
	public static <T> List<Instance<T>> produceInstances(SequentialAnnotationHandler<T> producer, AnalysisEngine engine, JCas jCas)
			throws UIMAException, CleartkException {
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
			AnalysisEngine engine, JCas jCas) throws UIMAException, CleartkException {
		UimaContext context = engine.getUimaContext();
		AnnotatorConsumer<T> consumer = new AnnotatorConsumer<T>(returnValue);
		UIMAUtil.initialize(producer, context);
		producer.process(jCas, consumer);
		return consumer.instances;
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
	public static <T> List<Instance<T>> produceInstances(SequentialAnnotationHandler<T> producer, T returnValue,
			AnalysisEngine engine, JCas jCas) throws UIMAException, CleartkException {
		UimaContext context = engine.getUimaContext();
		AnnotatorConsumer<T> consumer = new AnnotatorConsumer<T>(returnValue);
		UIMAUtil.initialize(producer, context);
		producer.process(jCas, consumer);
		return consumer.instances;
	}

	/**
	 * A simple instance consumer that stores all instances in a public
	 * attribute. Intended primarily for testing.
	 */
	public static class AnnotatorConsumer<T> 
	implements InstanceConsumer<T>, SequentialInstanceConsumer<T> {
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



}
