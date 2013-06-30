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
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.jdom2.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

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
    elementNames.put(DocumentCreationTime.class.getName(), "TIMEX3");
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
        "mod" }) {
      timeAttributes.add(new NamePair(name, name));
    }

    tlinkAttributes.add(new NamePair("lid", "id"));
    tlinkAttributes.add(new NamePair("relType", "relationType"));
    tlinkAttributes.add(new NamePair("signalID", "signalID"));

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

  public static String toTimeMLElementName(AnnotationFS annotation) {
    if (annotation instanceof Event) {
      return "EVENT";
    } else if (annotation instanceof Time) {
      return "TIMEX3";
    } else if (annotation instanceof TemporalLink) {
      return "TLINK";
    } else {
      return null;
    }
  }

  public static Attributes toTimeMLAttributes(AnnotationFS annotation, String elementName) {
    // add attributes that have a simple one-to-one mapping
    AttributesImpl attributes = new AttributesImpl();
    for (NamePair names : timemlAttributeLists.get(elementName)) {
      Feature feature = annotation.getType().getFeatureByBaseName(names.uimaName);
      addAttribute(attributes, names.timemlName, annotation.getFeatureValueAsString(feature));
    }
    // add un-mappable attributes
    if (annotation instanceof TemporalLink) {
      TemporalLink tlink = (TemporalLink) annotation;
      Anchor source = tlink.getSource();
      Anchor target = tlink.getTarget();
      if (source instanceof Event) {
        Event event = (Event) source;
        addAttribute(attributes, "eventID", event.getId());
        addAttribute(attributes, "eventInstanceID", event.getEventInstanceID());
      } else if (source instanceof Time) {
        addAttribute(attributes, "timeID", source.getId());
      }
      if (target instanceof Event) {
        Event event = (Event) target;
        addAttribute(attributes, "relatedToEvent", event.getId());
        addAttribute(attributes, "relatedToEventInstance", event.getEventInstanceID());
      } else if (target instanceof Time) {
        addAttribute(attributes, "relatedToTime", target.getId());
      }
    }
    return attributes;
  }

  public static Attributes toTempEval2007Attributes(AnnotationFS annotation, String elementName) {
    // add attributes that have a simple one-to-one mapping
    AttributesImpl attributes = new AttributesImpl();
    for (NamePair names : timemlAttributeLists.get(elementName)) {
      if (!names.timemlName.equals("eiid")) {
        Feature feature = annotation.getType().getFeatureByBaseName(names.uimaName);
        addAttribute(attributes, names.timemlName, annotation.getFeatureValueAsString(feature));
      }
    }
    // add un-mappable attributes
    if (annotation instanceof TemporalLink) {
      TemporalLink tlink = (TemporalLink) annotation;
      Anchor source = tlink.getSource();
      Anchor target = tlink.getTarget();
      if (source instanceof Event) {
        Event event = (Event) source;
        addAttribute(attributes, "eventID", event.getId());
      } else if (source instanceof Time) {
        addAttribute(attributes, "timeID", source.getId());
      }
      if (target instanceof Event) {
        Event event = (Event) target;
        addAttribute(attributes, "relatedToEvent", event.getId());
      } else if (target instanceof Time) {
        addAttribute(attributes, "relatedToTime", target.getId());
      }
    }
    return attributes;
  }

  private static void addAttribute(AttributesImpl attributes, String name, String value) {
    if (value != null) {
      attributes.addAttribute("", name, name, "CDATA", value);
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
