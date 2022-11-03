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
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.tksvmlight.model.TreeKernelSvmModel;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;

/**
 * A class that provided interfaces to train, package and unpackage a
 * {@link TreeKernelSvmBooleanOutcomeClassifier} into a jar file.
 * 
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 */
@Beta
public class TkSvmLightBooleanOutcomeClassifierBuilder
    extends
    TreeKernelSvmBooleanOutcomeClassifierBuilder<TreeKernelSvmBooleanOutcomeClassifier>{

  static final Logger logger = UIMAFramework.getLogger(TkSvmLightBooleanOutcomeClassifierBuilder.class);

  public static final String COMMAND_ARGUMENT = "--executable";

  /**
   * Train a SVMTK classifier. Assumes the executable name is "tk_svm_learn". Note: this is public
   * so the One verse All TK SVMlight classifier may take advantage of it, casual users probably
   * shouldn't use this interface.
   * 
   * @param args
   *          The arguments to be used by the tk_svm_classify command. Note: -t 5 is used to specify
   *          the use of Tree Kernels.
   */
  public static void train(String filePath, String[] args) throws Exception {
    String executable = "tk_svm_learn";
    if (args.length > 0 && args[0].equals(COMMAND_ARGUMENT)) {
      executable = args[1];
      String[] tempArgs = new String[args.length - 2];
      System.arraycopy(args, 2, tempArgs, 0, tempArgs.length);
      args = tempArgs;
    }

    String[] command = new String[args.length + 3];
    command[0] = executable;
    System.arraycopy(args, 0, command, 1, args.length);
    command[command.length - 2] = new File(filePath).getPath();
    command[command.length - 1] = new File(filePath + ".model").getPath();

    logger.log(Level.FINE, "training with tree kernel svmlight using the following command: "
        + Joiner.on(" ").join(command));
    Process process = Runtime.getRuntime().exec(command);
    process.getOutputStream().close();
    ByteStreams.copy(process.getInputStream(), System.out);
    ByteStreams.copy(process.getErrorStream(), System.err);
    process.waitFor();
  }

  @Override
public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.svmlight");
  }

  private File getModelFile(File dir) {
    return new File(dir, "training-data.svmlight.model");
  }

  /**
   * Train this SVMTK classifier.
   * 
   * @param dir
   *          The directory where the training data has been written.
   * @param args
   *          The arguments to be used by the tk_svm_classify command. Note: -t 5 is used to specify
   *          the use of Tree Kernels.
   */
  @Override
public void trainClassifier(File dir, String... args) throws Exception {
    File trainingDataFile = (dir.isDirectory() ? getTrainingDataFile(dir) : dir);
    train(trainingDataFile.getPath(), args);
  }

  /**
   * package the classifier found in dir into the a Jar file.
   */
  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, "model.svmlight", getModelFile(dir));
  }

  private TreeKernelSvmModel model;

  /**
   * unpackage the model files found in a JarInputStream.
   */
  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, "model.svmlight");
    model = TreeKernelSvmModel.fromInputStream(modelStream);
  }

  /**
   * Create a TKSVMlightClassifier.
   */
  @Override
  protected TreeKernelSvmBooleanOutcomeClassifier newClassifier() {
    return new TreeKernelSvmBooleanOutcomeClassifier(
        this.featuresEncoder,
        this.outcomeEncoder,
        this.model);
  }
}
