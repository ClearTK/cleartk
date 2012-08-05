/*
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.jar.ClassifierBuilder_ImplBase;
import org.cleartk.classifier.jar.JarStreams;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class SVMlightClassifierBuilder_ImplBase<CLASSIFIER_TYPE extends Classifier<OUTCOME_TYPE>, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
    extends
    ClassifierBuilder_ImplBase<CLASSIFIER_TYPE, FeatureVector, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> {

  static Logger logger = UIMAFramework.getLogger(SVMlightClassifierBuilder_ImplBase.class);

  public static String COMMAND_ARGUMENT = "--executable";

  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.svmlight");
  }

  protected File getModelFile(File dir) {
    return new File(dir, "training-data.svmlight.model");
  }

  public void trainClassifier(File dir, String... args) throws Exception {
    File trainingDataFile = getTrainingDataFile(dir);
    this.trainClassifier(dir, trainingDataFile, args);
  }

  public void trainClassifier(File dir, File trainingDataFile, String... args) throws Exception {
    String executable = "svm_learn";
    if (args.length > 0 && args[0].equals(COMMAND_ARGUMENT)) {
      executable = args[1];
      String[] tempArgs = new String[args.length - 2];
      System.arraycopy(args, 2, tempArgs, 0, tempArgs.length);
      args = tempArgs;
    }

    String[] command = new String[args.length + 3];
    command[0] = executable;
    System.arraycopy(args, 0, command, 1, args.length);
    command[command.length - 2] = trainingDataFile.getPath();
    command[command.length - 1] = trainingDataFile.getPath() + ".model";

    logger.log(
        Level.FINE,
        "training svmlight using the following command: " + Joiner.on(" ").join(command));
    Process process = Runtime.getRuntime().exec(command);
    process.getOutputStream().close();
    ByteStreams.copy(process.getInputStream(), System.out);
    ByteStreams.copy(process.getErrorStream(), System.err);
    process.waitFor();
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, "model.svmlight", getModelFile(dir));
  }

  protected SVMlightModel model;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, "model.svmlight");
    this.model = SVMlightModel.fromInputStream(modelStream);
  }
}
