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
package org.cleartk.timeml;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.corpus.timeml.type.Event;
import org.cleartk.type.Sentence;
import org.cleartk.util.AnnotationRetrieval;

/**
 * Base class for annotators of TimeML EVENT attributes.
 * 
 * @author Steven Bethard
 */
public abstract class EventAttributeAnnotator<OUTCOME_TYPE> extends CleartkAnnotator<OUTCOME_TYPE> {

  /**
   * The list of feature extractors that will be applied directly to the Event annotation.
   * 
   * Subclasses should override {@link #initialize(org.apache.uima.UimaContext)} to fill this list.
   */
  protected List<SimpleFeatureExtractor> eventFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();

  /**
   * The list of feature extractors that will be applied to the Event annotation, with a Sentence window. 
   * 
   * Subclasses should override {@link #initialize(org.apache.uima.UimaContext)} to fill this list.
   */
  protected List<WindowExtractor> windowFeatureExtractors = new ArrayList<WindowExtractor>();

  /**
   * The attribute value that should be considered as a default, e.g. "NONE".
   * When the attribute value is null in the CAS, this value will be used instead.
   * Additionally, when the classifier produces this value, no attribute will be added.
   * 
   * @return The default attribute value.
   */
  protected abstract OUTCOME_TYPE getDefaultValue();
  
  /**
   * Get the attribute value from the Event annotation.
   * Typically this will be by calling something like {@link Event#getTense()}.
   * 
   * If this method returns null, {@link #getDefaultValue()} will be called to
   * produce an appropriate attribute value.
   * 
   * @param event The Event annotation whose attribute is to be retrieved.
   * @return      The selected attribute value.
   */
  protected abstract OUTCOME_TYPE getAttribute(Event event);

  /**
   * Set the attribute value on the Event annotation.
   * Typically this will be by calling something like {@link Event#setTense(String)}.
   * 
   * This method will not be called if the value is equal to {@link #getDefaultValue()}. 
   * 
   * @param event The Event annotation whose attribute is to be set.
   * @param value The new attribute value.
   */
  protected abstract void setAttribute(Event event, OUTCOME_TYPE value);
  
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      this.processSimple(jCas);
    } catch (CleartkException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
  
  /**
   * Helper method to work around CleartkException annoyance.
   */
  private void processSimple(JCas jCas) throws AnalysisEngineProcessException, CleartkException {
    for (Sentence sentence: AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
      for (Event event: AnnotationRetrieval.getAnnotations(jCas, sentence, Event.class)) {

        // assemble features
        List<Feature> features = new ArrayList<Feature>();
        for (SimpleFeatureExtractor extractor: this.eventFeatureExtractors) {
          features.addAll(extractor.extract(jCas, event));
        }
        for (WindowExtractor extractor: this.windowFeatureExtractors) {
          features.addAll(extractor.extract(jCas, event, sentence));
        }
        
        // if training, determine the attribute value and write the instance
        if (this.isTraining()) {
          OUTCOME_TYPE attribute = this.getAttribute(event);
          if (attribute == null) {
            attribute = this.getDefaultValue();
          }
          Instance<OUTCOME_TYPE> instance = new Instance<OUTCOME_TYPE>();
          instance.addAll(features);
          instance.setOutcome(attribute);
          this.dataWriter.write(instance);
        }

        // if predicting, propose an attribute value and modify the event annotation
        else {
          OUTCOME_TYPE label = this.classifier.classify(features);
          if (!this.getDefaultValue().equals(label)) {
            this.setAttribute(event, label);
          }
        }
      }
    }
  }
}
