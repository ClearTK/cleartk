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
import java.io.FileNotFoundException;
import java.util.List;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequenceClassifier;
import org.cleartk.classifier.SequenceDataWriter;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

/**
 * Superclass for {@link SequenceDataWriter} implementations that write training data to a file
 * using {@link FeaturesEncoder}s and {@link OutcomeEncoder}s.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public abstract class SequenceDataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE extends EncodingJarClassifierBuilder<? extends SequenceClassifier<OUTCOME_TYPE>, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
    extends
    EncodingDirectoryDataWriter<CLASSIFIER_BUILDER_TYPE, SequenceClassifier<OUTCOME_TYPE>, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
    implements SequenceDataWriter<OUTCOME_TYPE> {

  public SequenceDataWriter_ImplBase(File outputDirectory) throws FileNotFoundException {
    super(outputDirectory);
  }

  public void write(List<Instance<OUTCOME_TYPE>> instances) throws CleartkProcessingException {
    for (Instance<OUTCOME_TYPE> instance : instances) {
      writeEncoded(
          this.classifierBuilder.getFeaturesEncoder().encodeAll(instance.getFeatures()),
          this.classifierBuilder.getOutcomeEncoder().encode(instance.getOutcome()));
    }
    this.writeEndSequence();
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

  /**
   * Write the marker for the end of a sequence. This will be called at the end of each
   * {@link #writeSequence}, after all instances have been written by {@link #writeEncoded}.
   */
  protected abstract void writeEndSequence();

}
