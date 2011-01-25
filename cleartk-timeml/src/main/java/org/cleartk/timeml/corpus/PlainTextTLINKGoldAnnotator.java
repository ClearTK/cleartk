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
package org.cleartk.timeml.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.ParamUtil;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 * 
 */
public class PlainTextTLINKGoldAnnotator extends JCasAnnotator_ImplBase {

  public static final String PARAM_TLINK_FILE_URL = ConfigurationParameterFactory
      .createConfigurationParameterName(PlainTextTLINKGoldAnnotator.class, "tlinkFileUrl");

  @ConfigurationParameter(mandatory = true, description = "the URL to a plain-text TLINK file, e.g."
      + "http://people.cs.kuleuven.be/~steven.bethard/data/timebank-verb-clause.txt")
  private String tlinkFileUrl;

  public void setTlinkFileUrl(String tlinkFileUrl) {
    this.tlinkFileUrl = tlinkFileUrl;
  }

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        PlainTextTLINKGoldAnnotator.class,
        TimeMLComponents.TYPE_SYSTEM_DESCRIPTION,
        PARAM_TLINK_FILE_URL,
        ParamUtil.getParameterValue(
            PARAM_TLINK_FILE_URL,
            "http://people.cs.kuleuven.be/~steven.bethard/data/timebank-verb-clause.txt"));
  }

  private Map<String, List<TLINK>> fileTLINKs;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    this.fileTLINKs = new HashMap<String, List<TLINK>>();
    try {
      BufferedReader tlinkFileReader = new BufferedReader(new InputStreamReader(new URL(
          this.tlinkFileUrl).openStream()));
      String line;
      while ((line = tlinkFileReader.readLine()) != null)
        if (!line.startsWith("#")) {
          String[] columns = line.split("\\s+");
          TLINK tlink = new TLINK(columns[1], columns[2], columns[3]);
          if (!this.fileTLINKs.containsKey(columns[0])) {
            this.fileTLINKs.put(columns[0], new ArrayList<TLINK>());
          }
          this.fileTLINKs.get(columns[0]).add(tlink);
        }
      tlinkFileReader.close();
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String filePath = ViewURIUtil.getURI(jCas);
    String fileBase = new File(filePath).getName().replaceAll("\\..*", "");
    if (this.fileTLINKs.containsKey(fileBase)) {
      Map<String, Anchor> anchors = new HashMap<String, Anchor>();
      for (Anchor anchor : AnnotationRetrieval.getAnnotations(jCas, Anchor.class)) {
        anchors.put(anchor.getId(), anchor);
        if (anchor instanceof Event) {
          Event event = (Event) anchor;
          anchors.put(event.getEventInstanceID(), event);
        }
      }
      for (TLINK tlink : this.fileTLINKs.get(fileBase)) {
        int offset = jCas.getDocumentText().length();
        TemporalLink temporalLink = new TemporalLink(jCas, offset, offset);
        Anchor source = this.getAnchor(anchors, tlink.sourceID);
        Anchor target = this.getAnchor(anchors, tlink.targetID);
        temporalLink.setSource(source);
        temporalLink.setTarget(target);
        if (source instanceof Event) {
          temporalLink.setEventID(source.getId());
          temporalLink.setEventInstanceID(((Event) source).getEventInstanceID());
        } else if (source instanceof Time) {
          temporalLink.setTimeID(source.getId());
        }
        if (target instanceof Event) {
          temporalLink.setRelatedToEvent(target.getId());
          temporalLink.setRelatedToEventInstance(((Event) target).getEventInstanceID());
        } else if (target instanceof Time) {
          temporalLink.setRelatedToTime(target.getId());
        }
        temporalLink.setRelationType(tlink.relationType);
        temporalLink.addToIndexes();
      }
    }
  }

  private Anchor getAnchor(Map<String, Anchor> anchors, String id) {
    Anchor anchor = anchors.get(id);
    if (anchor == null) {
      throw new RuntimeException(String.format("no anchor for id %s", id));
    }
    return anchor;
  }

  private static class TLINK {
    public String sourceID;

    public String targetID;

    public String relationType;

    public TLINK(String sourceID, String targetID, String relationType) {
      this.sourceID = sourceID;
      this.targetID = targetID;
      this.relationType = relationType;
    }

    @Override
    public String toString() {
      return String.format("TLINK(%s, %s, %s)", this.sourceID, this.targetID, this.relationType);
    }
  }

}
