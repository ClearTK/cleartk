/*
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
package org.cleartk.classifier.svmlight;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.classifier.jar.JarStreams;
import org.cleartk.classifier.sigmoid.Sigmoid;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class SvmLightBooleanOutcomeClassifierBuilder extends
    SvmLightClassifierBuilder_ImplBase<SvmLightBooleanOutcomeClassifier, Boolean, Boolean> {

  private File getSigmoidFile(File dir) {
    return new File(dir, "training-data.svmlight.sigmoid");
  }

  @Override
  public void trainClassifier(File dir, String... args) throws Exception {
    super.trainClassifier(dir, args);

    Sigmoid s = FitSigmoid.fit(this.getModelFile(dir), this.getTrainingDataFile(dir));
    System.out.println("Computed output mapping function: " + s.toString());

    ObjectOutput o = new ObjectOutputStream(new FileOutputStream(getSigmoidFile(dir)));
    o.writeObject(s);
    o.close();
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, "model.sigmoid", getSigmoidFile(dir));
  }

  protected Sigmoid sigmoid;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, "model.sigmoid");
    ObjectInput in = new ObjectInputStream(modelStream);
    try {
      this.sigmoid = (Sigmoid) in.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
    in.close();
  }

  @Override
  protected SvmLightBooleanOutcomeClassifier newClassifier() {
    return new SvmLightBooleanOutcomeClassifier(
        this.featuresEncoder,
        this.outcomeEncoder,
        this.model,
        this.sigmoid);
  }
}
