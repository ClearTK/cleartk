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

import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.transform.TransformableFeature;
import org.cleartk.ml.feature.transform.extractor.TfidfExtractor;
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

public class TfidfExtractorTest extends DefaultTestBase {

  @Test
  public void testBasic() throws Exception {
    CleartkExtractor<DocumentAnnotation, Token> countsExtractor = new CleartkExtractor<DocumentAnnotation, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    TfidfExtractor<String, DocumentAnnotation> tfidfExtractor = new TfidfExtractor<String, DocumentAnnotation>(
        "TFIDF",
        countsExtractor);

    this.tokenBuilder.buildTokens(this.jCas, "the quick quick quick quick fox the lazy dog dog dog");

    DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    List<Feature> features = tfidfExtractor.extract(jCas, doc);
    Instance<String> instance1 = new Instance<String>(features);
    assertEquals(1, features.size());
    assertEquals(TransformableFeature.class.getName(), features.get(0).getClass().getName());
    assertEquals("TFIDF", features.get(0).getName());

    features = ((TransformableFeature) (features.get(0))).getFeatures();
    assertFeature("Count_Covered_the", 2, features.get(0));
    assertFeature("Count_Covered_quick", 4, features.get(1));
    assertFeature("Count_Covered_fox", 1, features.get(2));
    assertFeature("Count_Covered_lazy", 1, features.get(3));
    assertFeature("Count_Covered_dog", 3, features.get(4));

    jCas.reset();
    this.tokenBuilder.buildTokens(this.jCas, "the the the the slow mouse lazy dog dog dog dog dog");
    doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    features = tfidfExtractor.extract(jCas, doc);
    Instance<String> instance2 = new Instance<String>(features);
    assertEquals(1, features.size());
    assertEquals(TransformableFeature.class.getName(), features.get(0).getClass().getName());
    assertEquals("TFIDF", features.get(0).getName());

    features = ((TransformableFeature) (features.get(0))).getFeatures();
    assertFeature("Count_Covered_the", 4, features.get(0));
    assertFeature("Count_Covered_slow", 1, features.get(1));
    assertFeature("Count_Covered_mouse", 1, features.get(2));
    assertFeature("Count_Covered_lazy", 1, features.get(3));
    assertFeature("Count_Covered_dog", 5, features.get(4));

    // passing in two instances to the train method.
    Iterable<Instance<String>> instances = Arrays.asList(instance1, instance2);
    tfidfExtractor.train(instances);

    features = tfidfExtractor.extract(jCas, doc);
    assertEquals(5, features.size());
    // tf for 'the' is 4
    // df for 'the' = 2 + 1 = 3 (add one smoothing)
    // total document count is 2 + 2 = 4 (add two smoothing) (the first 2 comes from the fact that
    // we passed in two instances to the train method.)
    // 4* log(4/3)
    assertFeature("TF-IDF_Count_Covered_the", 1.1507282898071234, features.get(0));
    assertFeature("TF-IDF_Count_Covered_slow", 0.6931471805599453, features.get(1));
    assertFeature("TF-IDF_Count_Covered_mouse", 0.6931471805599453, features.get(2));
    assertFeature("TF-IDF_Count_Covered_lazy", 0.28768207245178085, features.get(3));
    assertFeature("TF-IDF_Count_Covered_dog", 1.4384103622589042, features.get(4));

  }
}
