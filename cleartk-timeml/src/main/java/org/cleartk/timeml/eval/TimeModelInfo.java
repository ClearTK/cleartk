package org.cleartk.timeml.eval;

import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.timeml.type.Time;
import org.cleartk.timeml.util.CleartkInternalModelFactory;

public class TimeModelInfo extends ModelInfo<Time> {

  public TimeModelInfo(
      String annotatedFeatureName,
      CleartkInternalModelFactory modelFactory,
      String[] trainingArguments) {
    super(
        Time.class,
        annotatedFeatureName,
        AnnotationStatistics.<Time> annotationToSpan(),
        modelFactory,
        trainingArguments);
  }

  public TimeModelInfo(String annotatedFeatureName, CleartkInternalModelFactory modelFactory) {
    this(annotatedFeatureName, modelFactory, new String[0]);
  }

}
