/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes;

import org.cleartk.CleartkException;
import org.cleartk.classifier.jar.ClassifierBuilder;
import org.cleartk.classifier.jar.JarDataWriter;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 */

public abstract class LIBSVMDataWriter<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE> extends
    JarDataWriter<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE, FeatureVector> {

  public static final String TRAINING_DATA_FILE_NAME = "training-data.libsvm";

  public LIBSVMDataWriter(File outputDirectory) throws IOException {
    super(outputDirectory);

    // set up files
    File trainingDataFile = getFile(TRAINING_DATA_FILE_NAME);
    trainingDataFile.delete();

    // set up writer
    trainingDataWriter = this.getPrintWriter(TRAINING_DATA_FILE_NAME);

    // set manifest attributes for classifier
    Map<String, Attributes> entries = classifierManifest.getEntries();
    if (!entries.containsKey(LIBSVMClassifier.ATTRIBUTES_NAME)) {
      entries.put(LIBSVMClassifier.ATTRIBUTES_NAME, new Attributes());
    }
    Attributes attributes = entries.get(LIBSVMClassifier.ATTRIBUTES_NAME);
    attributes.putValue(
        LIBSVMClassifier.SCALE_FEATURES_KEY,
        LIBSVMClassifier.SCALE_FEATURES_VALUE_NORMALIZEL2);
  }

  @Override
  public void writeEncoded(FeatureVector features, OUTPUTOUTCOME_TYPE outcome)
      throws CleartkException {
    String classString = encode(outcome);

    StringBuffer output = new StringBuffer();

    output.append(classString);

    for (FeatureVector.Entry entry : features) {
      if (Double.isInfinite(entry.value) || Double.isNaN(entry.value))
        throw new CleartkException(String.format(
            "illegal value in entry %d:%.7f",
            entry.index,
            entry.value));
      output.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
    }

    trainingDataWriter.println(output);
  }

  @Override
  public void finish() throws CleartkException {
    super.finish();

    // flush and close writer
    trainingDataWriter.flush();
    trainingDataWriter.close();
  }

  public abstract Class<? extends ClassifierBuilder<INPUTOUTCOME_TYPE>> getDefaultClassifierBuilderClass();

  protected abstract String encode(OUTPUTOUTCOME_TYPE outcome);

  private PrintWriter trainingDataWriter;

}
