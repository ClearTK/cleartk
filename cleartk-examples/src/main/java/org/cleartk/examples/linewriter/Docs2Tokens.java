/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.examples.linewriter;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.token.type.Token;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.ae.linewriter.AnnotationWriter;
import org.cleartk.util.ae.linewriter.LineWriter;
import org.cleartk.util.cr.UriCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class Docs2Tokens {

  public static class Options extends Options_ImplBase {
    @Option(
        name = "-i",
        aliases = "--inputFileName",
        usage = "specify the directory to read plain text files from",
        required = false)
    public File inputDirectoryName = new File("src/test/resources/data/twain");

    @Option(
        name = "-o",
        aliases = "--outputFileName",
        usage = "specify the file to write tokens to",
        required = false)
    public String outputFileName = "target/test/twain-tokens.txt";

  }

  public static void main(String[] args) throws UIMAException, IOException {

    Options options = new Options();
    options.parseOptions(args);

    CollectionReader reader = UriCollectionReader.getCollectionReaderFromDirectory(options.inputDirectoryName);

    AnalysisEngineDescription uriToText = UriToDocumentTextAnnotator.getDescription();

    AnalysisEngineDescription sentences = SentenceAnnotator.getDescription();

    AnalysisEngineDescription tokenizer = TokenAnnotator.getDescription();

    AnalysisEngineDescription posTagger = PosTaggerAnnotator.getDescription();

    AnalysisEngineDescription lineWriter = AnalysisEngineFactory.createPrimitiveDescription(
        LineWriter.class,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        options.outputFileName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        TokenAnnotationWriter.class.getName());

    SimplePipeline.runPipeline(reader, uriToText, sentences, tokenizer, posTagger, lineWriter);
    System.out.println("results written to " + options.outputFileName);
  }

  public static class TokenAnnotationWriter implements AnnotationWriter<Token> {

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
    }

    @Override
    public String writeAnnotation(JCas jCas, Token token) throws AnalysisEngineProcessException {
      return token.getCoveredText() + "\t" + token.getPos();
    }

  }
}