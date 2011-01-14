/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier.opennlp;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;

import opennlp.model.MaxentModel;
import opennlp.maxent.io.BinaryGISModelReader;

import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.jar.JarClassifier;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public abstract class MaxentClassifier_ImplBase<OUTCOME_TYPE> extends
        JarClassifier<OUTCOME_TYPE, String, List<NameNumber>> {

  protected MaxentModel model;

  public MaxentClassifier_ImplBase(JarFile modelFile) throws IOException {
    super(modelFile);
    ZipEntry modelEntry = modelFile.getEntry("model.maxent");
    this.model = new BinaryGISModelReader(new DataInputStream(new GZIPInputStream(
            modelFile.getInputStream(modelEntry)))).getModel();
  }

  public OUTCOME_TYPE classify(List<Feature> features) throws CleartkException {
    EvalParams evalParams = convertToEvalParams(features);
    String encodedOutcome = this.model.getBestOutcome(this.model.eval(evalParams.getContext(),
            evalParams.getValues()));
    return outcomeEncoder.decode(encodedOutcome);
  }

  @Override
  public List<ScoredOutcome<OUTCOME_TYPE>> score(List<Feature> features, int maxResults)
          throws CleartkException {
    EvalParams evalParams = convertToEvalParams(features);
    double[] evalResults = this.model.eval(evalParams.getContext(), evalParams.getValues());
    String[] encodedOutcomes = (String[]) this.model.getDataStructures()[2];

    List<ScoredOutcome<OUTCOME_TYPE>> returnValues = new ArrayList<ScoredOutcome<OUTCOME_TYPE>>();

    if (maxResults == 1) {
      String bestOutcome = this.model.getBestOutcome(evalResults);
      OUTCOME_TYPE encodedBestOutcome = outcomeEncoder.decode(bestOutcome);
      double bestResult = evalResults[this.model.getIndex(bestOutcome)];
      returnValues.add(new ScoredOutcome<OUTCOME_TYPE>(encodedBestOutcome, bestResult));
      return returnValues;
    }

    for (int i = 0; i < evalResults.length; i++) {
      returnValues.add(new ScoredOutcome<OUTCOME_TYPE>(outcomeEncoder.decode(encodedOutcomes[i]),
              evalResults[i]));
    }

    Collections.sort(returnValues);
    if (returnValues.size() > maxResults) {
      return returnValues.subList(0, maxResults);
    }

    return returnValues;
  }

  protected EvalParams convertToEvalParams(List<Feature> features) throws CleartkException {

    List<NameNumber> contexts = featuresEncoder.encodeAll(features);

    String[] context = new String[contexts.size()];
    float[] values = new float[contexts.size()];

    for (int i = 0; i < contexts.size(); i++) {
      NameNumber contextValue = contexts.get(i);
      context[i] = contextValue.name;
      values[i] = contextValue.number.floatValue();
    }

    return new EvalParams(context, values);
  }

  public class EvalParams {
    private String[] context;

    private float[] values;

    public String[] getContext() {
      return context;
    }

    public float[] getValues() {
      return values;
    }

    public EvalParams(String[] context, float[] values) {
      this.context = context;
      this.values = values;
    }

  }

}
