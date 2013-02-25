package org.cleartk.timeml.eval;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.SequenceDataWriter;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.viterbi.DefaultOutcomeFeatureExtractor;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.TemporalLink;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class Model<ANNOTATION_TYPE extends TOP> {

  public static class Params {
    public Class<?> dataWriterClass;

    public int nViterbiOutcomes;
    
    public String[] trainingArguments;

    public Params(Class<?> dataWriterClass, String... trainingArguments) {
      this(dataWriterClass, 0, trainingArguments);
    }
    
    public Params(Class<?> dataWriterClass, int nViterbiOutcomes, String ... trainingArguments) {
      this.dataWriterClass = dataWriterClass;
      this.nViterbiOutcomes = nViterbiOutcomes;
      this.trainingArguments = trainingArguments;
    }

    @Override
    public String toString() {
      Objects.ToStringHelper helper = Objects.toStringHelper(this.getClass());
      helper.add("dataWriterClass", this.dataWriterClass.getSimpleName());
      if (this.nViterbiOutcomes > 0) {
        helper.add("nViterbiOutcomes", this.nViterbiOutcomes);
      }
      if (this.trainingArguments.length > 0) {
        helper.add("trainingArguments", Joiner.on(' ').join(this.trainingArguments));
      }
      return helper.toString();
    }

  }

  public enum EvaluationType {
    NORMAL, GOLD_SPANS, SYSTEM_SPANS, INTERSECTED_SPANS
  }
  
  public enum LoggingType {
    NONE, SYSTEM_PREDICTIONS
  }

  public String name;

  public List<Model<?>> prerequisites;

  private Class<? extends AnalysisComponent> annotatorClass;

  public Model.Params bestParams;

  public List<Model.Params> paramsToSearch;

  private Model.EvaluationType evaluationType;
  
  private Model.LoggingType loggingType;

  private Class<ANNOTATION_TYPE> annotationClass;

  Function<ANNOTATION_TYPE, ?> annotationToSpan;

  private Function<ANNOTATION_TYPE, String> annotationToOutcome;

  private String featureToRemove;

  public Model(
      String name,
      List<Model<?>> prerequisites,
      Class<? extends AnalysisComponent> annotatorClass,
      Model.Params bestParams,
      List<Model.Params> paramsToSearch,
      Model.EvaluationType evaluationType,
      Model.LoggingType loggingType,
      Class<ANNOTATION_TYPE> annotationClass,
      Function<ANNOTATION_TYPE, ?> annotationToSpan,
      Function<ANNOTATION_TYPE, String> annotationToOutcome,
      String featureToRemove) {
    this.name = name;
    this.prerequisites = prerequisites;
    this.annotatorClass = annotatorClass;
    this.bestParams = bestParams;
    this.paramsToSearch = paramsToSearch;
    this.evaluationType = evaluationType;
    this.loggingType = loggingType;
    this.annotationClass = annotationClass;
    this.annotationToSpan = annotationToSpan;
    this.annotationToOutcome = annotationToOutcome;
    this.featureToRemove = featureToRemove;
  }
  
  @Override
  public String toString() {
    Objects.ToStringHelper helper = Objects.toStringHelper(this.getClass());
    helper.add("name", this.name);
    return helper.toString();
  }

  public AnalysisEngineDescription getWriterDescription(File directory, Model.Params params)
      throws ResourceInitializationException {
    AnalysisEngineDescription desc;
    if (params.nViterbiOutcomes > 0) {
      desc = AnalysisEngineFactory.createPrimitiveDescription(
          this.annotatorClass,
          CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
          ViterbiDataWriterFactory.class,
          ViterbiDataWriterFactory.PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS,
          DefaultDataWriterFactory.class,
          DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
          params.dataWriterClass,
          ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
          DefaultOutcomeFeatureExtractor.class,
          DefaultOutcomeFeatureExtractor.PARAM_MOST_RECENT_OUTCOME,
          1,
          DefaultOutcomeFeatureExtractor.PARAM_LEAST_RECENT_OUTCOME,
          params.nViterbiOutcomes,
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          this.getModelDirectory(directory, params));
    } else {
      String datatWriterParamName;
      if (SequenceDataWriter.class.isAssignableFrom(params.dataWriterClass)) {
        datatWriterParamName = DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME;
      } else if (DataWriter.class.isAssignableFrom(params.dataWriterClass)) {
        datatWriterParamName = DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME;
      } else {
        throw new RuntimeException("Invalid data writer class: " + params.dataWriterClass);
      }
      desc = AnalysisEngineFactory.createPrimitiveDescription(
          this.annotatorClass,
          datatWriterParamName,
          params.dataWriterClass,
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          this.getModelDirectory(directory, params));
    }
    return desc;
  }

  public void train(File directory, Model.Params params) throws Exception {
    Train.main(this.getModelDirectory(directory, params), params.trainingArguments);
  }

  public AnalysisEngineDescription getAnnotatorDescription(File directory, Model.Params params)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
    this.annotatorClass,
    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
    JarClassifierBuilder.getModelJarFile(this.getModelDirectory(directory, params)));
  }

  public void evaluate(JCas goldView, JCas systemView, AnnotationStatistics<String> stats) {
    Collection<ANNOTATION_TYPE> goldAnnotations = this.select(goldView);
    Collection<ANNOTATION_TYPE> systemAnnotations = this.select(systemView);
    final Set<Object> spans;
    switch (this.evaluationType) {
      case NORMAL:
        spans = null;
        break;
      case GOLD_SPANS:
        spans = Sets.newHashSet(Iterables.transform(this.select(goldView), this.annotationToSpan));
        break;
      case SYSTEM_SPANS:
        spans = Sets.newHashSet(Iterables.transform(this.select(systemView), this.annotationToSpan));
        break;
      case INTERSECTED_SPANS:
        spans = Sets.intersection(
            Sets.newHashSet(Iterables.transform(this.select(goldView), this.annotationToSpan)),
            Sets.newHashSet(Iterables.transform(this.select(systemView), this.annotationToSpan)));
        break;
      default:
        throw new IllegalArgumentException("Unknown evaluation type: " + this.evaluationType);
    }
    if (spans != null) {
      Predicate<ANNOTATION_TYPE> isSelectedSpan = new Predicate<ANNOTATION_TYPE>() {
        @Override
        public boolean apply(ANNOTATION_TYPE annotation) {
          return spans.contains(annotationToSpan.apply(annotation));
        }
      };
      goldAnnotations = Lists.newArrayList(Iterables.filter(goldAnnotations, isSelectedSpan));
      systemAnnotations = Lists.newArrayList(Iterables.filter(systemAnnotations, isSelectedSpan));
    }
    
    switch (this.loggingType) {
      case NONE: break;
      case SYSTEM_PREDICTIONS:
        Map<Object, ANNOTATION_TYPE> goldMap = Maps.newHashMap();
        for (ANNOTATION_TYPE annotation : goldAnnotations) {
          goldMap.put(this.annotationToSpan.apply(annotation), annotation);
        }
        Map<Object, ANNOTATION_TYPE> systemMap = Maps.newHashMap();
        for (ANNOTATION_TYPE annotation : systemAnnotations) {
          systemMap.put(this.annotationToSpan.apply(annotation), annotation);
        }
        for (Object span : Sets.union(goldMap.keySet(), systemMap.keySet())) {
          ANNOTATION_TYPE goldAnnotation = goldMap.get(span);
          ANNOTATION_TYPE systemAnnotation = systemMap.get(span);
          String goldOutcome = goldAnnotation == null ? null : this.annotationToOutcome.apply(goldAnnotation);
          String systemOutcome = systemAnnotation == null ? null : this.annotationToOutcome.apply(systemAnnotation);
          if (goldAnnotation == null) {
            System.err.printf("%s: System added %s\n", this.name, format(systemAnnotation));
          } else if (systemAnnotation == null) {
            System.err.printf("%s: System missed %s\n", this.name, format(goldAnnotation));
          } else if (!goldOutcome.equals(systemOutcome)) {
            String message = "%s: System misclassified %s as %s\n";
            System.err.printf(message, this.name, goldOutcome, format(systemAnnotation));
          } else {
            System.err.printf("%s: System found %s\n", this.name, format(systemAnnotation));
          }
        }
    }
    
    stats.add(goldAnnotations, systemAnnotations, this.annotationToSpan, this.annotationToOutcome);
  }
  
  private String format(ANNOTATION_TYPE annotation) {
    return this.format(annotation, this.annotationToOutcome.apply(annotation));
  }
  
  private String format(TOP top, String outcome) {
    String result;
    String text = top.getCAS().getDocumentText().replaceAll("[\r\n]", " ");
    int nChars = 30;
    if (top instanceof TemporalLink) {
      TemporalLink link = (TemporalLink) top;
      Anchor source = link.getSource();
      Anchor target = link.getTarget();
      result = String.format(
          "%s(%s, %s)",
          link.getRelationType(),
          format(source, null),
          format(target, null));
    } else if (top instanceof Annotation) {
      Annotation annotation = (Annotation) top;
      int begin = annotation.getBegin();
      int end = annotation.getEnd();
      int preBegin = Math.max(begin - nChars, 0);
      int postEnd = Math.min(end + nChars, text.length());
      result = String.format(
          "...%s[%s%s]%s...",
          text.substring(preBegin, begin),
          text.substring(begin, end),
          outcome == null ? "" : "=" + outcome,
          text.substring(end, postEnd));
    } else {
      throw new IllegalArgumentException("unsupported annotation type: " + top);
    }
    return result;
  }

  public Map<ANNOTATION_TYPE, String> removeModelAnnotations(JCas jCas) {
    Map<ANNOTATION_TYPE, String> annotations = Maps.newHashMap();
    for (ANNOTATION_TYPE annotation : this.select(jCas)) {
      if (this.featureToRemove != null) {
        Feature feature = annotation.getType().getFeatureByBaseName(this.featureToRemove);
        annotations.put(annotation, annotation.getFeatureValueAsString(feature));
        annotation.setFeatureValueFromString(feature, null);
      } else {
        annotations.put(annotation, null);
        annotation.removeFromIndexes();
      }
    }
    return annotations;
  }
  
  public void restoreModelAnnotations(JCas jCas, Map<? extends TOP, String> annotations) {
    for (TOP annotation : annotations.keySet()) {
      String value = annotations.get(annotation);
      if (this.featureToRemove != null) {
        if (value != null) {
          Feature feature = annotation.getType().getFeatureByBaseName(this.featureToRemove);
          annotation.setFeatureValueFromString(feature, value);
        }
      } else {
        annotation.addToIndexes();
      }
    }
  }

  private List<ANNOTATION_TYPE> select(JCas jCas) {
    // should restrict ourselves to the Text element, but restricting to exact
    // class should exclude DocumentCreationTimes, the one thing outside the Text element
    List<ANNOTATION_TYPE> annotations = Lists.newArrayList();
    for (ANNOTATION_TYPE annotation : JCasUtil.select(jCas, this.annotationClass)) {
      if (annotation.getClass().equals(this.annotationClass)) {
        annotations.add(annotation);
      }
    }
    return annotations;
  }

  private File getModelDirectory(File directory, Model.Params params) {
    String dataWriterName = params.dataWriterClass.getSimpleName();
    String viterbi = params.nViterbiOutcomes > 0 ? "viterbi" + params.nViterbiOutcomes + "_": "";
    String fileName = viterbi + Joiner.on("_").join(params.trainingArguments);
    return new File(new File(new File(directory, this.name), dataWriterName), fileName);
  }
}