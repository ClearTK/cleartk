/** 
 * Copyright (c) 2013, Regents of the University of Colorado 
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

import static org.cleartk.ml.ClassifierTestUtil.assertFeature;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.transform.TransformableFeature;
import org.cleartk.ml.feature.transform.extractor.TfidfExtractor.IDFMap;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.test.util.type.Token;
import org.junit.Test;

/**
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class CentroidTfidfSimilarityExtractorTest extends DefaultTestBase {

  @Test
  public void testBasic() throws Exception {
    CleartkExtractor<DocumentAnnotation, Token> countsExtractor = new CleartkExtractor<DocumentAnnotation, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    CentroidTfidfSimilarityExtractor<String, DocumentAnnotation> extractor = new CentroidTfidfSimilarityExtractor<String, DocumentAnnotation>(
        "CTSE",
        countsExtractor);

    tokenBuilder.buildTokens(this.jCas, "the quick quick quick quick fox the lazy dog dog dog");

    DocumentAnnotation doc1 = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    List<Feature> features1 = extractor.extract(jCas, doc1);
    Instance<String> instance1 = new Instance<String>(features1);
    assertEquals(1, features1.size());
    assertEquals(TransformableFeature.class.getName(), features1.get(0).getClass().getName());
    assertEquals("CTSE", features1.get(0).getName());

    features1 = ((TransformableFeature) (features1.get(0))).getFeatures();
    assertFeature("Count_Covered_the", 2, features1.get(0));
    assertFeature("Count_Covered_quick", 4, features1.get(1));
    assertFeature("Count_Covered_fox", 1, features1.get(2));
    assertFeature("Count_Covered_lazy", 1, features1.get(3));
    assertFeature("Count_Covered_dog", 3, features1.get(4));

    jCas.reset();
    tokenBuilder.buildTokens(this.jCas, "the the the the slow mouse lazy dog dog dog dog dog");
    DocumentAnnotation doc2 = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    List<Feature> features2 = extractor.extract(jCas, doc2);
    Instance<String> instance2 = new Instance<String>(features2);
    assertEquals(1, features2.size());
    assertEquals(TransformableFeature.class.getName(), features2.get(0).getClass().getName());
    assertEquals("CTSE", features2.get(0).getName());

    features2 = ((TransformableFeature) (features2.get(0))).getFeatures();
    assertFeature("Count_Covered_the", 4, features2.get(0));
    assertFeature("Count_Covered_slow", 1, features2.get(1));
    assertFeature("Count_Covered_mouse", 1, features2.get(2));
    assertFeature("Count_Covered_lazy", 1, features2.get(3));
    assertFeature("Count_Covered_dog", 5, features2.get(4));

    // passing in two instances to the train method.
    Iterable<Instance<String>> instances = Arrays.asList(instance1, instance2);

    IDFMap idfMap = extractor.createIdfMap(instances);

    Map<String, Double> centroid = extractor.computeCentroid(instances, idfMap);

    // there are only two documents and so there are only two values for idf: one for words that
    // appear in both documents and one for words that appear in one
    // words that appear in both will have an idf = log(4/3) = 0.28768207245178085d
    // words that appear in one will have an idf = log(4/2) = 0.6931471805599453d
    // see IDFMap.getIDF() for details
    double IDF1 = 0.6931471805599453d;
    double IDFBOTH = 0.28768207245178085d;
    assertEquals(IDF1, idfMap.getIDF("Count_Covered_fox"), 0.000001d);
    assertEquals(IDF1, idfMap.getIDF("Count_Covered_mouse"), 0.000001d);
    assertEquals(IDFBOTH, idfMap.getIDF("Count_Covered_the"), 0.000001d);
    assertEquals(IDF1, idfMap.getIDF("Count_Covered_slow"), 0.000001d);
    assertEquals(IDFBOTH, idfMap.getIDF("Count_Covered_lazy"), 0.000001d);
    assertEquals(IDF1, idfMap.getIDF("Count_Covered_quick"), 0.000001d);
    assertEquals(IDFBOTH, idfMap.getIDF("Count_Covered_dog"), 0.000001d);

    // the centroid value is simply the average tfidf for each word
    // 2*IDFBOTH + 4*IDFBOTH / 2
    assertEquals(centroid.get("Count_Covered_fox"), (1 * IDF1) / 2, 0.0001d);
    assertEquals(centroid.get("Count_Covered_mouse"), (1 * IDF1) / 2, 0.0001d);
    assertEquals(centroid.get("Count_Covered_the"), (2 * IDFBOTH + 4 * IDFBOTH) / 2, 0.0001d);
    assertEquals(centroid.get("Count_Covered_slow"), (1 * IDF1) / 2, 0.0001d);
    assertEquals(centroid.get("Count_Covered_lazy"), (1 * IDFBOTH + 1 * IDFBOTH) / 2, 0.0001d);
    assertEquals(centroid.get("Count_Covered_quick"), (4 * IDF1) / 2, 0.0001d);
    assertEquals(centroid.get("Count_Covered_dog"), (3 * IDFBOTH + 5 * IDFBOTH) / 2, 0.0001d);

    extractor.train(instances);

    List<Feature> centroidSimFeatures = extractor.extract(jCas, doc2);
    assertEquals(1, centroidSimFeatures.size());
    assertFeature("CTSE", 0.7240753623626945, centroidSimFeatures.get(0));

    SimilarityFunction simFunction = new FixedCosineSimilarity(centroid);
    Map<String, Double> featureMap = extractor.featuresToFeatureMap(countsExtractor.extract(
        jCas,
        doc2));
    // we've tested the distance metric elsewhere so here we will just verify that the distance
    // metric and the feature value are the same
    double distance = simFunction.distance(featureMap, centroid);
    assertFeature("CTSE", distance, centroidSimFeatures.get(0));

  }
}
