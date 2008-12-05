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
package org.cleartk.classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.InstanceConsumer_ImplBase;
import org.cleartk.util.EmptyAnnotator;
import org.cleartk.util.TestsUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class InstanceConsumer_ImplBaseTests {
	
	// class that sets the class-level producer variables
	private static abstract class Handler implements AnnotationHandler<Object> {
		public Handler() {
			InstanceConsumer_ImplBaseTests.producer = this;
		}
		public void initialize(UimaContext context) throws ResourceInitializationException {
			InstanceConsumer_ImplBaseTests.producerIsInitialized = true;
		}
	}

	// class that calls the consumer's consume method on each instance
	public static class HandlerOne extends Handler {
		public void process(JCas cas, InstanceConsumer<Object> consumer) {
			for (Instance<Object> instance: InstanceConsumer_ImplBaseTests.instances) {
				consumer.consume(instance);
			}
		}
	}

	// class that calls the consumer's consumeAll method on the instances
	public static class HandlerAll extends Handler {
		public void process(JCas cas, InstanceConsumer<Object> consumer) {
			consumer.consumeAll(InstanceConsumer_ImplBaseTests.instances);
		}
	}

	// class that tracks calls to methods and stores observed instances
	public class Consumer extends InstanceConsumer_ImplBase<Object> {
	
		public int consumeCount = 0;
		public int consumeAllCount = 0;
		public List<Instance<Object>> instances;
		
		public Consumer() {
			this.instances = new ArrayList<Instance<Object>>();
		}
		public Object consume(Instance<Object> instance) {
			this.consumeCount++;
			this.instances.add(instance);
			return null;
		}
		public List<Object> consumeAll(List<Instance<Object>> instances) {
			this.consumeAllCount++;
			this.instances.addAll(instances);
			return null;
		}
		public boolean expectsOutcomes() {
			return false;
		}

	}

	@Test
	public void testBadHandlerName() {
		try {
			TestsUtil.getAnalysisEngine(
					InstanceConsumer_ImplBaseTests.Consumer.class,
					TestsUtil.getTypeSystem("desc/TypeSystem.xml"),
					InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER, "Foo");
			Assert.fail("expected exception with bad AnnotationHandler name");
		} catch (ResourceInitializationException e) {}
	}
	
	@Test
	public void testConsumerInitializesHandler() throws UIMAException, IOException {
		
		// get a UimaContext containing a producer class
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				EmptyAnnotator.class,
				TestsUtil.getTypeSystem("desc/TypeSystem.xml"),
				InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER,
				InstanceConsumer_ImplBaseTests.HandlerOne.class.getName());
		UimaContext context = engine.getUimaContext();
		
		// create the consumer
		Consumer consumer = new Consumer();

		// unset producer variables
		InstanceConsumer_ImplBaseTests.producer = null;
		InstanceConsumer_ImplBaseTests.producerIsInitialized = false;
		
		// initialize the consumer
		consumer.initialize(context);
		
		// make sure the producer was initialized
		Assert.assertNotNull(InstanceConsumer_ImplBaseTests.producer);
		Assert.assertTrue(InstanceConsumer_ImplBaseTests.producerIsInitialized);
	}
	
	@Test
	public void testProcessCallsHandlerOne() throws UIMAException, IOException {
		
		// for 2 instances, expect 2 calls to consume() and 0 calls to consumeAll()
		this.testProcessCallsHandler(
				InstanceConsumer_ImplBaseTests.HandlerOne.class, 2, 2, 0);
	}
	
	@Test
	public void testProcessCallsHandlerAll() throws UIMAException, IOException {

		// for 2 instances, expect 0 calls to consume() and 1 call to consumeAll()
		this.testProcessCallsHandler(
				InstanceConsumer_ImplBaseTests.HandlerAll.class, 2, 0, 1);
	}
	
	// helper method for testing that AnnotatorConsumer.process calls the Producers 
	private void testProcessCallsHandler(
			Class<? extends AnnotationHandler<Object>> producerClass,
			int instanceCount, int consumeCount, int consumeAllCount)
	throws UIMAException, IOException {
		
		// initialize a simple AnalysisEngine
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				EmptyAnnotator.class,
				TestsUtil.getTypeSystem("desc/TypeSystem.xml"),
				InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER,
				producerClass.getName());
		
		// initialize the consumer
		UimaContext context = engine.getUimaContext();
		Consumer consumer = new Consumer();
		consumer.initialize(context);
		
		// set up some classification instances
		List<Instance<Object>> instances = new ArrayList<Instance<Object>>();
		for (int i = 0; i < instanceCount; i++) {
			instances.add(new Instance<Object>());
		}
		InstanceConsumer_ImplBaseTests.instances = instances;
		
		// make sure process calls the producer and that consume() or consumeAll()
		// is called the expected number of times
		consumer.process(engine.newJCas());
		Assert.assertEquals(consumeCount, consumer.consumeCount);
		Assert.assertEquals(consumeAllCount, consumer.consumeAllCount);
		
		// make sure all instances were observed
		Assert.assertEquals(instanceCount, consumer.instances.size());
		for (int i = 0; i < instanceCount; i++) {
			Assert.assertEquals(instances.get(i), consumer.instances.get(i));
		}
	}
	
	private static AnnotationHandler<Object> producer;
	private static boolean producerIsInitialized;
	private static List<Instance<Object>> instances;

}
