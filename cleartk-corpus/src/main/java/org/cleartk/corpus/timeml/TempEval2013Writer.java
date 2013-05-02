/*
 * Copyright (c) 2013, Regents of the University of Colorado 
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.util.ViewURIUtil;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.output.XMLOutputter;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

/**
 * Write events, times and temporal relations in the TempEval 2013 format.
 * 
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2013Writer extends JCasAnnotator_ImplBase {
  
  public static AnalysisEngineDescription getDescription(File outputDir)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        TempEval2013Writer.class,
        PARAM_OUTPUT_DIRECTORY,
        outputDir);
  }

  public static final String PARAM_OUTPUT_DIRECTORY = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2013Writer.class,
      "outputDirectory");

  @ConfigurationParameter(
      description = "Provides the path where the TimeML documents should be written.",
      mandatory = true)
  private File outputDirectory;

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String text = jCas.getDocumentText();
    
    // collect annotations that will be written and sort them by decreasing begin and increasing end
    Ordering<Span> spanOrdering = Span.BY_DECREASING_BEGIN.compound(Span.BY_INCREASING_END);
    List<AnnotationFS> annotations = this.getAnnotations(jCas);
    Collections.sort(annotations, spanOrdering.onResultOf(new Function<AnnotationFS, Span>() {
      @Override
      public Span apply(AnnotationFS annotation) {
        return new Span(annotation);
      }
    }));
    
    // create an XML element for each annotation; each annotation's children will already be
    // complete because of the sorting order
    TreeMap<Span, List<Element>> spanToElements = new TreeMap<Span, List<Element>>(spanOrdering); 
    for (AnnotationFS annotation : annotations) {
      
      // collect the spans covered by this annotation
      // (assume annotations with 0-spans do not cover any annotations) 
      List<Span> coveredSpans = new ArrayList<Span>();
      if (annotation.getBegin() != annotation.getEnd()) {
        for (Span span : spanToElements.headMap(new Span(annotation), true).keySet()) {
          if (annotation.getBegin() <= span.begin && span.end <= annotation.getEnd()) {
            coveredSpans.add(span);
          }
        }
      }
      
      // collect begin and end points for all the covered XML elements, in left-to-right order
      List<Integer> boundaries = new ArrayList<Integer>();
      boundaries.add(annotation.getBegin());
      boundaries.add(annotation.getEnd());
      for (Span span : coveredSpans) {
        boundaries.add(span.begin);
        boundaries.add(span.end);
      }
      Collections.sort(boundaries);
      
      // create the children - first a text node, then an element, then a text node, etc.
      List<Content> children = new ArrayList<Content>();
      for (int i = 0; i < boundaries.size() - 1; ++i) {
        int begin = boundaries.get(i);
        int end = boundaries.get(i + 1);
        
        // text node between elements
        if (i % 2 == 0) {
          children.add(new Text(text.substring(begin, end)));
        }
        
        // element that has already been completed
        else {
          children.addAll(spanToElements.remove(new Span(begin, end)));
        }
      }
      
      // convert the annotation to an element and add the children
      Element element = this.toElement(annotation);
      element.addContent(children);

      // map the annotation's span to the newly created elements 
      Span span = new Span(annotation);
      if (!spanToElements.containsKey(span)) {
        spanToElements.put(span, new ArrayList<Element>());
      }
      spanToElements.get(span).add(element);
    }
    
    // the root will be the only remaining element in the map, and will span the entire text
    Span rootSpan = new Span(0, text.length());
    List<Element> rootElements = spanToElements.get(rootSpan);
    if (rootElements == null || rootElements.size() != 1) {
      throw new IllegalArgumentException("Expected exactly one root, found " + spanToElements);
    }
    Element root = rootElements.get(0);
    
    // write the XML to the output file
    XMLOutputter outputter = new XMLOutputter();
    String fileName = new File(ViewURIUtil.getURI(jCas).getPath()).getName();
    String inputSuffix = ".TE3input";
    if (fileName.endsWith(inputSuffix)) {
      fileName = fileName.substring(0, fileName.length() - inputSuffix.length());
    }
    if (!fileName.endsWith(".tml")) {
      fileName += ".tml";
    }
    if (!this.outputDirectory.exists()) {
      this.outputDirectory.mkdirs();
    }
    File outputFile = new File(this.outputDirectory, fileName);
    try {
      FileOutputStream outputStream = new FileOutputStream(outputFile);
      try {
        outputter.output(root, outputStream);
      } finally {
        outputStream.close();
      }
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
  
  protected List<AnnotationFS> getAnnotations(JCas jCas) {
    int makeInstanceOffset = jCas.getDocumentText().length();
    List<AnnotationFS> annotations = new ArrayList<AnnotationFS>();
    FSIterator<Annotation> iterator = jCas.getAnnotationIndex().iterator();
    while (iterator.isValid() && iterator.hasNext()) {
      Annotation annotation = iterator.next();
      if (annotation instanceof DocumentAnnotation || annotation instanceof org.cleartk.timeml.type.Text || annotation instanceof Event || annotation instanceof Time  || annotation instanceof TemporalLink) {
        annotations.add(annotation);
        if (annotation instanceof DocumentCreationTime) {
          annotations.add(new DCT((DocumentCreationTime) annotation));
        }
        if (annotation instanceof Event) {
          annotations.add(new MakeInstance((Event) annotation, makeInstanceOffset));
        }
      }
    }
    return annotations;
  }
  
  protected Element toElement(AnnotationFS annotation) {
    Element element;
    if (annotation instanceof DocumentAnnotation) {
      element = new Element("TimeML");
    } else if (annotation instanceof DCT) {
      element = new Element("DCT");
    } else if (annotation instanceof org.cleartk.timeml.type.Text) {
      element = new Element("TEXT");
    } else if (annotation instanceof Event) {
      Event event = (Event) annotation;
      element = new Element("EVENT");
      element.setAttribute("eid", event.getId());
      element.setAttribute("class", nullToEmpty(event.getEventClass()));
      element.setAttribute("tense", nullToEmpty(event.getTense()));
      element.setAttribute("aspect", nullToEmpty(event.getAspect()));
      element.setAttribute("polarity", nullToEmpty(event.getPolarity()));
      element.setAttribute("modality", nullToEmpty(event.getModality()));
    } else if (annotation instanceof MakeInstance) {
      MakeInstance makeInstance = (MakeInstance) annotation;
      element = new Element("MAKEINSTANCE");
      element.setAttribute("eiid", makeInstance.annotation.getEventInstanceID());
      element.setAttribute("eventID", makeInstance.annotation.getId());
    } else if (annotation instanceof Time) {
      Time time = (Time) annotation;
      element = new Element("TIMEX3");
      element.setAttribute("tid", time.getId());
      element.setAttribute("type", nullToEmpty(time.getTimeType()));
      element.setAttribute("value", nullToEmpty(time.getValue()));
    } else if (annotation instanceof TemporalLink) {
      TemporalLink tlink = (TemporalLink) annotation;
      Anchor source = tlink.getSource();
      Anchor target = tlink.getTarget();
      element = new Element("TLINK");
      element.setAttribute("lid", tlink.getId());
      element.setAttribute("relType", tlink.getRelationType());
      if (source instanceof Event) {
        Event event = (Event) source;
        element.setAttribute("eventInstanceID", event.getEventInstanceID());
      } else if (source instanceof Time) {
        element.setAttribute("timeID", source.getId());
      }
      if (target instanceof Event) {
        Event event = (Event) target;
        element.setAttribute("relatedToEventInstance", event.getEventInstanceID());
      } else if (target instanceof Time) {
        element.setAttribute("relatedToTime", target.getId());
      }
    } else {
      throw new IllegalArgumentException("Unsupported annotation type: " + annotation);
    }
    return element;
  }
  
  private static String nullToEmpty(String string) {
    if (string == null) {
      string = "";
    }
    return string;
  }

  private static class Span implements Comparable<Span>{
    int begin;
    int end;

    public Span(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }
    
    public Span(AnnotationFS annotation) {
      this(annotation.getBegin(), annotation.getEnd());
    }
    
    @Override
    public String toString() {
      ToStringHelper helper = Objects.toStringHelper(this.getClass()); 
      return helper.add("begin", this.begin).add("end", this.end).toString();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.begin, this.end);
    }

    @Override
    public boolean equals(Object obj) {
      boolean result = false;
      if (obj.getClass().equals(Span.class)) {
        Span that = (Span)obj;
        result = this.begin == that.begin && this.end == that.end; 
      }
      return result;
    }

    @Override
    public int compareTo(Span that) {
      int compare = Ints.compare(this.begin, that.begin);
      if (compare != 0) {
        compare = Ints.compare(this.end, that.end);
      }
      return compare;
    }
    
    static Ordering<Span> BY_DECREASING_BEGIN = new Ordering<Span>() {
      @Override
      public int compare(Span left, Span right) {
        return -Ints.compare(left.begin, right.begin);
      }
    };
    
    static Ordering<Span> BY_INCREASING_END = new Ordering<Span>() {
      @Override
      public int compare(Span left, Span right) {
        return Ints.compare(left.end, right.end);
      }
    };
  }
  
  private static class MakeInstance extends FakeAnnotation<Event> {
    public MakeInstance(Event annotation, int offset) {
      super(annotation, offset, offset);
    }
  }
  
  private static class DCT extends FakeAnnotation<Time> {
    public DCT(Time time) {
      super(time, time.getBegin(), time.getEnd());
    }
  }
  
  // Fake annotation for various elements
  private static class FakeAnnotation<T extends Annotation> implements AnnotationFS {
    
    T annotation;
    private int begin;
    private int end;

    public FakeAnnotation(T annotation, int begin, int end) {
      this.annotation = annotation;
      this.begin = begin;
      this.end = end;
    }

    @Override
    public Object clone() {
      throw new UnsupportedOperationException();
    }

    @Override
    public CAS getView() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Type getType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setFeatureValue(Feature feat, FeatureStructure fs) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public FeatureStructure getFeatureValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setStringValue(Feature feat, String s) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getStringValue(Feature f) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public float getFloatValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setFloatValue(Feature feat, float f) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getIntValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setIntValue(Feature feat, int i) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public byte getByteValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setByteValue(Feature feat, byte i) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBooleanValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setBooleanValue(Feature feat, boolean i) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public short getShortValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setShortValue(Feature feat, short i) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLongValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setLongValue(Feature feat, long i) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getDoubleValue(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setDoubleValue(Feature feat, double i) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getFeatureValueAsString(Feature feat) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setFeatureValueFromString(Feature feat, String s) throws CASRuntimeException {
      throw new UnsupportedOperationException();
    }

    @Override
    public CAS getCAS() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getBegin() {
      return this.begin;
    }

    @Override
    public int getEnd() {
      return this.end;
    }

    @Override
    public String getCoveredText() {
      throw new UnsupportedOperationException();
    }
  }
}
