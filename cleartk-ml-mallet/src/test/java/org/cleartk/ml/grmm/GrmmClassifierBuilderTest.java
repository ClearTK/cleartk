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
package org.cleartk.ml.grmm;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Instance;
import org.cleartk.ml.grmm.GrmmDataWriter;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.Train;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Before;
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
public class GrmmClassifierBuilderTest extends DefaultTestBase {

  public static class Test1Annotator extends CleartkSequenceAnnotator<String[]> {
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
      }
    }

  }

  private AnalysisEngine dataWriterAnnotator;

  @Before
  public void init() {
    try {
      dataWriterAnnotator = AnalysisEngineFactory.createEngine(
          Test1Annotator.class,
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          outputDirectoryName,
          DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
          GrmmDataWriter.class.getName());
      dataWriterAnnotator.process(jCas);
      dataWriterAnnotator.collectionProcessComplete();
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    } catch (AnalysisEngineProcessException e) {
      e.printStackTrace();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void trainTemplateNotExistsTest() throws Exception {
    HideOutput hider = new HideOutput();
    // file does not exist, expect exception
    Train.main(outputDirectoryName, "template.txt");
    hider.restoreOutput();
  }

  @Test(expected = IllegalArgumentException.class)
  public void trainTemplateNullTest() throws Exception {
    HideOutput hider = new HideOutput();
    // file does not exist, expect exception
    Train.main(outputDirectoryName, null);
    hider.restoreOutput();
  }

  @Test
  public void trainTest() throws Exception {
    String templateFilename = "template.txt";
    GrmmTestDataGenerator.createBigramTemplate(outputDirectoryName, templateFilename);
    HideOutput hider = new HideOutput();
    Train.main(this.outputDirectoryName, templateFilename);
    hider.restoreOutput();
  }
}
