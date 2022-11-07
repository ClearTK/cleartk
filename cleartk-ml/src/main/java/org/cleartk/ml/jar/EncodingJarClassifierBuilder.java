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
package org.cleartk.ml.jar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.ClassLookup;
import org.cleartk.util.ReflectionUtil;

/**
 * Superclass for builders which write to a training data file using {@link FeaturesEncoder}s and
 * {@link OutcomeEncoder}s, and package classifiers as jar files.
 * 
 * Subclasses will typically override:
 * <ul>
 * <li>{@link #saveToTrainingDirectory(File)} to add items to the model training directory</li>
 * <li>{@link #packageClassifier(File, JarOutputStream)} to copy items to the classifier jar</li>
 * <li>{@link #unpackageClassifier(JarInputStream)} to load items from the classifier jar</li>
 * <li>{@link #newClassifier()} to create a classifier from the loaded attributes</li>
 * </ul>
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class EncodingJarClassifierBuilder<CLASSIFIER_TYPE, ENCODED_FEATURES_TYPE, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>
        extends JarClassifierBuilder<CLASSIFIER_TYPE> {

  private static final String ENCODERS_FILE_NAME = FeaturesEncoder_ImplBase.ENCODERS_FILE_NAME;

  public static File getEncodersFile(File dir) {
    return new File(dir, ENCODERS_FILE_NAME);
  }

  protected FeaturesEncoder<ENCODED_FEATURES_TYPE> featuresEncoder;

  public FeaturesEncoder<ENCODED_FEATURES_TYPE> getFeaturesEncoder() {
    return featuresEncoder;
  }

  public void setFeaturesEncoder(FeaturesEncoder<ENCODED_FEATURES_TYPE> featuresEncoder) {
    this.featuresEncoder = featuresEncoder;
  }

  protected OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> outcomeEncoder;

  public OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> getOutcomeEncoder() {
    return outcomeEncoder;
  }

  public void setOutcomeEncoder(OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> outcomeEncoder) {
    this.outcomeEncoder = outcomeEncoder;
  }

  public abstract File getTrainingDataFile(File dir);

  @Override
  public void saveToTrainingDirectory(File dir) throws IOException {
    super.saveToTrainingDirectory(dir);
    // finalize the encoder feature set
    this.featuresEncoder.finalizeFeatureSet(dir);
    this.outcomeEncoder.finalizeOutcomeSet(dir);

    // save the encoders to the directory
    File encodersFile = getEncodersFile(dir);
    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(encodersFile));
    ObjectOutputStream os = new ObjectOutputStream(outputStream);
    os.writeObject(this.featuresEncoder);
    os.writeObject(this.outcomeEncoder);
    os.close();
    outputStream.close();

  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, ENCODERS_FILE_NAME, getEncodersFile(dir));
  }

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, ENCODERS_FILE_NAME);
    ObjectInputStream is = ClassLookup.streamObjects(modelStream);
    try {
      this.featuresEncoder = this.featuresEncoderCast(is.readObject());
      this.outcomeEncoder = this.outcomeEncoderCast(is.readObject());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Classes not found for serialized encoder objects", e);
    }
  }

  @SuppressWarnings("unchecked")
  private FeaturesEncoder<ENCODED_FEATURES_TYPE> featuresEncoderCast(Object object) {
    FeaturesEncoder<ENCODED_FEATURES_TYPE> encoder = (FeaturesEncoder<ENCODED_FEATURES_TYPE>) object;

    ReflectionUtil.checkTypeParametersAreEqual(EncodingJarClassifierBuilder.class,
            "ENCODED_FEATURES_TYPE", this, FeaturesEncoder.class, "ENCODED_FEATURES_TYPE", encoder,
            ClassCastException.class);

    return encoder;
  }

  @SuppressWarnings("unchecked")
  private OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> outcomeEncoderCast(Object object) {
    OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> encoder;
    encoder = (OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE>) object;

    ReflectionUtil.checkTypeParametersAreEqual(EncodingJarClassifierBuilder.class, "OUTCOME_TYPE",
            this, OutcomeEncoder.class, "OUTCOME_TYPE", encoder, ClassCastException.class);

    ReflectionUtil.checkTypeParametersAreEqual(EncodingJarClassifierBuilder.class,
            "ENCODED_OUTCOME_TYPE", this, OutcomeEncoder.class, "ENCODED_OUTCOME_TYPE", encoder,
            ClassCastException.class);

    return encoder;
  }
}
