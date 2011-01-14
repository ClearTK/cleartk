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
package org.cleartk.classifier.grmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkSequentialAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialClassifier;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.jar.JarSequentialDataWriterFactory;
import org.cleartk.classifier.jar.Train;
import org.cleartk.test.DefaultTestBase;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2010, University of Würzburg <br>
 * All rights reserved.
 * <p>
 * 
 * @author Martin Toepfer
 */
public class GrmmClassifierTest extends DefaultTestBase {

  public static class Test1Annotator extends CleartkSequentialAnnotator<String[]> {

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
    }

    public void process(JCas cas) throws AnalysisEngineProcessException {
      try {
        this.processSimple(cas);
      } catch (CleartkException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }

    public void processSimple(JCas cas) throws AnalysisEngineProcessException, CleartkException {
      if (this.isTraining()) {
        for (int i = 0; i < 5; i++) {
          List<Instance<String[]>> instances = GrmmTestDataGenerator.createInstances2();
          this.sequentialDataWriter.writeSequence(instances);
          instances = GrmmTestDataGenerator.createInstances1();
          this.sequentialDataWriter.writeSequence(instances);
          instances = GrmmTestDataGenerator.createInstances3();
          this.sequentialDataWriter.writeSequence(instances);
        }
      } else {
        // simple test
        List<Instance<String[]>> instances = GrmmTestDataGenerator.createInstances1test();
        List<String[]> outcomes = this.classifySequence(instances);
        assertEquals(instances.size(), outcomes.size());

        // test classification with outcomes
        instances = GrmmTestDataGenerator.createInstances2test();
        outcomes = this.classifySequence(instances);
        assertEquals(instances.size(), outcomes.size());
        List<Instance<String[]>> gold = GrmmTestDataGenerator.createInstances2();
        for (int i = 0; i < gold.size(); i++) {
          String[] goldOut = gold.get(i).getOutcome();
          String[] testOut = outcomes.get(i);
          for (int j = 0; j < testOut.length; j++) {
            assertEquals(goldOut[j], testOut[j]);
          }
        }
      }
    }
  }

  @Test
  public void runTest1() throws Exception {

    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
            Test1Annotator.class, typeSystemDescription,
            JarSequentialDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDirectoryName,
            CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME,
            DefaultGrmmDataWriterFactory.class.getName());

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    BufferedReader reader = new BufferedReader(new FileReader(new File(outputDirectoryName,
            GrmmDataWriter.TRAINING_DATA_FILE_NAME)));
    reader.readLine();
    reader.close();

    // hide output during training:
    HideOutput hider = new HideOutput();
    // create a template:
    String templateFilename = "template.txt";
    GrmmTestDataGenerator.createBigramTemplate(outputDirectoryName, templateFilename);
    // train and create a model with this template:
    Train.main(outputDirectoryName, templateFilename);
    hider.restoreOutput();

    // try to use model for classification:
    File mFile = new File(outputDirectoryName, "model.jar");
    assertTrue(mFile.exists());
    JarFile modelFile = new JarFile(mFile);
    assertNotNull(modelFile);
    GrmmClassifier classifier = new GrmmClassifier(modelFile);
    modelFile.close();
    assertTrue(classifier instanceof SequentialClassifier<?>);

    String modelJar = outputDirectoryName + "/model.jar";
    AnalysisEngine sequentialClassifierAnnotator = AnalysisEngineFactory.createPrimitive(
            Test1Annotator.class, typeSystemDescription,
            JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelJar);
    jCas.reset();
    sequentialClassifierAnnotator.process(jCas);
    sequentialClassifierAnnotator.collectionProcessComplete();
  }

}
