/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.UimaContextFactory;
import org.uutuc.util.TearDownUtil;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class CleartkSequentialAnnotatorTest {

	private static String outputDirectory = "test/data/classifierannotator";
	
	@Before
	public void setUp() {
		new File(outputDirectory).mkdirs();
	}

	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(new File(outputDirectory));
	}

	@Test
	public void testIsTraining() {
		assertFalse(new StringTestAnnotator().isTraining());
	}
	
	@Test
	public void testBadFileName() throws CleartkException {
		try {
			CleartkSequentialAnnotator<String> classifierAnnotator = new StringTestAnnotator();
			classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
					CleartkSequentialAnnotator.PARAM_CLASSIFIER_JAR_PATH, 
					new File(outputDirectory, "asdf.jar").getPath()));
			classifierAnnotator.classifySequence(Collections.singletonList(InstanceFactory.createInstance("hello", 1, 1)));
			fail("expected exception for invalid classifier name");
		} catch (ResourceInitializationException e) {}
	}

	@Test
	public void testStringClassifierStringAnnotator() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new StringTestBuilder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(outputDirectory);

		CleartkSequentialAnnotator<String> classifierAnnotator = new StringTestAnnotator();
		classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
				CleartkSequentialAnnotator.PARAM_CLASSIFIER_JAR_PATH,
				new File(outputDirectory, "model.jar").getPath()));
		classifierAnnotator.classifySequence(Collections.singletonList(InstanceFactory.createInstance("hello", 1, 1)));
	}

	@Test
	public void testIntegerClassifierStringAnnotator() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new IntegerTestBuilder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		try {
			new StringTestAnnotator().initialize(UimaContextFactory.createUimaContext(
					CleartkSequentialAnnotator.PARAM_CLASSIFIER_JAR_PATH,
					new File(outputDirectory, "model.jar").getPath()));
			fail("expected exception for Integer classifier and String annotator");
		} catch (ResourceInitializationException e) {}
	}


	@Test
	public void testChildClassifierParentAnnotator() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new ChildTestBuilder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		CleartkSequentialAnnotator<Parent> classifierAnnotator = new ParentTestAnnotator();
		classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
				CleartkSequentialAnnotator.PARAM_CLASSIFIER_JAR_PATH,
				new File(outputDirectory, "model.jar").getPath()));
	}

	@Test
	public void testParentClassifierChildAnnotator() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new ParentTestBuilder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		try {
			new ChildTestAnnotator().initialize(UimaContextFactory.createUimaContext(
					CleartkSequentialAnnotator.PARAM_CLASSIFIER_JAR_PATH,
					new File(outputDirectory, "model.jar").getPath()));
			fail("expected exception for Parent classifier and Child annotator");
		} catch (ResourceInitializationException e) {}
	}

	@Test
	public void testGenericClassifierGenericAnnotator() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new TestBuilder<Object>());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		CleartkSequentialAnnotator<Object> classifierAnnotator = new TestAnnotator<Object>();
		classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
				CleartkSequentialAnnotator.PARAM_CLASSIFIER_JAR_PATH,
				new File(outputDirectory, "model.jar").getPath()));
	}

	public static class TestAnnotator<T> extends CleartkSequentialAnnotator<T> {
		@Override
		public void process(JCas aJCas) throws AnalysisEngineProcessException {}
	}
	
	public static class TestBuilder<T> implements ClassifierBuilder<T>, SequentialClassifier<T> {
		public TestBuilder() {}
		public TestBuilder(JarFile modelFile) {}
		@SuppressWarnings("unchecked")
		public Class<? extends SequentialClassifier<T>> getClassifierClass() {
			return (Class<? extends SequentialClassifier<T>>) this.getClass();
		}
		public void train(File dir, String[] args) throws Exception {}
		public void buildJar(File dir, String[] args) throws Exception {
			BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
			stream.close();
		}
		public List<T> classifySequence(List<List<Feature>> features) throws CleartkException {
			assertEquals(1, features.size());
			assertEquals(1, features.get(0).size());
			return null;
		}
		public List<ScoredOutcome<List<T>>> scoreSequence(List<List<Feature>> features, int maxResults) throws CleartkException {
			return null;
		}
	}
	
	public class Parent {}
	public class Child extends Parent {}
	
	public static class StringTestAnnotator extends TestAnnotator<String> {}
	public static class IntegerTestAnnotator extends TestAnnotator<Integer> {}
	public static class ParentTestAnnotator extends TestAnnotator<Parent> {}
	public static class ChildTestAnnotator extends TestAnnotator<Child> {}

	public static class StringTestBuilder extends TestBuilder<String> {
		public StringTestBuilder() {}
		public StringTestBuilder(JarFile modelFile) {}
	}
	public static class IntegerTestBuilder extends TestBuilder<Integer> {
		public IntegerTestBuilder() {}
		public IntegerTestBuilder(JarFile modelFile) {}
	}
	public static class ParentTestBuilder extends TestBuilder<Parent> {
		public ParentTestBuilder() {}
		public ParentTestBuilder(JarFile modelFile) {}
	}
	public static class ChildTestBuilder extends TestBuilder<Child> {
		public ChildTestBuilder() {}
		public ChildTestBuilder(JarFile modelFile) {}
	}
	
	public static class StringTestDataWriterFactory implements DataWriterFactory<String> {
		public static StringTestDataWriter WRITER = new StringTestDataWriter();
		public DataWriter<String> createDataWriter() throws IOException {
			return WRITER;
		}
	}
	public static class StringTestDataWriter implements DataWriter<String> {
		public static boolean isFinished = false;
		public static int instanceCount = 0;
		public void write(Instance<String> instance) throws CleartkException {
			++instanceCount;
		}
		public void finish() throws CleartkException {
			isFinished = true;
		}
		public Class<? extends ClassifierBuilder<String>> getDefaultClassifierBuilderClass() {
			return null;
		}
	}
	public static class StringTestClassifier implements SequentialClassifier<String> {
		public static int classifications = 0;
		public static int scores = 0;
		public List<String> classifySequence(List<List<Feature>> features) throws CleartkException {
			++classifications;
			return null;
		}
		public List<ScoredOutcome<List<String>>> scoreSequence(List<List<Feature>> features, int maxResults) throws CleartkException {
			++scores;
			return null;
		}
		
	}
	public static class StaticClassifierStringTestBuilder extends TestBuilder<String> {
		@Override
		public Class<? extends SequentialClassifier<String>> getClassifierClass() {
			return StringTestClassifier.class;
		}
	}
}
