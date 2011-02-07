/** 
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
package org.cleartk.classifier.multi.jar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.multi.MultiClassifierFactory;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class JarMultiClassifierFactory<OUTCOME_TYPE> implements
    MultiClassifierFactory<OUTCOME_TYPE>, Initializable {

  public static final String PARAM_CLASSIFIER_JAR_DIR = ConfigurationParameterFactory
      .createConfigurationParameterName(JarMultiClassifierFactory.class, "classifierJarDir");

  @ConfigurationParameter(mandatory = true, description = "the directory where the jar files to instantiate the classifier reside.")
  private String classifierJarDir;

  public void setClassifierJarDir(String classifierJarDir) {
    this.classifierJarDir = classifierJarDir;
  }

  public void initialize(UimaContext context) throws ResourceInitializationException {
    ConfigurationParameterInitializer.initialize(this, context);
  }

  @SuppressWarnings("unchecked")
  public Classifier<OUTCOME_TYPE> createClassifier(String name) throws IOException {
    return this.createUntypedClassifier(Classifier.class, name);
  }

  private <CLASSIFIER_TYPE> CLASSIFIER_TYPE createUntypedClassifier(
      Class<CLASSIFIER_TYPE> superClass,
      String name) throws IOException {
    InputStream stream;
    // Build a path to the classifier jar file -- this is the same convention used by the dataWriter
    File dir = new File(this.classifierJarDir, name);
    File classifierJarPath = new File(dir.getPath(), "model.jar"); // FIXME: see issue #227
    try {
      stream = new URL(classifierJarPath.getPath()).openStream();
    } catch (MalformedURLException e) {
      stream = new FileInputStream(classifierJarPath.getPath());
    }
    stream = new BufferedInputStream(stream);
    JarInputStream modelStream = new JarInputStream(stream);
    JarClassifierBuilder<?> builder = JarClassifierBuilder.fromManifest(modelStream.getManifest());
    try {
      return superClass.cast(builder.loadClassifier(modelStream));
    } finally {
      stream.close();
    }
  }
}
