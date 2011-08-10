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
package org.cleartk.classifier.baseline;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.jar.JarStreams;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class SingleOutcomeClassifierBuilder<OUTCOME_TYPE> extends
    JarClassifierBuilder<SingleOutcomeClassifier<OUTCOME_TYPE>> {

  private static final String MODEL_NAME = "model.singlevalue";

  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.outcomes");
  }

  protected abstract OUTCOME_TYPE parseOutcome(String outcome);

  protected void writeOutcome(File dir, OUTCOME_TYPE outcome) throws IOException {
    Files.write(outcome.toString(), new File(dir, MODEL_NAME), Charsets.US_ASCII);
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, MODEL_NAME, new File(dir, MODEL_NAME));
  }

  protected OUTCOME_TYPE value;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, MODEL_NAME);
    InputStreamReader reader = new InputStreamReader(modelStream, Charsets.US_ASCII);
    this.value = this.parseOutcome(CharStreams.toString(reader));
  }

  @Override
  protected SingleOutcomeClassifier<OUTCOME_TYPE> newClassifier() {
    return new SingleOutcomeClassifier<OUTCOME_TYPE>(this.value);
  }
}
