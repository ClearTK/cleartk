/*
 * Copyright (c) 2007-2013, Regents of the University of Colorado 
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
package org.cleartk.ml.opennlp.maxent;

import java.io.File;
import java.io.IOException;

import org.cleartk.ml.Classifier;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.Instance;
import org.cleartk.ml.jar.EncodingDirectoryDataWriter;
import org.cleartk.ml.opennlp.maxent.encoder.ContextValues;
import org.cleartk.ml.opennlp.maxent.encoder.ContextValuesFeaturesEncoder;

import opennlp.tools.ml.model.RealValueFileEventStream;

/**
 * <br>
 * Copyright (c) 2007-2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * Each line of the training data contains a label/result for the instance followed by a string
 * representation of each feature. Models trained with data generated by this class should use
 * RealValueFileEventStream. For relevant discussion, please see
 * <a href="https://sourceforge.net/forum/forum.php?thread_id=1925312&forum_id=18385">this forum
 * post</a>.
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 * @see RealValueFileEventStream
 */
public abstract class MaxentDataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE extends MaxentClassifierBuilder_ImplBase<? extends MaxentClassifier_ImplBase<OUTCOME_TYPE>, OUTCOME_TYPE>, OUTCOME_TYPE>
        extends
        EncodingDirectoryDataWriter<CLASSIFIER_BUILDER_TYPE, Classifier<OUTCOME_TYPE>, ContextValues, OUTCOME_TYPE, String>
        implements DataWriter<OUTCOME_TYPE> {

  public MaxentDataWriter_ImplBase(File outputDirectory) throws IOException {
    super(outputDirectory);
    this.setFeaturesEncoder(new ContextValuesFeaturesEncoder());
  }

  @Override
  public void write(Instance<OUTCOME_TYPE> instance) throws CleartkProcessingException {
    if (instance.getOutcome() == null) {
      throw CleartkProcessingException.noInstanceOutcome(instance.getFeatures());
    }
    String outcome = this.classifierBuilder.getOutcomeEncoder().encode(instance.getOutcome());
    ContextValues contextValues = this.classifierBuilder.getFeaturesEncoder()
            .encodeAll(instance.getFeatures());
    this.trainingDataWriter.printf("%s %s\n", outcome, contextValues.toMaxentString());
  }
}
