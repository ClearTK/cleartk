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
package org.cleartk.timeml.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 * 
 */
public class TimeMLUtil {

  private static final Map<String, String> elementNames;

  private static final List<NamePair> eventAttributes;

  private static final List<NamePair> timeAttributes;

  private static final List<NamePair> tlinkAttributes;

  private static final Map<String, List<NamePair>> timemlAttributeLists;

  private static final Map<Class<?>, List<NamePair>> uimaAttributeLists;

  static {
    elementNames = new HashMap<String, String>();
    eventAttributes = new ArrayList<NamePair>();
    timeAttributes = new ArrayList<NamePair>();
    tlinkAttributes = new ArrayList<NamePair>();
    timemlAttributeLists = new HashMap<String, List<NamePair>>();
    uimaAttributeLists = new HashMap<Class<?>, List<NamePair>>();

    elementNames.put("Document", "TimeML");
    elementNames.put(Event.class.getName(), "EVENT");
    elementNames.put(Time.class.getName(), "TIMEX3");
    elementNames.put(TemporalLink.class.getName(), "TLINK");

    eventAttributes.add(new NamePair("eid", "id"));
    eventAttributes.add(new NamePair("eiid", "eventInstanceID"));
    eventAttributes.add(new NamePair("class", "eventClass"));
    for (String name : new String[] {
        "stem",
        "pos",
        "tense",
        "aspect",
        "cardinality",
        "polarity",
        "modality",
        "signalID" }) {
      eventAttributes.add(new NamePair(name, name));
    }

    timeAttributes.add(new NamePair("tid", "id"));
    timeAttributes.add(new NamePair("type", "timeType"));
    for (String name : new String[] {
        "beginPoint",
        "endPoint",
        "quant",
        "freq",
        "functionInDocument",
        "temporalFunction",
        "value",
        "valueFromFunction",
        "mod",
        "anchorTimeID" }) {
      timeAttributes.add(new NamePair(name, name));
    }

    tlinkAttributes.add(new NamePair("lid", "id"));
    tlinkAttributes.add(new NamePair("relType", "relationType"));
    for (String name : new String[] {
        "eventInstanceID",
        "eventID",
        "timeID",
        "relatedToEventInstance",
        "relatedToEvent",
        "relatedToTime",
        "signalID" }) {
      tlinkAttributes.add(new NamePair(name, name));
    }

    timemlAttributeLists.put("EVENT", eventAttributes);
    timemlAttributeLists.put("MAKEINSTANCE", eventAttributes);
    timemlAttributeLists.put("TIMEX3", timeAttributes);
    timemlAttributeLists.put("TLINK", tlinkAttributes);

    uimaAttributeLists.put(Event.class, eventAttributes);
    uimaAttributeLists.put(Time.class, timeAttributes);
    uimaAttributeLists.put(TemporalLink.class, tlinkAttributes);
  }

  public static void copyAttributes(Element element, Annotation annotation, JCas jCas) {
    for (NamePair names : timemlAttributeLists.get(element.getName())) {
      String featureValue = element.getAttributeValue(names.timemlName);
      if (featureValue != null) {
        String className = annotation.getClass().getName();
        String uimaName = String.format("%s:%s", className, names.uimaName);
        Feature feature = jCas.getTypeSystem().getFeatureByFullName(uimaName);
        annotation.setFeatureValueFromString(feature, featureValue);
      }
    }
  }

  public static void removeInconsistentAttributes(Element element, Annotation annotation, JCas jCas) {
    for (NamePair names : timemlAttributeLists.get(element.getName())) {
      String newValue = element.getAttributeValue(names.timemlName);
      String className = annotation.getClass().getName();
      String uimaName = String.format("%s:%s", className, names.uimaName);
      Feature feature = jCas.getTypeSystem().getFeatureByFullName(uimaName);
      String oldValue = annotation.getFeatureValueAsString(feature);
      if (oldValue != null && !oldValue.equals(newValue)) {
        annotation.setFeatureValueFromString(feature, null);
      }
    }
  }

  public static void uimaToTimemlNames(Element element) {
    String name = element.getName();
    String replacementName = elementNames.get(name);
    if (replacementName != null) {
      element.setName(replacementName);
    }
    List<NamePair> namePairs = timemlAttributeLists.get(element.getName());
    if (namePairs != null) {
      Set<String> validNames = new HashSet<String>();
      for (NamePair names : namePairs) {
        validNames.add(names.timemlName);
        Attribute attribute = element.getAttribute(names.uimaName);
        if (attribute != null) {
          if (attribute.getValue().equals("null")) {
            element.removeAttribute(attribute);
          } else {
            attribute.setName(names.timemlName);
          }
        }
      }
      for (Object attrObj : element.getAttributes().toArray()) {
        Attribute attribute = (Attribute) attrObj;
        if (!validNames.contains(attribute.getName())) {
          element.removeAttribute(attribute);
        }
      }
    }
  }

  private static class NamePair {
    public String timemlName;

    public String uimaName;

    public NamePair(String timemlName, String uimaName) {
      this.timemlName = timemlName;
      this.uimaName = uimaName;
    }
  }
}
