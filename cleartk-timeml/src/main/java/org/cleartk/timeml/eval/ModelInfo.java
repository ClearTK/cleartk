package org.cleartk.timeml.eval;

import java.io.File;
import java.util.Collection;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;

public class ModelInfo<ANNOTATION_TYPE extends TOP> {

  public Class<ANNOTATION_TYPE> annotatedClass;

  public String annotatedFeatureName;

  public Function<ANNOTATION_TYPE, ? extends Object> annotationConverter;

  public CleartkInternalModelFactory modelFactory;

  public String[] trainingArguments;

  public ModelInfo(
      Class<ANNOTATION_TYPE> annotatedClass,
      String annotatedFeatureName,
      Function<ANNOTATION_TYPE, ? extends Object> annotationConverter,
      CleartkInternalModelFactory modelFactory,
      String[] trainingArguments) {
    this.annotatedClass = annotatedClass;
    this.annotatedFeatureName = annotatedFeatureName;
    this.annotationConverter = annotationConverter;
    this.modelFactory = modelFactory;
    this.trainingArguments = trainingArguments;
  }

  public File getModelSubdirectory(File directory) {
    String annotatorName = this.modelFactory.getAnnotatorClass().getName();
    return new File(new File(directory, "models"), annotatorName);
  }

  public void updateStatistics(AnnotationStatistics statistics, JCas goldView, JCas systemView) {
    Collection<ANNOTATION_TYPE> goldAnns = JCasUtil.select(goldView, this.annotatedClass);
    Collection<ANNOTATION_TYPE> systemAnns = JCasUtil.select(systemView, this.annotatedClass);
    statistics.add(goldAnns, systemAnns, this.annotationConverter);
  }
}