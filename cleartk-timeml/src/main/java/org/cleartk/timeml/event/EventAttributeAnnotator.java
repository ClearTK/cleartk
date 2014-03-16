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
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.timeml.type.Event;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
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
  protected List<FeatureExtractor1<Event>> eventFeatureExtractors;

  /**
   * The list of feature extractors that will be applied to the Event annotation, with a Sentence
   * window.
   * 
   * Subclasses should override {@link #initialize(org.apache.uima.UimaContext)} to fill this list.
   */
  protected List<CleartkExtractor<Event, Token>> contextExtractors;

  /**
   * The attribute value that should be considered as a default, e.g. "NONE". When the attribute
   * value is null in the CAS, this value will be used instead. Additionally, when the classifier
   * produces this value, no attribute will be added.
   * 
   * @return The default attribute value.
   */
  protected abstract OUTCOME_TYPE getDefaultValue();

  /**
   * Get the attribute value from the Event annotation. Typically this will be by calling something
   * like {@link Event#getTense()}.
   * 
   * If this method returns null, {@link #getDefaultValue()} will be called to produce an
   * appropriate attribute value.
   * 
   * @param event
   *          The Event annotation whose attribute is to be retrieved.
   * @return The selected attribute value.
   */
  protected abstract OUTCOME_TYPE getAttribute(Event event);

  /**
   * Set the attribute value on the Event annotation. Typically this will be by calling something
   * like {@link Event#setTense(String)}.
   * 
   * This method will not be called if the value is equal to {@link #getDefaultValue()}.
   * 
   * @param event
   *          The Event annotation whose attribute is to be set.
   * @param value
   *          The new attribute value.
   */
  protected abstract void setAttribute(Event event, OUTCOME_TYPE value);

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.eventFeatureExtractors = Lists.newArrayList();
    this.contextExtractors = Lists.newArrayList();
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Event event : JCasUtil.selectCovered(jCas, Event.class, sentence)) {

        // assemble features
        List<Feature> features = new ArrayList<Feature>();
        for (FeatureExtractor1<Event> extractor : this.eventFeatureExtractors) {
          features.addAll(extractor.extract(jCas, event));
        }
        for (CleartkExtractor<Event, Token> extractor : this.contextExtractors) {
          features.addAll(extractor.extractWithin(jCas, event, sentence));
        }

        // if training, determine the attribute value and write the
        // instance
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

        // if predicting, propose an attribute value and modify the
        // event annotation
        else {
          OUTCOME_TYPE label = this.classifier.classify(features);
          this.setAttribute(event, label);
        }
      }
    }
  }
}
