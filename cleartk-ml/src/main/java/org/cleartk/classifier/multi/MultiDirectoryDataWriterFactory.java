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

package org.cleartk.classifier.multi;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.util.ReflectionUtil;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * This factory delegates creation of {@link DataWriter} creation to a {@link DataWriterFactory} of
 * class {@link DirectoryDataWriterFactory} or one of its subclasses.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 */
public class MultiDirectoryDataWriterFactory<OUTCOME_TYPE> implements
    MultiDataWriterFactory<OUTCOME_TYPE>, Initializable {

  public static final String PARAM_OUTPUT_DIRECTORY = ConfigurationParameterFactory
      .createConfigurationParameterName(MultiDirectoryDataWriterFactory.class, "outputDirectory");

  @ConfigurationParameter(mandatory = false, description = "provides the name of the directory where the training data will be written.  If you do not set this parameter, then you must call setOutputDirectory directly.")
  protected File outputDirectory;

  public static final String PARAM_DATA_WRITER_FACTORY_CLASS_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(
          MultiDirectoryDataWriterFactory.class,
          "dataWriterFactoryClassName");

  @ConfigurationParameter(mandatory = false, description = "Provides the full name of the DataWriterFactory class to be used.  CleartkMultiAnnotator assumes this is a DirectoryDataWriter or subclass")
  private String dataWriterFactoryClassName;

  private DataWriterFactory<?> dataWriterFactory;

  private UimaContext uimaContext;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    // Initialize configuration parameters
    ConfigurationParameterInitializer.initialize(this, context);

    // create the factory and instantiate the data writer
    this.uimaContext = context;
    this.dataWriterFactory = InitializableFactory.create(
        context,
        dataWriterFactoryClassName,
        DataWriterFactory.class);

  }

  @Override
  public DataWriter<OUTCOME_TYPE> createDataWriter(String name) throws IOException {

    File dataWriterPath = new File(this.outputDirectory, name);
    DataWriter<?> untypedDataWriter;
    try {
      // Set path for dataWriterFactory
      // The following code assumes the factory is a DirectoryDataWriterFactory or subclass
      DirectoryDataWriterFactory dirDataWriterFactory = ReflectionUtil
          .uncheckedCast(dataWriterFactory);
      dirDataWriterFactory.setOutputDirectory(dataWriterPath);
      // / Create the dataWriter
      untypedDataWriter = dataWriterFactory.createDataWriter();

      InitializableFactory.initialize(untypedDataWriter, this.uimaContext);
      DataWriter<OUTCOME_TYPE> dataWriter = ReflectionUtil.uncheckedCast(untypedDataWriter);

      return dataWriter;
    } catch (ResourceInitializationException e) {
      throw new IOException(e);
    }

  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

}
