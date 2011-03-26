/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ae.linewriter.LineWriter;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 *         <p>
 *         This is a very simple example that takes a collection of files in a directory, reads them
 *         in one at a time, performs sentence segmentation, and writes each sentence to a single
 *         file - one per line.
 * 
 */
public class Docs2Sentences {

  public static class Options extends Options_ImplBase {
    @Option(name = "-i", aliases = "--inputFileName", usage = "specify the directory to read plain text files from", required = false)
    public String inputDirectoryName = "src/test/resources/data/twain";

    @Option(name = "-o", aliases = "--outputFileName", usage = "specify the file to write sentences to", required = false)
    public String outputFileName = "target/test/twain-sentences.txt";

  }

  public static void main(String[] args) throws UIMAException, IOException {
    Options options = new Options();
    options.parseOptions(args);

    CollectionReader filesReader = FilesCollectionReader
        .getCollectionReader(options.inputDirectoryName);
    AnalysisEngine sentences = AnalysisEngineFactory.createPrimitive(SentenceAnnotator
        .getDescription());
    AnalysisEngine lineWriter = AnalysisEngineFactory.createPrimitive(
        LineWriter.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        options.outputFileName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Sentence.class.getName());

    SimplePipeline.runPipeline(filesReader, sentences, lineWriter);
    System.out.println("results written to " + options.outputFileName);

  }
}
