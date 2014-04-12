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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.examples.documentclassification.advanced.DocumentClassificationEvaluation.AnnotatorMode;
import org.cleartk.examples.type.UsenetDocument;
import org.cleartk.util.ViewUriUtil;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

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

  public interface Options {
    @Option(
        longName = "test-dir",
        description = "Specify the directory containing the documents to label.",
        defaultValue = "data/3news-bydate/test")
    public File getTestDirectory();

    @Option(
        longName = "models-dir",
        description = "specify the directory containing the trained model jar",
        defaultValue = "target/document_classification/models")
    public File getModelsDirectory();
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);

    List<File> testFiles = DocumentClassificationEvaluation.getFilesFromDirectory(options.getTestDirectory());

    DocumentClassificationEvaluation evaluation = new DocumentClassificationEvaluation(
        options.getModelsDirectory());
    CollectionReader collectionReader = evaluation.getCollectionReader(testFiles);

    AggregateBuilder builder = DocumentClassificationEvaluation.createDocumentClassificationAggregate(
        options.getModelsDirectory(),
        AnnotatorMode.CLASSIFY);

    SimplePipeline.runPipeline(
        collectionReader,
        builder.createAggregateDescription(),
        AnalysisEngineFactory.createEngineDescription(PrintClassificationsAnnotator.class));
  }

  public static class PrintClassificationsAnnotator extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      UsenetDocument document = JCasUtil.select(jCas, UsenetDocument.class).iterator().next();
      System.out.println("classified " + ViewUriUtil.getURI(jCas) + " as " + document.getCategory()
          + ".");
    }
  }
}
