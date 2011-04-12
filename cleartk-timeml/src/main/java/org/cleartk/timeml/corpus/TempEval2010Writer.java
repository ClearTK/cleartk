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
package org.cleartk.timeml.corpus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.TimeMLViewName;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Joiner;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010Writer extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription(File outputDirectory)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        TempEval2010Writer.class,
        TimeMLComponents.TYPE_SYSTEM_DESCRIPTION,
        PARAM_OUTPUT_DIRECTORY,
        outputDirectory.getPath());
  }

  public static final String PARAM_OUTPUT_DIRECTORY = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "outputDirectory");

  @ConfigurationParameter(description = "The directory where the TempEval .tab files should be written.", mandatory = true)
  private File outputDirectory;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing the document text")
  private String textView;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing DocumentCreationTime annotations")
  private String documentCreationTimeView;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing Time annotations (with attributes)")
  private String timeView;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing Event annotations (with attributes)")
  private String eventView;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing TemporalLink annotations between events and the document creation time")
  private String temporalLinkEventToDocumentCreationTimeView;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing TemporalLink annotations between events and times within the same sentence")
  private String temporalLinkEventToSameSentenceTimeView;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing TemporalLink annotations between events and syntactically dominated events")
  private String temporalLinkEventToSubordinatedEventView;

  @ConfigurationParameter(defaultValue = CAS.NAME_DEFAULT_SOFA, description = "View containing TemporalLink annotations between main events in adjacent sentences")
  private String temporalLinkMainEventToNextSentenceMainEventView;

  public static final String PARAM_TEXT_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "textView");

  public static final String PARAM_DOCUMENT_CREATION_TIME_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "documentCreationTimeView");

  public static final String PARAM_TIME_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "timeView");

  public static final String PARAM_EVENT_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "eventView");

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "temporalLinkEventToDocumentCreationTimeView");

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "temporalLinkEventToSameSentenceTimeView");

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "temporalLinkEventToSubordinatedEventView");

  public static final String PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEW = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010Writer.class,
      "temporalLinkMainEventToNextSentenceMainEventView");

  private List<PrintWriter> writers;

  private PrintWriter baseWriter;

  private PrintWriter dctWriter;

  private PrintWriter timexExtentWriter;

  private PrintWriter timexAttributeWriter;

  private PrintWriter eventExtentWriter;

  private PrintWriter eventAttributeWriter;

  private PrintWriter tlinkDCTEventWriter;

  private PrintWriter tlinkMainEventsWriter;

  private PrintWriter tlinkSubordinatedEventsWriter;

  private PrintWriter tlinkTimexEventWriter;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if (!this.outputDirectory.exists()) {
      this.outputDirectory.mkdirs();
    }
    this.writers = new ArrayList<PrintWriter>();
    this.baseWriter = this.createWriter(TimeMLViewName.TEMPEVAL_BASE_SEGMENTATION);
    this.dctWriter = this.createWriter(TimeMLViewName.TEMPEVAL_DCT);
    this.timexExtentWriter = this.createWriter(TimeMLViewName.TEMPEVAL_TIMEX_EXTENTS);
    this.timexAttributeWriter = this.createWriter(TimeMLViewName.TEMPEVAL_TIMEX_ATTRIBUTES);
    this.eventExtentWriter = this.createWriter(TimeMLViewName.TEMPEVAL_EVENT_EXTENTS);
    this.eventAttributeWriter = this.createWriter(TimeMLViewName.TEMPEVAL_EVENT_ATTRIBUTES);
    this.tlinkDCTEventWriter = this.createWriter(TimeMLViewName.TEMPEVAL_TLINK_DCT_EVENT);
    this.tlinkMainEventsWriter = this.createWriter(TimeMLViewName.TEMPEVAL_TLINK_MAIN_EVENTS);
    this.tlinkSubordinatedEventsWriter = this.createWriter(TimeMLViewName.TEMPEVAL_TLINK_SUBORDINATED_EVENTS);
    this.tlinkTimexEventWriter = this.createWriter(TimeMLViewName.TEMPEVAL_TLINK_TIMEX_EVENT);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String fileName = ViewURIUtil.getURI(jCas).getFragment();
    JCas textJCas = JCasUtil.getView(jCas, this.textView, false);
    JCas dctJCas = JCasUtil.getView(jCas, this.documentCreationTimeView, false);
    JCas timeJCas = JCasUtil.getView(jCas, this.timeView, false);
    JCas eventJCas = JCasUtil.getView(jCas, this.eventView, false);

    // write the document creation time
    for (DocumentCreationTime time : JCasUtil.select(dctJCas, DocumentCreationTime.class)) {
      this.write(this.dctWriter, fileName, time.getValue().replaceAll("-", ""));
    }

    // align tokens to times
    Map<Token, Time> tokenTimexes = new HashMap<Token, Time>();
    for (Time time : JCasUtil.select(timeJCas, Time.class)) {
      for (Token token : JCasUtil.selectCovered(textJCas, Token.class, time)) {
        tokenTimexes.put(token, time);
      }
    }

    // align tokens to events
    Map<Token, Event> tokenEvents = new HashMap<Token, Event>();
    for (Event event : JCasUtil.select(eventJCas, Event.class)) {
      for (Token token : JCasUtil.selectCovered(textJCas, Token.class, event)) {
        tokenEvents.put(token, event);
      }
    }

    // walk through tokens by sentence, writing tokens, times, events, etc.
    int sentIndex = -1;
    for (Sentence sentence : JCasUtil.select(textJCas, Sentence.class)) {
      sentIndex += 1;
      int tokenIndex = -1;
      for (Token token : JCasUtil.selectCovered(textJCas, Token.class, sentence)) {
        tokenIndex += 1;

        // write the token to the segmentation file
        this.write(this.baseWriter, fileName, sentIndex, tokenIndex, token.getCoveredText());

        // write the time extent and attributes
        this.writeAnchors(
            this.timexExtentWriter,
            this.timexAttributeWriter,
            tokenTimexes,
            "timex3",
            token,
            fileName,
            sentIndex,
            tokenIndex,
            new AttributeGetter<Time>() {
              @Override
              public List<Attribute> getAttributes(Time time) {
                Attribute value = new Attribute("value", time.getValue());
                Attribute type = new Attribute("type", time.getTimeType());
                return Arrays.asList(value, type);
              }
            });

        // write the event extent and attributes
        this.writeAnchors(
            this.eventExtentWriter,
            this.eventAttributeWriter,
            tokenEvents,
            "event",
            token,
            fileName,
            sentIndex,
            tokenIndex,
            new AttributeGetter<Event>() {
              @Override
              public List<Attribute> getAttributes(Event event) {
                Attribute polarity = new Attribute("polarity", event.getPolarity());
                Attribute modality = new Attribute("modality", event.getModality());
                Attribute pos = new Attribute("pos", event.getPos());
                Attribute tense = new Attribute("tense", event.getTense());
                Attribute aspect = new Attribute("aspect", event.getAspect());
                Attribute eventClass = new Attribute("class", event.getEventClass());
                return Arrays.asList(polarity, modality, pos, tense, aspect, eventClass);
              }
            });
      }
    }

    // write the temporal links
    this.writeTemporalLinks(
        this.tlinkDCTEventWriter,
        jCas,
        this.temporalLinkEventToDocumentCreationTimeView,
        fileName);
    this.writeTemporalLinks(
        this.tlinkTimexEventWriter,
        jCas,
        this.temporalLinkEventToSameSentenceTimeView,
        fileName);
    this.writeTemporalLinks(
        this.tlinkSubordinatedEventsWriter,
        jCas,
        this.temporalLinkEventToSubordinatedEventView,
        fileName);
    this.writeTemporalLinks(
        this.tlinkMainEventsWriter,
        jCas,
        this.temporalLinkMainEventToNextSentenceMainEventView,
        fileName);
  }

  @Override
  public void batchProcessComplete() throws AnalysisEngineProcessException {
    super.batchProcessComplete();
    for (PrintWriter writer : this.writers) {
      writer.flush();
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    for (PrintWriter writer : this.writers) {
      writer.close();
    }
  }

  private PrintWriter createWriter(String tabFileName) throws ResourceInitializationException {
    PrintWriter writer;
    try {
      writer = new PrintWriter(new FileWriter(new File(this.outputDirectory, tabFileName)));
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
    this.writers.add(writer);
    return writer;
  }

  private void write(PrintWriter writer, Object... columns) {
    writer.println(Joiner.on('\t').join(columns));
  }

  private static class Attribute {
    public String name;

    public Object value;

    public Attribute(String name, Object value) {
      this.name = name;
      this.value = value;
    }
  }

  private static interface AttributeGetter<T extends Anchor> {
    public List<Attribute> getAttributes(T anchor);
  }

  private <T extends Anchor> void writeAnchors(
      PrintWriter extentWriter,
      PrintWriter attrWriter,
      Map<Token, T> tokenAnchors,
      String anchorType,
      Token token,
      String fileName,
      int sentIndex,
      int tokenIndex,
      AttributeGetter<T> attributeGetter) {
    T anchor = tokenAnchors.get(token);
    if (anchor != null) {
      String id = anchor.getId();
      this.write(extentWriter, fileName, sentIndex, tokenIndex, anchorType, id, "1");

      // write to the time attributes file
      boolean isFirstToken = token.getBegin() == anchor.getBegin();
      if (isFirstToken) {
        for (Attribute attr : attributeGetter.getAttributes(anchor)) {
          if (attr.value != null) {
            this.write(
                attrWriter,
                fileName,
                sentIndex,
                tokenIndex,
                anchorType,
                id,
                "1",
                attr.name,
                attr.value);
          }
        }
      }
    }
  }

  private void writeTemporalLinks(PrintWriter writer, JCas jCas, String viewName, String fileName)
      throws AnalysisEngineProcessException {
    JCas view = JCasUtil.getView(jCas, viewName, false);
    for (TemporalLink tlink : JCasUtil.select(view, TemporalLink.class)) {
      this.write(
          writer,
          fileName,
          tlink.getSource().getId(),
          tlink.getTarget().getId(),
          tlink.getRelationType());
    }
  }
}
