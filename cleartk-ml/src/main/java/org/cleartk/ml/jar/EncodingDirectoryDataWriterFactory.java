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
package org.cleartk.ml.jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.DataWriterFactory;
import org.cleartk.ml.SequenceDataWriterFactory;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.CleartkInitializationException;
import org.cleartk.util.ReflectionUtil;
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
public abstract class EncodingDirectoryDataWriterFactory<ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
    extends DirectoryDataWriterFactory implements Initializable {

  public static final String PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM = "loadEncodersFromFileSystem";

  @ConfigurationParameter(
      name = PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM,
      mandatory = false,
      description = "when true indicates that the FeaturesEncoder and "
          + "OutcomeEncoder should be loaded from the file system "
          + "instead of being created by the DataWriterFactory",
      defaultValue = "false")
  private boolean loadEncodersFromFileSystem = false;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    ConfigurationParameterInitializer.initialize(this, context);
    if (loadEncodersFromFileSystem) {
      try {
        File encoderFile = new File(outputDirectory, FeaturesEncoder_ImplBase.ENCODERS_FILE_NAME);

        if (!encoderFile.exists()) {
          throw CleartkInitializationException.fileNotFound(encoderFile);
        }

        ObjectInputStream is = new ObjectInputStream(new FileInputStream(encoderFile));

        // read the FeaturesEncoder and check the types
        FeaturesEncoder<?> untypedFeaturesEncoder = FeaturesEncoder.class.cast(is.readObject());
        ReflectionUtil.checkTypeParameterIsAssignable(
            FeaturesEncoder.class,
            "ENCODED_FEATURES_TYPE",
            untypedFeaturesEncoder,
            EncodingDirectoryDataWriterFactory.class,
            "ENCODED_FEATURES_TYPE",
            this);

        // read the OutcomeEncoder and check the types
        OutcomeEncoder<?, ?> untypedOutcomeEncoder = OutcomeEncoder.class.cast(is.readObject());
        ReflectionUtil.checkTypeParameterIsAssignable(
            OutcomeEncoder.class,
            "OUTCOME_TYPE",
            untypedOutcomeEncoder,
            EncodingDirectoryDataWriterFactory.class,
            "OUTCOME_TYPE",
            this);
        ReflectionUtil.checkTypeParameterIsAssignable(
            OutcomeEncoder.class,
            "ENCODED_OUTCOME_TYPE",
            untypedOutcomeEncoder,
            EncodingDirectoryDataWriterFactory.class,
            "ENCODED_OUTCOME_TYPE",
            this);

        // assign the encoders to the instance variables
        this.featuresEncoder = ReflectionUtil.uncheckedCast(untypedFeaturesEncoder);
        this.outcomeEncoder = ReflectionUtil.uncheckedCast(untypedOutcomeEncoder);
        is.close();
      } catch (Exception e) {
        throw new ResourceInitializationException(e);
      }
    }
  }

  protected boolean setEncodersFromFileSystem(
      EncodingDirectoryDataWriter<?, ?, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> dataWriter) {
    if (this.featuresEncoder != null && this.outcomeEncoder != null) {
      dataWriter.setFeaturesEncoder(this.featuresEncoder);
      dataWriter.setOutcomeEncoder(this.outcomeEncoder);
      return true;
    }
    return false;
  }

  protected FeaturesEncoder<ENCODED_FEATURES_TYPE> featuresEncoder = null;

  protected OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> outcomeEncoder = null;

}
