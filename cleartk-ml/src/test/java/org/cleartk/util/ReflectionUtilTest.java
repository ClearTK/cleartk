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

import java.io.File;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;

import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.jar.ClassifierBuilder_ImplBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */
public class ReflectionUtilTest {

  public static class TestSuperClass<T> {
  }

  public static class TestSubClass extends TestSuperClass<String> {
  }

  public static class TestArraySubClass extends TestSuperClass<double[]> {
  }

  public static class TestClassifierOutcomeType implements Classifier<String> {
    public String classify(List<org.cleartk.classifier.Feature> features) {
      return null;
    }

    public List<ScoredOutcome<String>> score(
        List<org.cleartk.classifier.Feature> features,
        int maxResults) {
      return null;
    }
  }

  public static class TestClassifierBuilderType extends
      ClassifierBuilder_ImplBase<TestClassifierOutcomeType, Boolean, String, Double> {

    @Override
    public void trainClassifier(File dir, String... args) {
      // Do nothing
    }

    @Override
    protected TestClassifierOutcomeType newClassifier() {
      return null;
    }

    @Override
    public File getTrainingDataFile(File dir) {
      return null;
    }

  }

  @Test
  public void testGetTypeArgument() throws Exception {

    Type type = ReflectionUtil.getTypeArgument(TestSuperClass.class, "T", new TestSubClass());
    Assert.assertEquals(String.class, type);

    type = ReflectionUtil.getTypeArgument(TestSuperClass.class, "T", new TestSuperClass<String>());
    Assert.assertNull(type);

    type = ReflectionUtil.getTypeArgument(TestSuperClass.class, "T", new TestArraySubClass());
    // Java6 bug - double[] is improperly returned as a generic array
    if (type instanceof GenericArrayType) {
      Assert.assertEquals(double.class, ((GenericArrayType) type).getGenericComponentType());
    }
    // Java7 (correct) behavior
    else {
      Assert.assertEquals(double[].class, type);
    }

    type = ReflectionUtil.getTypeArgument(
        Classifier.class,
        "OUTCOME_TYPE",
        new TestClassifierOutcomeType());
    Assert.assertEquals(String.class, type);

    type = ReflectionUtil.getTypeArgument(
        ClassifierBuilder_ImplBase.class,
        "OUTCOME_TYPE",
        new TestClassifierBuilderType());
    Assert.assertEquals(String.class, type);

    type = ReflectionUtil.getTypeArgument(
        ClassifierBuilder_ImplBase.class,
        "ENCODED_OUTCOME_TYPE",
        new TestClassifierBuilderType());
    Assert.assertEquals(Double.class, type);
  }
}
