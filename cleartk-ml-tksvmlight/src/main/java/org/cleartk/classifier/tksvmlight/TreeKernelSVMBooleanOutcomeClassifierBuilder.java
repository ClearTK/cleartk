package org.cleartk.classifier.tksvmlight;

import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.jar.ClassifierBuilder_ImplBase;

public abstract class TreeKernelSVMBooleanOutcomeClassifierBuilder<CLASSIFIER_TYPE extends Classifier<Boolean>> 
  extends ClassifierBuilder_ImplBase<CLASSIFIER_TYPE,TreeFeatureVector,Boolean,Boolean> {
}
