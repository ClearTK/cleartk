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
package org.cleartk.ml.svmlight.rank;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Instance;
import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.outcome.DoubleToDoubleOutcomeEncoder;
import org.cleartk.ml.svmlight.SvmLightDataWriter_ImplBase;
import org.cleartk.ml.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 *         Writes the training file for SVMlight's ranking classifier. Because ranking requires an
 *         additional item/query id (qid), instances passed into the write() method should be of
 *         type QidInstance.
 * 
 */
public class SvmLightRankDataWriter extends
    SvmLightDataWriter_ImplBase<SvmLightRankBuilder, Double, Double> {

  public SvmLightRankDataWriter(File outputDirectory) throws IOException {
    super(outputDirectory);
    this.setOutcomeEncoder(new DoubleToDoubleOutcomeEncoder());
    this.instQidToEncodedQidMap = new HashMap<String, Integer>();
    this.lastEncodedQid = 0;
  }

  @Override
  protected String outcomeToString(Double outcome) {
    return outcome.toString();
  }

  @Override
  public void write(Instance<Double> instance) throws CleartkProcessingException {
    if (!(instance instanceof QidInstance)) {
      throw new CleartkProcessingException("", "Unable to write non-QidInstance");
    }
    String qid = ((QidInstance<Double>) instance).getQid();

    writeEncoded(
        this.getEncodedQid(qid),
        this.classifierBuilder.getFeaturesEncoder().encodeAll(instance.getFeatures()),
        this.classifierBuilder.getOutcomeEncoder().encode(instance.getOutcome()));
  }

  @Override
  protected SvmLightRankBuilder newClassifierBuilder() {
    return new SvmLightRankBuilder();
  }

  public void writeEncoded(int qid, FeatureVector features, Double outcome)
      throws CleartkProcessingException {

    StringBuffer output = new StringBuffer();

    // The following two lines should be the only difference between this and
    // SVMLightDataWriter_ImplBase.writeEncoded()
    output.append(this.outcomeToString(outcome));
    output.append(String.format(Locale.US, " qid:%d", qid));

    for (FeatureVector.Entry entry : features) {
      if (Double.isInfinite(entry.value) || Double.isNaN(entry.value))
        throw CleartkEncoderException.invalidFeatureVectorValue(entry.index, entry.value);
      output.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
    }

    this.trainingDataWriter.println(output);

  }

  private int getEncodedQid(String qid) {
    if (this.instQidToEncodedQidMap.containsKey(qid)) {
      return this.instQidToEncodedQidMap.get(qid);
    } else {
      this.lastEncodedQid++;
      this.instQidToEncodedQidMap.put(qid, lastEncodedQid);
      return lastEncodedQid;
    }
  }

  private Map<String, Integer> instQidToEncodedQidMap;

  private int lastEncodedQid;

}
