/*
 * Copyright (c) 2016, Regents of the University of Colorado 
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
package org.cleartk.ml.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Logger;
import org.cleartk.ml.jar.ClassifierBuilder_ImplBase;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.script.util.StreamHandlerThread;
import org.cleartk.ml.util.featurevector.FeatureVector;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2016, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Tim Miller
 * @version 2.0.1
 * 
 */
@Beta
public abstract class ScriptStringOutcomeClassifierBuilder<T extends ScriptStringOutcomeClassifier>
    extends ClassifierBuilder_ImplBase<T, FeatureVector, String, Integer> {
  public static final Attributes.Name SCRIPT_DIR_PARAM = new Attributes.Name(
      "ScriptDirectory");
  protected static final Logger logger = UIMAFramework
      .getLogger(ScriptStringOutcomeClassifierBuilder.class);

  protected File modelDir = null;
  protected File scriptDir = null;

  public void setScriptDirectory(String scriptDir) {
    Attributes atts = this.manifest.getMainAttributes();
    atts.put(SCRIPT_DIR_PARAM, scriptDir);
  }

  @Override
  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.libsvm");
  }

  @Override
  public void trainClassifier(File dir, String... args) throws Exception {
    // args[0] should be path to directory with model training code:
    // args[1-] is the set of arguments the program takes.
    // dir is by convention the first argument that the training script takes.

    if (this.scriptDir == null) {
      this.scriptDir = new File(this.manifest.getMainAttributes().getValue(
          SCRIPT_DIR_PARAM));
    }
    // first find the train script:
    File trainScript = null;

    for (File file : this.scriptDir.listFiles()) {
      if (file.getName().startsWith("train.")) {
        if (trainScript != null) {
          throw new RuntimeException("There are multiple files named train.*");
        }
        trainScript = file;
      }
    }
    if (trainScript == null)
      throw new RuntimeException(
          "ERROR: Train directory does not contain any scripts named train.*");
    StringBuilder cmdArgs = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      cmdArgs.append(args[i]);
      cmdArgs.append(' ');
    }
    String arg2 = "";
    if (cmdArgs.length() > 0) {
      arg2 = cmdArgs.substring(0, cmdArgs.length() - 1);
    }
    Process p = Runtime.getRuntime().exec(
        new String[] { trainScript.getAbsolutePath(), dir.getAbsolutePath(),
            arg2 });

    StreamHandlerThread inHandler = new StreamHandlerThread(p.getInputStream(),
        logger);
    inHandler.start();

    StreamHandlerThread errHandler = new StreamHandlerThread(
        p.getErrorStream(), logger);
    errHandler.start();

    int ret = p.waitFor();
    if (ret != 0) {
      throw new RuntimeException();
    }
  }

  protected static boolean extractFileToDir(File dir,
      JarInputStream modelStream, String fn) throws IOException {
    JarEntry entry = JarStreams.getNextJarEntry(modelStream, fn);
    if (entry == null) {
      return false;
    }
    File outFile = new File(dir, fn);
    try (FileOutputStream fos = new FileOutputStream(outFile)) {
      byte[] byteArray = new byte[1024];
      int i;
      while ((i = modelStream.read(byteArray)) > 0) {
        // Write the bytes to the output stream
        fos.write(byteArray, 0, i);
      }
    }
    return true;
  }

}
