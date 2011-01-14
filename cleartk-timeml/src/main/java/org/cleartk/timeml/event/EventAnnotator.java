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

import static org.cleartk.classifier.feature.WindowFeature.ORIENTATION_LEFT;
import static org.cleartk.classifier.feature.WindowFeature.ORIENTATION_RIGHT;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.chunker.ChunkLabeler_ImplBase;
import org.cleartk.chunker.Chunker;
import org.cleartk.chunker.ChunkerFeatureExtractor;
import org.cleartk.chunker.DefaultChunkLabeler;
import org.cleartk.classifier.CleartkComponents;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.type.Event;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Annotator for TimeML EVENT identification.
 * 
 * @author Steven Bethard
 */
public class EventAnnotator {

  public static final String MODEL_DIR = "src/main/resources/models/timeml/event";

  public static AnalysisEngineDescription getWriterDescription(String modelDir)
          throws ResourceInitializationException {
    return CleartkComponents.createCleartkSequentialAnnotator(Chunker.class,
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION, DefaultMalletCRFDataWriterFactory.class,
            modelDir, (List<Class<?>>) null, Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME,
            Token.class.getName(), Chunker.PARAM_SEQUENCE_CLASS_NAME, Sentence.class.getName(),
            Chunker.PARAM_CHUNK_LABELER_CLASS_NAME, DefaultChunkLabeler.class.getName(),
            Chunker.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME, FeatureExtractor.class.getName(),
            ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME, Event.class.getName());
  }

  public static AnalysisEngineDescription getWriterDescription()
          throws ResourceInitializationException {
    return getWriterDescription(MODEL_DIR);
  }

  public static AnalysisEngineDescription getAnnotatorDescription(String modelDir)
          throws ResourceInitializationException {
    return CleartkComponents.createCleartkSequentialAnnotator(Chunker.class,
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION, modelDir, null,
            Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME, Token.class.getName(),
            Chunker.PARAM_SEQUENCE_CLASS_NAME, Sentence.class.getName(),
            Chunker.PARAM_CHUNK_LABELER_CLASS_NAME, DefaultChunkLabeler.class.getName(),
            Chunker.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME, FeatureExtractor.class.getName(),
            ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME, Event.class.getName());
  }

  public static AnalysisEngineDescription getAnnotatorDescription()
          throws ResourceInitializationException {
    return getAnnotatorDescription(MODEL_DIR + "/model.jar");
  }

  public static class FeatureExtractor implements ChunkerFeatureExtractor {
    @ConfigurationParameter(name = Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME)
    private String labeledAnnotationClassName;

    private List<SimpleFeatureExtractor> tokenFeatureExtractors;

    private List<WindowExtractor> windowFeatureExtractors;

    public void initialize(UimaContext context) throws ResourceInitializationException {
      // get configured annotation
      ConfigurationParameterInitializer.initialize(this, context);
      Class<? extends Annotation> tokenClass = InitializableFactory.getClass(
              this.labeledAnnotationClassName, Annotation.class);

      // initialize feature lists
      this.tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
      this.windowFeatureExtractors = new ArrayList<WindowExtractor>();

      // add features: word, stem, pos
      this.tokenFeatureExtractors.add(new SpannedTextExtractor());
      this.tokenFeatureExtractors.add(new TypePathExtractor(tokenClass, "stem"));
      this.tokenFeatureExtractors.add(new TypePathExtractor(tokenClass, "pos"));

      // add window of features 2 before and 2 after
      for (SimpleFeatureExtractor extractor : this.tokenFeatureExtractors) {
        this.windowFeatureExtractors.add(new WindowExtractor(tokenClass, extractor,
                ORIENTATION_RIGHT, 0, 3));
        this.windowFeatureExtractors.add(new WindowExtractor(tokenClass, extractor,
                ORIENTATION_LEFT, 0, 3));
      }
    }

    public Instance<String> extractFeatures(JCas jCas, Annotation labeledAnnotation,
            Annotation sequence) throws CleartkException {
      Instance<String> instance = new Instance<String>();
      for (SimpleFeatureExtractor extractor : this.tokenFeatureExtractors) {
        instance.addAll(extractor.extract(jCas, labeledAnnotation));
      }
      for (WindowExtractor extractor : this.windowFeatureExtractors) {
        instance.addAll(extractor.extract(jCas, labeledAnnotation, sequence));
      }
      return instance;
    }
  }
}
