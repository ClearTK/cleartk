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
package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.jar.BuildJar;
import org.cleartk.classifier.jar.ClassifierBuilder;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class BinaryLIBSVMClassifierBuilder implements ClassifierBuilder<Boolean> {

  public String getCommand() {
    return "svm-train";
  }

  public String getModelName() {
    return LIBSVMClassifier.MODEL_NAME;
  }

  public void train(File dir, String[] args) throws Exception {
    String[] command = new String[args.length + 3];
    command[0] = this.getCommand();
    System.arraycopy(args, 0, command, 1, args.length);
    command[command.length - 2] = new File(dir, "training-data.libsvm").getPath();
    command[command.length - 1] = new File(dir, this.getModelName()).getPath();
    Process process = Runtime.getRuntime().exec(command);
    output(process.getInputStream(), System.out);
    output(process.getErrorStream(), System.err);
    process.waitFor();
  }

  public void buildJar(File dir, String[] args) throws Exception {
    BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
    stream.write(this.getModelName(), new File(dir, this.getModelName()));
    stream.close();
  }

  public Class<? extends Classifier<Boolean>> getClassifierClass() {
    return BinaryLIBSVMClassifier.class;
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
