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

import java.io.File;
import java.io.IOException;

import org.cleartk.CleartkException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.SequentialDataWriter;

/**
 * Superclass for {@link DataWriter} and {@link SequentialDataWriter} implementations that saves
 * files to a training directory using a {@link JarClassifierBuilder}.
 * 
 * Note that it does not declare that it implements either of the DataWriter interfaces. Subclasses
 * should do this.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public abstract class DirectoryDataWriter<CLASSIFIER_BUILDER extends JarClassifierBuilder<? extends CLASSIFIER_TYPE>, CLASSIFIER_TYPE> {

  protected File outputDirectory;

  protected CLASSIFIER_BUILDER classifierBuilder;

  public DirectoryDataWriter(File outputDirectory) {
    this.outputDirectory = outputDirectory;
    if (!this.outputDirectory.exists()) {
      this.outputDirectory.mkdirs();
    }
    this.classifierBuilder = this.newClassifierBuilder();
  }

  /**
   * Constructs a new {@link JarClassifierBuilder} that will be set as the
   * {@link #classifierBuilder} during object construction.
   */
  protected abstract CLASSIFIER_BUILDER newClassifierBuilder();

  /**
   * Get the {@link JarClassifierBuilder} associated with this DataWriter.
   * 
   * @return The classifier builder.
   */
  public CLASSIFIER_BUILDER getClassifierBuilder() {
    return classifierBuilder;
  }

  /**
   * Basic implementation of {@link DataWriter#finish()} and {@link SequentialDataWriter#finish()}
   * that calls {@link ClassifierBuilder#saveToTrainingDirectory(File)}
   */
  public void finish() throws CleartkException {
    try {
      this.classifierBuilder.saveToTrainingDirectory(this.outputDirectory);
    } catch (IOException e) {
      throw new CleartkException(e);
    }
  }
}
