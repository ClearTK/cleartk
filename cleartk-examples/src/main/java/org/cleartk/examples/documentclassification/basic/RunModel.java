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
package org.cleartk.examples.documentclassification.basic;

import java.io.File;

import org.apache.uima.collection.CollectionReader;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * Illustrates how to run a basic document classification annotator, once it is trained using the
 * TrainModel class. For a more in-depth example that demonstrates ClearTK best practices including
 * the use of more sophisticated feature extractors and the evaluation framework refer to the
 * examples in org.cleartk.examples.document.classification.advanced.
 * 
 * @author Lee Becker
 * 
 */
public class RunModel {

  public static class Options extends Options_ImplBase {
    @Option(name = "--test-dir", usage = "Specify the directory containing the documents to label.")
    public File testDirectory = new File("src/main/resources/data/3news-bydate/test");

    @Option(
        name = "--models-dir",
        usage = "specify the directory in which to write out the trained model files")
    public File modelsDirectory = new File("target/simple_document_classification/models");
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);

    // ////////////////////////////////////////
    // Create collection reader to load URIs
    // ////////////////////////////////////////
    CollectionReader reader = UriCollectionReader.getCollectionReaderFromDirectory(
        options.testDirectory,
        UriCollectionReader.RejectSystemFiles.class,
        UriCollectionReader.RejectSystemDirectories.class);

    // ////////////////////////////////////////
    // Create document classification pipeline
    // ////////////////////////////////////////
    AggregateBuilder builder = new AggregateBuilder();

    // Convert URIs in CAS URI View to Plain Text
    builder.add(UriToDocumentTextAnnotator.getDescription());

    // NLP pre-processing components
    builder.add(SentenceAnnotator.getDescription()); // Sentence segmentation
    builder.add(TokenAnnotator.getDescription()); // Tokenization
    builder.add(DefaultSnowballStemmer.getDescription("English")); // Stemming

    // Simple document classification annotator
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        BasicDocumentClassificationAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(options.modelsDirectory, "model.jar").getPath()));

    // //////////////////////////////////////////////////////////////////////////////
    // Run pipeline and classify documents
    // //////////////////////////////////////////////////////////////////////////////
    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
  }
}
