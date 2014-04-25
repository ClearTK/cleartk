/* 
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
package org.cleartk.ml.feature.transform.extractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.transform.TransformableFeature;

/**
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 */
public class CentroidTfidfSimilarityExtractor<OUTCOME_T, FOCUS_T extends Annotation> extends
    TfidfExtractor<OUTCOME_T, FOCUS_T> {

  private Map<String, Double> centroidMap;

  private SimilarityFunction simFunction;

  private static String docFreqFileSuffix = "_tfidf-centroid-extractor_idfmap.dat";

  private static String centroidMapFileSuffix = "_tfidf-centroid-extractor_centroidmap.dat";

  public static URI getDocumentFrequencyDataURI(String name, URI baseURI)
      throws MalformedURLException, URISyntaxException {
    return new URL(baseURI.toURL(), name + docFreqFileSuffix).toURI();
  }

  public static URI getCentroidDataURI(String name, URI baseURI) throws MalformedURLException,
      URISyntaxException {
    return new URL(baseURI.toURL(), name + centroidMapFileSuffix).toURI();
  }

  public CentroidTfidfSimilarityExtractor(String name) {
    super(name);
  }

  public CentroidTfidfSimilarityExtractor(String name, FeatureExtractor1<FOCUS_T> extractor) {
    super(name);
    this.subExtractor = extractor;
    this.isTrained = false;
    this.idfMap = new IDFMap();
  }

  @Override
  public Instance<OUTCOME_T> transform(Instance<OUTCOME_T> instance) {
    List<Feature> features = new ArrayList<Feature>();
    List<Feature> featuresToTransform = new ArrayList<Feature>();
    for (Feature feature : instance.getFeatures()) {
      if (this.isTransformable(feature)) {
        // Store off features for later similarity computation
        featuresToTransform.addAll(((TransformableFeature) feature).getFeatures());
      } else {
        // pass through non-transformable features
        features.add(feature);
      }
    }

    // Create centroid similarity feature
    Map<String, Double> featureMap = this.featuresToFeatureMap(featuresToTransform);
    features.add(new Feature(this.name, new Double(this.simFunction.distance(
        featureMap,
        centroidMap))));

    return new Instance<OUTCOME_T>(instance.getOutcome(), features);
  }

  public Map<String, Double> featuresToFeatureMap(List<Feature> features) {
    Map<String, Double> featureMap = new HashMap<String, Double>();
    for (Feature feature : features) {
      String termName = feature.getName();
      int tf = (Integer) feature.getValue();
      featureMap.put(termName, tf * this.idfMap.getIDF(termName));
    }
    return featureMap;
  }

  @Override
  public List<Feature> extract(JCas view, FOCUS_T focusAnnotation) throws CleartkExtractorException {

    List<Feature> extracted = this.subExtractor.extract(view, focusAnnotation);
    List<Feature> result = new ArrayList<Feature>();
    if (this.isTrained) {
      // We have trained / loaded a centroid tf*idf model, so now compute
      // a cosine similarity for the extracted values
      Map<String, Double> extractedFeatureMap = this.featuresToFeatureMap(extracted);
      result.add(new Feature(name, this.simFunction.distance(extractedFeatureMap, centroidMap)));

    } else {
      // We haven't trained this extractor yet, so just mark the existing features
      // for future modification, by creating one mega container feature
      result.add(new TransformableFeature(this.name, extracted));
    }

    return result;
  }

  protected Map<String, Double> computeCentroid(Iterable<Instance<OUTCOME_T>> instances, IDFMap idfs) {

    // Now compute centroid of all applicable terms (features) in all instances
    int numDocuments = idfs.getTotalDocumentCount();
    Map<String, Double> newCentroidMap = new HashMap<String, Double>();
    for (Instance<OUTCOME_T> instance : instances) {

      // Grab the matching tf*idf features from the set of all features in an instance
      for (Feature feature : instance.getFeatures()) {
        if (this.isTransformable(feature)) {
          // tf*idf features contain a list of features, these are actually what get added
          // to our document frequency map
          for (Feature untransformedFeature : ((TransformableFeature) feature).getFeatures()) {
            String termName = untransformedFeature.getName();
            int tf = (Integer) untransformedFeature.getValue();
            double tfidf = tf * idfs.getIDF(termName);
            double sumTfidf = (newCentroidMap.containsKey(termName))
                ? sumTfidf = newCentroidMap.get(termName)
                : 0.0;
            newCentroidMap.put(termName, sumTfidf + tfidf);
          }
        }
      }
    }

    for (Map.Entry<String, Double> entry : newCentroidMap.entrySet()) {
      double mean = entry.getValue() / numDocuments;
      newCentroidMap.put(entry.getKey(), mean);
    }
    return newCentroidMap;
  }

  @Override
  public void train(Iterable<Instance<OUTCOME_T>> instances) {
    this.idfMap = this.createIdfMap(instances);
    this.centroidMap = this.computeCentroid(instances, this.idfMap);
    this.isTrained = true;
    this.simFunction = new FixedCosineSimilarity(this.centroidMap);
  }

  @Override
  public void save(URI baseURI) throws IOException {
    URI documentFreqDataURI;
    URI centroidDataURI;
    try {
      documentFreqDataURI = getDocumentFrequencyDataURI(this.name, baseURI);
      centroidDataURI = getCentroidDataURI(this.name, baseURI);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }

    // Save off idfMap document frequency data
    this.idfMap.save(documentFreqDataURI);

    // Save off centroid map data
    File out = new File(centroidDataURI);
    BufferedWriter writer = null;
    writer = new BufferedWriter(new FileWriter(out));

    for (Map.Entry<String, Double> entry : this.centroidMap.entrySet()) {
      writer.append(String.format(Locale.ROOT, "%s\t%f\n", entry.getKey(), entry.getValue()));
    }
    writer.close();
  }

  public void load(URI baseURI) throws IOException {
    URI documentFreqDataURI;
    URI centroidDataURI;
    try {
      documentFreqDataURI = getDocumentFrequencyDataURI(this.name, baseURI);
      centroidDataURI = getCentroidDataURI(this.name, baseURI);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }

    // Load document frequency data
    this.idfMap.load(documentFreqDataURI);

    // Reads in centroid map as tab separated values (feature name, mean-tfidf value)
    File in = new File(centroidDataURI);
    BufferedReader reader = null;
    this.centroidMap = new HashMap<String, Double>();
    reader = new BufferedReader(new FileReader(in));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] featureMeanTfidf = line.split("\\t");
      double tfidf = Double.parseDouble(featureMeanTfidf[1]);
      this.centroidMap.put(featureMeanTfidf[0], tfidf);
    }
    reader.close();

    this.simFunction = new FixedCosineSimilarity(this.centroidMap);
    this.isTrained = true;
  }

}
