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

import java.io.File;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.ae.linewriter.LineWriter;
import org.cleartk.util.cr.UriCollectionReader;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

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

  public interface Options {
    @Option(
        shortName = "i",
        longName = "inputFileName",
        description = "specify the directory to read plain text files from",
        defaultValue = "src/test/resources/data/twain")
    public File getInputDirectory();

    @Option(
        shortName = "o",
        longName = "outputFileName",
        description = "specify the file to write sentences to",
        defaultValue = "target/test/twain-sentences.txt")
    public File getOutputFile();

  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);

    CollectionReader reader = UriCollectionReader.getCollectionReaderFromDirectory(options.getInputDirectory());

    AggregateBuilder builder = new AggregateBuilder();
    builder.add(UriToDocumentTextAnnotator.getDescription());
    builder.add(SentenceAnnotator.getDescription());
    builder.add(AnalysisEngineFactory.createEngineDescription(
        LineWriter.class,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        options.getOutputFile(),
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Sentence.class.getName()));

    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
    System.out.println("results written to " + options.getOutputFile());

  }
}
