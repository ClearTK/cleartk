package org.cleartk.timeml.eval;

import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.util.CleartkInternalModelFactory;

public class EventModelInfo extends ModelInfo<Event> {

  public EventModelInfo(
      String annotatedFeatureName,
      CleartkInternalModelFactory modelFactory,
      String[] trainingArguments) {
    super(
        Event.class,
        annotatedFeatureName,
        AnnotationStatistics.<Event> annotationToSpan(),
        modelFactory,
        trainingArguments);
  }

  public EventModelInfo(String annotatedFeatureName, CleartkInternalModelFactory modelFactory) {
    this(annotatedFeatureName, modelFactory, new String[0]);
  }
}
