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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
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

public class ClassifierAnnotatorTest {

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
	public void testExpectsOutcomes() {
		assertFalse(new ClassifierAnnotator<String>().expectsOutcomes());
	}

	@Test
	public void testDescriptor() throws Exception {
		ResourceInitializationException rie = null;
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.classifier.ClassifierAnnotator");
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNotNull(rie);

		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new Test1Builder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		AnalysisEngineFactory
				.createAnalysisEngine("org.cleartk.classifier.ClassifierAnnotator",
						ClassifierAnnotator.PARAM_ANNOTATION_HANDLER, Test1Handler.class.getName(),
						ClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, BuildJar.MODEL_FILE_NAME)
								.getPath());

	}

	@Test
	public void testBadFileName() throws CleartkException {
		IOException ioe = null;
		try {
			ClassifierAnnotator<String> classifierAnnotator = new ClassifierAnnotator<String>();
			classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
					ClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, "asdf.jar").getPath(),
					ClassifierAnnotator.PARAM_ANNOTATION_HANDLER, Test1Handler.class.getName()));
			classifierAnnotator.consume(InstanceFactory.createInstance("hello", 1, 1));
		}
		catch (ResourceInitializationException e) {
			ioe = (IOException) e.getCause();
		}
		assertNotNull(ioe);

	}

	@Test
	public void test1() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new Test1Builder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		ClassifierAnnotator<String> classifierAnnotator = new ClassifierAnnotator<String>();

		ResourceInitializationException rie = null;

		try {
			classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
					ClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, "model.jar").getPath(),
					ClassifierAnnotator.PARAM_ANNOTATION_HANDLER, Test1Handler.class.getName()));
			classifierAnnotator.consume(InstanceFactory.createInstance("hello", 1, 1));
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNull(rie);
	}

	public static class Test1Classifier implements Classifier<String> {

		public Test1Classifier(JarFile modelFile) throws IOException {
		}

		public String classify(List<Feature> features) throws CleartkException {
			assertEquals(1, features.size());
			return null;
		}

		public List<ScoredOutcome<String>> score(List<Feature> features, int maxResults) throws CleartkException {
			return null;
		}
	}

	public static class Test1Handler implements AnnotationHandler<String> {

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException,
				CleartkException {
		}

	}

	public static class Test1Builder implements ClassifierBuilder<String> {

		public void buildJar(File dir, String[] args) throws Exception {
			BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
			stream.close();
		}

		public Class<? extends Classifier<String>> getClassifierClass() {
			return Test1Classifier.class;
		}

		public void train(File dir, String[] args) throws Exception {

		}

	}

	@Test
	public void test2() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new Test2Builder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		ClassifierAnnotator<String> classifierAnnotator = new ClassifierAnnotator<String>();

		ResourceInitializationException rie = null;

		try {
			classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
					ClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, "model.jar").getPath(),
					ClassifierAnnotator.PARAM_ANNOTATION_HANDLER, Test2Handler.class.getName()));
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNotNull(rie);
	}

	public static class Test2Classifier implements Classifier<String> {

		public Test2Classifier(JarFile modelFile) throws IOException {
		}

		public String classify(List<Feature> features) throws CleartkException {
			assertEquals(1, features.size());
			return null;
		}

		public List<ScoredOutcome<String>> score(List<Feature> features, int maxResults) throws CleartkException {
			return null;
		}
	}

	public static class Test2Handler implements AnnotationHandler<Integer> {

		public void process(JCas cas, InstanceConsumer<Integer> consumer) throws AnalysisEngineProcessException,
				CleartkException {
		}

	}

	public static class Test2Builder implements ClassifierBuilder<String> {

		public void buildJar(File dir, String[] args) throws Exception {
			BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
			stream.close();
		}

		public Class<? extends Classifier<String>> getClassifierClass() {
			return Test2Classifier.class;
		}

		public void train(File dir, String[] args) throws Exception {

		}

	}

	@Test
	public void test3() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new Test3Builder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		ClassifierAnnotator<String> classifierAnnotator = new ClassifierAnnotator<String>();

		ResourceInitializationException rie = null;

		try {
			classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
					ClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, "model.jar").getPath(),
					ClassifierAnnotator.PARAM_ANNOTATION_HANDLER, Test3Handler.class.getName()));
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNull(rie);
	}

	public class A {

	}

	public class B extends A {

	}

	public static class Test3Classifier implements Classifier<B> {

		public Test3Classifier(JarFile modelFile) throws IOException {
		}

		public B classify(List<Feature> features) throws CleartkException {
			return null;
		}

		public List<ScoredOutcome<B>> score(List<Feature> features, int maxResults) throws CleartkException {
			return null;
		}
	}

	public static class Test3Handler implements AnnotationHandler<A> {

		public void process(JCas cas, InstanceConsumer<A> consumer) throws AnalysisEngineProcessException,
				CleartkException {
		}

	}

	public static class Test3Builder implements ClassifierBuilder<B> {

		public void buildJar(File dir, String[] args) throws Exception {
			BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
			stream.close();
		}

		public Class<? extends Classifier<B>> getClassifierClass() {
			return Test3Classifier.class;
		}

		public void train(File dir, String[] args) throws Exception {
		}

	}

	@Test
	public void test4() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new Test4Builder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		ClassifierAnnotator<String> classifierAnnotator = new ClassifierAnnotator<String>();

		ResourceInitializationException rie = null;

		try {
			classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
					ClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, "model.jar").getPath(),
					ClassifierAnnotator.PARAM_ANNOTATION_HANDLER, Test4Handler.class.getName()));
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNotNull(rie);
	}

	public static class Test4Classifier implements Classifier<A> {

		public Test4Classifier(JarFile modelFile) throws IOException {
		}

		public A classify(List<Feature> features) throws CleartkException {
			return null;
		}

		public List<ScoredOutcome<A>> score(List<Feature> features, int maxResults) throws CleartkException {
			return null;
		}
	}

	public static class Test4Handler implements AnnotationHandler<B> {

		public void process(JCas cas, InstanceConsumer<B> consumer) throws AnalysisEngineProcessException,
				CleartkException {
		}

	}

	public static class Test4Builder implements ClassifierBuilder<A> {

		public void buildJar(File dir, String[] args) throws Exception {
			BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
			stream.close();
		}

		public Class<? extends Classifier<A>> getClassifierClass() {
			return Test4Classifier.class;
		}

		public void train(File dir, String[] args) throws Exception {
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void test5() throws Exception {
		ClassifierManifest manifest = new ClassifierManifest();
		manifest.setClassifierBuilder(new Test5Builder());
		manifest.write(new File(outputDirectory));
		BuildJar.main(new String[] { outputDirectory });

		ClassifierAnnotator<String> classifierAnnotator = new ClassifierAnnotator<String>();

		ResourceInitializationException rie = null;

		try {
			classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
					ClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, "model.jar").getPath(),
					ClassifierAnnotator.PARAM_ANNOTATION_HANDLER, Test5Handler.class.getName()));
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNull(rie);
	}

	public static class Test5Classifier<OUTCOME_TYPE> implements Classifier<OUTCOME_TYPE> {

		public Test5Classifier(JarFile modelFile) throws IOException {
		}

		public OUTCOME_TYPE classify(List<Feature> features) throws CleartkException {
			return null;
		}

		public List<ScoredOutcome<OUTCOME_TYPE>> score(List<Feature> features, int maxResults) throws CleartkException {
			return null;
		}
	}

	public static class Test5Handler<T> implements AnnotationHandler<T> {

		public void process(JCas cas, InstanceConsumer<T> consumer) throws AnalysisEngineProcessException,
				CleartkException {
		}

	}

	public static class Test5Builder<OUTCOME_TYPE> implements ClassifierBuilder<OUTCOME_TYPE> {

		public void buildJar(File dir, String[] args) throws Exception {
			BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
			stream.close();
		}

		public Class<?> getClassifierClass() {
			return Test5Classifier.class;
		}

		public void train(File dir, String[] args) throws Exception {
		}

	}

}
