package org.cleartk.classifier.feature.transform;

import java.io.Serializable;

public interface RunningStat extends Serializable {

  public void add(double x);

  public void clear();

  int getNumSamples();

}
