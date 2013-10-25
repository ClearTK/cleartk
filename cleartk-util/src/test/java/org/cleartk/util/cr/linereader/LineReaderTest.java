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
package org.cleartk.util.cr.linereader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.JCasIterator;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ViewUriUtil;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */
public class LineReaderTest {

  private String inputDir = "src/test/resources/linereader";

  private String toURI(String relativePath) {
    return new File(inputDir, relativePath).toURI().toString();
  }

  @Test
  public void test1() throws Exception {
    String languageCode = "en-us";
    CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        LineReader.class,
        null,
        LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
        inputDir,
        LineReader.PARAM_LANGUAGE,
        languageCode,
        LineReader.PARAM_SUFFIXES,
        new String[] { ".txt" });

    JCasIterator jCasIterator = new JCasIterable(reader).iterator();

    test(
        jCasIterator,
        "# this file was created by Philip Ogren on Monday 10/27/2008",
        toURI("a-test1.txt") + "#1");
    test(jCasIterator, "# for more files like this please make your own", toURI("a-test1.txt")
        + "#2");
    test(jCasIterator, "A|This is the first sentence.", toURI("a-test1.txt") + "#3");
    test(jCasIterator, "B|This is the second sentence.  ", toURI("a-test1.txt") + "#4");
    test(
        jCasIterator,
        "C|You are likely completely absorbed by the narrative at this point.",
        toURI("a-test1.txt") + "#5");
    test(jCasIterator, "D|... but too bad!        	", toURI("a-test1.txt") + "#6");
    test(jCasIterator, "EEEK|will it ever end?  yes - very soon...", toURI("a-test1.txt") + "#7");
    test(jCasIterator, "Z|Fin", toURI("a-test1.txt") + "#8");

    reader = CollectionReaderFactory.createReaderDescription(
        LineReader.class,
        null,
        LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
        inputDir,
        LineReader.PARAM_LANGUAGE,
        languageCode,
        LineReader.PARAM_SUFFIXES,
        new String[] { ".dat" });

    jCasIterator = new JCasIterable(reader).iterator();

    test(jCasIterator, "//this file was also created on Monday 10/27/2008", toURI("b-test2.dat")
        + "#1");
    test(
        jCasIterator,
        "//please see a-test1.txt for an introduction to the material contained in this file.",
        toURI("b-test2.dat") + "#2");
    test(jCasIterator, "// another comment", toURI("b-test2.dat") + "#3");
    test(jCasIterator, "1234|a bc def ghij klmno pqrstu vwxyz", toURI("b-test2.dat") + "#4");
    assertFalse(jCasIterator.hasNext());
  }

  @Test
  public void test2() throws Exception {
    CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        LineReader.class,
        null,
        LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
        inputDir,
        LineReader.PARAM_LINE_HANDLER_CLASS_NAME,
        SimpleLineHandler.class.getName(),
        SimpleLineHandler.PARAM_DELIMITER,
        "|",
        LineReader.PARAM_SUFFIXES,
        new String[] { ".txt" },
        LineReader.PARAM_COMMENT_SPECIFIERS,
        new String[] { "#", "//" });

    JCasIterator jCasIterator = new JCasIterable(reader).iterator();

    test(jCasIterator, "This is the first sentence.", toURI("a-test1.txt") + "#A");
    test(jCasIterator, "This is the second sentence.  ", toURI("a-test1.txt") + "#B");
    test(
        jCasIterator,
        "You are likely completely absorbed by the narrative at this point.",
        toURI("a-test1.txt") + "#C");
    test(jCasIterator, "... but too bad!        	", toURI("a-test1.txt") + "#D");
    test(jCasIterator, "will it ever end?  yes - very soon...", toURI("a-test1.txt") + "#EEEK");
    test(jCasIterator, "Fin", toURI("a-test1.txt") + "#Z");

    reader = CollectionReaderFactory.createReaderDescription(
        LineReader.class,
        null,
        LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
        inputDir,
        LineReader.PARAM_LINE_HANDLER_CLASS_NAME,
        SimpleLineHandler.class.getName(),
        SimpleLineHandler.PARAM_DELIMITER,
        "|",
        LineReader.PARAM_SUFFIXES,
        new String[] { ".dat" },
        LineReader.PARAM_COMMENT_SPECIFIERS,
        new String[] { "#", "//" });
    jCasIterator = new JCasIterable(reader).iterator();
    test(jCasIterator, "a bc def ghij klmno pqrstu vwxyz", toURI("b-test2.dat") + "#1234");
    assertFalse(jCasIterator.hasNext());
  }

  @Test
  public void test3() throws Exception {
    File file = new File(inputDir, "b-test2.dat");

    CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        LineReader.class,
        null,
        LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
        file.getPath(),
        LineReader.PARAM_COMMENT_SPECIFIERS,
        new String[] { "//" },
        LineReader.PARAM_SKIP_BLANK_LINES,
        false);

    JCasIterator jCasIterator = new JCasIterable(reader).iterator();

    test(jCasIterator, "", file.toURI() + "#1");
    test(jCasIterator, "", file.toURI() + "#2");
    test(jCasIterator, "", file.toURI() + "#3");
    test(jCasIterator, "1234|a bc def ghij klmno pqrstu vwxyz", file.toURI() + "#4");
    test(jCasIterator, "", file.toURI() + "#5");
    test(jCasIterator, "    	", file.toURI() + "#6");
    assertFalse(jCasIterator.hasNext());
  }

  private void test(JCasIterator jCasIterator, String text, String uri) throws Exception {
    JCas jCas = jCasIterator.next();
    assertEquals(text, jCas.getDocumentText());
    assertEquals(uri, ViewUriUtil.getURI(jCas).toString());
  }

}
