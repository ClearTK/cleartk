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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Logger;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;
import org.cleartk.ml.jar.Classifier_ImplBase;
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
public class ScriptStringOutcomeClassifier extends
    Classifier_ImplBase<FeatureVector, String, Integer> {
  File modelDir = null;
  Process classifierProcess = null;
  PrintStream toClassifier = null;
  BufferedReader reader = null;
  Logger logger = UIMAFramework.getLogger(ScriptStringOutcomeClassifier.class);

  public ScriptStringOutcomeClassifier(
      FeaturesEncoder<FeatureVector> featuresEncoder,
      OutcomeEncoder<String, Integer> outcomeEncoder, File modelDir,
      File scriptDir) {
    super(featuresEncoder, outcomeEncoder);
    this.modelDir = modelDir;

    File classifyScript = null;
    for (File file : scriptDir.listFiles()) {
      if (file.getName().startsWith("classify.")) {
        if (classifyScript != null) {
          throw new RuntimeException(
              "There are multiple files named classify.*");
        }
        classifyScript = file;
      }
    }

    if (classifyScript == null) {
      throw new RuntimeException("There are no files named classify.*");
    }

    try {
      this.classifierProcess = Runtime.getRuntime().exec(
          new String[] { classifyScript.getAbsolutePath(),
              modelDir.getAbsolutePath() });
      // start the classifier process running, give it a chance to read the
      // model, and
      // set classifierProcess to the running classifier
      new StreamHandlerThread(classifierProcess.getErrorStream(), logger)
          .start();
      toClassifier = new PrintStream(classifierProcess.getOutputStream());
      reader = new BufferedReader(new InputStreamReader(
          classifierProcess.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public String classify(List<Feature> features)
      throws CleartkProcessingException {
    // Encode the features and pass them to the standard input of the classifier
    // process
    // and then read the standard output prediction, which will be in the string
    // format expected by
    // the annotator.

    this.toClassifier.println(featuresToString(features));
    this.toClassifier.flush();

    String line = "";
    try {
      line = reader.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return line;
  }

  protected String featuresToString(List<Feature> features)
      throws CleartkEncoderException {
    StringBuilder buf = new StringBuilder();

    for (FeatureVector.Entry featureNode : this.featuresEncoder
        .encodeAll(features)) {
      buf.append(String.format(Locale.US, " %d:%.7f", featureNode.index,
          featureNode.value));
    }

    return buf.substring(1);
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();

    this.toClassifier.print('\n');
    classifierProcess.waitFor();
  }
}
