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

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.multi.jar.JarMultiClassifierFactory;
import org.cleartk.classifier.util.InstanceFactory;
import org.cleartk.test.DefaultTestBase;
import org.junit.Test;
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
          JarMultiClassifierFactory.PARAM_CLASSIFIER_JAR_DIR,
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
        CleartkMultiAnnotator.PARAM_MULTI_CLASSIFIER_FACTORY_CLASS_NAME,
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
          CleartkMultiAnnotator.PARAM_MULTI_CLASSIFIER_FACTORY_CLASS_NAME,
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
        CleartkMultiAnnotator.PARAM_MULTI_CLASSIFIER_FACTORY_CLASS_NAME,
        ChildClassifierFactory.class.getName()));
  }

  @Test
  public void testParentClassifierChildAnnotator() throws Exception {
    try {
      CleartkMultiAnnotator<Child> multiClassifierAnnotator = new ChildTestAnnotator();
      multiClassifierAnnotator.initialize(UimaContextFactory.createUimaContext(
          CleartkMultiAnnotator.PARAM_MULTI_CLASSIFIER_FACTORY_CLASS_NAME,
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
        CleartkMultiAnnotator.PARAM_MULTI_CLASSIFIER_FACTORY_CLASS_NAME,
        TestClassifierFactory.class.getName()));
  }

  public static class TestMultiAnnotator<T> extends CleartkMultiAnnotator<T> {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    }
  }

  public static class TestMultiClassifier<T> implements Classifier<T> {

    public T classify(List<Feature> features) {
      assertEquals(1, features.size());
      return null;
    }

    public List<ScoredOutcome<T>> score(List<Feature> features, int maxResults) {
      return null;
    }
  }

  public static class TestClassifierFactory<T> implements MultiClassifierFactory<T> {

    @SuppressWarnings( { "unchecked", "rawtypes" })
    public Classifier<T> createClassifier(String name) {
      return new TestMultiClassifier();
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

  public static class StringTestClassifier extends TestMultiClassifier<String> {
  }

  public static class StringTestClassifierFactory implements MultiClassifierFactory<String> {
    public Classifier<String> createClassifier(String name) {
      return new StringTestClassifier();
    }
  }

  public static class IntegerTestClassifier extends TestMultiClassifier<Integer> {
  }

  public static class IntegerTestClassifierFactory implements MultiClassifierFactory<Integer> {
    public Classifier<Integer> createClassifier(String name) {
      return new IntegerTestClassifier();
    }
  }

  public static class ParentClassifier extends TestMultiClassifier<Parent> {
  }

  public static class ParentClassifierFactory implements MultiClassifierFactory<Parent> {
    public Classifier<Parent> createClassifier(String name) {
      return new ParentClassifier();
    }
  }

  public static class ChildClassifier extends TestMultiClassifier<Child> {
  }

  public static class ChildClassifierFactory implements MultiClassifierFactory<Child> {
    public Classifier<Child> createClassifier(String name) {
      return new ChildClassifier();
    }
  }

}
