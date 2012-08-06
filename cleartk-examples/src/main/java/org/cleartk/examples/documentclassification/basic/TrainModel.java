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
import java.util.Arrays;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.examples.documentclassification.advanced.GoldDocumentCategoryAnnotator;
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
 * Illustrates how to train a simple document classification annotator. For a more in-depth example
 * that demonstrates ClearTK best practices including the use of more sophisticated feature
 * extractors and the evaluation framework refer to the examples in
 * org.cleartk.examples.document.classification
 * 
 * 
 * @author Lee Becker
 * 
 */
public class TrainModel {

  public static class Options extends Options_ImplBase {
    @Option(
        name = "--train-dir",
        usage = "Specify the directory containing the training documents.  This is used for cross-validation, and for training in a holdout set evaluation. "
            + "When we run this example we point to a directory containing training data from a subset of the 20 newsgroup corpus - i.e. a directory called '3news-bydate/train'")
    public File trainDirectory = new File("src/main/resources/data/3news-bydate/train");

    @Option(
        name = "--models-dir",
        usage = "specify the directory in which to write out the trained model files")
    public File modelsDirectory = new File("target/simple_document_classification/models");

    @Option(
        name = "--training-args",
        usage = "specify training arguments to be passed to the learner.  For multiple values specify -ta for each - e.g. '-ta -t -ta 0'")
    public List<String> trainingArguments = Arrays.asList("-t", "0");
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);

    // ////////////////////////////////////////
    // Create collection reader to load URIs
    // ////////////////////////////////////////
    CollectionReader reader = UriCollectionReader.getCollectionReaderFromDirectory(
        options.trainDirectory,
        UriCollectionReader.RejectSystemFiles.class,
        UriCollectionReader.RejectSystemDirectories.class);

    // ////////////////////////////////////////
    // Create document classification pipeline
    // ////////////////////////////////////////
    AggregateBuilder builder = new AggregateBuilder();

    // Convert URIs in CAS URI View to Plain Text
    builder.add(UriToDocumentTextAnnotator.getDescription());

    // Label documents with gold labels for training
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(GoldDocumentCategoryAnnotator.class));

    // NLP pre-processing components
    builder.add(SentenceAnnotator.getDescription()); // Sentence segmentation
    builder.add(TokenAnnotator.getDescription()); // Tokenization
    builder.add(DefaultSnowballStemmer.getDescription("English")); // Stemming

    // The simple document classification annotator
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        BasicDocumentClassificationAnnotator.class,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        LIBSVMStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        options.modelsDirectory));

    // ///////////////////////////////////////////
    // Run pipeline to create training data file
    // ///////////////////////////////////////////
    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());

    // //////////////////////////////////////////////////////////////////////////////
    // Train and write model
    // //////////////////////////////////////////////////////////////////////////////
    JarClassifierBuilder.trainAndPackage(
        options.modelsDirectory,
        options.trainingArguments.toArray(new String[options.trainingArguments.size()]));
  }

}
