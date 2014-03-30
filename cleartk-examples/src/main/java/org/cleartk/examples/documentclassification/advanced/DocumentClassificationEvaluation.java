/** 
 * Copyright (c) 2007-2012, Regents of the University of Colorado 
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.pipeline.JCasIterator;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.examples.type.UsenetDocument;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.transform.InstanceDataWriter;
import org.cleartk.ml.feature.transform.InstanceStream;
import org.cleartk.ml.feature.transform.extractor.CentroidTfidfSimilarityExtractor;
import org.cleartk.ml.feature.transform.extractor.MinMaxNormalizationExtractor;
import org.cleartk.ml.feature.transform.extractor.TfidfExtractor;
import org.cleartk.ml.feature.transform.extractor.ZeroMeanUnitStddevExtractor;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.libsvm.LibSvmStringOutcomeDataWriter;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import com.google.common.base.Function;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This evaluation class provides a concrete example of how to train and evaluate classifiers.
 * Specifically this class will train a document categorizer using a subset of the 20 newsgroups
 * dataset. It evaluates performance using 2-fold cross validation as well as a holdout set.
 * <p>
 * 
 * Key points: <br>
 * <ul>
 * <li>Creating training and evaluation pipelines
 * <li>Example of feature transformation / normalization
 * </ul>
 * 
 * 
 * @author Lee Becker
 */
public class DocumentClassificationEvaluation extends
    Evaluation_ImplBase<File, AnnotationStatistics<String>> {

  public interface Options {
    @Option(
        longName = "train-dir",
        description = "Specify the directory containing the training documents.  This is used for cross-validation, and for training in a holdout set evaluation. "
            + "When we run this example we point to a directory containing training data from a subset of the 20 newsgroup corpus - i.e. a directory called '3news-bydate/train'",
        defaultValue = "data/3news-bydate/train")
    public File getTrainDirectory();

    @Option(
        longName = "test-dir",
        description = "Specify the directory containing the test (aka holdout/validation) documents.  This is for holdout set evaluation. "
            + "When we run this example we point to a directory containing training data from a subset of the 20 newsgroup corpus - i.e. a directory called '3news-bydate/test'",
        defaultValue = "data/3news-bydate/test")
    public File getTestDirectory();

    @Option(
        longName = "models-dir",
        description = "specify the directory in which to write out the trained model files",
        defaultValue = "target/document_classification/models")
    public File getModelsDirectory();

    @Option(
        longName = "training-args",
        description = "specify training arguments to be passed to the learner.  For multiple values specify -ta for each - e.g. '-ta -t -ta 0'",
        defaultValue = { "-t", "0" })
    public List<String> getTrainingArguments();
  }

  public static enum AnnotatorMode {
    TRAIN, TEST, CLASSIFY
  }

  public static List<File> getFilesFromDirectory(File directory) {
    IOFileFilter fileFilter = FileFilterUtils.makeSVNAware(HiddenFileFilter.VISIBLE);
    IOFileFilter dirFilter = FileFilterUtils.makeSVNAware(FileFilterUtils.and(
        FileFilterUtils.directoryFileFilter(),
        HiddenFileFilter.VISIBLE));
    return new ArrayList<File>(FileUtils.listFiles(directory, fileFilter, dirFilter));
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);

    List<File> trainFiles = getFilesFromDirectory(options.getTrainDirectory());
    List<File> testFiles = getFilesFromDirectory(options.getTestDirectory());

    DocumentClassificationEvaluation evaluation = new DocumentClassificationEvaluation(
        options.getModelsDirectory(),
        options.getTrainingArguments());

    // Run Cross Validation
    List<AnnotationStatistics<String>> foldStats = evaluation.crossValidation(trainFiles, 2);
    AnnotationStatistics<String> crossValidationStats = AnnotationStatistics.addAll(foldStats);

    System.err.println("Cross Validation Results:");
    System.err.print(crossValidationStats);
    System.err.println();
    System.err.println(crossValidationStats.confusions());
    System.err.println();

    // Run Holdout Set
    AnnotationStatistics<String> holdoutStats = evaluation.trainAndTest(trainFiles, testFiles);
    System.err.println("Holdout Set Results:");
    System.err.print(holdoutStats);
    System.err.println();
    System.err.println(holdoutStats.confusions());
  }

  public static final String GOLD_VIEW_NAME = "DocumentClassificationGoldView";

  public static final String SYSTEM_VIEW_NAME = CAS.NAME_DEFAULT_SOFA;

  private List<String> trainingArguments;

  public DocumentClassificationEvaluation(File baseDirectory) {
    super(baseDirectory);
    this.trainingArguments = Arrays.<String> asList();
  }

  public DocumentClassificationEvaluation(File baseDirectory, List<String> trainingArguments) {
    super(baseDirectory);
    this.trainingArguments = trainingArguments;
  }

  @Override
  protected CollectionReader getCollectionReader(List<File> items) throws Exception {
    return UriCollectionReader.getCollectionReaderFromFiles(items);
  }

  @Override
  public void train(CollectionReader collectionReader, File outputDirectory) throws Exception {
    // ////////////////////////////////////////////////////////////////////////////////
    // Step 1: Extract features and serialize the raw instance objects
    // Note: DocumentClassificationAnnotator sets the various extractor URI values to null by
    // default. This signals to the feature extractors that they are being written out for training
    // ////////////////////////////////////////////////////////////////////////////////
    System.err.println("Step 1: Extracting features and writing raw instances data");

    // Create and run the document classification training pipeline
    AggregateBuilder builder = DocumentClassificationEvaluation.createDocumentClassificationAggregate(
        outputDirectory,
        AnnotatorMode.TRAIN);
    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());

    // Load the serialized instance data
    Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(outputDirectory);

    // ////////////////////////////////////////////////////////////////////////////////
    // Step 2: Transform features and write training data
    // In this phase, the normalization statistics are computed and the raw
    // features are transformed into normalized features.
    // Then the adjusted values are written with a DataWriter (libsvm in this case)
    // for training
    // ////////////////////////////////////////////////////////////////////////////////
    System.err.println("Collection feature normalization statistics");

    // Collect TF*IDF stats for computing tf*idf values on extracted tokens
    URI tfIdfDataURI = DocumentClassificationAnnotator.createTokenTfIdfDataURI(outputDirectory);
    TfidfExtractor<String, DocumentAnnotation> extractor = new TfidfExtractor<String, DocumentAnnotation>(
        DocumentClassificationAnnotator.TFIDF_EXTRACTOR_KEY);
    extractor.train(instances);
    extractor.save(tfIdfDataURI);

    // Collect TF*IDF Centroid stats for computing similarity to corpus centroid
    URI tfIdfCentroidSimDataURI = DocumentClassificationAnnotator.createIdfCentroidSimilarityDataURI(outputDirectory);
    CentroidTfidfSimilarityExtractor<String, DocumentAnnotation> simExtractor = new CentroidTfidfSimilarityExtractor<String, DocumentAnnotation>(
        DocumentClassificationAnnotator.CENTROID_TFIDF_SIM_EXTRACTOR_KEY);
    simExtractor.train(instances);
    simExtractor.save(tfIdfCentroidSimDataURI);

    // Collect ZMUS stats for feature normalization
    URI zmusDataURI = DocumentClassificationAnnotator.createZmusDataURI(outputDirectory);
    ZeroMeanUnitStddevExtractor<String, DocumentAnnotation> zmusExtractor = new ZeroMeanUnitStddevExtractor<String, DocumentAnnotation>(
        DocumentClassificationAnnotator.ZMUS_EXTRACTOR_KEY);
    zmusExtractor.train(instances);
    zmusExtractor.save(zmusDataURI);

    // Collect MinMax stats for feature normalization
    URI minmaxDataURI = DocumentClassificationAnnotator.createMinMaxDataURI(outputDirectory);
    MinMaxNormalizationExtractor<String, DocumentAnnotation> minmaxExtractor = new MinMaxNormalizationExtractor<String, DocumentAnnotation>(
        DocumentClassificationAnnotator.MINMAX_EXTRACTOR_KEY);
    minmaxExtractor.train(instances);
    minmaxExtractor.save(minmaxDataURI);

    // Rerun training data writer pipeline, to transform the extracted instances -- an alternative,
    // more costly approach would be to reinitialize the DocumentClassificationAnnotator above with
    // the URIs for the feature
    // extractor.
    //
    // In this example, we now write in the libsvm format
    System.err.println("Write out model training data");
    LibSvmStringOutcomeDataWriter dataWriter = new LibSvmStringOutcomeDataWriter(outputDirectory);
    for (Instance<String> instance : instances) {
      instance = extractor.transform(instance);
      instance = simExtractor.transform(instance);
      instance = zmusExtractor.transform(instance);
      instance = minmaxExtractor.transform(instance);
      dataWriter.write(instance);
    }
    dataWriter.finish();

    // //////////////////////////////////////////////////////////////////////////////
    // Stage 3: Train and write model
    // Now that the features have been extracted and normalized, we can proceed
    // in running machine learning to train and package a model
    // //////////////////////////////////////////////////////////////////////////////
    System.err.println("Train model and write model.jar file.");
    HideOutput hider = new HideOutput();
    JarClassifierBuilder.trainAndPackage(
        outputDirectory,
        this.trainingArguments.toArray(new String[this.trainingArguments.size()]));
    hider.restoreOutput();
  }

  /**
   * Creates the preprocessing pipeline needed for document classification. Specifically this
   * consists of:
   * <ul>
   * <li>Populating the default view with the document text (as specified in the URIView)
   * <li>Sentence segmentation
   * <li>Tokenization
   * <li>Stemming
   * <li>[optional] labeling the document with gold-standard document categories
   * </ul>
   */
  public static AggregateBuilder createPreprocessingAggregate(
      File modelDirectory,
      AnnotatorMode mode) throws ResourceInitializationException {
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(UriToDocumentTextAnnotator.getDescription());

    // NLP pre-processing components
    builder.add(SentenceAnnotator.getDescription());
    builder.add(TokenAnnotator.getDescription());
    builder.add(DefaultSnowballStemmer.getDescription("English"));

    // Now annotate documents with gold standard labels
    switch (mode) {
      case TRAIN:
        // If this is training, put the label categories directly into the default view
        builder.add(AnalysisEngineFactory.createEngineDescription(GoldDocumentCategoryAnnotator.class));
        break;

      case TEST:
        // Copies the text from the default view to a separate gold view
        builder.add(AnalysisEngineFactory.createEngineDescription(
            ViewTextCopierAnnotator.class,
            ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME,
            CAS.NAME_DEFAULT_SOFA,
            ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME,
            GOLD_VIEW_NAME));

        // If this is testing, put the document categories in the gold view
        // The extra parameters to add() map the default view to the gold view.
        builder.add(
            AnalysisEngineFactory.createEngineDescription(GoldDocumentCategoryAnnotator.class),
            CAS.NAME_DEFAULT_SOFA,
            GOLD_VIEW_NAME);
        break;

      case CLASSIFY:
      default:
        // In normal mode don't deal with gold labels
        break;
    }

    return builder;
  }

  /**
   * Creates the aggregate builder for the document classification pipeline
   */
  public static AggregateBuilder createDocumentClassificationAggregate(
      File modelDirectory,
      AnnotatorMode mode) throws ResourceInitializationException {

    AggregateBuilder builder = DocumentClassificationEvaluation.createPreprocessingAggregate(
        modelDirectory,
        mode);

    switch (mode) {
      case TRAIN:
        // For training we will create DocumentClassificationAnnotator that
        // Extracts the features as is, and then writes out the data to
        // a serialized instance file.
        builder.add(AnalysisEngineFactory.createEngineDescription(
            DocumentClassificationAnnotator.class,
            DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
            InstanceDataWriter.class.getName(),
            DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
            modelDirectory.getPath()));
        break;
      case TEST:
      case CLASSIFY:
      default:
        // For testing and standalone classification, we want to create a
        // DocumentClassificationAnnotator using
        // all of the model data computed during training. This includes feature normalization data
        // and thei model jar file for the classifying algorithm
        AnalysisEngineDescription documentClassificationAnnotator = AnalysisEngineFactory.createEngineDescription(
            DocumentClassificationAnnotator.class,
            CleartkAnnotator.PARAM_IS_TRAINING,
            false,
            GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
            JarClassifierBuilder.getModelJarFile(modelDirectory));

        ConfigurationParameterFactory.addConfigurationParameters(
            documentClassificationAnnotator,
            DocumentClassificationAnnotator.PARAM_TF_IDF_URI,
            DocumentClassificationAnnotator.createTokenTfIdfDataURI(modelDirectory),
            DocumentClassificationAnnotator.PARAM_TF_IDF_CENTROID_SIMILARITY_URI,
            DocumentClassificationAnnotator.createIdfCentroidSimilarityDataURI(modelDirectory),
            DocumentClassificationAnnotator.PARAM_MINMAX_URI,
            DocumentClassificationAnnotator.createMinMaxDataURI(modelDirectory),
            DocumentClassificationAnnotator.PARAM_ZMUS_URI,
            DocumentClassificationAnnotator.createZmusDataURI(modelDirectory));
        builder.add(documentClassificationAnnotator);
        break;
    }
    return builder;
  }

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File directory)
      throws Exception {
    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();

    // Create the document classification pipeline
    AggregateBuilder builder = DocumentClassificationEvaluation.createDocumentClassificationAggregate(
        directory,
        AnnotatorMode.TEST);
    AnalysisEngine engine = builder.createAggregate();

    // Run and evaluate
    Function<UsenetDocument, ?> getSpan = AnnotationStatistics.annotationToSpan();
    Function<UsenetDocument, String> getCategory = AnnotationStatistics.annotationToFeatureValue("category");
    JCasIterator iter = new JCasIterator(collectionReader, engine);
    while (iter.hasNext()) {
      JCas jCas = iter.next();
      JCas goldView = jCas.getView(GOLD_VIEW_NAME);
      JCas systemView = jCas.getView(DocumentClassificationEvaluation.SYSTEM_VIEW_NAME);

      // Get results from system and gold views, and update results accordingly
      Collection<UsenetDocument> goldCategories = JCasUtil.select(goldView, UsenetDocument.class);
      Collection<UsenetDocument> systemCategories = JCasUtil.select(
          systemView,
          UsenetDocument.class);
      stats.add(goldCategories, systemCategories, getSpan, getCategory);
    }

    return stats;
  }
}