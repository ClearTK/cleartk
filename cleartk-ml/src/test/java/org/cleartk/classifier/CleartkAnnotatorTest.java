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
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
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
 * @author Philip Ogren
 * 
 */

public class CleartkAnnotatorTest extends DefaultTestBase {

  @Test
  public void testIsTraining() {
    assertFalse(new StringTestAnnotator().isTraining());
  }

  @Test
  public void testBadFileName() throws CleartkException {
    try {
      CleartkAnnotator<String> classifierAnnotator = new StringTestAnnotator();
      classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          new File(outputDirectoryName, "asdf.jar").getPath()));
      classifierAnnotator.classifier.classify(InstanceFactory
          .createInstance("hello", 1, 1)
          .getFeatures());
      fail("expected exception for invalid classifier name");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testStringClassifierStringAnnotator() throws Exception {
    CleartkAnnotator<String> classifierAnnotator = new StringTestAnnotator();
    classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        StringTestClassifierFactory.class.getName()));
    classifierAnnotator.classifier.classify(InstanceFactory
        .createInstance("hello", 1, 1)
        .getFeatures());
  }

  @Test
  public void testIntegerClassifierStringAnnotator() throws Exception {
    try {
      new StringTestAnnotator().initialize(UimaContextFactory.createUimaContext(
          CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
          IntegerTestClassifierFactory.class.getName()));
      fail("expected exception for Integer classifier and String annotator");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testChildClassifierParentAnnotator() throws Exception {
    CleartkAnnotator<Parent> classifierAnnotator = new ParentTestAnnotator();
    classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        ChildClassifierFactory.class.getName()));
  }

  @Test
  public void testParentClassifierChildAnnotator() throws Exception {
    try {
      new ChildTestAnnotator().initialize(UimaContextFactory.createUimaContext(
          CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
          ParentClassifierFactory.class.getName()));
      fail("expected exception for Parent classifier and Child annotator");
    } catch (ResourceInitializationException e) {
    }
  }

  @Test
  public void testGenericClassifierGenericAnnotator() throws Exception {
    CleartkAnnotator<Object> classifierAnnotator = new TestAnnotator<Object>();
    classifierAnnotator.initialize(UimaContextFactory.createUimaContext(
        CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        TestClassifierFactory.class.getName()));
  }

  @Test
  public void testDescriptor() throws UIMAException {
    try {
      AnalysisEngineFactory.createPrimitive(
          StringTestAnnotator.class,
          typeSystemDescription,
          CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
          DefaultStringTestDataWriterFactory.class.getName());
      Assert.fail("expected exception with missing output directory");
    } catch (ResourceInitializationException e) {
    }

    try {
      AnalysisEngineFactory.createPrimitive(
          StringTestAnnotator.class,
          typeSystemDescription,
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          outputDirectoryName);
      Assert.fail("expected exception with missing classifier jar");
    } catch (ResourceInitializationException e) {
    }

    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        StringTestAnnotator.class,
        typeSystemDescription,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultStringTestDataWriterFactory.class.getName());

    Object dataWriter = engine
        .getConfigParameterValue(CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME);
    Assert.assertEquals(DefaultStringTestDataWriterFactory.class.getName(), dataWriter);

    Object outputDir = engine
        .getConfigParameterValue(DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY);
    Assert.assertEquals(outputDirectoryName, outputDir);

    engine.collectionProcessComplete();
  }

  public static class TestAnnotator<T> extends CleartkAnnotator<T> {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    }
  }

  public static class TestClassifier<T> implements Classifier<T> {

    public T classify(List<Feature> features) throws CleartkException {
      assertEquals(1, features.size());
      return null;
    }

    public List<ScoredOutcome<T>> score(List<Feature> features, int maxResults)
        throws CleartkException {
      return null;
    }
  }

  public static class TestClassifierFactory<T> implements ClassifierFactory<T> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Classifier<T> createClassifier() throws IOException, CleartkException {
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

  public static class StringTestClassifier extends TestClassifier<String> {
  }

  public static class StringTestClassifierFactory implements ClassifierFactory<String> {
    public Classifier<String> createClassifier() throws IOException, CleartkException {
      return new StringTestClassifier();
    }
  }

  public static class IntegerTestClassifier extends TestClassifier<Integer> {
  }

  public static class IntegerTestClassifierFactory implements ClassifierFactory<Integer> {
    public Classifier<Integer> createClassifier() throws IOException, CleartkException {
      return new IntegerTestClassifier();
    }
  }

  public static class ParentClassifier extends TestClassifier<Parent> {
  }

  public static class ParentClassifierFactory implements ClassifierFactory<Parent> {
    public Classifier<Parent> createClassifier() throws IOException, CleartkException {
      return new ParentClassifier();
    }
  }

  public static class ChildClassifier extends TestClassifier<Child> {
  }

  public static class ChildClassifierFactory implements ClassifierFactory<Child> {
    public Classifier<Child> createClassifier() throws IOException, CleartkException {
      return new ChildClassifier();
    }
  }

}
