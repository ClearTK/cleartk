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
package org.cleartk.examples.documentclassification.advanced;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.transform.extractor.CentroidTfidfSimilarityExtractor;
import org.cleartk.classifier.feature.transform.extractor.MinMaxNormalizationExtractor;
import org.cleartk.classifier.feature.transform.extractor.TfidfExtractor;
import org.cleartk.classifier.feature.transform.extractor.ZeroMeanUnitStddevExtractor;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.examples.documentclassification.basic.BasicDocumentClassificationAnnotator;
import org.cleartk.examples.type.UsenetDocument;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * This class demonstrates how to write a new CleartkAnnotator. Like the
 * {@link BasicDocumentClassificationAnnotator}, this class is used for building and categorizing
 * documents according to their Usenet group. The feature extraction flow illustrates how to extract
 * more complex features that require aggregating statistics for transformation prior to training
 * and classification.
 * 
 * @author Lee Becker
 * 
 */
public class DocumentClassificationAnnotator extends CleartkAnnotator<String> {

  public static final String PARAM_TF_IDF_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      DocumentClassificationAnnotator.class,
      "tfIdfUri");

  @ConfigurationParameter(
      mandatory = false,
      description = "provides a URI where the tf*idf map will be written")
  protected URI tfIdfUri;

  public static final String PARAM_TF_IDF_CENTROID_SIMILARITY_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      DocumentClassificationAnnotator.class,
      "tfIdfCentroidSimilarityUri");

  @ConfigurationParameter(
      mandatory = false,
      description = "provides a URI where the tf*idf centroid data will be written")
  protected URI tfIdfCentroidSimilarityUri;

  public static final String PARAM_ZMUS_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      DocumentClassificationAnnotator.class,
      "zmusUri");

  @ConfigurationParameter(
      mandatory = false,
      description = "provides a URI where the Zero Mean, Unit Std Dev feature data will be written")
  protected URI zmusUri;

  public static final String PARAM_MINMAX_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      DocumentClassificationAnnotator.class,
      "minmaxUri");

  @ConfigurationParameter(
      mandatory = false,
      description = "provides a URI where the min-max feature normalizaation data will be written")
  protected URI minmaxUri;

  public static final String PREDICTION_VIEW_NAME = "ExampleDocumentClassificationPredictionView";

  public static final String TFIDF_EXTRACTOR_KEY = "Token";

  public static final String CENTROID_TFIDF_SIM_EXTRACTOR_KEY = "CentroidTfIdfSimilarity";

  public static final String ZMUS_EXTRACTOR_KEY = "LengthFeatures";

  public static final String MINMAX_EXTRACTOR_KEY = "LengthFeatures";

  private CombinedExtractor extractor;

  public static URI createTokenTfIdfDataURI(File outputDirectoryName) {
    File f = new File(outputDirectoryName, TFIDF_EXTRACTOR_KEY + "_tfidf_extractor.dat");
    return f.toURI();
  }

  public static URI createIdfCentroidSimilarityDataURI(File outputDirectoryName) {
    File f = new File(outputDirectoryName, CENTROID_TFIDF_SIM_EXTRACTOR_KEY);
    return f.toURI();
  }

  public static URI createZmusDataURI(File outputDirectoryName) {
    File f = new File(outputDirectoryName, ZMUS_EXTRACTOR_KEY + "_zmus_extractor.dat");
    return f.toURI();
  }

  public static URI createMinMaxDataURI(File outputDirectoryName) {
    File f = new File(outputDirectoryName, MINMAX_EXTRACTOR_KEY + "_minmax_extractor.dat");
    return f.toURI();
  }

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    try {
      TfidfExtractor<String> tfIdfExtractor = initTfIdfExtractor();
      CentroidTfidfSimilarityExtractor<String> simExtractor = initCentroidTfIdfSimilarityExtractor();
      ZeroMeanUnitStddevExtractor<String> zmusExtractor = initZmusExtractor();
      MinMaxNormalizationExtractor<String> minmaxExtractor = initMinMaxExtractor();
      this.extractor = new CombinedExtractor(
          tfIdfExtractor,
          simExtractor,
          zmusExtractor,
          minmaxExtractor);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  private TfidfExtractor<String> initTfIdfExtractor() throws IOException {
    CleartkExtractor countsExtractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    TfidfExtractor<String> tfIdfExtractor = new TfidfExtractor<String>(
        DocumentClassificationAnnotator.TFIDF_EXTRACTOR_KEY,
        countsExtractor);

    if (this.tfIdfUri != null) {
      tfIdfExtractor.load(this.tfIdfUri);
    }
    return tfIdfExtractor;
  }

  private CentroidTfidfSimilarityExtractor<String> initCentroidTfIdfSimilarityExtractor()
      throws IOException {
    CleartkExtractor countsExtractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    CentroidTfidfSimilarityExtractor<String> simExtractor = new CentroidTfidfSimilarityExtractor<String>(
        DocumentClassificationAnnotator.CENTROID_TFIDF_SIM_EXTRACTOR_KEY,
        countsExtractor);

    if (this.tfIdfCentroidSimilarityUri != null) {
      simExtractor.load(this.tfIdfCentroidSimilarityUri);
    }
    return simExtractor;
  }

  private ZeroMeanUnitStddevExtractor<String> initZmusExtractor() throws IOException {
    CombinedExtractor featuresToNormalizeExtractor = new CombinedExtractor(
        new CountAnnotationExtractor(Sentence.class),
        new CountAnnotationExtractor(Token.class));

    ZeroMeanUnitStddevExtractor<String> zmusExtractor = new ZeroMeanUnitStddevExtractor<String>(
        ZMUS_EXTRACTOR_KEY,
        featuresToNormalizeExtractor);

    if (this.zmusUri != null) {
      zmusExtractor.load(this.zmusUri);
    }

    return zmusExtractor;
  }

  private MinMaxNormalizationExtractor<String> initMinMaxExtractor() throws IOException {
    CombinedExtractor featuresToNormalizeExtractor = new CombinedExtractor(
        new CountAnnotationExtractor(Sentence.class),
        new CountAnnotationExtractor(Token.class));

    MinMaxNormalizationExtractor<String> minmaxExtractor = new MinMaxNormalizationExtractor<String>(
        MINMAX_EXTRACTOR_KEY,
        featuresToNormalizeExtractor);

    if (this.minmaxUri != null) {
      minmaxExtractor.load(this.minmaxUri);
    }

    return minmaxExtractor;
  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();

    Instance<String> instance = new Instance<String>();
    instance.addAll(this.extractor.extract(jCas, doc));

    if (isTraining()) {
      UsenetDocument document = JCasUtil.selectSingle(jCas, UsenetDocument.class);
      instance.setOutcome(document.getCategory());
      this.dataWriter.write(instance);
    } else {
      // This is classification, so classify and create UsenetDocument annotation
      String result = this.classifier.classify(instance.getFeatures());
      UsenetDocument document = new UsenetDocument(jCas, 0, jCas.getDocumentText().length());
      document.setCategory(result);
      document.addToIndexes();
      // System.out.println("classified " + ViewURIUtil.getURI(jCas) + " as " + result + ".");
    }
  }

  public static AnalysisEngineDescription getClassifierDescription(File classifierJarFile)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        DocumentClassificationAnnotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJarFile.toString());
  }

  public static class CountAnnotationExtractor implements SimpleFeatureExtractor {

    @SuppressWarnings("rawtypes")
    private Class annotationType;

    @SuppressWarnings("rawtypes")
    public CountAnnotationExtractor(Class annotationType) {
      this.annotationType = annotationType;
    }

    @Override
    public List<Feature> extract(JCas view, Annotation focusAnnotation)
        throws CleartkExtractorException {

      @SuppressWarnings({ "rawtypes", "unchecked" })
      List annotations = JCasUtil.selectCovered(this.annotationType, focusAnnotation);
      return Arrays.asList(new Feature("Count_" + annotationType.getName(), annotations.size()));
    }
  }

}
