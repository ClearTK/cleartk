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
package org.cleartk.classifier.svmlight.rank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.svmlight.SvmLightClassifierBuilder_ImplBase;

import com.google.common.base.Joiner;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 *         Trains an svm_light ranking model.
 */
public class SvmLightRankBuilder extends
    SvmLightClassifierBuilder_ImplBase<SvmLightRank, Double, Double> {

  static Logger logger = UIMAFramework.getLogger(SvmLightClassifierBuilder_ImplBase.class);

  @Override
  public void trainClassifier(File dir, String... args) throws Exception {
    File trainingDataFile = getTrainingDataFile(dir);
    this.trainClassifier(dir, trainingDataFile, args);
  }

  public void trainClassifier(File dir, File trainingDataFile, String... args) throws Exception {
    String executable = "svm_rank_learn";
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

    logger.log(Level.FINE, "training svmlight rank using the following command: "
        + Joiner.on(" ").join(command));
    Process process = Runtime.getRuntime().exec(command);
    process.getOutputStream().close();
    output(process.getInputStream(), System.out);
    output(process.getErrorStream(), System.err);
    process.waitFor();
  }

  @Override
  protected SvmLightRank newClassifier() {
    return new SvmLightRank(this.featuresEncoder, this.outcomeEncoder, this.model);
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
