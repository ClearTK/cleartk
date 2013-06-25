/**
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Würzburg nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
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
public class GrmmDataWriterTest extends DefaultTestBase {

  public static class Test1Annotator extends CleartkSequenceAnnotator<String[]> {

    public void process(JCas cas) throws AnalysisEngineProcessException {
      List<Instance<String[]>> instances = new ArrayList<Instance<String[]>>();
      List<Feature> features = Arrays.asList(
          new Feature("pos", "NN"),
          new Feature("distance", 3.0),
          new Feature("precision", 1.234));
      List<String> el = new ArrayList<String>();
      el.add("A");
      el.add("X");
      Instance<String[]> instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      features = Arrays.asList(new Feature("name", "2PO"), new Feature("ps", 2));
      el = new ArrayList<String>();
      el.add("B");
      el.add("Y");
      instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      el = new ArrayList<String>();
      el.add("C");
      el.add("Z");
      instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      features = Arrays.asList(new Feature("A_B", "AB"));

      el = new ArrayList<String>();
      el.add("A");
      el.add("Z");
      instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      this.dataWriter.write(instances);

      // twice:
      instances = new ArrayList<Instance<String[]>>();

      features = Arrays.asList(new Feature("A_B", "AB"));

      el = new ArrayList<String>();
      el.add("A");
      el.add("Z");
      instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      features = Arrays.asList(new Feature("name", "2PO"), new Feature("ps", 3));
      el = new ArrayList<String>();
      el.add("B");
      el.add("Y");
      instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      features = Arrays.asList(new Feature("name", "2PO"), new Feature("ps", 2));
      el = new ArrayList<String>();
      el.add("C");
      el.add("Z");
      instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      features = Arrays.asList(new Feature("A_B", "AB"));

      el = new ArrayList<String>();
      el.add("A");
      el.add("Z");
      instance = new Instance<String[]>(el.toArray(new String[2]), features);
      instances.add(instance);

      this.dataWriter.write(instances);
    }
  }

  @Test
  public void test1DataWriter() throws Exception {
    AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
        Test1Annotator.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        GrmmDataWriter.class.getName());

    dataWriterAnnotator.process(jCas);
    dataWriterAnnotator.collectionProcessComplete();

    File trainFile = new GrmmClassifierBuilder().getTrainingDataFile(this.outputDirectory);
    String[] lines = FileUtil.loadListOfStrings(trainFile);
    assertEquals("A X ---- pos_NN:1.0 distance:3.0 precision:1.234", lines[0]);
    assertEquals("B Y ---- name_2PO:1.0 ps:2", lines[1]);
    assertEquals("C Z ---- name_2PO:1.0 ps:2", lines[2]);
    assertEquals("A Z ---- A_B_AB:1.0", lines[3]);
    // twice:
    assertEquals("A Z ---- A_B_AB:1.0", lines[4]);
    assertEquals("B Y ---- name_2PO:1.0 ps:3", lines[5]);
    assertEquals("C Z ---- name_2PO:1.0 ps:2", lines[6]);
    assertEquals("A Z ---- A_B_AB:1.0", lines[7]);

    File template = new File(outputDirectoryName, "template.txt");
    FileWriter fileWriter = new FileWriter(template);
    fileWriter.write("new ACRF.BigramTemplate (0)\nnew ACRF.BigramTemplate (1)\nnew ACRF.PairwiseFactorTemplate (0,1)\n");
    fileWriter.close();

    HideOutput hider = new HideOutput();
    Train.main(outputDirectoryName, "template.txt");
    hider.restoreOutput();
    hider.close();
  }

}
