/** 
 * Copyright (c) 2009-2010, Regents of the University of Colorado 
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
package org.cleartk.ml.viterbi;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.resource.ConfigurationManager;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.DataWriterFactory;
import org.cleartk.ml.SequenceDataWriter;
import org.cleartk.ml.SequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;

/**
 * <br>
 * Copyright (c) 2009-2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren, Philipp Wetzler
 * 
 */

public class ViterbiDataWriterFactory<OUTCOME_TYPE> extends DirectoryDataWriterFactory implements
    SequenceDataWriterFactory<OUTCOME_TYPE>, Initializable {

  public static final String PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES = "outcomeFeatureExtractorNames";

  @ConfigurationParameter(
      name = PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
      mandatory = false,
      description = "An optional, multi-valued, string parameter that "
          + "specifies which OutcomeFeatureExtractors should be used. "
          + "Each value of this parameter should be the name of a "
          + "class that implements OutcomeFeatureExtractor. One valid "
          + "value that you might use is "
          + "org.cleartk.ml.feature.extractor.outcome.DefaultOutcomeFeatureExtractor")
  protected String outcomeFeatureExtractorNames[];

  public static final String PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS = "delegatedDataWriterFactoryClass";

  @ConfigurationParameter(
      name = PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS,
      mandatory = true,
      defaultValue = "org.cleartk.ml.jar.DefaultDataWriterFactory",
      description = "A single, required, string parameter that provides "
          + "the full name of the DataWriterFactory class that will be " + "wrapped.")
  protected String delegatedDataWriterFactoryClass;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    ConfigurationParameterInitializer.initialize(this, context);

    OutcomeFeatureExtractor outcomeFeatureExtractors[];
    if (outcomeFeatureExtractorNames == null) {
      outcomeFeatureExtractors = new OutcomeFeatureExtractor[0];
    } else {
      outcomeFeatureExtractors = new OutcomeFeatureExtractor[outcomeFeatureExtractorNames.length];
      for (int i = 0; i < outcomeFeatureExtractorNames.length; i++) {
        outcomeFeatureExtractors[i] = InitializableFactory.create(
            context,
            outcomeFeatureExtractorNames[i],
            OutcomeFeatureExtractor.class);
      }
    }

    dataWriter = new ViterbiDataWriter<OUTCOME_TYPE>(outputDirectory, outcomeFeatureExtractors);

    // set the output directory parameter to the delegated directory
    UimaContextAdmin contextAdmin = (UimaContextAdmin) context;
    ConfigurationManager manager = contextAdmin.getConfigurationManager();
    ViterbiClassifierBuilder<OUTCOME_TYPE> builder = dataWriter.getClassifierBuilder();
    File delegatedDir = builder.getDelegatedModelDirectory(this.outputDirectory);
    manager.setConfigParameterValue(contextAdmin.getQualifiedContextName()
        + DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, delegatedDir.getPath());

    // initialize the delegated data writer
    try {
      DataWriterFactory<OUTCOME_TYPE> delegatedDataWriterFactory = createDelegatedDataWriterFactory(
          delegatedDataWriterFactoryClass,
          context);
      DataWriter<OUTCOME_TYPE> delegatedDataWriter = delegatedDataWriterFactory.createDataWriter();
      dataWriter.setDelegatedDataWriter(delegatedDataWriter);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

    // restore the output directory parameter
    finally {
      manager.setConfigParameterValue(contextAdmin.getQualifiedContextName()
          + DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, this.outputDirectory.getPath());
    }
  }

  public SequenceDataWriter<OUTCOME_TYPE> createDataWriter() {
    return dataWriter;
  }

  @SuppressWarnings("unchecked")
  private DataWriterFactory<OUTCOME_TYPE> createDelegatedDataWriterFactory(
      String delegatedDataWriterFactoryClassName,
      UimaContext context) throws ResourceInitializationException {
    return InitializableFactory.create(
        context,
        delegatedDataWriterFactoryClassName,
        DataWriterFactory.class);
  }

  private ViterbiDataWriter<OUTCOME_TYPE> dataWriter;
}
