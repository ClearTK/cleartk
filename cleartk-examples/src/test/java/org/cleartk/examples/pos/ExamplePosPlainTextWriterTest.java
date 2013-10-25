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
package org.cleartk.examples.pos;

import java.io.File;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.util.FileUtils;
import org.cleartk.examples.ExamplesTestBase;
import org.cleartk.util.ViewUriUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.uima.fit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class ExamplePosPlainTextWriterTest extends ExamplesTestBase {

  private final File outputDirectory = new File("test/data/example");

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.outputDirectory.mkdirs();
  }

  @After
  public void tearDown() throws Exception {
    for (File file : this.outputDirectory.listFiles()) {
      file.delete();
    }
    this.outputDirectory.delete();
  }

  @Test
  public void test() throws Exception {

    AnalysisEngine engine = AnalysisEngineFactory.createEngine(
        ExamplePosPlainTextWriter.class,
        ExamplePosPlainTextWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        this.outputDirectory.getPath());

    tokenBuilder.buildTokens(
        jCas,
        "I walked home. It was a nice day!",
        "I walked home .\nIt was a nice day !",
        "PRP VBD NN . PRP VBD DT JJ NN .");
    ViewUriUtil.setURI(jCas, new File("xxx").toURI());
    engine.process(jCas);
    engine.collectionProcessComplete();

    String text = "I/PRP walked/VBD home/NN ./. \nIt/PRP was/VBD a/DT nice/JJ day/NN !/. \n";
    File outputFile = new File(this.outputDirectory, "xxx.pos");
    Assert.assertEquals(text, FileUtils.file2String(outputFile).replace("\r", ""));
  }

  @Test
  public void testDescriptor() throws UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(ExamplePosPlainTextWriter.class);
    Object outDirectory = engine.getConfigParameterValue(ExamplePosPlainTextWriter.PARAM_OUTPUT_DIRECTORY_NAME);
    Assert.assertEquals(ExamplePosPlainTextWriter.DEFAULT_OUTPUT_DIRECTORY, outDirectory);
  }
}
