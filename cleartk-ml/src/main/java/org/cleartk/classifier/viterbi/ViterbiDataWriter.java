/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.viterbi;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cleartk.CleartkException;
import org.cleartk.CleartkRuntimeException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriter;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.ReflectionUtil.TypeArgumentDelegator;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 */

public class ViterbiDataWriter<OUTCOME_TYPE> extends
    DirectoryDataWriter<ViterbiClassifierBuilder<OUTCOME_TYPE>, ViterbiClassifier<OUTCOME_TYPE>>
    implements SequentialDataWriter<OUTCOME_TYPE>, TypeArgumentDelegator {

  public ViterbiDataWriter(File outputDirectory, OutcomeFeatureExtractor outcomeFeatureExtractors[]) {
    super(outputDirectory);
    this.outcomeFeatureExtractors = outcomeFeatureExtractors;
    this.classifierBuilder.setOutcomeFeatureExtractors(this.outcomeFeatureExtractors);
  }

  @Override
  protected ViterbiClassifierBuilder<OUTCOME_TYPE> newClassifierBuilder() {
    return new ViterbiClassifierBuilder<OUTCOME_TYPE>();
  }

  public void setDelegatedDataWriter(DataWriter<OUTCOME_TYPE> delegatedDataWriter) {
    this.delegatedDataWriter = delegatedDataWriter;
  }

  public void writeSequence(List<Instance<OUTCOME_TYPE>> instances) throws CleartkException {
    if (this.delegatedDataWriter == null)
      throw new CleartkException("delegatedDataWriter must be set before calling writeSequence");

    List<Object> outcomes = new ArrayList<Object>();
    for (Instance<OUTCOME_TYPE> instance : instances) {
      List<Feature> instanceFeatures = instance.getFeatures();
      for (OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
        instanceFeatures.addAll(outcomeFeatureExtractor.extractFeatures(outcomes));
      }
      outcomes.add(instance.getOutcome());
      delegatedDataWriter.write(instance);
    }

  }

  public void finish() throws CleartkException {
    if (this.delegatedDataWriter == null)
      throw new CleartkException("delegatedDataWriter must be set before calling finish");

    this.delegatedDataWriter.finish();
    super.finish();
  }

  public Map<String, Type> getTypeArguments(Class<?> genericType) {
    if (this.delegatedDataWriter == null)
      throw new CleartkRuntimeException(
          "delegatedDataWriter must be set before calling getTypeArguments");

    if (genericType.equals(SequentialDataWriter.class)) {
      genericType = DataWriter.class;
    }
    return ReflectionUtil.getTypeArguments(genericType, this.delegatedDataWriter);
  }

  protected File outputDirectory;

  protected OutcomeFeatureExtractor outcomeFeatureExtractors[];

  protected DataWriter<OUTCOME_TYPE> delegatedDataWriter = null;
}
