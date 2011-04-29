/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
package org.cleartk.timeml.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.chunker.ChunkLabeler_ImplBase;
import org.cleartk.chunker.Chunker;
import org.cleartk.chunker.ChunkerFeatureExtractor;
import org.cleartk.chunker.DefaultChunkLabeler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Annotator for TimeML EVENT identification.
 * 
 * @author Steven Bethard
 */
public class EventAnnotator extends Chunker {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {

    @Override
    public Class<?> getAnnotatorClass() {
      return EventAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterFactoryClass() {
      return DefaultMalletCRFDataWriterFactory.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(
          EventAnnotator.class,
          TimeMLComponents.TYPE_SYSTEM_DESCRIPTION,
          Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME,
          Token.class.getName(),
          Chunker.PARAM_SEQUENCE_CLASS_NAME,
          Sentence.class.getName(),
          Chunker.PARAM_CHUNK_LABELER_CLASS_NAME,
          DefaultChunkLabeler.class.getName(),
          Chunker.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME,
          FeatureExtractor.class.getName(),
          ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME,
          Event.class.getName());
    }
  };

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    super.process(jCas);

    int index = 1;
    for (Event event : JCasUtil.select(jCas, Event.class)) {
      String id = String.format("e%d", index);
      event.setId(id);
      index += 1;
    }
  }

  public static class FeatureExtractor implements ChunkerFeatureExtractor {

    private List<SimpleFeatureExtractor> tokenFeatureExtractors;

    private List<ContextExtractor<?>> contextExtractors;

    public void initialize(UimaContext context) throws ResourceInitializationException {

      // add features: word, stem, pos
      this.tokenFeatureExtractors = Arrays.asList(
          new SpannedTextExtractor(),
          new TypePathExtractor(Token.class, "stem"),
          new TypePathExtractor(Token.class, "pos"),
          new ParentNodeFeaturesExtractor());

      // add window of features before and after
      this.contextExtractors = Arrays.<ContextExtractor<?>> asList(new ContextExtractor<Token>(
          Token.class,
          new SpannedTextExtractor(),
          new Preceding(3),
          new Following(3)));
    }

    public Instance<String> extractFeatures(
        JCas jCas,
        Annotation labeledAnnotation,
        Annotation sequence) throws CleartkExtractorException {
      Instance<String> instance = new Instance<String>();
      for (SimpleFeatureExtractor extractor : this.tokenFeatureExtractors) {
        instance.addAll(extractor.extract(jCas, labeledAnnotation));
      }
      for (ContextExtractor<?> extractor : this.contextExtractors) {
        instance.addAll(extractor.extractWithin(jCas, labeledAnnotation, sequence));
      }
      return instance;
    }
  }

  private static class ParentNodeFeaturesExtractor implements SimpleFeatureExtractor {
    public ParentNodeFeaturesExtractor() {
    }

    @Override
    public List<Feature> extract(JCas view, Annotation focusAnnotation)
        throws CleartkExtractorException {
      TreebankNode node = TreebankNodeUtil.selectMatchingLeaf(view, focusAnnotation);
      List<Feature> features = new ArrayList<Feature>();
      if (node != null) {
        TreebankNode parent = node.getParent();
        if (parent != null) {
          features.add(new Feature("ParentNodeType", parent.getNodeType()));
          TreebankNode firstSibling = parent.getChildren(0);
          if (firstSibling != node && firstSibling.getLeaf()) {
            features.add(new Feature("FirstSiblingText", firstSibling.getCoveredText()));
          }
        }
      }
      return features;
    }
  }
}
