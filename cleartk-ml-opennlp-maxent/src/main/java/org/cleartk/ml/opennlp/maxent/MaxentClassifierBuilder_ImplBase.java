/** 
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
package org.cleartk.ml.opennlp.maxent;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;

import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.model.MaxentModel;

import org.cleartk.ml.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.ml.jar.ClassifierBuilder_ImplBase;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.opennlp.maxent.encoder.ContextValues;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public abstract class MaxentClassifierBuilder_ImplBase<CLASSIFIER_TYPE extends MaxentClassifier_ImplBase<OUTCOME_TYPE>, OUTCOME_TYPE>
    extends ClassifierBuilder_ImplBase<CLASSIFIER_TYPE, ContextValues, OUTCOME_TYPE, String> {

  private static final String MODEL_NAME = "model.maxent";

  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.maxent");
  }

  public void trainClassifier(File dir, String... args) throws Exception {
    String[] maxentArgs = new String[args.length + 1];
    maxentArgs[0] = getTrainingDataFile(dir).getPath();
    System.arraycopy(args, 0, maxentArgs, 1, args.length);
    opennlp.model.RealValueFileEventStream.main(maxentArgs);
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, MODEL_NAME, new File(dir, "training-data.maxent.bin.gz"));
    File featureLookup = new File(dir, NameNumberFeaturesEncoder.LOOKUP_FILE_NAME);
    if (featureLookup.exists()) {
      JarStreams.putNextJarEntry(modelStream, "name-lookup.txt", featureLookup);
    }
  }

  protected MaxentModel model;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, MODEL_NAME);
    this.model = new BinaryGISModelReader(new DataInputStream(new GZIPInputStream(modelStream)))
        .getModel();
  }

}
