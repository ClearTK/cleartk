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
package org.cleartk.util.ae.linewriter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.cleartk.util.ViewUriUtil;
import org.cleartk.util.ae.linewriter.annotation.CoveredTextAnnotationWriter;
import org.cleartk.util.ae.linewriter.block.BlankLineBlockWriter;
import org.cleartk.util.ae.linewriter.block.DoNothingBlockWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class LineWriterTest extends DefaultTestBase {

  static String newline = System.getProperty("line.separator");

  private String outputFileName;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    outputFileName = new File(outputDirectory, "linewriter.txt").getPath();
  }

  @Test
  public void test1() throws Exception {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        LineWriter.class,
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        this.outputDirectory.getPath(),
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");

    String text = "What if we built a rocket ship made of cheese?" + newline
        + "We could fly it to the moon for repairs.";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "What if we built a rocket ship made of cheese ?\nWe could fly it to the moon for repairs .",
        "A B C D E F G H I J K L M N O P Q R S T U");
    ViewUriUtil.setURI(jCas, new File("1234").toURI());
    engine.process(jCas);
    engine.collectionProcessComplete();

    String expectedText = newline + "What" + newline + "if" + newline + "we" + newline + "built"
        + newline + "a" + newline + "rocket" + newline + "ship" + newline + "made" + newline + "of"
        + newline + "cheese" + newline + "?" + newline + "" + newline + "We" + newline + "could"
        + newline + "fly" + newline + "it" + newline + "to" + newline + "the" + newline + "moon"
        + newline + "for" + newline + "repairs" + newline + "." + newline;

    File outputFile = new File(this.outputDirectory, "1234.txt");
    assertTrue(outputFile.exists());
    String actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(expectedText, actualText);

    jCas.reset();
    tokenBuilder.buildTokens(
        jCas,
        text,
        "What if we \n built a rocket \n ship made of cheese ?\nWe could fly it \nto the moon for repairs .",
        "A B C D E F G H I J K L M N O P Q R S T U");
    ViewUriUtil.setURI(jCas, new File("1234").toURI());
    engine.process(jCas);
    engine.collectionProcessComplete();

    expectedText = newline + "What" + newline + "if" + newline + "we" + newline + "" + newline
        + "built" + newline + "a" + newline + "rocket" + newline + "" + newline + "ship" + newline
        + "made" + newline + "of" + newline + "cheese" + newline + "?" + newline + "" + newline
        + "We" + newline + "could" + newline + "fly" + newline + "it" + newline + "" + newline
        + "to" + newline + "the" + newline + "moon" + newline + "for" + newline + "repairs"
        + newline + "." + newline;

    outputFile = new File(this.outputDirectory, "1234.txt");
    assertTrue(outputFile.exists());
    actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(expectedText, actualText);
  }

  @Test
  public void test2() throws Exception {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        LineWriter.class,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        new File(outputDirectory, "output.txt").getPath(),
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        ExampleTokenWriter.class.getName());

    String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
        "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
    engine.process(jCas);
    engine.collectionProcessComplete();

    String expectedText = "Me\t1" + newline + "and\t2" + newline + "all\t3" + newline + "my\t4"
        + newline + "friends\t5" + newline + "are\t6" + newline + "non-conformists\t7" + newline
        + ".\t8" + newline + "I\t9" + newline + "will\t10" + newline + "subjugate\t11" + newline
        + "my\t12" + newline + "freedom\t13" + newline + "oppressor\t14" + newline + ".\t15"
        + newline;

    File outputFile = new File(this.outputDirectory, "output.txt");
    assertTrue(outputFile.exists());
    String actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(expectedText, actualText);
  }

  @Test
  public void test3() throws Exception {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        LineWriter.class,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        new File(outputDirectory, "output.txt").getPath(),
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        "org.apache.uima.jcas.tcas.DocumentAnnotation",
        LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME,
        DoNothingBlockWriter.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName());

    String text = "If you like line writer, then you should really check out line rider.";
    tokenBuilder.buildTokens(jCas, text);
    engine.process(jCas);
    Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Assert.assertEquals("If", token.getCoveredText());

    jCas.reset();
    text = "I highly recommend reading 'Three Cups of Tea' by Greg Mortenson.\n Swashbuckling action and inspirational story.";
    tokenBuilder.buildTokens(jCas, text);
    engine.process(jCas);

    DocumentAnnotation da = JCasUtil.selectByIndex(jCas, DocumentAnnotation.class, 0);
    assertNotNull(da);

    engine.collectionProcessComplete();

    String expectedText = "If you like line writer, then you should really check out line rider."
        + newline + "I highly recommend reading 'Three Cups of Tea' by Greg Mortenson." + newline
        + "Swashbuckling action and inspirational story." + newline;

    File outputFile = new File(this.outputDirectory, "output.txt");
    assertTrue(outputFile.exists());
    String actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(expectedText, actualText);
  }

  @Test
  public void testDescriptor() throws Exception {
    ResourceInitializationException rie = getResourceInitializationException();
    assertNotNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        outputFileName);
    assertNotNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        "edu.cleartk.");
    assertNotNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName());
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        Token.class.getName());
    assertNotNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName());
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        outputFileName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName());
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        outputFileName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        outputFileName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Annotation.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        outputFileName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        String.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNotNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        DoNothingBlockWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNotNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME,
        SillyBlockWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNotNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME,
        BlankLineBlockWriter.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        ".txt");
    assertNull(rie);

    rie = getResourceInitializationException(
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectoryName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        ExampleTokenWriter.class.getName());
    assertNotNull(rie);
  }

  private ResourceInitializationException getResourceInitializationException(Object... params)
      throws Exception {
    ResourceInitializationException rie = null;
    try {
      AnalysisEngineFactory.createPrimitive(LineWriter.class, params);
    } catch (ResourceInitializationException e) {
      rie = e;
    }
    return rie;

  }

  @Test
  public void miscGenericTests() {
    assertTrue(Annotation.class.isAssignableFrom(Token.class));
    assertTrue(Annotation.class.isAssignableFrom(DocumentAnnotation.class));
  }

  @Test
  public void testTokenWriter() throws Exception {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        LineWriter.class,
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        this.outputDirectory.getPath(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        "txt",
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        CoveredTextAnnotationWriter.class.getName(),
        LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME,
        BlankLineBlockWriter.class.getName());

    String spacedTokens = "What if we built a large , wooden badger ?\nHmm? ";
    tokenBuilder.buildTokens(jCas, "What if we built\na large, wooden badger? Hmm?", spacedTokens);
    ViewUriUtil.setURI(jCas, new File("identifier").toURI());
    engine.process(jCas);
    engine.collectionProcessComplete();

    String expected = "\n" + spacedTokens.replace("\n", "\n\n").replace(' ', '\n');
    File outputFile = new File(this.outputDirectory, "identifier.txt");
    String actual = FileUtils.file2String(outputFile).replace("\r", "");
    Assert.assertEquals(expected, actual);
  }

}
