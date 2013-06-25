/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Text;
import org.cleartk.timeml.type.Time;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.ViewURIUtil;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 * 
 */
@SofaCapability(inputSofas = { TimeMLGoldAnnotator.TIMEML_VIEW_NAME, CAS.NAME_DEFAULT_SOFA })
public class TimeMLGoldAnnotator extends JCasAnnotator_ImplBase {

  public static final String TIMEML_VIEW_NAME = "TimeMLView";

  public static final String PARAM_LOAD_TLINKS = "loadTlinks";

  @ConfigurationParameter(
      name = PARAM_LOAD_TLINKS,
      description = "when false indicates that annotation should not be created for TLINKs (though annotations will still be created for TIMEX3s, EVENTs, etc.).",
      defaultValue = "true")
  private boolean loadTlinks;

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(TimeMLGoldAnnotator.class);
  }

  public static AnalysisEngineDescription getDescriptionNoTLINKs()
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        TimeMLGoldAnnotator.class,
        PARAM_LOAD_TLINKS,
        false);
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    JCas timemlView;
    JCas initialView;
    try {
      timemlView = jCas.getView(TIMEML_VIEW_NAME);
      initialView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }

    String timeML = timemlView.getDocumentText();
    SAXBuilder builder = new SAXBuilder();
    builder.setDTDHandler(null);
    Element root;
    try {
      Document doc = builder.build(new StringReader(timeML));
      root = doc.getRootElement();
    } catch (JDOMException e) {
      getContext().getLogger().log(
          Level.SEVERE,
          "problem parsing document: " + ViewURIUtil.getURI(jCas));
      throw new AnalysisEngineProcessException(e);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }

    // collect the document text, add the Event, Time and TemporalLink annotations,
    // and collect the necessary information to fill in the cross-annotation links
    StringBuffer textBuffer = new StringBuffer();
    Map<String, Anchor> anchors = new HashMap<String, Anchor>();
    Map<Time, String> anchorTimeIDs = new HashMap<Time, String>();
    List<Element> makeInstances = new ArrayList<Element>();
    Map<TemporalLink, String> tlinkSourceIDs = new HashMap<TemporalLink, String>();
    Map<TemporalLink, String> tlinkTargetIDs = new HashMap<TemporalLink, String>();
    this.addAnnotations(
        initialView,
        root,
        textBuffer,
        anchors,
        anchorTimeIDs,
        makeInstances,
        tlinkSourceIDs,
        tlinkTargetIDs);
    initialView.setDocumentText(textBuffer.toString());

    // point make-instance IDs to their events, and copy attributes over
    Set<Event> processedEvents = new HashSet<Event>();
    for (Element makeInstance : makeInstances) {
      String eventID = makeInstance.getAttributeValue("eventID");
      String eventInstanceID = makeInstance.getAttributeValue("eiid");
      Event event = (Event) this.getAnchor(jCas, anchors, eventID);
      anchors.put(eventInstanceID, event);
      if (!processedEvents.contains(event)) {
        TimeMLUtil.copyAttributes(makeInstance, event, jCas);
        processedEvents.add(event);
      } else {
        String makeInstanceXML = new XMLOutputter().outputString(makeInstance);
        String message = "Ignoring attributes from additional %s in %s";
        String fileName = ViewURIUtil.getURI(jCas).toString();
        this.getLogger().warn(String.format(message, makeInstanceXML, fileName));
      }
    }

    // set anchor times
    for (Time time : anchorTimeIDs.keySet()) {
      Time anchorTime = (Time) this.getAnchor(jCas, anchors, anchorTimeIDs.get(time));
      time.setAnchorTime(anchorTime);
    }

    // set tlink sources and targets
    for (TemporalLink tlink : tlinkSourceIDs.keySet()) {
      tlink.setSource(this.getAnchor(jCas, anchors, tlinkSourceIDs.get(tlink)));
      tlink.setTarget(this.getAnchor(jCas, anchors, tlinkTargetIDs.get(tlink)));
    }
  }

  private void addAnnotations(
      JCas jCas,
      Element element,
      StringBuffer textBuffer,
      Map<String, Anchor> anchors,
      Map<Time, String> anchorTimeIDs,
      List<Element> makeInstances,
      Map<TemporalLink, String> tlinkSourceIDs,
      Map<TemporalLink, String> tlinkTargetIDs) throws AnalysisEngineProcessException {
    int startOffset = textBuffer.length();
    for (Content content : element.getContent()) {
      if (content instanceof org.jdom2.Text) {
        textBuffer.append(((org.jdom2.Text) content).getText());
      } else if (content instanceof Element) {
        this.addAnnotations(
            jCas,
            (Element) content,
            textBuffer,
            anchors,
            anchorTimeIDs,
            makeInstances,
            tlinkSourceIDs,
            tlinkTargetIDs);
      }
    }
    int endOffset = textBuffer.length();

    if (element.getName().equals("TIMEX3")) {
      String funcInDoc = element.getAttributeValue("functionInDocument");
      boolean isCreationTime = funcInDoc != null && funcInDoc.equals("CREATION_TIME");
      Time time = isCreationTime
          ? new DocumentCreationTime(jCas, startOffset, endOffset)
          : new Time(jCas, startOffset, endOffset);
      TimeMLUtil.copyAttributes(element, time, jCas);
      String anchorTimeID = element.getAttributeValue("anchorTimeID");
      if (anchorTimeID != null) {
        anchorTimeIDs.put(time, anchorTimeID);
      }
      anchors.put(time.getId(), time);
      time.addToIndexes();
    } else if (element.getName().equals("EVENT")) {
      Event event = new Event(jCas, startOffset, endOffset);
      TimeMLUtil.copyAttributes(element, event, jCas);
      anchors.put(event.getId(), event);
      event.addToIndexes();
    } else if (element.getName().equals("MAKEINSTANCE")) {
      makeInstances.add(element);
    } else if (element.getName().equals("TLINK") && this.loadTlinks) {
      TemporalLink temporalLink = new TemporalLink(jCas, startOffset, endOffset);
      TimeMLUtil.copyAttributes(element, temporalLink, jCas);
      String sourceID = this.getOneOf(element, "eventInstanceID", "eventID", "timeID");
      String targetID = this.getOneOf(
          element,
          "relatedToEventInstance",
          "relatedToEvent",
          "relatedToTime");
      tlinkSourceIDs.put(temporalLink, sourceID);
      tlinkTargetIDs.put(temporalLink, targetID);
      temporalLink.addToIndexes();
    } else if (element.getName().equals("TEXT")) {
      Text text = new Text(jCas, startOffset, endOffset);
      text.addToIndexes();
    } else if (element.getName().toLowerCase().equals("s")) {
      Sentence sentence = new Sentence(jCas, startOffset, endOffset);
      sentence.addToIndexes();
    }
  }

  private String getOneOf(Element element, String... attributeNames) {
    for (String name : attributeNames) {
      String result = element.getAttributeValue(name);
      if (result != null) {
        return result;
      }
    }
    throw new RuntimeException(String.format(
        "unable to find in %s any of the following attributes: %s",
        element,
        Arrays.asList(attributeNames)));
  }

  private Anchor getAnchor(JCas jCas, Map<String, Anchor> anchors, String id)
      throws AnalysisEngineProcessException {
    Anchor anchor = anchors.get(id);
    if (anchor == null) {
      throw new RuntimeException(String.format(
          "%s: no anchor for id %s",
          ViewURIUtil.getURI(jCas),
          id));
    }
    return anchor;
  }

  public void setLoadTlinks(boolean loadTLINKs) {
    this.loadTlinks = loadTLINKs;
  }
}
