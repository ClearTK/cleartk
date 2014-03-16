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

package org.cleartk.examples.pos;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.corpus.penntreebank.TreebankGoldAnnotator;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.jar.Train;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 *         For examples of using the ExamplePOSAnnotationHandler using different classifiers, please
 *         see org.cleartk.example.pos.ExamplePosClassifierTest
 */

public class BuildTestExamplePosModel {

  public static void main(String... args) throws Exception {
    String outputDirectory = ExamplePosAnnotator.DEFAULT_OUTPUT_DIRECTORY;

    // select all the .tree files in the treebank directory
    File treebankDirectory = new File("data/pos/treebank");
    IOFileFilter treeFileFilter = FileFilterUtils.suffixFileFilter(".tree");
    Collection<File> files = FileUtils.listFiles(treebankDirectory, treeFileFilter, null);

    // A collection reader that creates one CAS per file, containing the file's URI
    CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(files);

    // The pipeline of annotators
    AggregateBuilder builder = new AggregateBuilder();

    // An annotator that creates an empty treebank view in the CAS
    builder.add(AnalysisEngineFactory.createEngineDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        PennTreebankReader.TREEBANK_VIEW));

    // An annotator that reads the treebank-formatted text into the treebank view
    builder.add(
        UriToDocumentTextAnnotator.getDescription(),
        CAS.NAME_DEFAULT_SOFA,
        PennTreebankReader.TREEBANK_VIEW);

    // An annotator that uses the treebank text to add tokens and POS tags to the CAS
    builder.add(TreebankGoldAnnotator.getDescriptionPOSTagsOnly());

    // The POS annotator, configured to write training data
    builder.add(ExamplePosAnnotator.getWriterDescription(outputDirectory));

    // Run the pipeline of annotators on each of the CASes produced by the reader
    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());

    System.out.println("training data written to " + ExamplePosAnnotator.DEFAULT_OUTPUT_DIRECTORY);
    System.out.println("training model...");

    // Train a classifier on the training data, and package it into a .jar file
    Train.main(outputDirectory);

    System.out.println("model written to "
        + JarClassifierBuilder.getModelJarFile(ExamplePosAnnotator.DEFAULT_OUTPUT_DIRECTORY).getPath());

  }
}
