/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public abstract class GenericJarClassifierFactory<CLASSIFIER_TYPE> implements Initializable {

  public static final String PARAM_CLASSIFIER_JAR_PATH = ConfigurationParameterFactory
      .createConfigurationParameterName(GenericJarClassifierFactory.class, "classifierJarPath");

  @ConfigurationParameter(mandatory = true, description = "provides the path to the jar file that should be used to instantiate the classifier.")
  private String classifierJarPath;

  public void setClassifierJarPath(String classifierJarPath) {
    this.classifierJarPath = classifierJarPath;
  }

  public void initialize(UimaContext context) throws ResourceInitializationException {
    ConfigurationParameterInitializer.initialize(this, context);
  }

  public CLASSIFIER_TYPE createClassifier() throws IOException {
    InputStream stream;
    try {
      stream = new URL(this.classifierJarPath).openStream();
    } catch (MalformedURLException e) {
      stream = new FileInputStream(this.classifierJarPath);
    }
    stream = new BufferedInputStream(stream);
    JarInputStream modelStream = new JarInputStream(stream);
    JarClassifierBuilder<?> builder = JarClassifierBuilder.fromManifest(modelStream.getManifest());
    try {
      return this.getClassifierClass().cast(builder.loadClassifier(modelStream));
    } finally {
      stream.close();
    }
  }

  protected abstract Class<CLASSIFIER_TYPE> getClassifierClass();
}
