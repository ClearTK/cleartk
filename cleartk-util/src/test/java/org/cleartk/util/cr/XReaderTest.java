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
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Token;
import org.cleartk.util.ViewURIFileNamer;
import org.cleartk.util.ViewURIUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

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
  public void testReaderXmi() throws IOException, UIMAException {

    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(XWriter.class,
            typeSystemDescription, XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
            this.outputDirectory.getPath(), XWriter.PARAM_FILE_NAMER_CLASS_NAME,
            ViewURIFileNamer.class.getName());
    tokenBuilder.buildTokens(jCas, "I like\nspam!", "I like spam !", "PRP VB NN .");
    String uri = new File(outputDirectory, "test").toURI().toString();
    ViewURIUtil.setURI(jCas, uri);
    engine.process(jCas);
    engine.collectionProcessComplete();

    CollectionReader reader = CollectionReaderFactory.createCollectionReader(XReader.class,
            typeSystemDescription, FilesCollectionReader.PARAM_ROOT_FILE, new File(outputDirectory,
                    "test.xmi").getPath());

    Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

    jCas = new JCasIterable(reader).next();

    String jCasText = jCas.getDocumentText();
    String docText = "I like\nspam!";
    Assert.assertEquals(jCasText, docText);

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("I", token.getCoveredText());
    reader.close();

  }

  @Test
  public void testReaderXcas() throws IOException, UIMAException {

    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(XWriter.class,
            typeSystemDescription, XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
            this.outputDirectory.getPath(), XWriter.PARAM_XML_SCHEME_NAME, XWriter.XCAS,
            XWriter.PARAM_FILE_NAMER_CLASS_NAME, ViewURIFileNamer.class.getName());
    tokenBuilder.buildTokens(jCas, "I like\nspam!", "I like spam !", "PRP VB NN .");

    String uri = new File(outputDirectory, "test").toURI().toString();
    ViewURIUtil.setURI(jCas, uri);
    engine.process(jCas);
    engine.collectionProcessComplete();

    CollectionReader reader = CollectionReaderFactory.createCollectionReader(XReader.class,
            typeSystemDescription, FilesCollectionReader.PARAM_ROOT_FILE, new File(outputDirectory,
                    "test.xcas").getPath(), XReader.PARAM_XML_SCHEME, XReader.XCAS);

    Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

    jCas = new JCasIterable(reader).next();

    String jCasText = jCas.getDocumentText();
    String docText = "I like\nspam!";
    Assert.assertEquals(jCasText, docText);

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("I", token.getCoveredText());
    reader.close();

  }

  @Test
  public void testDescriptor() throws UIMAException, IOException {
    try {
      CollectionReaderFactory.createCollectionReader(XReader.class, typeSystemDescription);
      Assert.fail("expected exception with no file or directory specified");
    } catch (ResourceInitializationException e) {
    }

    CollectionReader reader = CollectionReaderFactory
            .createCollectionReader(XReader.class, typeSystemDescription,
                    FilesCollectionReader.PARAM_ROOT_FILE, outputDirectory.getPath());

    Object fileOrDirectory = reader.getConfigParameterValue(FilesCollectionReader.PARAM_ROOT_FILE);
    Assert.assertEquals(outputDirectory.getPath(), fileOrDirectory);

  }

}
