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
import org.cleartk.examples.documentclassification.basic.BasicDocumentClassificationAnnotator;
import org.cleartk.examples.type.UsenetDocument;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.CombinedExtractor1;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;
import org.cleartk.ml.feature.transform.extractor.CentroidTfidfSimilarityExtractor;
import org.cleartk.ml.feature.transform.extractor.MinMaxNormalizationExtractor;
import org.cleartk.ml.feature.transform.extractor.TfidfExtractor;
import org.cleartk.ml.feature.transform.extractor.ZeroMeanUnitStddevExtractor;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;

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

  public static final String PARAM_TF_IDF_URI = "tfIdfUri";

  @ConfigurationParameter(
      name = PARAM_TF_IDF_URI,
      mandatory = false,
      description = "provides a URI where the tf*idf map will be written")
  protected URI tfIdfUri;

  public static final String PARAM_TF_IDF_CENTROID_SIMILARITY_URI = "tfIdfCentroidSimilarityUri";

  @ConfigurationParameter(
      name = PARAM_TF_IDF_CENTROID_SIMILARITY_URI,
      mandatory = false,
      description = "provides a URI where the tf*idf centroid data will be written")
  protected URI tfIdfCentroidSimilarityUri;

  public static final String PARAM_ZMUS_URI = "zmusUri";

  @ConfigurationParameter(
      name = PARAM_ZMUS_URI,
      mandatory = false,
      description = "provides a URI where the Zero Mean, Unit Std Dev feature data will be written")
  protected URI zmusUri;

  public static final String PARAM_MINMAX_URI = "minmaxUri";

  @ConfigurationParameter(
      name = PARAM_MINMAX_URI,
      mandatory = false,
      description = "provides a URI where the min-max feature normalizaation data will be written")
  protected URI minmaxUri;

  public static final String PREDICTION_VIEW_NAME = "ExampleDocumentClassificationPredictionView";

  public static final String TFIDF_EXTRACTOR_KEY = "Token";

  public static final String CENTROID_TFIDF_SIM_EXTRACTOR_KEY = "CentroidTfIdfSimilarity";

  public static final String ZMUS_EXTRACTOR_KEY = "ZMUSFeatures";

  public static final String MINMAX_EXTRACTOR_KEY = "MINMAXFeatures";

  private CombinedExtractor1<DocumentAnnotation> extractor;

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
      TfidfExtractor<String, DocumentAnnotation> tfIdfExtractor = initTfIdfExtractor();
      CentroidTfidfSimilarityExtractor<String, DocumentAnnotation> simExtractor = initCentroidTfIdfSimilarityExtractor();
      ZeroMeanUnitStddevExtractor<String, DocumentAnnotation> zmusExtractor = initZmusExtractor();
      MinMaxNormalizationExtractor<String, DocumentAnnotation> minmaxExtractor = initMinMaxExtractor();
      this.extractor = new CombinedExtractor1<DocumentAnnotation>(
          tfIdfExtractor,
          simExtractor,
          zmusExtractor,
          minmaxExtractor);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  private TfidfExtractor<String, DocumentAnnotation> initTfIdfExtractor() throws IOException {
    CleartkExtractor<DocumentAnnotation, Token> countsExtractor = new CleartkExtractor<DocumentAnnotation, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    TfidfExtractor<String, DocumentAnnotation> tfIdfExtractor = new TfidfExtractor<String, DocumentAnnotation>(
        DocumentClassificationAnnotator.TFIDF_EXTRACTOR_KEY,
        countsExtractor);

    if (this.tfIdfUri != null) {
      tfIdfExtractor.load(this.tfIdfUri);
    }
    return tfIdfExtractor;
  }

  private CentroidTfidfSimilarityExtractor<String, DocumentAnnotation> initCentroidTfIdfSimilarityExtractor()
      throws IOException {
    CleartkExtractor<DocumentAnnotation, Token> countsExtractor = new CleartkExtractor<DocumentAnnotation, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    CentroidTfidfSimilarityExtractor<String, DocumentAnnotation> simExtractor = new CentroidTfidfSimilarityExtractor<String, DocumentAnnotation>(
        DocumentClassificationAnnotator.CENTROID_TFIDF_SIM_EXTRACTOR_KEY,
        countsExtractor);

    if (this.tfIdfCentroidSimilarityUri != null) {
      simExtractor.load(this.tfIdfCentroidSimilarityUri);
    }
    return simExtractor;
  }

  private ZeroMeanUnitStddevExtractor<String, DocumentAnnotation> initZmusExtractor()
      throws IOException {
    CombinedExtractor1<DocumentAnnotation> featuresToNormalizeExtractor = new CombinedExtractor1<DocumentAnnotation>(
        new CountAnnotationExtractor<DocumentAnnotation>(Sentence.class),
        new CountAnnotationExtractor<DocumentAnnotation>(Token.class));

    ZeroMeanUnitStddevExtractor<String, DocumentAnnotation> zmusExtractor = new ZeroMeanUnitStddevExtractor<String, DocumentAnnotation>(
        ZMUS_EXTRACTOR_KEY,
        featuresToNormalizeExtractor);

    if (this.zmusUri != null) {
      zmusExtractor.load(this.zmusUri);
    }

    return zmusExtractor;
  }

  private MinMaxNormalizationExtractor<String, DocumentAnnotation> initMinMaxExtractor()
      throws IOException {
    CombinedExtractor1<DocumentAnnotation> featuresToNormalizeExtractor = new CombinedExtractor1<DocumentAnnotation>(
        new CountAnnotationExtractor<DocumentAnnotation>(Sentence.class),
        new CountAnnotationExtractor<DocumentAnnotation>(Token.class));

    MinMaxNormalizationExtractor<String, DocumentAnnotation> minmaxExtractor = new MinMaxNormalizationExtractor<String, DocumentAnnotation>(
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
    return AnalysisEngineFactory.createEngineDescription(
        DocumentClassificationAnnotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJarFile.toString());
  }

  public static class CountAnnotationExtractor<T extends Annotation> implements
      NamedFeatureExtractor1<T> {

    private Class<? extends Annotation> annotationType;

    private String name;

    public CountAnnotationExtractor(Class<? extends Annotation> annotationType) {
      this.annotationType = annotationType;
      this.name = "Count_" + this.annotationType.getName();
    }

    @Override
    public String getFeatureName() {
      return this.name;
    }

    @Override
    public List<Feature> extract(JCas view, Annotation focusAnnotation)
        throws CleartkExtractorException {
      List<?> annotations = JCasUtil.selectCovered(this.annotationType, focusAnnotation);
      return Arrays.asList(new Feature(this.name, annotations.size()));
    }
  }

}
