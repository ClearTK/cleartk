/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier.mallet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.jar.JarStreams;
import org.cleartk.classifier.jar.SequenceClassifierBuilder_ImplBase;

import cc.mallet.fst.SimpleTagger;
import cc.mallet.fst.Transducer;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */

public class MalletCrfStringOutcomeClassifierBuilder extends
    SequenceClassifierBuilder_ImplBase<MalletCrfStringOutcomeClassifier, List<NameNumber>, String, String> {

  private static final String MODEL_NAME = "model.malletcrf";

  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.malletcrf");
  }

  public void trainClassifier(File dir, String... args) throws Exception {
    String[] malletArgs = new String[args.length + 5];
    System.arraycopy(args, 0, malletArgs, 0, args.length);
    malletArgs[malletArgs.length - 5] = "--train";
    malletArgs[malletArgs.length - 4] = "true";
    malletArgs[malletArgs.length - 3] = "--model-file";
    malletArgs[malletArgs.length - 2] = new File(dir, MODEL_NAME).getPath();
    malletArgs[malletArgs.length - 1] = getTrainingDataFile(dir).getPath();
    String leaveMyLoggingAloneMallet = "java.util.logging.config.file";
    String propValue = System.getProperty(leaveMyLoggingAloneMallet);
    System.setProperty(leaveMyLoggingAloneMallet, "anything-but-null");
    try {
      SimpleTagger.main(malletArgs);
    } finally {
      System.getProperties().remove(leaveMyLoggingAloneMallet);
      if (propValue != null) {
        System.setProperty(leaveMyLoggingAloneMallet, propValue);
      }
    }
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, MODEL_NAME, new File(dir, MODEL_NAME));
  }

  protected Transducer transducer;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, MODEL_NAME);
    ObjectInputStream objectStream = new ObjectInputStream(modelStream);
    try {
      this.transducer = (Transducer) objectStream.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  @Override
  protected MalletCrfStringOutcomeClassifier newClassifier() {
    return new MalletCrfStringOutcomeClassifier(this.featuresEncoder, this.outcomeEncoder, this.transducer);
  }
}
