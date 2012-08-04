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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Bag;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Covered;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.opennlp.MaxentDataWriter;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.timeml.util.TextSliceExtractor;
import org.cleartk.timeml.util.TokenTextForSelectedPOSExtractor;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Annotator for the "tense" attribute of TimeML EVENTs.
 * 
 * @author Steven Bethard
 */
public class EventTenseAnnotator extends EventAttributeAnnotator<String> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {
    @Override
    public Class<?> getAnnotatorClass() {
      return EventTenseAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterClass() {
      return MaxentDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(EventTenseAnnotator.class);
    }
  };

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.eventFeatureExtractors.add(new TextSliceExtractor(-2));
    this.eventFeatureExtractors.add(new CleartkExtractor(Token.class, new TypePathExtractor(
        Token.class,
        "pos"), new Bag(new Covered())));

    this.contextExtractors.add(new CleartkExtractor(
        Token.class,
        new TokenTextForSelectedPOSExtractor("MD", "TO", "IN", "VB"),
        new Preceding(3)));
  }

  @Override
  protected String getDefaultValue() {
    return "NONE";
  }

  @Override
  protected String getAttribute(Event event) {
    return event.getTense();
  }

  @Override
  protected void setAttribute(Event event, String value) {
    event.setTense(value);
  }
}
