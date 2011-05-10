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
package org.cleartk.classifier.tksvmlight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.jar.ClassifierBuilder_ImplBase;
import org.cleartk.classifier.jar.JarStreams;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @version 0.2.1
 * 
 * A class that provided interfaces to train, package and unpackage a SVMTKClassifier into a jar file.
 */

public class TKSVMlightClassifierBuilder extends
    ClassifierBuilder_ImplBase<TKSVMlightClassifier, TreeFeatureVector, Boolean, Boolean> {

  static Logger logger = UIMAFramework.getLogger(TKSVMlightClassifierBuilder.class);

  public static String COMMAND_ARGUMENT = "--executable";

  /**
   * Train a SVMTK classifier. Assumes the executable name is "tk_svm_learn". Note: this is public so
   * the One verse All TK SVMlight classifier may take advantage of it, casual users probably shouldn't use this
   * interface.
   * @param dir The directory where the training data has been written.
   * @param args The arguments to be used by the tk_svm_classify command. Note: -t 5 is used to specify
   * the use of Tree Kernels.
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

    logger.log(Level.INFO, "training with tree kernel svmlight using the following command: "
        + toString(command));
    logger
        .log(
            Level.INFO,
            "if the tree kernel svmlight learner does not seem to be working correctly, then try running the above command directly to see if e.g. svm_learn or svm_perf_learn gives a useful error message.");
    Process process = Runtime.getRuntime().exec(command);
    output(process.getInputStream(), System.out);
    output(process.getErrorStream(), System.err);
    process.waitFor();
  }

  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.svmlight");
  }

  private File getModelFile(File dir) {
    return new File(dir, "training-data.svmlight.model");
  }

  /**
   * Train this SVMTK classifier.
   * @param dir The directory where the training data has been written.
   * @param args The arguments to be used by the tk_svm_classify command. Note: -t 5 is used to specify
   * the use of Tree Kernels.
   */
  public void trainClassifier(File dir, String... args) throws Exception {
    File trainingDataFile = getTrainingDataFile(dir);
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

  private File modelFile;

  /**
   * unpackage the model files found in a JarInputStream.
   */
  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, "model.svmlight");
    this.modelFile = File.createTempFile("model", ".svmlight");
    BufferedWriter out = new BufferedWriter(new FileWriter(this.modelFile));
    BufferedReader in = new BufferedReader(new InputStreamReader(modelStream));
    String line;
    while ((line = in.readLine()) != null) {
      out.append(line);
      out.append("\n");
    }
    out.close();    
  }

  /**
   * Create a TKSVMlightClassifier.
   */
  @Override
  protected TKSVMlightClassifier newClassifier() {
    return new TKSVMlightClassifier(
        this.featuresEncoder,
        this.outcomeEncoder,
        this.modelFile);
  }

  private static String toString(String[] command) {
    StringBuilder sb = new StringBuilder();
    for (String cmmnd : command) {
      sb.append(cmmnd + " ");
    }
    return sb.toString();
  }

  private static void output(InputStream input, PrintStream output) throws IOException {
    byte[] buffer = new byte[128];
    int count = input.read(buffer);
    while (count != -1) {
      output.write(buffer, 0, count);
      count = input.read(buffer);
    }
  }

}
