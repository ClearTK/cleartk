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
package org.cleartk.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */
public class Instance<OUTCOME_TYPE> {

  /**
   * Create a new ClassifierInstance with an empty set of features and a null label.
   */
  public Instance() {
    this.features = new ArrayList<Feature>();
  }

  public Instance(OUTCOME_TYPE outcome) {
    this();
    this.outcome = outcome;
  }

  public Instance(OUTCOME_TYPE outcome, Collection<Feature> features) {
    this(outcome);
    addAll(features);
  }

  public Instance(Collection<Feature> features) {
    this();
    addAll(features);
  }

  /**
   * Get the list of features for this instance.
   * 
   * @return The list of features
   */
  public List<Feature> getFeatures() {
    return this.features;
  }

  /**
   * Add a feature to the instance.
   * 
   * @param feature
   *          The feature to add.
   */
  public void add(Feature feature) {
    this.features.add(feature);
  }

  /**
   * Add a collection of features to the instance.
   * 
   * @param feats
   *          The features to add.
   */
  public void addAll(Collection<Feature> feats) {
    this.features.addAll(feats);
  }

  /**
   * Get the current label for the instance.
   * 
   * @return The current label value.
   */
  public OUTCOME_TYPE getOutcome() {
    return this.outcome;
  }

  /**
   * Set the current label for the instance.
   * 
   * @param outcome
   *          The new label value.
   */
  public void setOutcome(OUTCOME_TYPE outcome) {
    this.outcome = outcome;
  }

  private List<Feature> features;

  private OUTCOME_TYPE outcome = null;

}
