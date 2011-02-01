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
package org.cleartk.classifier.jar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.SequenceDataWriter;

/**
 * Superclass for builders which package classifiers as jar files. Saves a manifest from which new
 * instances of this class can later be loaded.
 * 
 * ClassifierBuilder subclasses that use feature or outcome encoders should instead subclass from
 * {@link EncodingJarClassifierBuilder}.
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
public abstract class JarClassifierBuilder<CLASSIFIER_TYPE> {

  /**
   * The name of the attribute where the classifier builder class is stored.
   */
  private static final Attributes.Name CLASSIFIER_BUILDER_ATTRIBUTE_NAME = new Attributes.Name(
      "classifierBuilderClass");

  /**
   * The manifest associated with this classifier builder. The manifest will be saved to directories
   * and jar files (via {@link #saveToTrainingDirectory(File)} and {@link #packageClassifier(File)}
   * and can be used to load a new instance of the classifier builder (via
   * {@link #loadClassifier(InputStream)} and {@link #loadClassifierFromTrainingDirectory(File)}.
   */
  protected Manifest manifest;

  /**
   * Loads a classifier builder from manifest in the training directory.
   * 
   * @param dir
   *          The training directory where the classifier builder was written by a call to
   *          {@link #saveToTrainingDirectory(File)}. This is typically the same directory as was
   *          used for {@link DirectoryDataWriterFactory#PARAM_OUTPUT_DIRECTORY}.
   * @return A new classifier builder.
   */
  public static JarClassifierBuilder<?> fromTrainingDirectory(File dir) throws IOException {
    InputStream stream = new BufferedInputStream(new FileInputStream(getManifestFile(dir)));
    Manifest manifest = new Manifest(stream);
    stream.close();
    return fromManifest(manifest);
  }

  /**
   * Loads a classifier builder from a manifest.
   * 
   * @param manifest
   *          The classifier manifest, either from a training directory or from a classifier jar.
   * @return A new classifier builder.
   */
  public static JarClassifierBuilder<?> fromManifest(Manifest manifest) {
    String className = manifest.getMainAttributes().getValue(CLASSIFIER_BUILDER_ATTRIBUTE_NAME);
    JarClassifierBuilder<?> builder;
    try {
      builder = Class.forName(className).asSubclass(JarClassifierBuilder.class).newInstance();
    } catch (Exception e) {
      throw new RuntimeException("ClassifierBuilder class read from manifest does not exist", e);
    }
    builder.manifest = manifest;
    return builder;
  }

  /**
   * Creates a new classifier builder with a default manifest.
   */
  public JarClassifierBuilder() {
    super();
    this.manifest = new Manifest();
    Attributes attributes = this.manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.put(CLASSIFIER_BUILDER_ATTRIBUTE_NAME, this.getClass().getName());
  }

  /**
   * Write all information stored in the classifier builder to the training directory. Typically
   * called by {@link DataWriter#finish()} or {@link SequenceDataWriter#finish()}.
   * 
   * @param dir
   *          The directory where classifier information should be written.
   */
  public void saveToTrainingDirectory(File dir) throws IOException {
    // save the manifest to the directory
    FileOutputStream manifestStream = new FileOutputStream(getManifestFile(dir));
    this.manifest.write(manifestStream);
    manifestStream.close();
  }

  /**
   * Train a classifier from a training directory, as prepared by
   * {@link #saveToTrainingDirectory(File)}. Typically called at the command line by
   * {@link Train#main(String...)}.
   * 
   * @param dir
   *          The directory where training data and other classifier information has been written
   *          and where the trained classifier should be stored.
   * @param args
   *          Additional command line arguments for the classifier trainer.
   */
  public abstract void trainClassifier(File dir, String... args) throws Exception;

  /**
   * Create a classifier jar file from a directory where a classifier model was trained by
   * {@link #trainClassifier(File, String[])}.
   * 
   * This method should typically not be overridden by subclasses - use
   * {@link #packageClassifier(File, JarOutputStream)} instead.
   * 
   * @param dir
   *          The directory where the classifier model was trained.
   */
  public void packageClassifier(File dir) throws IOException {
    JarOutputStream modelStream = new JarOutputStream(new BufferedOutputStream(
        new FileOutputStream(this.getModelJarFile(dir))), this.manifest);
    this.packageClassifier(dir, modelStream);
    modelStream.close();
  }

  /**
   * Add elements to a classifier jar.
   * 
   * @param dir
   *          The directory where the classifier model was trained.
   * @param modelStream
   *          The jar where the classifier is being written.
   * @throws IOException
   *           For errors reading the directory or writing to the jar.
   */
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    // Used by subclasses
  }

  /**
   * Get the classifier jar file, as packaged by {@link #packageClassifier(File)}.
   * 
   * @param dir
   *          The directory where the classifier was packaged.
   * @return The classifier jar file.
   */
  public File getModelJarFile(File dir) {
    return new File(dir, "model.jar");
  }

  /**
   * Load a classifier packaged by {@link #packageClassifier(File)} from the jar file in the
   * training directory.
   * 
   * This method should typically not be overridden by subclasses - use
   * {@link #unpackageClassifier(JarInputStream)} and {@link #newClassifier()} instead.
   * 
   * @param dir
   *          The directory where the classifier was trained and packaged.
   * @return The loaded classifier.
   */
  public CLASSIFIER_TYPE loadClassifierFromTrainingDirectory(File dir) throws IOException {
    File modelJarFile = this.getModelJarFile(dir);
    InputStream inputStream = new BufferedInputStream(new FileInputStream(modelJarFile));
    try {
      return this.loadClassifier(inputStream);
    } finally {
      inputStream.close();
    }
  }

  /**
   * Load a classifier packaged by {@link #packageClassifier(File)} from an {@link InputStream}.
   * 
   * This method should typically not be overridden by subclasses - use
   * {@link #unpackageClassifier(JarInputStream)} and {@link #newClassifier()} instead.
   * 
   * @param inputStream
   *          The classifier stream.
   * @return The loaded classifier.
   */
  public CLASSIFIER_TYPE loadClassifier(InputStream inputStream) throws IOException {
    JarInputStream modelStream = inputStream instanceof JarInputStream
        ? (JarInputStream) inputStream
        : new JarInputStream(inputStream);
    this.unpackageClassifier(modelStream);
    return this.newClassifier();
  }

  /**
   * Load classifier elements from a classifier jar. This will typically save such attributes as
   * instance variables in preparation for a call to {@link #newClassifier()}.
   * 
   * @param modelStream
   *          The classifier jar
   * @throws IOException
   *           For errors reading from the jar.
   */
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    // Used by subclasses
  }

  /**
   * Create a new classifier using the attributes loaded by
   * {@link #unpackageClassifier(JarInputStream)}.
   * 
   * @return The loaded and initialized classifier.
   */
  protected abstract CLASSIFIER_TYPE newClassifier();

  /**
   * Gets the standard {@link File} for a jar {@link Manifest}.
   * 
   * @param dir
   *          The directory from which a jar will be built.
   * @return The file where the manifest is expected.
   */
  private static File getManifestFile(File dir) {
    return new File(dir, "MANIFEST.MF");
  }
}
