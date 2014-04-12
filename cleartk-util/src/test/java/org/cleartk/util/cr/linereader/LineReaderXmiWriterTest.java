/** 
 * Copyright (c) 2014, Regents of the University of Colorado 
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

package org.cleartk.util.cr.linereader;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2014, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class LineReaderXmiWriterTest extends DefaultTestBase {

  private String inputDir = "src/test/resources/linereader";

  @Test
  public void testXmiWriter() throws Exception {
    File outputDir = new File(outputDirectory, "line-reader-xmi");
    outputDir.mkdirs();

    String languageCode = "en-us";
    CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        LineReader.class,
        null,
        LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
        inputDir,
        LineReader.PARAM_LANGUAGE,
        languageCode,
        LineReader.PARAM_SUFFIXES,
        new String[] { ".txt", ".dat" });

    AnalysisEngineDescription xmiWriter = LineReaderXmiWriter.getDescription(outputDir);

    SimplePipeline.runPipeline(reader, xmiWriter);
    List<File> errorFiles = new ArrayList<File>();
    String[] fileNames = new String[] {
        "a-test1.txt.1.xmi",
        "a-test1.txt.2.xmi",
        "a-test1.txt.3.xmi",
        "a-test1.txt.4.xmi",
        "a-test1.txt.5.xmi",
        "a-test1.txt.6.xmi",
        "a-test1.txt.7.xmi",
        "a-test1.txt.8.xmi",
        "b-test2.dat.9.xmi",
        "b-test2.dat.10.xmi",
        "b-test2.dat.11.xmi",
        "b-test2.dat.12.xmi" };
    for (String fileName : fileNames) {
      File file = new File(outputDir, fileName);
      if (!file.exists()) {
        errorFiles.add(file);
      }
    }

    if (errorFiles.size() > 0) {
      StringBuilder message = new StringBuilder(
          "expected the following files but they were missing: ");
      for (File errorFile : errorFiles) {
        message.append("\n" + errorFile.getPath());
      }
      fail(message.toString());
    }

  }
}
