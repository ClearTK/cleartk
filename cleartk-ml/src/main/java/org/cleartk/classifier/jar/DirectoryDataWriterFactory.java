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

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.SequenceDataWriterFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.Initializable;

/**
 * Superclass for {@link DataWriterFactory} and {@link SequenceDataWriterFactory} implementations
 * that use {@link FeaturesEncoder}s and {@link OutcomeEncoder}s.
 * 
 * Note that it does not declare that it implements either of the DataWriterFactory interfaces.
 * Subclasses should do this.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public abstract class DirectoryDataWriterFactory implements Initializable {

  public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

  @ConfigurationParameter(
      name = PARAM_OUTPUT_DIRECTORY,
      mandatory = true,
      description = "provides the name of the directory where the "
          + "training data will be written.  if you do not set this "
          + "parameter, then you must call setOutputDirectory directly.")
  protected File outputDirectory;

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    ConfigurationParameterInitializer.initialize(this, context);
  }
}
