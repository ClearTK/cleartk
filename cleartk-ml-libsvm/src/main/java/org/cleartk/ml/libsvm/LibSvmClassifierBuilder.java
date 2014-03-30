/** 
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
package org.cleartk.ml.libsvm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import libsvm.svm_model;

import org.apache.commons.io.IOUtils;
import org.cleartk.ml.libsvm.util.LibSvmTrain;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class LibSvmClassifierBuilder<CLASSIFIER_TYPE extends LibSvmClassifier<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE, MODEL_TYPE>
    extends
    GenericLibSvmClassifierBuilder<CLASSIFIER_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE, libsvm.svm_model> {

  public static final String SCALE_FEATURES_KEY = "scaleFeatures";

  public static final String SCALE_FEATURES_VALUE_NORMALIZEL2 = "normalizeL2";

  @Override
  public String getTrainingDataName() {
    return "training-data.libsvm";
  }

  @Override
  protected String getModelName() {
    return "model.libsvm";
  }

  @Override
  public void trainClassifier(File dir, String... args) throws Exception {
    args = Arrays.copyOf(args, args.length + 4);
    // force probability estimates so we can implement Classifier.score
    args[args.length - 4] = "-b";
    args[args.length - 3] = "1";
    args[args.length - 2] = this.getTrainingDataFile(dir).getPath();
    args[args.length - 1] = this.getModelFile(dir).getPath();
    LibSvmTrain.main(args);
  }

  @Override
  protected svm_model loadModel(InputStream inputStream) throws IOException {
    File tmpFile = File.createTempFile("tmp", ".mdl");
    FileOutputStream output = new FileOutputStream(tmpFile);
    try {
      IOUtils.copy(inputStream, output);
      return libsvm.svm.svm_load_model(tmpFile.getPath());
    } finally {
      output.close();
      tmpFile.delete();
    }
  }
}
