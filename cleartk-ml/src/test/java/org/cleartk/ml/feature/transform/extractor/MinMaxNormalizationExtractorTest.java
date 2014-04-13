/** 
 * Copyright (c) 2014, Regents of the University of Colorado 
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
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.test.util.type.Token;
import org.junit.Test;

/**
 * Copyright (c) 2014, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class MinMaxNormalizationExtractorTest extends DefaultTestBase {
  @Test
  public void testBasic() throws Exception {
    CleartkExtractor<DocumentAnnotation, Token> countsExtractor = new CleartkExtractor<DocumentAnnotation, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    MinMaxNormalizationExtractor<String, DocumentAnnotation> mmnExtractor = new MinMaxNormalizationExtractor<String, DocumentAnnotation>(
        "MinMax",
        countsExtractor);

    this.tokenBuilder.buildTokens(this.jCas, "a b b c c c d d d d e e e e e");

    DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    List<Feature> features = mmnExtractor.extract(jCas, doc);
    Instance<String> instance1 = new Instance<String>(features);
    assertEquals(1, features.size());
    assertEquals(TransformableFeature.class.getName(), features.get(0).getClass().getName());
    assertEquals("MinMax", features.get(0).getName());

    features = ((TransformableFeature) (features.get(0))).getFeatures();
    assertFeature("Count_Covered_a", 1, features.get(0));
    assertFeature("Count_Covered_b", 2, features.get(1));
    assertFeature("Count_Covered_c", 3, features.get(2));
    assertFeature("Count_Covered_d", 4, features.get(3));
    assertFeature("Count_Covered_e", 5, features.get(4));

    jCas.reset();
    this.tokenBuilder.buildTokens(this.jCas, "a a a a a a a a b c c c d d e e");
    doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    features = mmnExtractor.extract(jCas, doc);
    Instance<String> instance2 = new Instance<String>(features);
    assertEquals(1, features.size());
    assertEquals(TransformableFeature.class.getName(), features.get(0).getClass().getName());
    assertEquals("MinMax", features.get(0).getName());

    features = ((TransformableFeature) (features.get(0))).getFeatures();
    assertFeature("Count_Covered_a", 8, features.get(0));
    assertFeature("Count_Covered_b", 1, features.get(1));
    assertFeature("Count_Covered_c", 3, features.get(2));
    assertFeature("Count_Covered_d", 2, features.get(3));
    assertFeature("Count_Covered_e", 2, features.get(4));

    // train on two instances
    Iterable<Instance<String>> instances = Arrays.asList(instance1, instance2);
    mmnExtractor.train(instances);

    jCas.reset();
    this.tokenBuilder.buildTokens(this.jCas, "a a b b b c c c d d d e f f f f");
    doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    features = mmnExtractor.extract(jCas, doc);
    features = mmnExtractor.extract(jCas, doc);
    assertEquals(6, features.size());
    // mmn(l) = (count(l) - min(l)) / (max(l) - min(l))
    // mmn(a) = (2 - 1)/(8 - 1)
    assertFeature("MINMAX_NORMED_Count_Covered_a", 0.1428571428571429d, features.get(0));
    // mmn(b): count(b) == 3 > max(b) == 2 - new maximum so return 1
    assertFeature("MINMAX_NORMED_Count_Covered_b", 1.0d, features.get(1));
    // mmn(c) = 3 - 3 / 3 - 3 - let's avoid divide by zero and return 0.5.
    assertFeature("MINMAX_NORMED_Count_Covered_c", 0.5d, features.get(2));
    // mmn(d) = (3 - 2) / (4 - 2)
    assertFeature("MINMAX_NORMED_Count_Covered_d", 0.5d, features.get(3));
    // mmn(e): count(e) == 1 < min(e) == 2 - new minimum so return 0
    assertFeature("MINMAX_NORMED_Count_Covered_e", 0.0d, features.get(4));
    // mmn(f): first time we've seen f so give 0.5
    assertFeature("MINMAX_NORMED_Count_Covered_f", 0.5, features.get(5));

  }
}
