/* 
 * Copyright (c) 2010, Regents of the University of Colorado 
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

package org.cleartk.token.pos;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.util.FileUtils;
import org.cleartk.token.TokenComponents;
import org.cleartk.token.TokenTestBase;
import org.cleartk.token.tokenizer.chunk.ChunkTokenizerFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.ae.linewriter.LineWriter;
import org.cleartk.util.ae.linewriter.block.BlankLineBlockWriter;
import org.cleartk.util.ae.linewriter.block.DocumentIdBlockWriter;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class TokenPOSWriterTest extends TokenTestBase {

  static String newline = System.getProperty("line.separator");

  @Test
  public void testTokenPosWriter() throws Exception {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        LineWriter.class,
        typeSystemDescription,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        new File(outputDirectory, "output.txt").getPath(),
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        TokenPOSWriter.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        Sentence.class.getName(),
        LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME,
        BlankLineBlockWriter.class.getName());

    String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
        "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
    engine.process(jCas);
    engine.collectionProcessComplete();

    String expectedText = newline + "Me\t1" + newline + "and\t2" + newline + "all\t3" + newline
        + "my\t4" + newline + "friends\t5" + newline + "are\t6" + newline + "non-conformists\t7"
        + newline + ".\t8" + newline + newline + "I\t9" + newline + "will\t10" + newline
        + "subjugate\t11" + newline + "my\t12" + newline + "freedom\t13" + newline
        + "oppressor\t14" + newline + ".\t15" + newline;

    File outputFile = new File(this.outputDirectory, "output.txt");
    assertTrue(outputFile.exists());
    String actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(expectedText, actualText);
  }

  @Test
  public void testTokenPosWriter2() throws Exception {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        LineWriter.class,
        typeSystemDescription,
        LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectory.getPath(),
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_FILE_SUFFIX,
        "txt",
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        TokenPOSWriter.class.getName(),
        LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME,
        DocumentAnnotation.class.getName(),
        LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME,
        DocumentIdBlockWriter.class.getName());

    String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
    tokenBuilder.buildTokens(
        jCas,
        text,
        "Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
        "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
    ViewURIUtil.setURI(jCas, "1234");
    engine.process(jCas);
    engine.collectionProcessComplete();

    String expectedText = "1234" + newline + "Me\t1" + newline + "and\t2" + newline + "all\t3"
        + newline + "my\t4" + newline + "friends\t5" + newline + "are\t6" + newline
        + "non-conformists\t7" + newline + ".\t8" + newline + "I\t9" + newline + "will\t10"
        + newline + "subjugate\t11" + newline + "my\t12" + newline + "freedom\t13" + newline
        + "oppressor\t14" + newline + ".\t15" + newline;

    File outputFile = new File(this.outputDirectory, "1234.txt");
    assertTrue(outputFile.exists());
    String actualText = FileUtils.file2String(outputFile);
    Assert.assertEquals(expectedText, actualText);
  }

  @Test
  public void testLineWriterAfterChunkTokenizer() throws Exception {
    AnalysisEngine[] engines = new AnalysisEngine[] {
        AnalysisEngineFactory.createPrimitive(TokenComponents.createSubtokenizer()),
        AnalysisEngineFactory.createPrimitive(ChunkTokenizerFactory
            .createChunkTokenizer("src/test/resources/token/chunk/model.jar")),
        AnalysisEngineFactory.createPrimitive(
            LineWriter.class,
            typeSystemDescription,
            LineWriter.PARAM_OUTPUT_DIRECTORY_NAME,
            this.outputDirectory.getPath(),
            LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
            Token.class.getName()) };
    String s1 = "Philip Ogren didn't write this sentence.";
    String s2 = "ROIs are required for CD28-mediated activation of the NF-kappa B/CD28-responsive complex.";
    String text = s1 + "\n" + s2;
    jCas.setDocumentText(text);
    new Sentence(jCas, 0, s1.length()).addToIndexes();
    new Sentence(jCas, s1.length() + 1, text.length()).addToIndexes();
    ViewURIUtil.setURI(jCas, "id");
    for (AnalysisEngine engine : engines) {
      engine.process(jCas);
    }
    for (AnalysisEngine engine : engines) {
      engine.collectionProcessComplete();
    }

    // make sure there were two sentences
    int sentCount = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).size();
    Assert.assertEquals(2, sentCount);

    // make sure there no extra blank lines
    String text2 = FileUtils.file2String(new File(this.outputDirectory, "id"));
    boolean hasDoubleNewlines = Pattern.compile("\n\\s*\n").matcher(text2).find();
    Assert.assertFalse(hasDoubleNewlines);
  }

}
