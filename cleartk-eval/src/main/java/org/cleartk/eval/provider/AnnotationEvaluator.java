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
package org.cleartk.eval.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.factory.initializable.InitializableFactory;
import org.uimafit.util.JCasUtil;

/**
 * An annotator that compares the annotations in a gold (human-annotated) view to the annotations in
 * a system-annotated view. It can be used either to compare annotation spans (e.g. for a task like
 * named entity recognition) or to compare annotation attributes (e.g. for a task like document
 * classification).
 * 
 * Evaluation statistics (e.g. precision, recall, F1) for each batch as defined by
 * {@link #batchProcessComplete()} and for the overall collection as defined by
 * {@link #collectionProcessComplete()} will be logged at {@link Level#WARNING}. Additionally,
 * information about each system error will be logged at {@link Level#INFO}.
 * 
 * (Though it might have been preferable to log the main evaluation statistics at {@link Level#INFO}
 * and the fine-grained error information at {@link Level#FINE}, this is not practical because UIMA
 * already logs lots of useless method begin/end messages at {@link Level#FINE}.)
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class AnnotationEvaluator<T extends Comparable<? super T>> extends JCasAnnotator_ImplBase {

  public static interface SpanExtractor<T extends Comparable<? super T>> {
    public T getSpan(Annotation annotation);
  }

  public static class AnnotationSpanExtractor implements SpanExtractor<AnnotationSpan> {
    @Override
    public AnnotationSpan getSpan(Annotation annotation) {
      return new AnnotationSpan(annotation);
    }
  }

  public static AnalysisEngineDescription getSpanDescription(
      Class<? extends Annotation> annotationClass,
      String goldView,
      String systemView) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        AnnotationEvaluator.class,
        TypeSystemDescriptionFactory.createTypeSystemDescription(annotationClass),
        PARAM_ANNOTATION_CLASS_NAME,
        annotationClass.getName(),
        PARAM_GOLD_VIEW_NAME,
        goldView,
        PARAM_SYSTEM_VIEW_NAME,
        systemView);
  }

  public static AnalysisEngineDescription getAttributeDescription(
      Class<? extends Annotation> annotationClass,
      String annotationAttributeName,
      String goldView,
      String systemView) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        AnnotationEvaluator.class,
        TypeSystemDescriptionFactory.createTypeSystemDescription(annotationClass),
        PARAM_ANNOTATION_CLASS_NAME,
        annotationClass.getName(),
        PARAM_ANNOTATION_ATTRIBUTE_NAME,
        annotationAttributeName,
        PARAM_GOLD_VIEW_NAME,
        goldView,
        PARAM_SYSTEM_VIEW_NAME,
        systemView);
  }

  @ConfigurationParameter(
      mandatory = true,
      description = "The annotation class whose gold and system spans should be compared")
  private String annotationClassName;

  @ConfigurationParameter(
      description = "The attribute whose gold and system values should be compared (if null, only "
          + "the annotation spans will be compared)")
  private String annotationAttributeName;

  @ConfigurationParameter(
      defaultValue = "org.cleartk.eval.provider.AnnotationEvaluator$AnnotationSpanExtractor",
      description = "The name of the class that extracts the span of an annotation. Defaults to "
          + "an extractor that looks at the annotation begin and end offsets.")
  private String spanExtractorClassName;

  @ConfigurationParameter(
      mandatory = true,
      description = "The name of the CAS view containing the gold (manual) annotations")
  private String goldViewName;

  @ConfigurationParameter(
      mandatory = true,
      description = "The name of the CAS view containing the system (automatic) annotations")
  private String systemViewName;

  @ConfigurationParameter(
      defaultValue = "false",
      description = "Ignore additional spans produced by the system that do not match any of the "
          + "gold spans. This can be useful when doing an attribute classification task where "
          + "gold standard data is only available for a subset of all possible classifications.")
  private boolean ignoreSystemSpansNotInGold;

  public static final String PARAM_ANNOTATION_CLASS_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      AnnotationEvaluator.class,
      "annotationClassName");

  public static final String PARAM_ANNOTATION_ATTRIBUTE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      AnnotationEvaluator.class,
      "annotationAttributeName");

  public static final String PARAM_SPAN_EXTRACTOR_CLASS_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      AnnotationEvaluator.class,
      "spanExtractorClassName");

  public static final String PARAM_GOLD_VIEW_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      AnnotationEvaluator.class,
      "goldViewName");

  public static final String PARAM_SYSTEM_VIEW_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      AnnotationEvaluator.class,
      "systemViewName");

  public static final String PARAM_IGNORE_SYSTEM_SPANS_NOT_IN_GOLD = ConfigurationParameterFactory.createConfigurationParameterName(
      AnnotationEvaluator.class,
      "ignoreSystemSpansNotInGold");

  private Class<? extends Annotation> annotationClass;

  private SpanExtractor<T> spanExtractor;

  private int batch;

  private Stats collectionStats;

  private Stats batchStats;

  private Logger logger;

  @Override
  @SuppressWarnings("unchecked")
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.logger = context.getLogger();
    this.annotationClass = InitializableFactory.getClass(this.annotationClassName, Annotation.class);
    this.spanExtractor = InitializableFactory.create(
        context,
        this.spanExtractorClassName,
        SpanExtractor.class);
    this.collectionStats = new Stats();
    this.batchStats = new Stats();
    this.batch = 0;
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    JCas goldView, systemView;
    try {
      goldView = jCas.getView(this.goldViewName);
      systemView = jCas.getView(this.systemViewName);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }

    // convert the annotations in the CAS views to span->tag maps
    Map<T, String> goldSpanTags = this.getSpanTags(goldView);
    Map<T, String> systemSpanTags = this.getSpanTags(systemView);
    if (this.ignoreSystemSpansNotInGold) {
      for (T span : new HashSet<T>(systemSpanTags.keySet())) {
        if (!goldSpanTags.containsKey(span)) {
          systemSpanTags.remove(span);
        }
      }
    }

    // find the span+tags that were labeled correctly
    Set<T> matchingSpans = new HashSet<T>();
    matchingSpans.addAll(goldSpanTags.keySet());
    matchingSpans.retainAll(systemSpanTags.keySet());
    Map<T, String> matchingSpanTags = new HashMap<T, String>();
    for (T span : matchingSpans) {
      if (this.annotationAttributeName == null) {
        matchingSpanTags.put(span, null);
      } else {
        String goldTag = goldSpanTags.get(span);
        String systemTag = systemSpanTags.get(span);
        if (goldTag.equals(systemTag)) {
          matchingSpanTags.put(span, goldTag);
        }
      }
    }

    // update counts
    int gold = goldSpanTags.size();
    int system = systemSpanTags.size();
    int matching = matchingSpanTags.size();
    this.collectionStats.gold += gold;
    this.collectionStats.system += system;
    this.collectionStats.matching += matching;
    this.batchStats.gold += gold;
    this.batchStats.system += system;
    this.batchStats.matching += matching;

    // print out the errors
    Set<T> spans = new HashSet<T>();
    spans.addAll(goldSpanTags.keySet());
    spans.addAll(systemSpanTags.keySet());
    if (!spans.equals(matchingSpanTags.keySet())) {
      StringBuilder message = new StringBuilder();
      message.append(ViewURIUtil.getURI(jCas)).append('\n');
      List<T> spansList = new ArrayList<T>(spans);
      Collections.sort(spansList);
      for (T span : spansList) {
        String goldTag = goldSpanTags.get(span);
        String systemTag = systemSpanTags.get(span);
        if (!matchingSpanTags.containsKey(span)) {
          boolean isGold = goldSpanTags.containsKey(span);
          boolean isSystem = systemSpanTags.containsKey(span);
          if (isGold && isSystem) {
            message.append(String.format("WRONG: system=%s gold=%s %s\n", systemTag, goldTag, span));
          } else if (isGold) {
            String attr = goldTag == null ? "" : String.format("gold=%s ", goldTag);
            message.append(String.format("DROPPED: %s%s\n", attr, span));
          } else if (isSystem) {
            if (!this.ignoreSystemSpansNotInGold) {
              String attr = systemTag == null ? "" : String.format("system=%s ", systemTag);
              message.append(String.format("ADDED: %s%s\n", attr, span));
            }
          }
        }
      }
      this.logger.log(Level.INFO, message.toString().trim());
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    this.logStats("Collection", this.collectionStats);
    this.collectionStats = new Stats();
  }

  @Override
  public void batchProcessComplete() throws AnalysisEngineProcessException {
    super.batchProcessComplete();
    this.logStats("Batch " + this.batch, this.batchStats);
    this.batchStats = new Stats();
    this.batch += 1;
  }

  private void logStats(String heading, Stats stats) {
    String annotationName = this.annotationClass.getSimpleName();
    String name;
    if (this.annotationAttributeName != null) {
      name = String.format("%s:%s", annotationName, this.annotationAttributeName);
    } else {
      name = annotationName;
    }
    // @formatter:off
    String message = String.format(
        "%s %s\n" +
        "gold      = %d\n" +
        "system    = %d\n" +
        "matching  = %d\n" + 
        "precision = %.3f\n" +
        "recall    = %.3f\n" +
        "f1        = %.3f",
        name, heading,
        stats.gold, stats.system, stats.matching,
        stats.precision(), stats.recall(), stats.f1());
    // @formatter:on
    this.logger.log(Level.WARNING, message);
  }

  private Map<T, String> getSpanTags(JCas view) {
    Map<T, String> spans = new HashMap<T, String>();
    for (Annotation ann : JCasUtil.iterate(view, this.annotationClass)) {
      T span = this.spanExtractor.getSpan(ann);
      if (this.annotationAttributeName == null) {
        spans.put(span, null);
      } else {
        Feature feature = ann.getType().getFeatureByBaseName(this.annotationAttributeName);
        String featureValue = ann.getFeatureValueAsString(feature);
        if (featureValue != null) {
          spans.put(span, featureValue);
        }
      }
    }
    return spans;
  }

  private static class Stats {
    public int gold;

    public int system;

    public int matching;

    public Stats() {
      this.gold = 0;
      this.system = 0;
      this.matching = 0;

    }

    public double precision() {
      return this.matching / (double) this.system;
    }

    public double recall() {
      return this.matching / (double) this.gold;
    }

    public double f1() {
      double p = this.precision();
      double r = this.recall();
      return 2 * p * r / (p + r);
    }
  }

  public static class Span<T extends Comparable<? super T>> implements Comparable<Span<T>> {

    protected T begin;

    protected T end;

    public Span(T begin, T end) {
      this.begin = begin;
      this.end = end;
    }

    @Override
    public String toString() {
      return String.format("%s(%s, %s)", this.getClass().getSimpleName(), this.begin, this.end);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(new Object[] { this.begin, this.end });
    }

    @Override
    public boolean equals(Object obj) {
      boolean equals = false;
      if (obj instanceof Span) {
        Span<?> that = (Span<?>) obj;
        equals = this.begin.equals(that.begin) && this.end.equals(that.end);
      }
      return equals;
    }

    @Override
    public int compareTo(Span<T> that) {
      int diff = this.begin.compareTo(that.begin);
      if (diff == 0) {
        diff = this.end.compareTo(that.end);
      }
      return diff;
    }

  }

  public static class AnnotationSpan extends Span<Integer> {

    protected String textWindow;

    public AnnotationSpan(Annotation annotation) {
      super(annotation.getBegin(), annotation.getEnd());
      int nChars = 20;
      String docText = annotation.getCAS().getDocumentText();
      int windowBegin = Math.max(this.begin - nChars, 0);
      int windowEnd = Math.min(this.end + nChars, docText.length());
      this.textWindow = String.format(
          "%s%s[%s]%s%s",
          windowBegin == 0 ? "" : "...",
          docText.substring(windowBegin, this.begin),
          annotation.getCoveredText(),
          docText.substring(this.end, windowEnd),
          windowEnd == docText.length() ? "" : "...").replace('\n', ' ');
    }

    @Override
    public String toString() {
      String name = this.getClass().getSimpleName();
      return String.format("%s(%s, %s, %s)", name, this.begin, this.end, this.textWindow);
    }
  }
}