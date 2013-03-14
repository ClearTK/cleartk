/*
 * Copyright (c) 2013, Regents of the University of Colorado 
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
package org.cleartk.classifier.jar;

import java.io.File;

import org.cleartk.classifier.Instance;
import org.cleartk.classifier.baseline.MostFrequentStringClassifierBuilder;
import org.cleartk.classifier.baseline.MostFrequentStringDataWriter;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.UimaContextFactory;

/**
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class ClassifierFactoryTest extends DefaultTestBase {

  @Test
  public void testJarClassifierFactory() throws Exception {
    JarClassifierFactory<String> factory = new JarClassifierFactory<String>();

    // train and package a simple classifier
    MostFrequentStringDataWriter writer = new MostFrequentStringDataWriter(this.outputDirectory);
    writer.write(new Instance<String>("X"));
    writer.finish();
    MostFrequentStringClassifierBuilder builder = writer.getClassifierBuilder();
    builder.trainClassifier(this.outputDirectory);
    builder.packageClassifier(this.outputDirectory);

    // move the classifier to the classpath
    File modelDir = new File("target/test-classes");
    File modelFile = JarClassifierBuilder.getModelJarFile(modelDir);
    JarClassifierBuilder.getModelJarFile(this.outputDirectory).renameTo(modelFile);
    try {

      // test File
      factory.initialize(UimaContextFactory.createUimaContext(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          modelFile));
      factory.createClassifier();

      // test URI
      factory.initialize(UimaContextFactory.createUimaContext(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          modelFile.toURI()));
      factory.createClassifier();

      // test classpath
      factory.initialize(UimaContextFactory.createUimaContext(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          "/model.jar"));
      factory.createClassifier();

    } finally {
      Assert.assertTrue(modelFile.delete());
    }
  }
}
