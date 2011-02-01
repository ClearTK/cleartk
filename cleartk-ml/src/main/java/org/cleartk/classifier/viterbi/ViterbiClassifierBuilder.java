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
package org.cleartk.classifier.viterbi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.jar.JarStreams;
import org.cleartk.classifier.jar.Train;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class ViterbiClassifierBuilder<OUTCOME_TYPE> extends
    JarClassifierBuilder<ViterbiClassifier<OUTCOME_TYPE>> {

  private static final String OUTCOME_FEATURE_EXTRACTOR_FILE_NAME = "outcome-features-extractors.ser";

  private static final String DELEGATED_MODEL_DIRECTORY_NAME = "delegated-model";

  private static final String DELEGATED_MODEL_FILE_NAME = "delegated-model.jar";

  private static File getOutcomeFeatureExtractorsFile(File dir) {
    return new File(dir, OUTCOME_FEATURE_EXTRACTOR_FILE_NAME);
  }

  public File getDelegatedModelDirectory(File dir) {
    return new File(dir, DELEGATED_MODEL_DIRECTORY_NAME);
  }

  private OutcomeFeatureExtractor[] outcomeFeatureExtractors;

  public void setOutcomeFeatureExtractors(OutcomeFeatureExtractor[] outcomeFeatureExtractors) {
    this.outcomeFeatureExtractors = outcomeFeatureExtractors;
  }

  private Classifier<OUTCOME_TYPE> delegatedClassifier;

  @Override
  public void saveToTrainingDirectory(File dir) throws IOException {
    super.saveToTrainingDirectory(dir);
    ObjectOutputStream extractorsStream = new ObjectOutputStream(new BufferedOutputStream(
        new FileOutputStream(getOutcomeFeatureExtractorsFile(dir))));
    extractorsStream.writeObject(this.outcomeFeatureExtractors);
    extractorsStream.close();
  }

  @Override
  public void trainClassifier(File dir, String... args) throws Exception {
    String[] delegatedArgs = new String[args.length + 1];
    System.arraycopy(args, 0, delegatedArgs, 1, args.length);
    delegatedArgs[0] = this.getDelegatedModelDirectory(dir).getPath();
    Train.main(delegatedArgs);
  }

  @Override
  public void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);

    JarStreams.putNextJarEntry(
        modelStream,
        DELEGATED_MODEL_FILE_NAME,
        this.getModelJarFile(this.getDelegatedModelDirectory(dir)));

    JarStreams.putNextJarEntry(
        modelStream,
        OUTCOME_FEATURE_EXTRACTOR_FILE_NAME,
        getOutcomeFeatureExtractorsFile(dir));
  }

  @Override
  public void unpackageClassifier(JarInputStream modelStream) throws IOException {
    JarStreams.getNextJarEntry(modelStream, DELEGATED_MODEL_FILE_NAME);
    JarInputStream delegatedModelStream = new JarInputStream(modelStream);
    Manifest delegatedManifest = delegatedModelStream.getManifest();
    JarClassifierBuilder<?> delegatedBuilder = JarClassifierBuilder.fromManifest(delegatedManifest);
    this.delegatedClassifier = this.cast(delegatedBuilder.loadClassifier(delegatedModelStream));

    JarStreams.getNextJarEntry(modelStream, OUTCOME_FEATURE_EXTRACTOR_FILE_NAME);
    ObjectInputStream objectStream = new ObjectInputStream(modelStream);
    try {
      this.outcomeFeatureExtractors = (OutcomeFeatureExtractor[]) objectStream.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  @Override
  public ViterbiClassifier<OUTCOME_TYPE> newClassifier() {
    return new ViterbiClassifier<OUTCOME_TYPE>(
        this.delegatedClassifier,
        this.outcomeFeatureExtractors);
  }

  @SuppressWarnings("unchecked")
  private Classifier<OUTCOME_TYPE> cast(Object object) {
    Classifier<OUTCOME_TYPE> classifier = (Classifier<OUTCOME_TYPE>) object;
    // Can't check that OUTCOME_TYPE matches - ViterbiClassifier is generic and so has no concrete
    // OUTCOME_TYPE at runtime. But ViterbiClassifier should work with any classifier, so this
    // should be okay.
    return classifier;
  }

}
