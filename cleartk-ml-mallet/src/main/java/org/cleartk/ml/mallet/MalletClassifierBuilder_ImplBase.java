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
package org.cleartk.ml.mallet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.jar.ClassifierBuilder_ImplBase;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.mallet.factory.ClassifierTrainerFactory;
import org.cleartk.util.ReflectionUtil;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.types.InstanceList;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 */

public abstract class MalletClassifierBuilder_ImplBase<CLASSIFIER_TYPE extends MalletClassifier_ImplBase<OUTCOME_TYPE>, OUTCOME_TYPE>
    extends ClassifierBuilder_ImplBase<CLASSIFIER_TYPE, List<NameNumber>, OUTCOME_TYPE, String> {

  private static final String MODEL_NAME = "model.mallet";

  @Override
  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.mallet");
  }

  public void trainClassifier(File dir, String... args) throws Exception {

    InstanceListCreator instanceListCreator = new InstanceListCreator();
    InstanceList instanceList = instanceListCreator.createInstanceList(getTrainingDataFile(dir));
    instanceList.save(new File(dir, "training-data.ser"));

    String factoryName = args[0];
    Class<ClassifierTrainerFactory<?>> factoryClass = createTrainerFactory(factoryName);
    if (factoryClass == null) {
      String factoryName2 = "org.cleartk.ml.mallet.factory." + factoryName
          + "TrainerFactory";
      factoryClass = createTrainerFactory(factoryName2);
    }
    if (factoryClass == null) {
      throw new IllegalArgumentException(
          String
              .format(
                  "name for classifier trainer factory is not valid: name given ='%s'.  Valid classifier names include: %s, %s, %s, and %s",
                  factoryName,
                  ClassifierTrainerFactory.NAMES[0],
                  ClassifierTrainerFactory.NAMES[1],
                  ClassifierTrainerFactory.NAMES[2],
                  ClassifierTrainerFactory.NAMES[3]));
    }

    String[] factoryArgs = new String[args.length - 1];
    System.arraycopy(args, 1, factoryArgs, 0, factoryArgs.length);

    ClassifierTrainerFactory<?> factory = factoryClass.newInstance();
    ClassifierTrainer<?> trainer = null;
    try {
      trainer = factory.createTrainer(factoryArgs);
    } catch (Throwable t) {
      throw new IllegalArgumentException("Unable to create trainer.  Usage for "
          + factoryClass.getCanonicalName() + ": " + factory.getUsageMessage(), t);
    }

    this.classifier = trainer.train(instanceList);

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(dir, MODEL_NAME)));
    oos.writeObject(classifier);
    oos.close();

  }

  private Class<ClassifierTrainerFactory<?>> createTrainerFactory(String className) {
    try {
      return ReflectionUtil.uncheckedCast(Class.forName(className));
    } catch (ClassNotFoundException cnfe) {
      return null;
    }
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, MODEL_NAME, new File(dir, MODEL_NAME));
  }

  protected Classifier classifier;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, MODEL_NAME);
    ObjectInputStream objectStream = new ObjectInputStream(modelStream);
    try {
      this.classifier = (Classifier) objectStream.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }
}
