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
package org.cleartk.ml.tksvmlight;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.features.BooleanEncoder;
import org.cleartk.ml.encoder.features.NumberEncoder;
import org.cleartk.ml.encoder.features.StringEncoder;
import org.cleartk.ml.encoder.outcome.BooleanToBooleanOutcomeEncoder;
import org.cleartk.ml.jar.DataWriter_ImplBase;
import org.cleartk.ml.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2007-2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @author Tim Miller
 */
public abstract class TreeKernelSvmBooleanOutcomeDataWriter extends 
  DataWriter_ImplBase<TreeKernelSvmBooleanOutcomeClassifierBuilder<TreeKernelSvmBooleanOutcomeClassifier>, TreeFeatureVector, Boolean,Boolean>{

  public TreeKernelSvmBooleanOutcomeDataWriter(File outputDirectory) throws FileNotFoundException {
    super(outputDirectory);
    TreeFeatureVectorFeaturesEncoder myFeaturesEncoder = new TreeFeatureVectorFeaturesEncoder();
    myFeaturesEncoder.addEncoder(new NumberEncoder());
    myFeaturesEncoder.addEncoder(new BooleanEncoder());
    myFeaturesEncoder.addEncoder(new StringEncoder());
    this.setFeaturesEncoder(myFeaturesEncoder);
    this.setOutcomeEncoder(new BooleanToBooleanOutcomeEncoder());
  }

  /**
   * creates and formats a string (without the outcome) for a specific feature vector. Public so
   * that the one versus all data writer may use it, not for casual use.
   * 
   * @param features
   *          The features to write into a string format.
   * @return The string that represents the features in a format that tk_svm_classify can utilize.
   */
  public static String createString(TreeFeatureVector features) throws CleartkProcessingException {
    StringBuffer output = new StringBuffer();

    for (String tree : features.getTrees().values()) {
      output.append(String.format(Locale.US, " |BT| %s", tree));
    }
    if (!features.getTrees().isEmpty()) {
      output.append(" |ET|");
    }

    for (FeatureVector.Entry entry : features.getFeatures()) {
      if (Double.isInfinite(entry.value) || Double.isNaN(entry.value))
        throw CleartkEncoderException.invalidFeatureVectorValue(entry.index, entry.value);
      output.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
    }

    return output.toString();
  }
  
  /**
   * Write a line representing the feature vector with the appropriate outcome to be used by the
   * tk_svm_classify command.
   * 
   * @param features
   *          The feature vector to be written.
   * @param outcome
   *          The correct classification for that feature vector.
   */
  @Override
  public void writeEncoded(TreeFeatureVector features, Boolean outcome)
      throws CleartkProcessingException {
    StringBuffer output = new StringBuffer();

    if (outcome == null) {
      output.append("0");
    } else if (outcome.booleanValue()) {
      output.append("+1");
    } else {
      output.append("-1");
    }

    output.append(createString(features));

    this.trainingDataWriter.println(output);
  }
}
