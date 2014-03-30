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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.TreeMap;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.encoder.features.BooleanEncoder;
import org.cleartk.ml.encoder.features.NumberEncoder;
import org.cleartk.ml.encoder.features.StringEncoder;
import org.cleartk.ml.encoder.outcome.StringToIntegerOutcomeEncoder;
import org.cleartk.ml.jar.DataWriter_ImplBase;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @author Tim Miller
 */

public abstract class TreeKernelSvmStringOutcomeDataWriter<CLASSIFIER_BUILDER_TYPE extends TreeKernelSvmStringOutcomeClassifierBuilder>
    extends
    DataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE, TreeFeatureVector, String, Integer> {

  protected File allFalseFile;

  protected PrintWriter allFalseWriter;

  protected Map<Integer, PrintWriter> trainingDataWriters;

  public TreeKernelSvmStringOutcomeDataWriter(File outputDirectory) throws FileNotFoundException {
    super(outputDirectory);
    TreeFeatureVectorFeaturesEncoder myFeaturesEncoder = new TreeFeatureVectorFeaturesEncoder();
    myFeaturesEncoder.addEncoder(new NumberEncoder());
    myFeaturesEncoder.addEncoder(new BooleanEncoder());
    myFeaturesEncoder.addEncoder(new StringEncoder());
    this.setFeaturesEncoder(myFeaturesEncoder);
    this.setOutcomeEncoder(new StringToIntegerOutcomeEncoder());

    // aliases to make it easy to remember what the "main" file is being used for
    allFalseFile = this.trainingDataFile;
    allFalseWriter = this.trainingDataWriter;

    // create the output writers
    trainingDataWriters = new TreeMap<Integer, PrintWriter>();
  }

  @Override
  protected void writeEncoded(TreeFeatureVector features, Integer outcome)
      throws CleartkProcessingException {
    if (outcome != null && !trainingDataWriters.containsKey(outcome)) {
      try {
        addClass(outcome);
      } catch (IOException e) {
        throw new CleartkProcessingException(e);
      }
    }

    StringBuffer featureString = new StringBuffer();
    featureString.append(TreeKernelSvmBooleanOutcomeDataWriter.createString(features));

    StringBuffer output = new StringBuffer();
    if (outcome == null)
      output.append("0");
    else
      output.append("-1");
    output.append(featureString);
    allFalseWriter.println(output);

    for (int i : trainingDataWriters.keySet()) {
      output = new StringBuffer();
      if (outcome == null)
        output.append("0");
      else if (outcome == i)
        output.append("+1");
      else
        output.append("-1");

      output.append(featureString);
      trainingDataWriters.get(i).println(output);
    }
  }

  /**
   * Callback called after the last feature vector has been written. Flushes the various streams and
   * deletes the all false file.
   */
  @Override
  public void finish() throws CleartkProcessingException {
    // close and remove all-false file
    allFalseWriter.close();
    allFalseFile.delete();

    // flush and close all training data writers
    for (PrintWriter pw : trainingDataWriters.values()) {
      pw.flush();
      pw.close();
    }

    // finish in the superclass
    super.finish();
  }

  private void addClass(int label) throws IOException {
    File newTDFile = this.classifierBuilder.getTrainingDataFile(this.outputDirectory, label);
    newTDFile.delete();

    allFalseWriter.flush();
    copyFile(allFalseFile, newTDFile);
    trainingDataWriters.put(label, new PrintWriter(new BufferedWriter(new FileWriter(
        newTDFile,
        true))));
  }

  private void copyFile(File source, File target) throws IOException {
    // Create channel on the source
    FileInputStream srcStream = new FileInputStream(source);
    FileChannel srcChannel = srcStream.getChannel();

    // Create channel on the destination
    FileOutputStream dstStream = new FileOutputStream(target);
    FileChannel dstChannel = dstStream.getChannel();

    // Copy file contents from source to destination
    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

    // Close the channels
    srcStream.close();
    srcChannel.close();
    dstStream.close();
    dstChannel.close();
  }
}
