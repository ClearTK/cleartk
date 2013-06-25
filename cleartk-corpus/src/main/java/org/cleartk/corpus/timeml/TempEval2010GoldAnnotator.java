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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010GoldAnnotator extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(TempEval2010GoldAnnotator.class);
  }

  @ConfigurationParameter(
      name = PARAM_TEXT_VIEWS, 
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where document text should be placed")
  private String[] textViews;

  @ConfigurationParameter(
      name = PARAM_DOCUMENT_CREATION_TIME_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where DocumentCreationTime annotations should be placed")
  private String[] documentCreationTimeViews;

  @ConfigurationParameter(
      name = PARAM_TIME_EXTENT_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where Time annotations should be placed")
  private String[] timeExtentViews;

  @ConfigurationParameter(
      name = PARAM_TIME_ATTRIBUTE_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where Time annotation attributes should be placed")
  private String[] timeAttributeViews;

  @ConfigurationParameter(
      name = PARAM_EVENT_EXTENT_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where Event annotations should be placed")
  private String[] eventExtentViews;

  @ConfigurationParameter(
      name = PARAM_EVENT_ATTRIBUTE_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where Event annotation attributes should be placed")
  private String[] eventAttributeViews;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where TemporalLink annotations between events and the document creation time should be placed")
  private String[] temporalLinkEventToDocumentCreationTimeViews;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where TemporalLink annotations between events and times within the same sentence should be placed")
  private String[] temporalLinkEventToSameSentenceTimeViews;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where TemporalLink annotations between events and syntactically dominated events should be placed")
  private String[] temporalLinkEventToSubordinatedEventViews;

  @ConfigurationParameter(
      name = PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEWS,
      defaultValue = CAS.NAME_DEFAULT_SOFA,
      description = "Views where TemporalLink annotations between main events in adjacent sentences should be placed")
  private String[] temporalLinkMainEventToNextSentenceMainEventViews;

  public static final String PARAM_TEXT_VIEWS = "textViews";

  public static final String PARAM_DOCUMENT_CREATION_TIME_VIEWS = "documentCreationTimeViews";

  public static final String PARAM_TIME_EXTENT_VIEWS = "timeExtentViews";

  public static final String PARAM_TIME_ATTRIBUTE_VIEWS = "timeAttributeViews";

  public static final String PARAM_EVENT_EXTENT_VIEWS = "eventExtentViews";

  public static final String PARAM_EVENT_ATTRIBUTE_VIEWS = "eventAttributeViews";

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEWS = "temporalLinkEventToDocumentCreationTimeViews";

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEWS =  "temporalLinkEventToSameSentenceTimeViews";

  public static final String PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEWS = "temporalLinkEventToSubordinatedEventViews";

  public static final String PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEWS = "temporalLinkMainEventToNextSentenceMainEventViews";

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    // load the sentences and tokens from the view
    ListMultimap<Integer, String> sentTokens = ArrayListMultimap.create();
    for (String line : lines(jCas, TempEval2010CollectionReader.BASE_SEGMENTATION_VIEW_NAME)) {
      String[] columns = split(line, "<filename>", "<sent_no>", "<token_no>", "<text>");
      int sentIndex = new Integer(columns[1]);
      String text = columns[3];
      sentTokens.put(sentIndex, text);
    }

    // create the sentences and tokens
    Map<String, StringBuilder> textBuilders = new HashMap<String, StringBuilder>();
    for (String viewName : this.textViews) {
      StringBuilder textBuilder = new StringBuilder("\n\n"); // leave line for document time
      JCas view = JCasUtil.getView(jCas, viewName, true);
      for (int i = 0; i < sentTokens.keySet().size(); ++i) {
        int sentBegin = textBuilder.length();
        List<Token> tokens = new ArrayList<Token>();
        for (String tokenText : sentTokens.get(i)) {
          int tokenBegin = textBuilder.length();
          textBuilder.append(tokenText);
          int tokenEnd = textBuilder.length();
          textBuilder.append(' ');
          Token token = new Token(view, tokenBegin, tokenEnd);
          token.addToIndexes();
          tokens.add(token);
        }
        int sentEnd = textBuilder.length() - 1;
        textBuilder.setCharAt(sentEnd, '\n');
        Sentence sentence = new Sentence(view, sentBegin, sentEnd);
        sentence.addToIndexes();
      }
      textBuilders.put(viewName, textBuilder);
    }

    // add the document creation time
    for (String line : lines(jCas, TempEval2010CollectionReader.DCT_VIEW_NAME)) {
      String[] dctColumns = split(line, "<filename>", "<dct>");
      String dctValue = dctColumns[1].replaceAll("(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3");
      for (String viewName : this.documentCreationTimeViews) {
        JCas view = JCasUtil.getView(jCas, viewName, true);
        DocumentCreationTime docTime = new DocumentCreationTime(view, 1, 1);
        docTime.setId("t0");
        docTime.setTimeType("DATE");
        docTime.setValue(dctValue);
        docTime.setFunctionInDocument("CREATION_TIME");
        docTime.addToIndexes();
      }
    }

    // add Time annotations
    addSpans(
        jCas,
        TempEval2010CollectionReader.TIMEX_EXTENTS_VIEW_NAME,
        "timex3",
        this.timeExtentViews,
        new AnnotationConstructor<Time>() {
          @Override
          public Time apply(JCas aJCas, int begin, int end) {
            return new Time(aJCas, begin, end);
          }
        });

    // add Time attributes
    addAttributes(
        jCas,
        TempEval2010CollectionReader.TIMEX_ATTRIBUTES_VIEW_NAME,
        Time.class,
        this.timeAttributeViews,
        new AttributeSetter<Time>() {
          @Override
          public void apply(Time time, String attrName, String attrValue) {
            if (attrName.equals("type")) {
              time.setTimeType(attrValue);
            } else if (attrName.equals("value")) {
              time.setValue(attrValue);
            } else {
              String message = "Unexpected TIMEX attribute %s=%s";
              throw new IllegalArgumentException(String.format(message, attrName, attrValue));
            }
          }
        });

    // add Event annotations
    addSpans(
        jCas,
        TempEval2010CollectionReader.EVENT_EXTENTS_VIEW_NAME,
        "event",
        this.eventExtentViews,
        new AnnotationConstructor<Event>() {
          @Override
          public Event apply(JCas aJCas, int begin, int end) {
            return new Event(aJCas, begin, end);
          }
        });

    // add Event attributes
    addAttributes(
        jCas,
        TempEval2010CollectionReader.EVENT_ATTRIBUTES_VIEW_NAME,
        Event.class,
        this.eventAttributeViews,
        new AttributeSetter<Event>() {
          @Override
          public void apply(Event event, String attrName, String attrValue) {
            if (attrName.equals("pos")) {
              event.setPos(attrValue);
            } else if (attrName.equals("tense")) {
              event.setTense(attrValue);
            } else if (attrName.equals("aspect")) {
              event.setAspect(attrValue);
            } else if (attrName.equals("class")) {
              event.setEventClass(attrValue);
            } else if (attrName.equals("polarity")) {
              event.setPolarity(attrValue);
            } else if (attrName.equals("modality")) {
              event.setModality(attrValue);
            } else {
              String message = "Unexpected EVENT attribute %s=%s";
              throw new IllegalArgumentException(String.format(message, attrName, attrValue));
            }
          }
        });

    // add TemporalLink annotations
    addTemporalLinks(
        jCas,
        TempEval2010CollectionReader.TLINK_DCT_EVENT_VIEW_NAME,
        textBuilders,
        this.temporalLinkEventToDocumentCreationTimeViews);
    addTemporalLinks(
        jCas,
        TempEval2010CollectionReader.TLINK_TIMEX_EVENT_VIEW_NAME,
        textBuilders,
        this.temporalLinkEventToSameSentenceTimeViews);
    addTemporalLinks(
        jCas,
        TempEval2010CollectionReader.TLINK_SUBORDINATED_EVENTS_VIEW_NAME,
        textBuilders,
        this.temporalLinkEventToSubordinatedEventViews);
    addTemporalLinks(
        jCas,
        TempEval2010CollectionReader.TLINK_MAIN_EVENTS_VIEW_NAME,
        textBuilders,
        this.temporalLinkMainEventToNextSentenceMainEventViews);

    // set the document text
    for (String viewName : this.textViews) {
      JCas view = JCasUtil.getView(jCas, viewName, true);
      view.setDocumentText(textBuilders.get(viewName).toString());
    }
  }

  private static String[] split(String line, String... expected) {
    String[] columns = line.split("\t");
    if (columns.length != expected.length) {
      throw new IllegalArgumentException(String.format(
          "Expected % d items, %s, found %d items, %s",
          expected.length,
          Joiner.on('\t').join(expected),
          columns.length,
          line));
    }
    return columns;
  }

  private static String[] lines(JCas jCas, String viewName) throws AnalysisEngineProcessException {
    JCas view;
    try {
      view = jCas.getView(viewName);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
    String text = view.getDocumentText();
    if (text == null) {
      throw new IllegalArgumentException("no text in view " + viewName);
    }
    return text.length() > 0 ? text.split("\n") : new String[0];
  }

  private static interface AnnotationConstructor<T extends Annotation> {
    public T apply(JCas jCas, int begin, int end);
  }

  private static <T extends Anchor> void addSpans(
      JCas jCas,
      String tabViewName,
      String elementName,
      String[] annotationViewNames,
      AnnotationConstructor<T> constructor) throws AnalysisEngineProcessException {
    String[] lines = lines(jCas, tabViewName);
    for (String annotationViewName : annotationViewNames) {
      JCas view = JCasUtil.getView(jCas, annotationViewName, true);
      Map<String, T> idMap = new HashMap<String, T>();
      List<List<Token>> sentenceTokens = new ArrayList<List<Token>>();
      for (Sentence sentence : JCasUtil.select(view, Sentence.class)) {
        sentenceTokens.add(JCasUtil.selectCovered(view, Token.class, sentence));
      }
      for (String line : lines) {
        String[] columns = split(
            line,
            "<filename>",
            "<sent_no>",
            "<token_no>",
            elementName,
            "<id>",
            "1");
        int sentIndex = Integer.parseInt(columns[1]);
        int tokenIndex = Integer.parseInt(columns[2]);
        String id = columns[4];
        Token token = sentenceTokens.get(sentIndex).get(tokenIndex);
        if (!idMap.containsKey(id)) {
          T ann = constructor.apply(view, token.getBegin(), token.getEnd());
          ann.setId(id);
          ann.addToIndexes();
          idMap.put(id, ann);
        } else {
          T ann = idMap.get(id);
          if (token.getBegin() < ann.getBegin()) {
            ann.setBegin(token.getBegin());
          }
          if (token.getEnd() > ann.getEnd()) {
            ann.setEnd(token.getEnd());
          }
        }
      }
    }
  }

  private static interface AttributeSetter<T extends Annotation> {
    public void apply(T ann, String attrName, String attrValue);
  }

  private static <T extends Anchor> void addAttributes(
      JCas jCas,
      String tabViewName,
      Class<T> cls,
      String[] annotationViewNames,
      AttributeSetter<T> setter) throws AnalysisEngineProcessException {
    String[] lines = lines(jCas, tabViewName);
    for (String annotationViewName : annotationViewNames) {
      JCas view = JCasUtil.getView(jCas, annotationViewName, false);
      Map<String, T> idMap = new HashMap<String, T>();
      for (T anchor : JCasUtil.select(view, cls)) {
        idMap.put(anchor.getId(), anchor);
      }
      for (String line : lines) {
        String[] columns = split(
            line,
            "<filename>",
            "<sent_no>",
            "<token_no>",
            "timex3",
            "<id>",
            "1",
            "<attribute>",
            "<value>");
        String id = columns[4];
        String attrName = columns[6];
        String attrValue = columns[7];
        setter.apply(idMap.get(id), attrName, attrValue);
      }
    }
  }

  private static void addTemporalLinks(
      JCas jCas,
      String tabViewName,
      Map<String, StringBuilder> textBuilders,
      String[] annotationViewNames) throws AnalysisEngineProcessException {
    String[] lines = lines(jCas, tabViewName);
    for (String annotationViewName : annotationViewNames) {
      JCas view = JCasUtil.getView(jCas, annotationViewName, true);
      Map<String, Anchor> idAnchors = new HashMap<String, Anchor>();
      for (Anchor anchor : JCasUtil.select(view, Anchor.class)) {
        idAnchors.put(anchor.getId(), anchor);
      }
      StringBuilder textBuilder = textBuilders.get(annotationViewName);
      for (String line : lines) {
        String[] columns = split(line, "<filename>", "<eid>", "<tid>", "<relation>");
        String sourceID = columns[1];
        String targetID = columns[2];
        String relation = columns[3];
        int offset = textBuilder.length();
        TemporalLink tlink = new TemporalLink(view, offset, offset);
        tlink.setSource(idAnchors.get(sourceID));
        tlink.setTarget(idAnchors.get(targetID));
        tlink.setRelationType(relation);
        tlink.addToIndexes();
        textBuilder.append('\n');
      }
    }
  }
}
