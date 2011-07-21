/* 
 * Copyright (c) 2009-2011, Regents of the University of Colorado 
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
package org.cleartk.classifier.svmlight;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.encoder.CleartkEncoderException;
import org.cleartk.classifier.jar.DataWriter_ImplBase;
import org.cleartk.classifier.jar.EncodingJarClassifierBuilder;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2009-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class SVMlightDataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE extends EncodingJarClassifierBuilder<? extends Classifier<OUTCOME_TYPE>, FeatureVector, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
    extends
    DataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE, FeatureVector, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> {

  public SVMlightDataWriter_ImplBase(File outputDirectory) throws IOException {
    super(outputDirectory);
  }

  protected abstract String outcomeToString(ENCODED_OUTCOME_TYPE outcome);

  @Override
  public void writeEncoded(FeatureVector features, ENCODED_OUTCOME_TYPE outcome)
      throws CleartkProcessingException {
    StringBuffer output = new StringBuffer();

    output.append(this.outcomeToString(outcome));

    for (FeatureVector.Entry entry : features) {
      if (Double.isInfinite(entry.value) || Double.isNaN(entry.value))
        throw CleartkEncoderException.invalidFeatureVectorValue(entry.index, entry.value);
      output.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
    }

    this.trainingDataWriter.println(output);
  }
}
