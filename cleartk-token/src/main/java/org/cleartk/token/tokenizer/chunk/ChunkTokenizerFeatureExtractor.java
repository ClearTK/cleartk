/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.token.tokenizer.chunk;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.chunker.Chunker;
import org.cleartk.chunker.ChunkerFeatureExtractor;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.WindowNGramFeature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.WindowNGramExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.WhiteSpaceExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.ContainsHyphenProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip
 * 
 */
@Deprecated
public class ChunkTokenizerFeatureExtractor implements ChunkerFeatureExtractor {

  private List<SimpleFeatureExtractor> simpleFeatureExtractors;

  private List<WindowExtractor> windowExtractors;

  private List<WindowNGramExtractor> windowNGramExtractors;

  protected Class<? extends Annotation> labeledAnnotationClass;

  public void initialize(UimaContext context) throws ResourceInitializationException {

    String labeledAnnotationClassName = (String) context.getConfigParameterValue(Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME);
    labeledAnnotationClass = InitializableFactory.getClass(
        labeledAnnotationClassName,
        Annotation.class);

    this.simpleFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.windowExtractors = new ArrayList<WindowExtractor>();
    this.windowNGramExtractors = new ArrayList<WindowNGramExtractor>();

    SimpleFeatureExtractor wordExtractor = new CoveredTextExtractor();

    int fromLeft = CharacterNGramProliferator.LEFT_TO_RIGHT;
    int fromRight = CharacterNGramProliferator.RIGHT_TO_LEFT;

    this.simpleFeatureExtractors.add(new ProliferatingExtractor(
        wordExtractor,
        new CapitalTypeProliferator(),
        new ContainsHyphenProliferator(),
        new LowerCaseProliferator(),
        new NumericTypeProliferator(),
        new CharacterNGramProliferator(fromLeft, 0, 1, 1, true),
        new CharacterNGramProliferator(fromLeft, 0, 2, 2, true),
        new CharacterNGramProliferator(fromLeft, 0, 3, 3, true),
        new CharacterNGramProliferator(fromRight, 0, 1, 1, true),
        new CharacterNGramProliferator(fromRight, 0, 2, 2, true),
        new CharacterNGramProliferator(fromRight, 0, 3, 3, true)));

    this.simpleFeatureExtractors.add(new WhiteSpaceExtractor());

    // add 2 stems to the left and right
    this.windowExtractors.add(new WindowExtractor(
        labeledAnnotationClass,
        wordExtractor,
        WindowFeature.ORIENTATION_RIGHT,
        0,
        2));
    this.windowExtractors.add(new WindowExtractor(
        labeledAnnotationClass,
        wordExtractor,
        WindowFeature.ORIENTATION_LEFT,
        0,
        2));

    this.windowNGramExtractors.add(new WindowNGramExtractor(
        labeledAnnotationClass,
        wordExtractor,
        WindowNGramFeature.ORIENTATION_LEFT,
        WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT,
        "_",
        0,
        2));

    this.windowNGramExtractors.add(new WindowNGramExtractor(
        labeledAnnotationClass,
        wordExtractor,
        WindowNGramFeature.ORIENTATION_RIGHT,
        WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT,
        "_",
        0,
        2));
  }

  public Instance<String> extractFeatures(
      JCas jCas,
      Annotation labeledAnnotation,
      Annotation sequence) throws CleartkExtractorException {

    Instance<String> instance = new Instance<String>();

    // extract all features that require only the token annotation
    for (SimpleFeatureExtractor extractor : this.simpleFeatureExtractors) {
      instance.addAll(extractor.extract(jCas, labeledAnnotation));
    }

    // extract all features that require the token and sentence annotations
    for (WindowExtractor extractor : this.windowExtractors) {
      instance.addAll(extractor.extract(jCas, labeledAnnotation, sequence));
    }

    for (WindowNGramExtractor extractor : this.windowNGramExtractors) {
      instance.add(extractor.extract(jCas, labeledAnnotation, sequence));
    }

    return instance;
  }

}
