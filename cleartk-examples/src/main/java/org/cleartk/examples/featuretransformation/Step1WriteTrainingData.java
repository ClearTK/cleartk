/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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

package org.cleartk.examples.featuretransformation;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.DefaultInstanceDataWriterFactory;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.classifier.feature.transform.util.MinMaxNormalizationExtractor;
import org.cleartk.classifier.feature.transform.util.TfIdfExtractor;
import org.cleartk.classifier.feature.transform.util.ZeroMeanUnitStddevExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 */

public class Step1WriteTrainingData {

  public static class Args {
    @Option(
        name = "-d",
        aliases = "--documentDirectory",
        usage = "specify the directory containing the training documents.  When we run this example we point to a directory containing the 20 newsgroup corpus - i.e. a directory called '20news-bydate-train'")
    public String documentDirectory = "src/main/resources/data/2newsgroups/2news-bydate-train";

    @Option(
        name = "-o",
        aliases = "--outputDirectoryName",
        usage = "specify the directory to write the training data to")
    public String outputDirectoryName = "target/examples/transform";

    public static Args parseArguments(String[] stringArgs) {
      Args args = new Args();
      CmdLineParser parser = new CmdLineParser(args);
      try {
        parser.parseArgument(stringArgs);
      } catch (CmdLineException e) {
        e.printStackTrace();
        parser.printUsage(System.err);
        System.exit(1);
      }
      return args;
    }
  }

  public static void main(String[] stringArgs) throws UIMAException, IOException {

    Args args = Args.parseArguments(stringArgs);
    String documentDirectory = args.documentDirectory;
    String outputDirectoryName = args.outputDirectoryName;
    URI tfIdfDataURI = DocumentClassificationAnnotator.createTokenTfIdfDataURI(outputDirectoryName);
    URI zmusDataURI = DocumentClassificationAnnotator.createZmusDataURI(outputDirectoryName);
    URI minmaxDataURI = DocumentClassificationAnnotator.createMinMaxDataURI(outputDirectoryName);

    // First pass just write serialized instances
    System.out.println("Write Instances");
    AnalysisEngineDescription documentClassificationAnnotatorDescription = AnalysisEngineFactory.createPrimitiveDescription(
        DocumentClassificationAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultInstanceDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DocumentClassificationAnnotator.PARAM_TFIDF_URI,
        null,
        DocumentClassificationAnnotator.PARAM_ZMUS_URI,
        null,
        DocumentClassificationAnnotator.PARAM_MINMAX_URI,
        null);

    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReader(documentDirectory),
        SentenceAnnotator.getDescription(),
        TokenAnnotator.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        AnalysisEngineFactory.createPrimitiveDescription(
            GoldAnnotator.class,
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION),
        documentClassificationAnnotatorDescription);

    // Collect TF*IDF stats
    File outputDirectory = new File(outputDirectoryName);
    Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(outputDirectory);
    TfIdfExtractor<String> extractor = new TfIdfExtractor<String>(
        DocumentClassificationAnnotator.TFIDF_EXTRACTOR_KEY);
    extractor.train(instances);
    extractor.save(tfIdfDataURI);

    // Collect ZMUS stats
    ZeroMeanUnitStddevExtractor<String> zmusExtractor = new ZeroMeanUnitStddevExtractor<String>(
        DocumentClassificationAnnotator.ZMUS_EXTRACTOR_KEY);
    zmusExtractor.train(instances);
    zmusExtractor.save(zmusDataURI);

    // Collect MinMax stats
    MinMaxNormalizationExtractor<String> minmaxExtractor = new MinMaxNormalizationExtractor<String>(
        DocumentClassificationAnnotator.MINMAX_EXTRACTOR_KEY);
    minmaxExtractor.train(instances);
    minmaxExtractor.save(minmaxDataURI);

    // Rerun training data writer pipeline
    AnalysisEngineDescription documentClassificationAnnotatorDescription2 = AnalysisEngineFactory.createPrimitiveDescription(
        DocumentClassificationAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultMultiClassLIBSVMDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName,
        DocumentClassificationAnnotator.PARAM_TFIDF_URI,
        tfIdfDataURI.toString(),
        DocumentClassificationAnnotator.PARAM_ZMUS_URI,
        zmusDataURI.toString(),
        DocumentClassificationAnnotator.PARAM_MINMAX_URI,
        minmaxDataURI.toString());

    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReader(documentDirectory),
        SentenceAnnotator.getDescription(),
        TokenAnnotator.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        AnalysisEngineFactory.createPrimitiveDescription(
            GoldAnnotator.class,
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION),
        documentClassificationAnnotatorDescription2);

  }
}
