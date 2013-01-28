/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.timeml.tlink;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.opennlp.MaxentStringOutcomeDataWriter;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.timeml.util.FilteringExtractor;
import org.cleartk.token.type.Sentence;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TemporalLinkEventToDocumentCreationTimeAnnotator extends
    TemporalLinkAnnotator_ImplBase<Event, DocumentCreationTime> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {
    @Override
    public Class<?> getAnnotatorClass() {
      return TemporalLinkEventToDocumentCreationTimeAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterClass() {
      return MaxentStringOutcomeDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(TemporalLinkEventToDocumentCreationTimeAnnotator.class);
    }
  };

  public TemporalLinkEventToDocumentCreationTimeAnnotator() {
    super(Event.class, DocumentCreationTime.class, "BEFORE", "OVERLAP", "AFTER", "INCLUDES", "IS_INCLUDED", "SIMULTANEOUS");
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // I explored a ton of features here, and the following were the only ones that worked
    // The only feature that I didn't try that seems like it might still have some promise
    // would be to find any times within, say, 5 tokens, and do the time value comparison
    // to see whether the nearby time is before, overlapping with or after the DCT
    this.setSourceExtractors(Arrays.asList(
        new TypePathExtractor(Event.class, "tense"),
        new TypePathExtractor(Event.class, "aspect"),
        new TypePathExtractor(Event.class, "eventClass"),
        new TypePathExtractor(Event.class, "polarity"),
        new TypePathExtractor(Event.class, "modality"),
        // the word, but only if it's an aspectual event
        new FilteringExtractor<Event>(Event.class, new CoveredTextExtractor()) {
          @Override
          protected boolean accept(Event event) {
            return event.getEventClass().equals("ASPECTUAL");
          }
        }));
  }

  @Override
  protected List<SourceTargetPair> getSourceTargetPairs(JCas jCas) {
    List<SourceTargetPair> pairs = Lists.newArrayList();
    DocumentCreationTime dct = JCasUtil.selectSingle(jCas, DocumentCreationTime.class);
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Event event : JCasUtil.selectCovered(jCas, Event.class, sentence)) {
        pairs.add(new SourceTargetPair(event, dct));
      }
    }
    return pairs;
  }
}
