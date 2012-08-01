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
package org.cleartk.util.ae;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.util.ViewURIUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class PlainTextWriterTest extends DefaultTestBase {

  @Test
  public void test() throws Exception {
    try {
      AnalysisEngineFactory.createPrimitive(PlainTextWriter.class);
      Assert.fail("expected exception with output directory not specified");
    } catch (ResourceInitializationException e) {
    }

    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        PlainTextWriter.class,
        PlainTextWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        this.outputDirectory.getPath());
    String text = "What if we built a large\r\n, wooden badger?";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "What if we built a large \n, wooden badger ?",
        "WDT TO PRP VBN DT JJ , JJ NN .");
    ViewURIUtil.setURI(jCas, new File("identifier").toURI());
    engine.process(jCas);
    engine.collectionProcessComplete();

    File outputFile = new File(this.outputDirectory, "identifier.txt");
    String actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(text, actualText);

    jCas.reset();
    text = "What if we built a large\n, wooden badger?";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "What if we built a large \n, wooden badger ?",
        "WDT TO PRP VBN DT JJ , JJ NN .");
    ViewURIUtil.setURI(jCas, new File("1234").toURI());
    engine.process(jCas);
    engine.collectionProcessComplete();

    outputFile = new File(this.outputDirectory, "1234.txt");
    actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(text, actualText);

  }

}
