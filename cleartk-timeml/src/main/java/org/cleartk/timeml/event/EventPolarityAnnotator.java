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
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.util.PrecedingTokenTextBagExtractor;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Annotator for the "polarity" attribute of TimeML EVENTs.
 * 
 * @author Steven Bethard
 */
public class EventPolarityAnnotator extends EventAttributeAnnotator<String> {

  public static final String MODEL_DIR = "src/main/resources/models/timeml/event/polarity";

  public static AnalysisEngineDescription getWriterDescription(String modelDir)
          throws ResourceInitializationException {
    return CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(EventPolarityAnnotator.class,
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION, DefaultMaxentDataWriterFactory.class,
            modelDir);
  }

  public static AnalysisEngineDescription getWriterDescription()
          throws ResourceInitializationException {
    return getWriterDescription(MODEL_DIR);
  }

  public static AnalysisEngineDescription getAnnotatorDescription(String modelDir)
          throws ResourceInitializationException {
    return CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(EventPolarityAnnotator.class,
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION, modelDir);
  }

  public static AnalysisEngineDescription getAnnotatorDescription()
          throws ResourceInitializationException {
    return getAnnotatorDescription(MODEL_DIR + "/model.jar");
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.eventFeatureExtractors.add(new PrecedingTokenTextBagExtractor(3, "RB"));
  }

  @Override
  protected String getDefaultValue() {
    return "POS";
  }

  @Override
  protected String getAttribute(Event event) {
    return event.getPolarity();
  }

  @Override
  protected void setAttribute(Event event, String value) {
    event.setPolarity(value);
  }
}
