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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.InstanceConsumer_ImplBase;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.opennlp.MaxentClassifierBuilder;
import org.cleartk.util.TestsUtil;
import org.cleartk.util.UIMAUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class DataWriter_ImplBaseTests {
	
	@Before
	public void setUp() throws Exception {
		new File(this.outputDir).mkdir();
	}

	@After
	public void tearDown() throws Exception {
		this.delete(new File(this.outputDir));
	}
	
	private void delete(File file) {
		if (file.isDirectory()) {
			for (File child: file.listFiles()) {
				this.delete(child);
			}
		}
		file.delete();
	}

	@Test
	public void testManifest() throws UIMAException, IOException {
		String expectedManifest = (
				"Manifest-Version: 1.0\n" +
				"classifierBuilderClass: org.cleartk.classifier.opennlp.MaxentClassifie\n" +
				" rBuilder");
		
		this.process();
		File manifestFile = new File(this.outputDir, "MANIFEST.MF");
		String actualManifest = FileUtils.file2String(manifestFile);
		Assert.assertEquals(expectedManifest, actualManifest.replaceAll("\r", "").trim());
	}
	
	@Test
	public void testConsumeAll() throws UIMAException {
		DataWriter_ImplBaseTests.consumeCount = 0;
		this.process(SingleProducer.class);
		Assert.assertEquals(1, DataWriter_ImplBaseTests.consumeCount);

		DataWriter_ImplBaseTests.consumeCount = 0;
		this.process(MultiProducer.class);
		Assert.assertEquals(2, DataWriter_ImplBaseTests.consumeCount);
	}
	
	@Test
	public void testPrintWriter() throws UIMAException, IOException {
		String actualText;

		this.process(SingleProducer.class, "foo.txt", "foo");
		actualText = FileUtils.file2String(new File(this.outputDir, "foo.txt"));
		Assert.assertEquals("foo\n", actualText.replaceAll("\r", ""));

		this.process(MultiProducer.class, "bar.txt", "bar");
		actualText = FileUtils.file2String(new File(this.outputDir, "bar.txt"));
		Assert.assertEquals("bar\nbar\n", actualText.replaceAll("\r", ""));
		
		this.process(MultiProducer.class, "foo/bar.txt", "bar");
		actualText = FileUtils.file2String(new File(this.outputDir, "foo/bar.txt"));
		Assert.assertEquals("bar\nbar\n", actualText.replaceAll("\r", ""));
		
		try {
			this.process(SingleProducer.class, ".", "shouldn't work");
			Assert.fail("expected exception on bad file name");
		} catch (ResourceInitializationException e) {}
	}
	
	@Test
	public void testNullFactory() throws UIMAException, IOException {
		try {
			TestsUtil.getAnalysisEngine(
					Writer.class, null,				
					InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER, SingleProducer.class.getName(),
					DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY, this.outputDir,
					DataWriter_ImplBase.PARAM_ENCODER_FACTORY_CLASS, NullFactory.class.getName(),
					Writer.PARAM_FILE_NAME, "foo.txt",
					Writer.PARAM_STRING_TO_WRITE, "foo");
			Assert.fail("Expected exception with factory returning null encoders");
		} catch (ResourceInitializationException e) {}
	}
	
	public static class SingleProducer<T> implements AnnotationHandler<T> {
		public void initialize(UimaContext context) throws ResourceInitializationException {}
		public void process(JCas cas, InstanceConsumer<T> consumer) {
			consumer.consume(new Instance<T>());
		}
	}

	public static class MultiProducer<T> implements AnnotationHandler<T> {
		public void initialize(UimaContext context) throws ResourceInitializationException {}
		public void process(JCas cas, InstanceConsumer<T> consumer) {
			List<Instance<T>> instances = new ArrayList<Instance<T>>();
			instances.add(new Instance<T>());
			instances.add(new Instance<T>());
			consumer.consumeAll(instances);
		}
	}
	
	public static class Writer extends DataWriter_ImplBase<Object,Object,Object> {

		public static final String PARAM_FILE_NAME = "FileName";
		public static final String PARAM_STRING_TO_WRITE = "StringToWrite";
		
		@Override
		public void initialize(UimaContext context) throws ResourceInitializationException {
			super.initialize(context);
			
			String fileName = (String)UIMAUtil.getRequiredConfigParameterValue(
					context, DataWriter_ImplBaseTests.Writer.PARAM_FILE_NAME);
			this.fooWriter = this.getPrintWriter(fileName);
			this.toWrite = (String)UIMAUtil.getRequiredConfigParameterValue(
					context, DataWriter_ImplBaseTests.Writer.PARAM_STRING_TO_WRITE);
		}

		public Object consume(Instance<Object> instance) {
			DataWriter_ImplBaseTests.consumeCount++;
			
			// write to the print writer
			this.fooWriter.println(this.toWrite);
			
			// use the feature encoder
			this.featuresEncoder.encodeAll(instance.getFeatures());
			
			// return null
			return null;
		}
		
		private PrintWriter fooWriter;
		private String toWrite;

		@Override
		protected Class<? extends ClassifierBuilder<? extends Object>> getDefaultClassifierBuilderClass() {
			return MaxentClassifierBuilder.class;
		}

		@Override
		protected Class<? extends EncoderFactory> getDefaultEncoderFactoryClass() {
			return Factory.class;
		}
	}
	
	public static class Factory implements EncoderFactory {
		public FeaturesEncoder<?> createFeaturesEncoder(UimaContext context) {
			return new EmptyFeaturesEncoder();
		}
		public OutcomeEncoder<?, ?> createOutcomeEncoder(UimaContext context) {
			return new EmptyOutcomeEncoder();
		}
		
	}
	
	public static class NullFactory implements EncoderFactory {
		public FeaturesEncoder<?> createFeaturesEncoder(UimaContext context) {
			return null;
		}
		public OutcomeEncoder<?, ?> createOutcomeEncoder(UimaContext context) {
			return null;
		}
		
	}
	
	public static class EmptyFeaturesEncoder implements FeaturesEncoder<Object> {

		private static final long serialVersionUID = 8574437254815448838L;

		public void allowNewFeatures(boolean flag) {
		}

		public Object encodeAll(Iterable<Feature> features) {
			return null;
		}
	}
	
	public static class EmptyOutcomeEncoder implements OutcomeEncoder<Object, Object> {

		private static final long serialVersionUID = -752385896104992463L;

		public Object decode(Object outcome) {
			return null;
		}

		public Object encode(Object outcome) {
			return null;
		}
		
	}
	
	private void process(Class<?> producerClass, String fileName, String toWrite)
	throws UIMAException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				Writer.class, null,				
				InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER, producerClass.getName(),
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY, this.outputDir,
				Writer.PARAM_FILE_NAME, fileName,
				Writer.PARAM_STRING_TO_WRITE, toWrite);
		JCas jCas = engine.newJCas();
		engine.process(jCas);
		engine.collectionProcessComplete();
	}
	
	private void process(Class<?> producerClass) throws UIMAException {
		this.process(producerClass, "foo.txt", "foo");
	}

	private void process() throws UIMAException {
		this.process(SingleProducer.class);
	}
	
	private final String outputDir = "test/data/classifiers";
	private static int consumeCount;
}
