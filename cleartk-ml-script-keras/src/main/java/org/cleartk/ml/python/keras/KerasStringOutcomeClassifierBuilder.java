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
package org.cleartk.ml.python.keras;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.uima.util.Level;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.script.ScriptStringOutcomeClassifierBuilder;

import com.google.common.annotations.Beta;
import com.google.common.io.Files;

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
public class KerasStringOutcomeClassifierBuilder extends
    ScriptStringOutcomeClassifierBuilder<KerasStringOutcomeClassifier> {

  @Override
  public void packageClassifier(File dir, JarOutputStream modelStream)
      throws IOException {
    super.packageClassifier(dir, modelStream);

    JarStreams.putNextJarEntry(modelStream, "outcome-lookup.txt", new File(dir,
        "outcome-lookup.txt"));

    int modelNum = 0;
    while (true) {
      File modelArchFile = new File(dir, getArchFilename(modelNum));
      File modelWeightsFile = new File(dir, getWeightsFilename(modelNum));
      if (!modelArchFile.exists())
        break;

      JarStreams.putNextJarEntry(modelStream, modelArchFile.getName(),
          modelArchFile.getAbsoluteFile());
      JarStreams.putNextJarEntry(modelStream, modelWeightsFile.getName(),
          modelWeightsFile.getAbsoluteFile());
      modelNum++;
    }
  }

  @Override
  protected void unpackageClassifier(JarInputStream modelStream)
      throws IOException {
    super.unpackageClassifier(modelStream);

    // create the model dir to unpack all the model files
    this.modelDir = Files.createTempDir();

    // grab the script dir from the manifest:
    this.scriptDir = new File(modelStream.getManifest().getMainAttributes()
        .getValue(SCRIPT_DIR_PARAM));

    extractFileToDir(modelDir, modelStream, "outcome-lookup.txt");

    int modelNum = 0;
    while (true) {
      String archFn = getArchFilename(modelNum);
      String wtsFn = getWeightsFilename(modelNum);

      try {
        if (!extractFileToDir(modelDir, modelStream, archFn))
          break;
        if (!extractFileToDir(modelDir, modelStream, wtsFn))
          break;
      } catch (IOException e) {
        logger.log(Level.WARNING,
            "Encountered the following exception: " + e.getMessage());
        break;
      }
      modelNum++;
    }
  }

  @Override
  protected KerasStringOutcomeClassifier newClassifier() {
    return new KerasStringOutcomeClassifier(this.featuresEncoder,
        this.outcomeEncoder, this.modelDir, this.scriptDir);
  }

  private static String getArchFilename(int num) {
    return "model_" + num + ".json";
  }

  private static String getWeightsFilename(int num) {
    return "model_" + num + ".h5";
  }
}
