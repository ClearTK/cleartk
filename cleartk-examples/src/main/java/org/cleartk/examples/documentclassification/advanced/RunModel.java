/** 
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
package org.cleartk.examples.documentclassification.advanced;

import java.io.File;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.cleartk.examples.documentclassification.advanced.DocumentClassificationEvaluation.AnnotatorMode;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.pipeline.SimplePipeline;

/**
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * Main method for training a document classifier model. This is essentially a wrapper for
 * DocumentClassificationEvaluation, which has the bulk of the training logic.
 * 
 * @author Lee Becker
 * 
 */
public class RunModel {

  public static class Options extends Options_ImplBase {
    @Option(name = "--test-dir", usage = "Specify the directory containing the documents to label.")
    public File testDirectory = new File("src/main/resources/data/3news-bydate/train");

    @Option(
        name = "--models-dir",
        usage = "specify the directory in which to write out the trained model files")
    public File modelsDirectory = new File("target/document_classification/models");
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);

    List<File> testFiles = DocumentClassificationEvaluation.getFilesFromDirectory(options.testDirectory);

    DocumentClassificationEvaluation evaluation = new DocumentClassificationEvaluation(
        options.modelsDirectory);
    CollectionReader collectionReader = evaluation.getCollectionReader(testFiles);

    AggregateBuilder builder = DocumentClassificationEvaluation.createDocumentClassificationAggregate(
        options.modelsDirectory,
        AnnotatorMode.CLASSIFY);

    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());
  }
}
