package org.cleartk.examples.featuretransformation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.classifier.feature.transform.extractor.CentroidTfidfSimilarityExtractor;
import org.cleartk.classifier.feature.transform.extractor.MinMaxNormalizationExtractor;
import org.cleartk.classifier.feature.transform.extractor.TfidfExtractor;
import org.cleartk.classifier.feature.transform.extractor.ZeroMeanUnitStddevExtractor;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.MultiClassLIBSVMDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.examples.type.UsenetDocument;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.ViewTextCopierAnnotator;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

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
    Evaluation_ImplBase<File, AnnotationStatistics> {

  public static class Options extends Options_ImplBase {
    @Option(
        name = "--train-dir",
        usage = "Specify the directory containing the training documents.  This is used for cross-validation, and for training in a holdout set evaluation. "
            + "When we run this example we point to a directory containing training data from a subset of the 20 newsgroup corpus - i.e. a directory called '3news-bydate/train'")
    public File trainDirectory = new File("src/main/resources/data/3news-bydate/train");

    @Option(
        name = "--test-dir",
        usage = "Specify the directory containing the test (aka holdout/validation) documents.  This is for holdout set evaluation. "
            + "When we run this example we point to a directory containing training data from a subset of the 20 newsgroup corpus - i.e. a directory called '3news-bydate/test'")
    public File testDirectory = new File("src/main/resources/data/3news-bydate/test");

    @Option(
        name = "-m",
        aliases = "--modelsDirectory",
        usage = "specify the directory in which to write out the trained model files")
    public File modelsDirectory = new File("target/document_classification/models");

    @Option(
        name = "-ta",
        aliases = "--trainingArguments",
        usage = "specify training arguments to be passed to the learner.  For multiple values specify -ta for each - e.g. '-ta -t -ta 0'")
    public List<String> trainingArguments = Arrays.asList("-t", "0");
  }

  public static List<File> getFilesFromDirectory(File directory) {
    IOFileFilter svnFileFilter = FileFilterUtils.makeSVNAware(null);
    IOFileFilter dirFilter = FileFilterUtils.makeSVNAware(FileFilterUtils.directoryFileFilter());
    return new ArrayList<File>(FileUtils.listFiles(directory, svnFileFilter, dirFilter));
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);

    List<File> trainFiles = getFilesFromDirectory(options.trainDirectory);
    List<File> testFiles = getFilesFromDirectory(options.testDirectory);

    String[] ta = { "-t", "0" };
    DocumentClassificationEvaluation evaluation = new DocumentClassificationEvaluation(
        options.modelsDirectory,
        ta);

    // Run Cross Validation
    List<AnnotationStatistics> foldStats = evaluation.crossValidation(trainFiles, 2);
    AnnotationStatistics crossValidationStats = AnnotationStatistics.addAll(foldStats);

    System.err.println("Cross Validation Results:");
    System.err.print(crossValidationStats);
    System.err.println();

    // Run Holdout Set
    AnnotationStatistics holdoutStats = evaluation.trainAndTest(trainFiles, testFiles);
    System.err.println("Holdout Set Results:");
    System.err.print(holdoutStats);
    System.err.println();
  }

  public static final String GOLD_VIEW_NAME = "DocumentClassificationGoldView";

  public static final String SYSTEM_VIEW_NAME = CAS.NAME_DEFAULT_SOFA;

  private String[] trainingArguments;

  public DocumentClassificationEvaluation(File baseDirectory, String[] trainingArguments) {
    super(baseDirectory);
    this.trainingArguments = trainingArguments;
  }

  @Override
  protected CollectionReader getCollectionReader(List<File> items) throws Exception {
    // convert the List<File> to a String[]
    String[] paths = new String[items.size()];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = items.get(i).getPath();
    }

    return UriCollectionReader.getCollectionReaderFromFiles(items);
    /*
     * return CollectionReaderFactory.createCollectionReader( FilesCollectionReader.class,
     * ExampleComponents.TYPE_SYSTEM_DESCRIPTION, //
     * TypeSystemDescriptionFactory.createTypeSystemDescription(),
     * FilesCollectionReader.PARAM_ROOT_FILE, new File(""), FilesCollectionReader.PARAM_FILE_NAMES,
     * paths);
     */
  }

  @Override
  public void train(CollectionReader collectionReader, File outputDirectory) throws Exception {

    Iterable<Instance<String>> instances = this.extractFeaturesAndWriteInstances(
        collectionReader,
        outputDirectory);

    this.transformFeaturesAndWriteTrainingData(instances, outputDirectory);
    this.trainAndWriteModel(outputDirectory);
  }

  protected Iterable<Instance<String>> extractFeaturesAndWriteInstances(
      CollectionReader collectionReader,
      File outputDirectory) throws Exception {
    // First pass: Extract features and serialize the instances
    // Note: DocumentClassificationAnnotator sets the various extractor URI values to null by
    // default. This signals to the feature extractors that they are being written out for training
    System.out.println("Extracting raw features and writing instances...");

    AggregateBuilder builder = new AggregateBuilder();

    builder.add(UriToDocumentTextAnnotator.getDescription());

    // NLP pre-processing components
    builder.add(SentenceAnnotator.getDescription());
    builder.add(TokenAnnotator.getDescription());
    builder.add(DefaultSnowballStemmer.getDescription("English"));

    // Annotates the text in the default view with a gold standard UsenetDocument category
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        GoldDocumentCategoryAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION));

    // Create the document classifier
    AnalysisEngineDescription documentClassificationAnnotatorDescription = AnalysisEngineFactory.createPrimitiveDescription(
        DocumentClassificationAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        InstanceDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory.getPath());
    builder.add(documentClassificationAnnotatorDescription);

    // Run the pipeline
    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());

    // Load the serialized data
    return InstanceStream.loadFromDirectory(outputDirectory);
  }

  protected void transformFeaturesAndWriteTrainingData(
      Iterable<Instance<String>> instances,
      File outputDirectory) throws IOException, CleartkProcessingException {

    System.out.println("Transforming features and writing training data");

    // Collect TF*IDF stats for computing tf*idf values on extracted tokens
    URI tfIdfDataURI = DocumentClassificationAnnotator.createTokenTfIdfDataURI(outputDirectory);
    TfidfExtractor<String> extractor = new TfidfExtractor<String>(
        DocumentClassificationAnnotator.TFIDF_EXTRACTOR_KEY);
    extractor.train(instances);
    extractor.save(tfIdfDataURI);

    // Collect TF*IDF Centroid stats for computing similarity to corpus centroid
    URI tfIdfCentroidSimDataURI = DocumentClassificationAnnotator.createIdfCentroidSimilarityDataURI(outputDirectory);
    CentroidTfidfSimilarityExtractor<String> simExtractor = new CentroidTfidfSimilarityExtractor<String>(
        DocumentClassificationAnnotator.CENTROID_TFIDF_SIM_EXTRACTOR_KEY);
    simExtractor.train(instances);
    simExtractor.save(tfIdfCentroidSimDataURI);

    // Collect ZMUS stats for feature normalization
    URI zmusDataURI = DocumentClassificationAnnotator.createZmusDataURI(outputDirectory);
    ZeroMeanUnitStddevExtractor<String> zmusExtractor = new ZeroMeanUnitStddevExtractor<String>(
        DocumentClassificationAnnotator.ZMUS_EXTRACTOR_KEY);
    zmusExtractor.train(instances);
    zmusExtractor.save(zmusDataURI);

    // Collect MinMax stats for feature normalization
    URI minmaxDataURI = DocumentClassificationAnnotator.createMinMaxDataURI(outputDirectory);
    MinMaxNormalizationExtractor<String> minmaxExtractor = new MinMaxNormalizationExtractor<String>(
        DocumentClassificationAnnotator.MINMAX_EXTRACTOR_KEY);
    minmaxExtractor.train(instances);
    minmaxExtractor.save(minmaxDataURI);

    // Rerun training data writer pipeline, to transform the extracted instances -- an alternative,
    // more costly approach would be to reinitialize the DocumentClassifcationAnnotator above with
    // the URIs for the feature
    // extractor.
    //
    // In this example, we now write in the libsvm format
    System.out.println("Write out model training file");
    MultiClassLIBSVMDataWriter dataWriter = new MultiClassLIBSVMDataWriter(outputDirectory);
    for (Instance<String> instance : instances) {
      instance = extractor.transform(instance);
      instance = simExtractor.transform(instance);
      instance = zmusExtractor.transform(instance);
      instance = minmaxExtractor.transform(instance);
      dataWriter.write(instance);
    }
    dataWriter.finish();

  }

  protected void trainAndWriteModel(File outputDirectory) throws Exception {
    System.out.println("Training and writing model file.");
    // train the classifier and package it into a .jar file
    HideOutput hider = new HideOutput();
    JarClassifierBuilder.trainAndPackage(outputDirectory, this.trainingArguments);
    hider.restoreOutput();
  }

  @Override
  protected AnnotationStatistics test(CollectionReader collectionReader, File directory)
      throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(UriToDocumentTextAnnotator.getDescription());

    // NLP pre-processing components
    builder.add(SentenceAnnotator.getDescription());
    builder.add(TokenAnnotator.getDescription());
    builder.add(DefaultSnowballStemmer.getDescription("English"));

    // Copies the text from the default view to a separate gold view
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewTextCopierAnnotator.class,
        ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME,
        CAS.NAME_DEFAULT_SOFA,
        ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME,
        GOLD_VIEW_NAME));

    // Annotates the text in the gold view with a UsenetDocument category
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        GoldDocumentCategoryAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION), CAS.NAME_DEFAULT_SOFA, GOLD_VIEW_NAME);

    // Create document classifier
    AnalysisEngineDescription documentClassificationAnnotatorDescription = AnalysisEngineFactory.createPrimitiveDescription(
        DocumentClassificationAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(directory, "model.jar").getPath());
    ConfigurationParameterFactory.addConfigurationParameters(
        documentClassificationAnnotatorDescription,
        DocumentClassificationAnnotator.PARAM_TF_IDF_URI,
        DocumentClassificationAnnotator.createTokenTfIdfDataURI(directory),
        DocumentClassificationAnnotator.PARAM_TF_IDF_CENTROID_SIMILARITY_URI,
        DocumentClassificationAnnotator.createIdfCentroidSimilarityDataURI(directory),
        DocumentClassificationAnnotator.PARAM_MINMAX_URI,
        DocumentClassificationAnnotator.createMinMaxDataURI(directory),
        DocumentClassificationAnnotator.PARAM_ZMUS_URI,
        DocumentClassificationAnnotator.createZmusDataURI(directory));
    builder.add(documentClassificationAnnotatorDescription);

    AnnotationStatistics stats = new AnnotationStatistics("category");

    AnalysisEngine engine = builder.createAggregate();
    for (JCas jCas : new JCasIterable(collectionReader, engine)) {
      JCas goldView = jCas.getView(GOLD_VIEW_NAME);
      JCas systemView = jCas.getView(DocumentClassificationEvaluation.SYSTEM_VIEW_NAME);

      // Get results from system and gold views, and update results accordingly
      Collection<UsenetDocument> goldCategories = JCasUtil.select(goldView, UsenetDocument.class);
      Collection<UsenetDocument> systemCategories = JCasUtil.select(
          systemView,
          UsenetDocument.class);
      stats.add(goldCategories, systemCategories);
    }

    return stats;
  }
}
