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
import java.io.FileNotFoundException;

import org.cleartk.ml.Classifier;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.Instance;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;

/**
 * Superclass for {@link DataWriter} implementations that write training data to a file using
 * {@link FeaturesEncoder}s and {@link OutcomeEncoder}s.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public abstract class DataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE extends EncodingJarClassifierBuilder<? extends Classifier<OUTCOME_TYPE>, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
    extends
    EncodingDirectoryDataWriter<CLASSIFIER_BUILDER_TYPE, Classifier<OUTCOME_TYPE>, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
    implements DataWriter<OUTCOME_TYPE> {

  public DataWriter_ImplBase(File outputDirectory) throws FileNotFoundException {
    super(outputDirectory);
  }

  public void write(Instance<OUTCOME_TYPE> instance) throws CleartkProcessingException {
    writeEncoded(
        this.classifierBuilder.getFeaturesEncoder().encodeAll(instance.getFeatures()),
        this.classifierBuilder.getOutcomeEncoder().encode(instance.getOutcome()));
  }

  /**
   * Write the encoded features and encoded outcome to the output directory.
   * 
   * @param features
   *          The encoded features.
   * @param outcome
   *          The encoded outcome.
   */
  protected abstract void writeEncoded(ENCODED_FEATURES_TYPE features, ENCODED_OUTCOME_TYPE outcome)
      throws CleartkProcessingException;

}
