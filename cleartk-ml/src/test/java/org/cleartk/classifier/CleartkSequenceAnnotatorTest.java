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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.util.InstanceFactory;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;
import org.apache.uima.fit.factory.UimaContextFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class CleartkSequenceAnnotatorTest extends DefaultTestBase {

  @Test
  public void testIsTraining() throws Throwable {
    StringTestAnnotator annotator = new StringTestAnnotator();
    assertFalse(annotator.isTraining());

    annotator.initialize(UimaContextFactory.createUimaContext(
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        StringDataWriter.class.getName()));
    assertTrue(annotator.isTraining());

    annotator.initialize(UimaContextFactory.createUimaContext(
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        StringDataWriter.class.getName(),
        CleartkSequenceAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        StringTestClassifierFactory.class.getName(),
        CleartkSequenceAnnotator.PARAM_IS_TRAINING,
        false));
    assertFalse(annotator.isTraining());

    annotator.initialize(UimaContextFactory.createUimaContext(
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        StringDataWriter.class.getName(),
        CleartkSequenceAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        StringTestClassifierFactory.class.getName(),
        CleartkSequenceAnnotator.PARAM_IS_TRAINING,
        true));
    assertTrue(annotator.isTraining());
  }

  @Test
  public void testBadFileName() throws Throwable {
    try {
      CleartkSequenceAnnotator<String> classifierAnnotator = new StringTestAnnotator();
      classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          new File(outputDirectoryName, "asdf.jar").getPath()));
      classifierAnnotator.classify(Collections.singletonList(InstanceFactory.createInstance(
          "hello",
          1,
          1)));
      fail("expected exception for invalid classifier name");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testStringClassifierStringAnnotator() throws Exception {
    CleartkSequenceAnnotator<String> classifierAnnotator = new StringTestAnnotator();
    classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkSequenceAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        StringTestClassifierFactory.class.getName()));
    classifierAnnotator.classify(Collections.singletonList(InstanceFactory.createInstance(
        "hello",
        1,
        1)));
  }

  @Test
  public void testIntegerClassifierStringAnnotator() throws Exception {
    try {
      new StringTestAnnotator().initialize(UimaContextFactory.createUimaContext(
          CleartkSequenceAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
          IntegerTestClassifierFactory.class.getName()));
      fail("expected exception for Integer classifier and String annotator");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testChildClassifierParentAnnotator() throws Exception {
    CleartkSequenceAnnotator<Parent> classifierAnnotator = new ParentTestAnnotator();
    classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkSequenceAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        ChildTestClassifierFactory.class.getName()));
  }

  @Test
  public void testParentClassifierChildAnnotator() throws Exception {
    try {
      new ChildTestAnnotator().initialize(UimaContextFactory.createUimaContext(
          CleartkSequenceAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
          ParentTestClassifierFactory.class.getName()));
      fail("expected exception for Parent classifier and Child annotator");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testGenericClassifierGenericAnnotator() throws Exception {
    CleartkSequenceAnnotator<Object> classifierAnnotator = new TestAnnotator<Object>();
    classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkSequenceAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        TestClassifierFactory.class.getName()));
  }

  public static class TestAnnotator<T> extends CleartkSequenceAnnotator<T> {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    }
  }

  public static class TestClassifier<T> implements SequenceClassifier<T> {
    @Override
    public List<T> classify(List<List<Feature>> features) {
      assertEquals(1, features.size());
      assertEquals(1, features.get(0).size());
      return null;
    }

    @Override
    public List<Map<T, Double>> score(List<List<Feature>> features)
        throws CleartkProcessingException {
      return null;
    }
  }

  public static class TestClassifierFactory<T> implements SequenceClassifierFactory<T> {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SequenceClassifier<T> createClassifier() {
      return new TestClassifier();
    }
  }

  public class Parent {
  }

  public class Child extends Parent {
  }

  public static class StringTestAnnotator extends TestAnnotator<String> {
  }

  public static class IntegerTestAnnotator extends TestAnnotator<Integer> {
  }

  public static class ParentTestAnnotator extends TestAnnotator<Parent> {
  }

  public static class ChildTestAnnotator extends TestAnnotator<Child> {
  }

  public static class StringTestBuilder extends TestClassifier<String> {
  }

  public static class StringTestClassifierFactory implements SequenceClassifierFactory<String> {
    public SequenceClassifier<String> createClassifier() {
      return new StringTestBuilder();
    }
  }

  public static class IntegerTestBuilder extends TestClassifier<Integer> {
  }

  public static class IntegerTestClassifierFactory implements SequenceClassifierFactory<Integer> {
    public SequenceClassifier<Integer> createClassifier() {
      return new IntegerTestBuilder();
    }
  }

  public static class ParentTestBuilder extends TestClassifier<Parent> {
  }

  public static class ParentTestClassifierFactory implements SequenceClassifierFactory<Parent> {
    public SequenceClassifier<Parent> createClassifier() {
      return new ParentTestBuilder();
    }
  }

  public static class ChildTestBuilder extends TestClassifier<Child> {
  }

  public static class ChildTestClassifierFactory implements SequenceClassifierFactory<Child> {
    public SequenceClassifier<Child> createClassifier() {
      return new ChildTestBuilder();
    }
  }

  public static class StringDataWriter implements SequenceDataWriterFactory<String> {
    public SequenceDataWriter<String> createDataWriter() throws IOException {
      return null;
    }
  }
}
