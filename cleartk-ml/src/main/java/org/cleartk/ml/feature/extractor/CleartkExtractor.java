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
package org.cleartk.ml.feature.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

/**
 * A feature extractor that finds other {@link Annotation}s in the context of a focus annotation and
 * extracts features from these. It can be used, for example, to:
 * <ul>
 * <li>Get the text of the 2 tokens before a focus annotation</li>
 * <li>Get the parts of speech of the 3 tokens after a focus annotation</li>
 * <li>Get the tokens after a focus annotation, beginning 2 after and ending 5 after, as a bag of
 * words</li>
 * <li>Get an ngram concatenating the stem of the first word before a focus annotation and the first
 * word contained in the focus annotation</li>
 * <li></li>
 * </ul>
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CleartkExtractor<FOCUS_T extends Annotation, SEARCH_T extends Annotation> implements
    FeatureExtractor1<FOCUS_T>, FeatureExtractor2<FOCUS_T, FOCUS_T> {

  private Class<SEARCH_T> annotationClass;

  private FeatureExtractor1<SEARCH_T> extractor;

  private Context[] contexts;

  /**
   * Create an extractor that finds {@link Annotation}s of the given type at the specified
   * {@link Context}s and applies the given feature extractor to the annotations.
   * 
   * @param annotationClass
   *          The type of annotation which should be searched for in the context.
   * @param extractor
   *          The feature extractor to apply to each annotation found.
   * @param contexts
   *          The contexts where the extractor should look for annotations.
   */
  public CleartkExtractor(
      Class<SEARCH_T> annotationClass,
      FeatureExtractor1<SEARCH_T> extractor,
      Context... contexts) {
    this.annotationClass = annotationClass;
    this.extractor = extractor;
    this.contexts = contexts;
  }

  @Override
  public List<Feature> extract(JCas view, FOCUS_T focusAnnotation) throws CleartkExtractorException {
    return this.extract(view, focusAnnotation, new NoBounds());
  }

  /**
   * Extract features from the annotations around the focus annotation and within the given bounds.
   * 
   * @param view
   *          The JCas containing the focus annotation.
   * @param focusAnnotation
   *          The annotation whose context is to be searched.
   * @param boundsAnnotation
   *          The boundary within which context annotations may be identified.
   * @return The features extracted in the context of the focus annotation.
   */
  public List<Feature> extractWithin(JCas view, FOCUS_T focusAnnotation, Annotation boundsAnnotation)
      throws CleartkExtractorException {
    Bounds bounds = new SpanBounds(boundsAnnotation.getBegin(), boundsAnnotation.getEnd());
    return this.extract(view, focusAnnotation, bounds);
  }

  public List<Feature> extractBetween(JCas view, FOCUS_T annotation1, FOCUS_T annotation2)
      throws CleartkExtractorException {
    int begin = annotation1.getEnd();
    int end = annotation2.getBegin();
    // FIXME: creating a new annotation may leak memory - is there a better approach?
    Annotation focusAnnotation = new Annotation(view, begin, end);
    return this.extract(view, focusAnnotation, new NoBounds());
  }

  @Override
  public java.util.List<Feature> extract(JCas view, FOCUS_T annotation1, FOCUS_T annotation2)
      throws CleartkExtractorException {
    return this.extractBetween(view, annotation1, annotation2);
  }

  private List<Feature> extract(JCas view, Annotation focusAnnotation, Bounds bounds)
      throws CleartkExtractorException {
    List<Feature> features = new ArrayList<Feature>();
    for (Context context : this.contexts) {
      features.addAll(context.extract(
          view,
          focusAnnotation,
          bounds,
          this.annotationClass,
          this.extractor));
    }
    return features;
  }

  /**
   * A class representing the bounds within which a {@link CleartkExtractor} should look for
   * annotations.
   */
  public static interface Bounds {

    /**
     * Determines whether or not an annotation lies within the given bounds.
     * 
     * @param annotation
     *          The annotation to be checked.
     * @return True if the annotation lies within the bounds.
     */
    public boolean contains(Annotation annotation);
  }

  /**
   * A Bounds implementation that puts no restrictions on the context.
   */
  private static class NoBounds implements Bounds {

    public NoBounds() {
    }

    @Override
    public boolean contains(Annotation annotation) {
      return true;
    }

  }

  /**
   * A Bounds implementation that restricts the context to annotations within a given span.
   */
  private static class SpanBounds implements Bounds {

    private int begin;

    private int end;

    public SpanBounds(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    @Override
    public boolean contains(Annotation annotation) {
      return annotation.getBegin() >= this.begin && annotation.getEnd() <= this.end;
    }

  }

  /**
   * A class representing a location that a {@link CleartkExtractor} should look for annotations.
   */
  public static interface Context {

    /**
     * Gets the base feature name that will be used in {@link Feature}s generated by this context.
     * The actual feature names may include additional information (e.g. relative position), but
     * this base name should be used in all aggregating contexts like {@link Bag} or {@link Ngram}.
     * 
     * @return The base feature name.
     */
    public String getName();

    /**
     * Extracts features in the given context.
     * 
     * @param jCas
     *          The {@link JCas} containing the focus annotation.
     * @param focusAnnotation
     *          The annotation whose context is to be searched.
     * @param annotationClass
     *          The type of annotation to be found in the context.
     * @param extractor
     *          The feature extractor that should be applied to each annotation found in the
     *          context.
     * @return The list of features extracted.
     */
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException;
  }

  /**
   * A subclass of {@link Feature} that the base feature extractors wrap their features in. This
   * enables aggregating contexts like {@link Bag} or {@link Ngram} to name their features properly.
   */
  private static class ContextFeature extends Feature {
    private static final long serialVersionUID = 1L;

    public Feature feature;

    public ContextFeature(String baseName, Feature feature) {
      this.feature = feature;
      this.setName(Feature.createName(baseName, feature.getName()));
      this.setValue(this.feature.getValue());

    }

    public ContextFeature(String baseName, int position, Feature feature) {
      this.feature = feature;
      this.setName(Feature.createName(baseName, String.valueOf(position), feature.getName()));
      this.setValue(feature.getValue());
    }

    public ContextFeature(String baseName, int position, int oobPosition, String featureName) {
      this.feature = new Feature(featureName, String.format(Locale.ROOT, "OOB%d", oobPosition));
      this.setName(Feature.createName(baseName, String.valueOf(position), featureName));
      this.setValue(this.feature.getValue());
    }

  }

  public static class NestedCountFeature extends ContextFeature {
    private static final long serialVersionUID = 1L;

    public final Object countedValue;

    public NestedCountFeature(String baseName, Feature feature, Object countedValue) {
      super(baseName, feature);
      this.countedValue = countedValue;
    }

  }

  /**
   * Base class for simple contexts that have a single begin and end.
   */
  private static abstract class Context_ImplBase implements Context {
    protected int begin;

    protected int end;

    private String name;

    public Context_ImplBase(int begin, int end) {
      if (begin > end) {
        String message = "expected begin < end, found begin=%d end=%d";
        throw new IllegalArgumentException(String.format(message, begin, end));
      }
      this.begin = begin;
      this.end = end;
      this.name = Feature.createName(
          this.getClass().getSimpleName(),
          String.valueOf(this.begin),
          String.valueOf(this.end));
    }

    @Override
    public String getName() {
      return this.name;
    }

    /**
     * Select annotations of the given type in the context of the focus annotation. The returned
     * annotations should be in order (smaller begin/end offsets before larger begin/end offsets).
     * 
     * @param jCas
     *          The {@link JCas} containing the focus annotation.
     * @param focusAnnotation
     *          The annotation whose context is to be searched.
     * @param annotationClass
     *          The type of annotation to be found in the context.
     * @param count
     *          The number of annotations to select. A smaller number may be returned if it is not
     *          possible to select the requested number.
     * @return The annotations in the context of the focus annotation.
     */
    protected abstract <T extends Annotation> List<T> select(
        JCas jCas,
        Annotation focusAnnotation,
        Class<T> annotationClass,
        int count);
  }

  /**
   * Base class for simple contexts that scan their annotations from right to left.
   */
  private static abstract class RightToLeftContext extends Context_ImplBase {

    public RightToLeftContext(int begin, int end) {
      super(begin, end);
    }

    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {
      String featureName = extractor instanceof NamedFeatureExtractor1
          ? ((NamedFeatureExtractor1<SEARCH_T>) extractor).getFeatureName()
          : null;

      // slice the appropriate annotations from the CAS
      List<SEARCH_T> anns = this.select(jCas, focusAnnotation, annotationClass, this.end);
      int missing = this.end - anns.size();
      anns = anns.subList(0, Math.max(0, anns.size() - this.begin));

      // figure out how many items are out of bounds
      int oobPos = missing;
      for (SEARCH_T ann : anns) {
        if (!bounds.contains(ann)) {
          oobPos += 1;
        }
      }

      // extract features at each position
      List<Feature> features = new ArrayList<Feature>();
      for (int pos = this.end - 1; pos >= this.begin; pos -= 1) {

        // if the annotation at the current position is in bounds, extract features from it
        int adjustedPos = this.end - 1 - pos - missing;
        SEARCH_T ann = adjustedPos >= 0 ? anns.get(adjustedPos) : null;
        if (ann != null && bounds.contains(ann)) {
          for (Feature feature : extractor.extract(jCas, ann)) {
            features.add(new ContextFeature(this.getName(), pos, feature, featureName));
          }
        }

        // if the annotation at the current position is out of bounds, add an out-of-bounds feature
        else {
          features.add(new ContextFeature(this.getName(), pos, oobPos, featureName));
          oobPos -= 1;
        }
      }
      return features;
    }
  }

  /**
   * Base class for simple contexts that scan their annotations from left to right.
   */
  private static abstract class LeftToRightContext extends Context_ImplBase {

    public LeftToRightContext(int begin, int end) {
      super(begin, end);
    }

    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {
      String featureName = extractor instanceof NamedFeatureExtractor1
          ? ((NamedFeatureExtractor1<SEARCH_T>) extractor).getFeatureName()
          : null;
      List<SEARCH_T> anns = this.select(jCas, focusAnnotation, annotationClass, this.end);
      int oobStart;
      if (this.begin <= anns.size()) {
        oobStart = 1;
        anns = anns.subList(this.begin, anns.size());
      } else {
        oobStart = this.begin - anns.size() + 1;
        anns = new ArrayList<SEARCH_T>();
      }
      List<Feature> features = new ArrayList<Feature>();
      Iterator<SEARCH_T> iter = anns.iterator();
      for (int pos = this.begin, oobPos = oobStart; pos < this.end; pos += 1) {
        SEARCH_T ann = iter.hasNext() ? iter.next() : null;
        if (ann != null && bounds.contains(ann)) {
          for (Feature feature : extractor.extract(jCas, ann)) {
            features.add(new ContextFeature(this.getName(), pos, feature));
          }
        } else {
          features.add(new ContextFeature(this.getName(), pos, oobPos, featureName));
          oobPos += 1;
        }
      }
      return features;
    }
  }

  /**
   * A {@link Context} for extracting the focus annotation. This is mainly useful when the focus
   * annotation should be combined with other annotations using, e.g. a {@link Bag} or {@link Ngram}
   * to aggregate over several contexts.
   */
  public static class Focus implements Context {

    private String name;

    /**
     * Constructs a context that will extract features over the focus annotation.
     */
    public Focus() {
      this.name = this.getClass().getSimpleName();
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {

      List<Feature> features = new ArrayList<Feature>();
      for (Feature feature : extractor.extract(jCas, annotationClass.cast(focusAnnotation))) {
        features.add(new ContextFeature(this.getName(), feature));
      }
      return features;
    }

  }

  /**
   * A {@link Context} for extracting annotations appearing before the focus annotation.
   */
  public static class Preceding extends RightToLeftContext {

    /**
     * Constructs a context that will extract features over the preceding N annotations.
     * 
     * @param end
     *          The number of annotations to extract.
     */
    public Preceding(int end) {
      super(0, end);
    }

    /**
     * Constructs a context that will extract features over a slice of the preceding N annotations.
     * 
     * The {@code begin} and {@code end} indexes count from 0, where index 0 identifies the
     * annotation immediately preceding the focus annotation. If either index is greater than the
     * index of the earliest possible annotation, special "out of bounds" features will be added for
     * each annotation that was requested but absent.
     * 
     * @param begin
     *          The index of the first annotation to include.
     * @param end
     *          The index of the last annotation to include. Must be greater than {@code begin}.
     */
    public Preceding(int begin, int end) {
      super(begin, end);
    }

    @Override
    protected <T extends Annotation> List<T> select(
        JCas jCas,
        Annotation focusAnnotation,
        Class<T> annotationClass,
        int count) {
      return JCasUtil.selectPreceding(jCas, annotationClass, focusAnnotation, count);
    }
  }

  /**
   * A {@link Context} for extracting annotations appearing after the focus annotation.
   */
  public static class Following extends LeftToRightContext {

    /**
     * Constructs a context that will extract features over the following N annotations.
     * 
     * @param end
     *          The number of annotations to extract.
     */
    public Following(int end) {
      super(0, end);
    }

    /**
     * Constructs a context that will extract features over a slice of the following N annotations.
     * 
     * The {@code begin} and {@code end} indexes count from 0, where index 0 identifies the
     * annotation immediately following the focus annotation. If either index is greater than the
     * index of the last possible annotation, special "out of bounds" features will be added for
     * each annotation that was requested but absent.
     * 
     * @param begin
     *          The index of the first annotation to include.
     * @param end
     *          The index of the last annotation to include. Must be greater than {@code begin}.
     */
    public Following(int begin, int end) {
      super(begin, end);
    }

    @Override
    protected <T extends Annotation> List<T> select(
        JCas jCas,
        Annotation focusAnnotation,
        Class<T> annotationClass,
        int count) {
      return JCasUtil.selectFollowing(jCas, annotationClass, focusAnnotation, count);
    }
  }

  /**
   * A {@link Context} for extracting all annotations within the focus annotation.
   */
  public static class Covered implements Context {

    /**
     * Constructs a context that will extract features over all annotations within the focus
     * annotation.
     */
    public Covered() {
    }

    @Override
    public String getName() {
      return "Covered";
    }

    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {
      List<Feature> features = new ArrayList<Feature>();
      int pos = 0;
      for (SEARCH_T ann : JCasUtil.selectCovered(jCas, annotationClass, focusAnnotation)) {
        for (Feature feature : extractor.extract(jCas, ann)) {
          features.add(new ContextFeature(this.getName(), pos, feature));
        }
        pos += 1;
      }
      return features;
    }

  }

  /**
   * A {@link Context} for extracting the first annotations within the focus annotation.
   */
  public static class FirstCovered extends LeftToRightContext {

    /**
     * Constructs a context that will extract features over the first N annotations within the focus
     * annotation.
     * 
     * @param end
     *          The number of annotations to extract.
     */
    public FirstCovered(int end) {
      super(0, end);
    }

    /**
     * Constructs a context that will extract features over a slice of the first N annotations
     * within the focus annotation.
     * 
     * The {@code begin} and {@code end} indexes count from 0, where index 0 identifies the first
     * annotation within the focus annotation. If either index is greater than the index of the last
     * annotation within the focus annotation, special "out of bounds" features will be added for
     * each annotation that was requested but absent.
     * 
     * @param begin
     *          The index of the first annotation to include.
     * @param end
     *          The index of the last annotation to include. Must be greater than {@code begin}.
     */
    public FirstCovered(int begin, int end) {
      super(begin, end);
    }

    @Override
    protected <T extends Annotation> List<T> select(
        JCas jCas,
        Annotation focusAnnotation,
        Class<T> annotationClass,
        int count) {
      List<T> anns = JCasUtil.selectCovered(jCas, annotationClass, focusAnnotation);
      return anns.subList(0, Math.min(count, anns.size()));
    }
  }

  /**
   * A {@link Context} for extracting the last annotations within the focus annotation.
   */
  public static class LastCovered extends RightToLeftContext {

    /**
     * Constructs a context that will extract features over the last N annotations within the focus
     * annotation.
     * 
     * @param end
     *          The number of annotations to extract.
     */
    public LastCovered(int end) {
      super(0, end);
    }

    /**
     * Constructs a context that will extract features over a slice of the last N annotations within
     * the focus annotation.
     * 
     * The {@code begin} and {@code end} indexes count from 0, where index 0 identifies the last
     * annotation within the focus annotation. If either index is greater than the index of the
     * first annotation within the focus annotation, special "out of bounds" features will be added
     * for each annotation that was requested but absent.
     * 
     * @param begin
     *          The index of the first annotation to include.
     * @param end
     *          The index of the last annotation to include. Must be greater than {@code begin}.
     */
    public LastCovered(int begin, int end) {
      super(begin, end);
    }

    @Override
    protected <T extends Annotation> List<T> select(
        JCas jCas,
        Annotation focusAnnotation,
        Class<T> annotationClass,
        int count) {
      List<T> anns = JCasUtil.selectCovered(jCas, annotationClass, focusAnnotation);
      return anns.subList(Math.max(anns.size() - count, 0), anns.size());
    }
  }

  /**
   * A {@link Context} that aggregates the features of other contexts into a "bag" where position
   * information of each individual feature is no longer maintained. Position information is not
   * entirely lost - the span of the bag is encoded as part of the feature name that is shared by
   * all of the features within the bag.
   */
  public static class Bag implements Context {

    private Context[] contexts;

    private String name;

    /**
     * Constructs a {@link Context} which converts the features extracted by the argument contexts
     * into a bag of features where all features have the same name.
     * 
     * @param contexts
     *          The contexts which should be combined into a bag.
     */
    public Bag(Context... contexts) {
      this.contexts = contexts;
      String[] names = new String[contexts.length + 1];
      names[0] = "Bag";
      for (int i = 1; i < names.length; ++i) {
        names[i] = contexts[i - 1].getName();
      }
      this.name = Feature.createName(names);
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {
      List<Feature> features = new ArrayList<Feature>();
      for (Context context : this.contexts) {
        for (Feature feature : context.extract(
            jCas,
            focusAnnotation,
            bounds,
            annotationClass,
            extractor)) {
          ContextFeature contextFeature = (ContextFeature) feature;
          Feature f2 = new Feature(contextFeature.feature.getName(), feature.getValue());
          features.add(new ContextFeature(this.getName(), f2));
        }
      }
      return features;
    }
  }

  /**
   * A {@link Context} that aggregates the features of other contexts into a bag of counts where
   * only the count of occurrence of each feature value is maintained. The span (offsets) of the bag
   * of counts is encoded as part of the feature name.
   */
  public static class Count implements Context {

    private Context[] contexts;

    private String name;

    /**
     * Constructs a {@link Context} which converts the features extracted by the argument contexts
     * into a bag of count features.
     * 
     * @param contexts
     *          The contexts which should be combined into a bag.
     */
    public Count(Context... contexts) {
      this.contexts = contexts;
      String[] names = new String[contexts.length + 1];
      names[0] = "Count";
      for (int i = 1; i < names.length; ++i) {
        names[i] = contexts[i - 1].getName();
      }
      this.name = Feature.createName(names);
    }

    @Override
    public String getName() {
      return this.name;
    }

    /**
     * This method got a bit gnarly in order to support nested Count contexts. It isn't clear why
     * someone would want to do this but we figured that it should just work even if it may be
     * contrived to set up a CleartkExtractor this way. The problems comes up if there are multiple
     * nested count contexts and keeping track of what exactly is being counted. The class
     * NestedContextFeature does this for us.
     */
    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {
      Multiset<String> featureCounts = LinkedHashMultiset.create();
      Map<String, Feature> featureMap = new HashMap<String, Feature>();
      for (Context context : this.contexts) {
        for (Feature feature : context.extract(
            jCas,
            focusAnnotation,
            bounds,
            annotationClass,
            extractor)) {

          String countedFeatureValue = null;
          if (feature instanceof NestedCountFeature) {
            countedFeatureValue = "" + ((NestedCountFeature) feature).countedValue;
          }

          String extractorName = extractor instanceof NamedFeatureExtractor1
              ? ((NamedFeatureExtractor1<SEARCH_T>) extractor).getFeatureName()
              : null;

          String featureName = Feature.createName(
              this.name,
              extractorName,
              countedFeatureValue,
              String.valueOf(feature.getValue()));
          featureCounts.add(featureName);
          featureMap.put(featureName, feature);
        }
      }
      List<Feature> features = new ArrayList<Feature>();
      for (String featureName : featureCounts.elementSet()) {
        Feature feature = featureMap.get(featureName);
        String countedFeatureValue = "" + feature.getValue();
        if (feature instanceof NestedCountFeature) {
          countedFeatureValue = ((NestedCountFeature) feature).countedValue + "_"
              + countedFeatureValue;
        }
        features.add(new NestedCountFeature(featureName, new Feature(
            featureCounts.count(featureName)), countedFeatureValue));
      }
      return features;
    }
  }

  /**
   * A {@link Context} that aggregates the features of other contexts into a single "ngram" feature,
   * where the feature values are concatenated together in order to form a single value.
   */
  public static class Ngram implements Context {
    private Context[] contexts;

    private String name;

    /**
     * Constructs a {@link Context} which converts the features extracted by the argument contexts
     * into a single ngram feature where all feature values have been concatenated together. That
     * is, it takes everything provided by the contexts and makes a single feature value from it.
     * For example, the code "new Ngram(new Preceding(2), new Following(2)))" if run on token
     * annotations would return the feature "A_B_D_E" for the token "C" in the text "A B C D E".
     * That is, it creates a single ngram from the preceding context and following context. Please
     * see org.cleartk.ml.feature.extractor.CleartkExtractorTest.testNgram() to run this example.
     * 
     * 
     * @param contexts
     *          The contexts which should be combined into an ngram.
     */
    public Ngram(Context... contexts) {
      this.contexts = contexts;
      String[] names = new String[contexts.length + 1];
      names[0] = "Ngram";
      for (int i = 1; i < names.length; ++i) {
        names[i] = contexts[i - 1].getName();
      }
      this.name = Feature.createName(names);
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {
      String featureName = extractor instanceof NamedFeatureExtractor1
          ? ((NamedFeatureExtractor1<SEARCH_T>) extractor).getFeatureName()
          : null;
      List<String> values = new ArrayList<String>();
      for (Context context : this.contexts) {
        for (Feature feature : context.extract(
            jCas,
            focusAnnotation,
            bounds,
            annotationClass,
            extractor)) {
          values.add(String.valueOf(feature.getValue()));
        }
      }
      Feature feature = new Feature(featureName, Joiner.on('_').join(values));
      List<Feature> features = new ArrayList<Feature>();
      features.add(new ContextFeature(this.getName(), feature));
      return features;
    }
  }

  /**
   * A {@link Context} that aggregates the features of other contexts into several "ngrams"
   * features, where sub-sequences of the feature values are concatenated together in order to form
   * single values.
   */
  public static class Ngrams implements Context {
    private int n;

    private Context[] contexts;

    private String name;

    /**
     * Constructs a {@link Context} which converts the features extracted by the argument contexts
     * into ngram features where sub-sequences feature values have been concatenated together.
     * 
     * For example, Ngrams(2, context) will extract all bigrams of features generated in the given
     * context.
     * 
     * @param n
     *          The length of the n-gram features
     * @param contexts
     *          The contexts which should be combined into an ngram.
     */
    public Ngrams(int n, Context... contexts) {
      this.n = n;
      this.contexts = contexts;
      String[] names = new String[contexts.length + 1];
      names[0] = this.n + "grams";
      for (int i = 1; i < names.length; ++i) {
        names[i] = contexts[i - 1].getName();
      }
      this.name = Feature.createName(names);
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public <SEARCH_T extends Annotation> List<Feature> extract(
        JCas jCas,
        Annotation focusAnnotation,
        Bounds bounds,
        Class<SEARCH_T> annotationClass,
        FeatureExtractor1<SEARCH_T> extractor) throws CleartkExtractorException {
      String featureName = extractor instanceof NamedFeatureExtractor1
          ? ((NamedFeatureExtractor1<SEARCH_T>) extractor).getFeatureName()
          : null;
      List<Feature> extractedFeatures = new ArrayList<Feature>();
      for (Context context : this.contexts) {
        extractedFeatures.addAll(context.extract(
            jCas,
            focusAnnotation,
            bounds,
            annotationClass,
            extractor));
      }
      List<Feature> features = new ArrayList<Feature>();
      for (int i = 0; i < extractedFeatures.size() - this.n + 1; ++i) {
        List<String> values = new ArrayList<String>();
        for (Feature feature : extractedFeatures.subList(i, i + this.n)) {
          values.add(feature.getValue().toString());
        }
        Feature feature = new Feature(featureName, Joiner.on('_').join(values));
        features.add(new ContextFeature(this.getName(), feature));
      }
      return features;
    }
  }
}
