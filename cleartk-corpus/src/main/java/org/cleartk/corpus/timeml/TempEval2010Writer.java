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
package org.cleartk.corpus.timeml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewUriUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
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

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(TempEval2010Writer.class);
  }

  @ConfigurationParameter(
      name = PARAM_OUTPUT_DIRECTORY,
      mandatory = true, description = "The directory where the TempEval .tab "
      + "files should be written.")
  private File outputDirectory;

  @ConfigurationParameter(
      name = PARAM_TEXT_VIEW, 
      mandatory = true, description = "View containing the document text.")
  private String textView;

  @ConfigurationParameter(
      name = PARAM_DOCUMENT_CREATION_TIME_VIEW,
      description = "View containing DocumentCreationTime annotations. If "
      + "provided, the document creation times file will be written.")
  private String documentCreationTimeView;

  @ConfigurationParameter(
      name = PARAM_TIME_EXTENT_VIEW,
      description = "View containing Time annotations. If provided, the time "
      + "extents file will be written.")
  private String timeExtentView;

  @ConfigurationParameter(
      name = PARAM_TIME_ATTRIBUTE_VIEW,
      description = "View containing Time annotations with their attributes. "
      + "If provided, the time attributes file will be written.")
  private String timeAttributeView;

  @ConfigurationParameter(
      name = PARAM_EVENT_EXTENT_VIEW,
      description = "View containing Event annotations. If provided, the "
      + "event extents will be written.")
  private String eventExtentView;

  @ConfigurationParameter(
      name = PARAM_EVENT_ATTRIBUTE_VIEW,
      description = "View containing Event annotations with their attributes. "
      + "If provided, the event attributes file will be written.")
  private String eventAttributeView;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEW,
      description = "View containing TemporalLink annotations between events "
      + "and the document creation time. If provided, the corresponding temporal links file will "
      + "be written.")
  private String temporalLinkEventToDocumentCreationTimeView;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEW,
      description = "View containing TemporalLink annotations between events "
      + "and times within the same sentence. If provided, the corresponding temporal links file "
      + "will be written.")
  private String temporalLinkEventToSameSentenceTimeView;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEW,
      description = "View containing TemporalLink annotations between events "
      + "and syntactically dominated events. If provided, the corresponding temporal links file "
      + "will be written.")
  private String temporalLinkEventToSubordinatedEventView;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEW,
      description = "View containing TemporalLink annotations between main "
      + "events in adjacent sentences. If provided, the corresponding temporal links file will be "
      + "written.")
  private String temporalLinkMainEventToNextSentenceMainEventView;

  public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

  public static final String PARAM_TEXT_VIEW = "textView";

  public static final String PARAM_DOCUMENT_CREATION_TIME_VIEW = "documentCreationTimeView";

  public static final String PARAM_TIME_EXTENT_VIEW = "timeExtentView";

  public static final String PARAM_TIME_ATTRIBUTE_VIEW = "timeAttributeView";

  public static final String PARAM_EVENT_EXTENT_VIEW = "eventExtentView";

  public static final String PARAM_EVENT_ATTRIBUTE_VIEW = "eventAttributeView";

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEW = "temporalLinkEventToDocumentCreationTimeView";

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEW = "temporalLinkEventToSameSentenceTimeView";

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEW = "temporalLinkEventToSubordinatedEventView";

  public static final String PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEW = "temporalLinkMainEventToNextSentenceMainEventView";

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
    this.baseWriter = this.createWriter(TempEval2010CollectionReader.BASE_SEGMENTATION_VIEW_NAME, this.textView);
    this.dctWriter = this.createWriter(TempEval2010CollectionReader.DCT_VIEW_NAME, this.documentCreationTimeView);
    this.timexExtentWriter = this.createWriter(
        TempEval2010CollectionReader.TIMEX_EXTENTS_VIEW_NAME,
        this.timeExtentView);
    this.timexAttributeWriter = this.createWriter(
        TempEval2010CollectionReader.TIMEX_ATTRIBUTES_VIEW_NAME,
        this.timeAttributeView);
    this.eventExtentWriter = this.createWriter(
        TempEval2010CollectionReader.EVENT_EXTENTS_VIEW_NAME,
        this.eventExtentView);
    this.eventAttributeWriter = this.createWriter(
        TempEval2010CollectionReader.EVENT_ATTRIBUTES_VIEW_NAME,
        this.eventAttributeView);
    this.tlinkDCTEventWriter = this.createWriter(
        TempEval2010CollectionReader.TLINK_DCT_EVENT_VIEW_NAME,
        this.temporalLinkEventToDocumentCreationTimeView);
    this.tlinkTimexEventWriter = this.createWriter(
        TempEval2010CollectionReader.TLINK_TIMEX_EVENT_VIEW_NAME,
        this.temporalLinkEventToSameSentenceTimeView);
    this.tlinkSubordinatedEventsWriter = this.createWriter(
        TempEval2010CollectionReader.TLINK_SUBORDINATED_EVENTS_VIEW_NAME,
        this.temporalLinkEventToSubordinatedEventView);
    this.tlinkMainEventsWriter = this.createWriter(
        TempEval2010CollectionReader.TLINK_MAIN_EVENTS_VIEW_NAME,
        this.temporalLinkMainEventToNextSentenceMainEventView);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // determine the filename
    URI uri = ViewUriUtil.getURI(jCas);
    String fileName = uri.getFragment();
    if (fileName == null) {
      fileName = new File(uri.getPath()).getName();
    }

    // get the view with text, sentences and tokens
    JCas textJCas = JCasUtil.getView(jCas, this.textView, false);

    // write the document creation time
    if (this.documentCreationTimeView != null) {
      JCas dctJCas = JCasUtil.getView(jCas, this.documentCreationTimeView, false);
      for (DocumentCreationTime time : JCasUtil.select(dctJCas, DocumentCreationTime.class)) {
        this.write(this.dctWriter, fileName, time.getValue().replaceAll("-", ""));
      }
    }

    // align tokens to times
    Map<Token, Time> tokenTimeExtents = new HashMap<Token, Time>();
    if (this.timeExtentView != null) {
      JCas timeExtentJCas = JCasUtil.getView(jCas, this.timeExtentView, false);
      for (Time time : JCasUtil.select(timeExtentJCas, Time.class)) {
        for (Token token : JCasUtil.selectCovered(textJCas, Token.class, time)) {
          tokenTimeExtents.put(token, time);
        }
      }
    }
    Map<Token, Time> tokenTimeAttributes = new HashMap<Token, Time>();
    if (this.timeAttributeView != null) {
      JCas timeAttributeJCas = JCasUtil.getView(jCas, this.timeAttributeView, false);
      for (Time time : JCasUtil.select(timeAttributeJCas, Time.class)) {
        for (Token token : JCasUtil.selectCovered(textJCas, Token.class, time)) {
          tokenTimeAttributes.put(token, time);
        }
      }
    }

    // align tokens to events
    Map<Token, Event> tokenEventExtents = new HashMap<Token, Event>();
    if (this.eventExtentView != null) {
      JCas eventExtentJCas = JCasUtil.getView(jCas, this.eventExtentView, false);
      for (Event event : JCasUtil.select(eventExtentJCas, Event.class)) {
        for (Token token : JCasUtil.selectCovered(textJCas, Token.class, event)) {
          tokenEventExtents.put(token, event);
        }
      }
    }

    Map<Token, Event> tokenEventAttributes = new HashMap<Token, Event>();
    if (this.eventAttributeView != null) {
      JCas eventAttributeJCas = JCasUtil.getView(jCas, this.eventAttributeView, false);
      for (Event event : JCasUtil.select(eventAttributeJCas, Event.class)) {
        for (Token token : JCasUtil.selectCovered(textJCas, Token.class, event)) {
          tokenEventAttributes.put(token, event);
        }
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
            tokenTimeExtents,
            tokenTimeAttributes,
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
            tokenEventExtents,
            tokenEventAttributes,
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

  private PrintWriter createWriter(String tabFileName, String viewParam)
      throws ResourceInitializationException {
    PrintWriter writer;
    if (viewParam != null) {
      try {
        writer = new PrintWriter(new FileWriter(new File(this.outputDirectory, tabFileName)));
      } catch (IOException e) {
        throw new ResourceInitializationException(e);
      }
    } else {
      writer = new PrintWriter(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          // do nothing
        }
      });
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
      Map<Token, T> tokenAnchorExtents,
      Map<Token, T> tokenAnchorAttributes,
      String anchorType,
      Token token,
      String fileName,
      int sentIndex,
      int tokenIndex,
      AttributeGetter<T> attributeGetter) {
    T anchor = tokenAnchorExtents.get(token);
    if (anchor != null) {
      String id = anchor.getId();
      this.write(extentWriter, fileName, sentIndex, tokenIndex, anchorType, id, "1");
    }
    anchor = tokenAnchorAttributes.get(token);
    if (anchor != null) {
      String id = anchor.getId();
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

  private void writeTemporalLinks(PrintWriter writer, JCas jCas, String viewName, String fileName) {
    if (viewName != null) {
      JCas view = JCasUtil.getView(jCas, viewName, false);
      for (TemporalLink tlink : JCasUtil.select(view, TemporalLink.class)) {
        String relation = tlink.getRelationType();
        if (relation == null) {
          relation = "NONE";
        }
        this.write(writer, fileName, tlink.getSource().getId(), tlink.getTarget().getId(), relation);
      }
    }
  }
}
