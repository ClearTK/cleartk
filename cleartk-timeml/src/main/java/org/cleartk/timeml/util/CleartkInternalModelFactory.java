/*
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
package org.cleartk.timeml.util;

import java.io.File;
import java.net.URL;
import java.util.MissingResourceException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.SequenceDataWriter;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.apache.uima.fit.factory.ResourceCreationSpecifierFactory;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class CleartkInternalModelFactory {

  public abstract AnalysisEngineDescription getBaseDescription()
      throws ResourceInitializationException;

  public abstract Class<?> getAnnotatorClass();

  public abstract Class<?> getDataWriterClass();

  public File getTrainingDirectory() {
    String path = this.getAnnotatorClass().getName().toLowerCase().replace('.', '/');
    return new File("src/main/resources/" + path);
  }

  public URL getClassifierJarURL() {
    String dirName = getAnnotatorClass().getSimpleName().toLowerCase();
    File resourceFile = JarClassifierBuilder.getModelJarFile(dirName);
    String resourceName = resourceFile.getPath().replaceAll("\\\\", "/");
    URL url = this.getAnnotatorClass().getResource(resourceName);
    if (url == null) {
      String className = this.getAnnotatorClass().getName();
      String format = "No classifier jar found at \"%s\" for class %s";
      String message = String.format(format, resourceName, className);
      throw new MissingResourceException(message, className, resourceName);
    }
    return url;
  }

  public AnalysisEngineDescription getWriterDescription(File outputDirectory)
      throws ResourceInitializationException {
    Class<?> dataWriterClass = this.getDataWriterClass();
    String paramName;
    if (SequenceDataWriter.class.isAssignableFrom(dataWriterClass)) {
      paramName = DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME;
    } else if (DataWriter.class.isAssignableFrom(dataWriterClass)) {
      paramName = DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME;
    } else {
      throw new RuntimeException("Invalid data writer class: " + dataWriterClass);
    }
    AnalysisEngineDescription desc = getBaseDescription();
    ResourceCreationSpecifierFactory.setConfigurationParameters(
        desc,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory.getPath(),
        paramName,
        dataWriterClass.getName());
    return desc;
  }

  public AnalysisEngineDescription getWriterDescription() throws ResourceInitializationException {
    return getWriterDescription(this.getTrainingDirectory());
  }

  public AnalysisEngineDescription getAnnotatorDescription(String modelFileName)
      throws ResourceInitializationException {
    AnalysisEngineDescription desc = getBaseDescription();
    ResourceCreationSpecifierFactory.setConfigurationParameters(
        desc,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        modelFileName);
    return desc;
  }

  public AnalysisEngineDescription getAnnotatorDescription() throws ResourceInitializationException {
    return getAnnotatorDescription(this.getClassifierJarURL().toString());
  }
}
