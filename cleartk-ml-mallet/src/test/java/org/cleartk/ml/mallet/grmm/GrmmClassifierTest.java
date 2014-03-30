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
package org.cleartk.ml.mallet.grmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Instance;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.jar.Train;
import org.cleartk.ml.mallet.grmm.GrmmClassifierBuilder;
import org.cleartk.ml.mallet.grmm.GrmmDataWriter;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2010, University of Würzburg <br>
 * All rights reserved.
 * <p>
 * 
 * @author Martin Toepfer
 */
public class GrmmClassifierTest extends DefaultTestBase {

  public static class Test1Annotator extends CleartkSequenceAnnotator<String[]> {

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
    }

    public void process(JCas cas) throws AnalysisEngineProcessException {
      if (this.isTraining()) {
        for (int i = 0; i < 5; i++) {
          List<Instance<String[]>> instances = GrmmTestDataGenerator.createInstances2();
          this.dataWriter.write(instances);
          instances = GrmmTestDataGenerator.createInstances1();
          this.dataWriter.write(instances);
          instances = GrmmTestDataGenerator.createInstances3();
          this.dataWriter.write(instances);
        }
      } else {
        // simple test
        List<Instance<String[]>> instances = GrmmTestDataGenerator.createInstances1test();
        List<String[]> outcomes = this.classify(instances);
        assertEquals(instances.size(), outcomes.size());

        // test classification with outcomes
        instances = GrmmTestDataGenerator.createInstances2test();
        outcomes = this.classify(instances);
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

    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createEngine(
        Test1Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        GrmmDataWriter.class.getName());

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    GrmmClassifierBuilder builder = new GrmmClassifierBuilder();
    File trainFile = builder.getTrainingDataFile(this.outputDirectory);
    BufferedReader reader = new BufferedReader(new FileReader(trainFile));
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

    // check that the classifier is successfully loaded from the model
    File modelJarFile = JarClassifierBuilder.getModelJarFile(this.outputDirectory);
    assertNotNull(modelJarFile);
    builder.loadClassifierFromTrainingDirectory(this.outputDirectory);

    // try to use model for classification:
    AnalysisEngine classifierAnnotator = AnalysisEngineFactory.createEngine(
        Test1Annotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        modelJarFile.getPath());
    jCas.reset();
    classifierAnnotator.process(jCas);
    classifierAnnotator.collectionProcessComplete();
  }

}
