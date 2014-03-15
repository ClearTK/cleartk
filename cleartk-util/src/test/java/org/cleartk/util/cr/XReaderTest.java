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
package org.cleartk.util.cr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.CasIOUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.test.util.type.Token;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * Unit tests for org.cleartk.readers.DirectoryCollectionReader.
 * 
 * @author Philip Ogren
 */
public class XReaderTest extends DefaultTestBase {

  @Test
  public void testReader() throws Exception {
    CollectionReaderDescription desc = CollectionReaderFactory.createReaderDescription(
        XReader.class,
        FilesCollectionReader.PARAM_ROOT_FILE,
        "src/test/resources/data/xreader-test/xmi",
        FilesCollectionReader.PARAM_SUFFIXES,
        new String[] { ".xmi" });
    int count = 0;
    for (@SuppressWarnings("unused")
    JCas unused : new JCasIterable(desc)) {
      count += 1;
    }
    Assert.assertEquals(11, count);
  }

  @Test
  public void testReaderXmi() throws Exception {

    tokenBuilder.buildTokens(jCas, "I like\nspam!", "I like spam !", "PRP VB NN .");
    File outputFile = new File(outputDirectory, "test.xmi");
    CasIOUtil.writeXmi(jCas, outputFile);

    CollectionReaderDescription desc = CollectionReaderFactory.createReaderDescription(
        XReader.class,
        FilesCollectionReader.PARAM_ROOT_FILE,
        outputFile.getPath());

    jCas = new JCasIterable(desc).iterator().next();

    String jCasText = jCas.getDocumentText();
    String docText = "I like\nspam!";
    Assert.assertEquals(jCasText, docText);

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("I", token.getCoveredText());

  }

  @Test
  public void testReaderXcas() throws Exception {

    tokenBuilder.buildTokens(jCas, "I like\nspam!", "I like spam !", "PRP VB NN .");
    File outputFile = new File(outputDirectory, "test.xcas");
    FileOutputStream out = new FileOutputStream(outputFile);
    XCASSerializer ser = new XCASSerializer(jCas.getTypeSystem());
    XMLSerializer xmlSer = new XMLSerializer(out, false);
    ser.serialize(jCas.getCas(), xmlSer.getContentHandler());

    CollectionReaderDescription desc = CollectionReaderFactory.createReaderDescription(
        XReader.class,
        FilesCollectionReader.PARAM_ROOT_FILE,
        new File(outputDirectory, "test.xcas").getPath(),
        XReader.PARAM_XML_SCHEME,
        XReader.XCAS);

    jCas = new JCasIterable(desc).iterator().next();

    String jCasText = jCas.getDocumentText();
    String docText = "I like\nspam!";
    Assert.assertEquals(jCasText, docText);

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("I", token.getCoveredText());
  }

  @Test
  public void testDescriptor() throws UIMAException {
    try {
      CollectionReaderFactory.createReader(XReader.class);
      Assert.fail("expected exception with no file or directory specified");
    } catch (ResourceInitializationException e) {
    }

    CollectionReader reader = CollectionReaderFactory.createReader(
        XReader.class,
        FilesCollectionReader.PARAM_ROOT_FILE,
        outputDirectory.getPath());

    Object fileOrDirectory = reader.getConfigParameterValue(FilesCollectionReader.PARAM_ROOT_FILE);
    Assert.assertEquals(outputDirectory.getPath(), fileOrDirectory);

  }

  // last time I ran this, I had to comment out a couple of lines in CleartkTestBase that
  // set up the temp output directory.
  public static void main(String[] args) throws Exception {
    XReaderTest xrt = new XReaderTest();
    xrt.setUp();
    xrt.buildTestXmiFiles();
  }

  private static void writeTestXMI(JCas jCas, String name) throws IOException {
    CasIOUtil.writeXmi(jCas, new File("src/test/resources/data/xreader-test/xmi/", name));
  }

  private void buildTestXmiFiles() throws Exception {
    tokenBuilder.buildTokens(jCas, "This is a test.", "This is a test .", "t1 t2 t3 t4 t5");
    writeTestXMI(jCas, "1.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(
        jCas,
        "This is only a test.",
        "This is only a test .",
        "t1 t2 t3 t4 t5 t6");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "A B C D", "A B C D", "t1 t2 t3 t4");
    writeTestXMI(jCas, "2.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "1 2 3 4", "1 2 3 4", "tA tB tC tD");
    writeTestXMI(jCas, "3.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "1 2 3 4", "1 2 3 4", "tA tB tC tD");
    writeTestXMI(jCas, "4.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "10 20 30 40 50", "10 20 30 40 50", "1 2 3 4 5");
    writeTestXMI(jCas, "5.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(
        jCas,
        "first sentence. second sentence.",
        "first sentence . \n second sentence .",
        "1 2 3 1 2 3");
    writeTestXMI(jCas, "6.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "A1 A1 B2 B2", "A1 A1 B2 B2", "A A B B");
    writeTestXMI(jCas, "7.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "AAAA", "A A A A", "1 2 3 4");
    writeTestXMI(jCas, "8.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "AA AA", "AA AA", "1 2");
    writeTestXMI(jCas, "9.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "A", "A", "1");
    writeTestXMI(jCas, "10.xmi");
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "b", "b", "BB1");
    writeTestXMI(jCas, "11.xmi");
  }

}
