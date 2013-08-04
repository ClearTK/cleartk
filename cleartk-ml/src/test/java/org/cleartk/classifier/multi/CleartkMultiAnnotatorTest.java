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

package org.cleartk.classifier.multi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ClassifierFactory;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.test.DefaultStringTestDataWriterFactory;
import org.cleartk.classifier.util.InstanceFactory;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.UimaContextFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 */

public class CleartkMultiAnnotatorTest extends DefaultTestBase {

  @Test
  public void testIsTraining() {
    assertFalse(new StringTestAnnotator().isTraining());
  }

  @Test
  public void testBadName() throws Throwable {
    try {
      StringTestAnnotator multiClassifierAnnotator = new StringTestAnnotator();
      multiClassifierAnnotator.initialize(UimaContextFactory.createUimaContext(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          outputDirectoryName));
      multiClassifierAnnotator.getClassifier("asdf").classify(
          InstanceFactory.createInstance("hello", 1, 1).getFeatures());
      fail("expected exception for invalid classifier name");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testStringClassifierStringAnnotator() throws Exception {
    StringTestAnnotator multiClassifierAnnotator = new StringTestAnnotator();
    multiClassifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkMultiAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        StringTestClassifierFactory.class.getName()));
    multiClassifierAnnotator.getClassifier("test").classify(
        InstanceFactory.createInstance("hello", 1, 1).getFeatures());
  }

  // This test ensures we can't pass classifier factories whose classifier types do not match the
  // kind of annotation p
  @Test
  public void testIntegerClassifierStringAnnotator() throws Exception {
    try {
      StringTestAnnotator stringTestAnnotator = new StringTestAnnotator();
      stringTestAnnotator.initialize(UimaContextFactory.createUimaContext(
          CleartkMultiAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
          IntegerTestClassifierFactory.class.getName()));
      stringTestAnnotator.getClassifier("NameDoesNotMatter");
      fail("expected exception for Integer classifier and String annotator");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testChildClassifierParentAnnotator() throws Exception {
    CleartkMultiAnnotator<Parent> multiClassifierAnnotator = new ParentTestAnnotator();
    multiClassifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkMultiAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        ChildClassifierFactory.class.getName()));
  }

  @Test
  public void testParentClassifierChildAnnotator() throws Exception {
    try {
      CleartkMultiAnnotator<Child> multiClassifierAnnotator = new ChildTestAnnotator();
      multiClassifierAnnotator.initialize(UimaContextFactory.createUimaContext(
          CleartkMultiAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
          ParentClassifierFactory.class.getName()));
      multiClassifierAnnotator.getClassifier("test");
      fail("expected exception for Parent classifier and Child annotator");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testGenericClassifierGenericAnnotator() throws Exception {
    CleartkMultiAnnotator<Object> classifierAnnotator = new TestMultiAnnotator<Object>();
    classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkMultiAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        TestClassifierFactory.class.getName()));
  }

  public static class TestMultiAnnotator<T> extends CleartkMultiAnnotator<T> {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    }
  }

  @Test
  public void testDescriptor() throws Exception {
    try {
      AnalysisEngineFactory.createPrimitive(
          StringTestAnnotator.class,
          CleartkMultiAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
          DefaultStringTestDataWriterFactory.class.getName());
      Assert.fail("expected exception with missing output directory");
    } catch (ResourceInitializationException e) {
    }

    try {
      AnalysisEngineFactory.createPrimitive(
          StringTestAnnotator.class,
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          outputDirectoryName);
      Assert.fail("expected exception with missing classifier jar");
    } catch (ResourceInitializationException e) {
    }

    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        StringTestAnnotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        InstanceDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName);

    Object outputDir = engine.getConfigParameterValue(DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY);
    Assert.assertEquals(outputDirectoryName, outputDir);

    engine.collectionProcessComplete();

  }

  public static class TestClassifier<T> implements Classifier<T> {

    @Override
    public T classify(List<Feature> features) {
      assertEquals(1, features.size());
      return null;
    }

    @Override
    public Map<T, Double> score(List<Feature> features) throws CleartkProcessingException {
      return null;
    }
  }

  public static class TestClassifierFactory<T> implements ClassifierFactory<T> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Classifier<T> createClassifier() throws IOException {
      return new TestClassifier();
    }

  }

  public class Parent {
  }

  public class Child extends Parent {
  }

  public static class StringTestAnnotator extends TestMultiAnnotator<String> {
  }

  public static class IntegerTestAnnotator extends TestMultiAnnotator<Integer> {
  }

  public static class ParentTestAnnotator extends TestMultiAnnotator<Parent> {
  }

  public static class ChildTestAnnotator extends TestMultiAnnotator<Child> {
  }

  public static class StringTestClassifier extends TestClassifier<String> {
  }

  public static class StringTestClassifierFactory implements ClassifierFactory<String> {
    public Classifier<String> createClassifier() {
      return new StringTestClassifier();
    }
  }

  public static class IntegerTestClassifier extends TestClassifier<Integer> {
  }

  public static class IntegerTestClassifierFactory implements ClassifierFactory<Integer> {
    public Classifier<Integer> createClassifier() {
      return new IntegerTestClassifier();
    }
  }

  public static class ParentClassifier extends TestClassifier<Parent> {
  }

  public static class ParentClassifierFactory implements ClassifierFactory<Parent> {
    public Classifier<Parent> createClassifier() {
      return new ParentClassifier();
    }
  }

  public static class ChildClassifier extends TestClassifier<Child> {
  }

  public static class ChildClassifierFactory implements ClassifierFactory<Child> {
    public Classifier<Child> createClassifier() {
      return new ChildClassifier();
    }
  }

}
