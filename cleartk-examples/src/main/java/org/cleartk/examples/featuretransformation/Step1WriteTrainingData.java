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
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.DefaultInstanceDataWriterFactory;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.classifier.feature.transform.extractor.MinMaxNormalizationExtractor;
import org.cleartk.classifier.feature.transform.extractor.TfidfExtractor;
import org.cleartk.classifier.feature.transform.extractor.ZeroMeanUnitStddevExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.cr.FilesCollectionReader;
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

  public static class Options extends Options_ImplBase {
    @Option(
        name = "-d",
        aliases = "--documentDirectory",
        usage = "specify the directory containing the training documents.  When we run this example we point to a directory containing the 20 newsgroup corpus - i.e. a directory called '20news-bydate-train'")
    public File documentDirectory = new File(
        "src/main/resources/data/2newsgroups/2news-bydate-train");

    @Option(
        name = "-o",
        aliases = "--outputDirectory",
        usage = "specify the directory to write the training data to")
    public File outputDirectory = new File("target/examples/transform");
  }

  public static void main(String[] args) throws UIMAException, IOException {
    Options options = new Options();
    options.parseOptions(args);

    URI tfIdfDataURI = DocumentClassificationAnnotator.createTokenTfIdfDataURI(options.outputDirectory);
    URI zmusDataURI = DocumentClassificationAnnotator.createZmusDataURI(options.outputDirectory);
    URI minmaxDataURI = DocumentClassificationAnnotator.createMinMaxDataURI(options.outputDirectory);

    // First pass just write serialized instances
    System.out.println("Write Instances");
    AnalysisEngineDescription documentClassificationAnnotatorDescription = AnalysisEngineFactory.createPrimitiveDescription(
        DocumentClassificationAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        DefaultInstanceDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        options.outputDirectory.getPath());

    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReader(options.documentDirectory.getPath()),
        SentenceAnnotator.getDescription(),
        TokenAnnotator.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        AnalysisEngineFactory.createPrimitiveDescription(
            GoldAnnotator.class,
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION),
        documentClassificationAnnotatorDescription);

    // Collect TF*IDF stats
    Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(options.outputDirectory);
    TfidfExtractor<String> extractor = new TfidfExtractor<String>(
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
    DefaultMultiClassLIBSVMDataWriterFactory dataWriterFactory = new DefaultMultiClassLIBSVMDataWriterFactory();
    dataWriterFactory.setOutputDirectory(options.outputDirectory);
    DataWriter<String> dataWriter = dataWriterFactory.createDataWriter();
    for (Instance<String> instance : instances) {
      instance = extractor.transform(instance);
      instance = zmusExtractor.transform(instance);
      instance = minmaxExtractor.transform(instance);
      dataWriter.write(instance);
    }
    dataWriter.finish();
  }
}
