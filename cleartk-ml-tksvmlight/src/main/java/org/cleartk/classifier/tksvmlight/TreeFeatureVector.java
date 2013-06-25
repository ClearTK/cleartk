/** 
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

package org.cleartk.classifier.tksvmlight;

import java.util.LinkedHashMap;
import org.cleartk.classifier.util.featurevector.FeatureVector;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @version 0.2.1
 * 
 * A vector which contains both standard name number features and also tree features.
 * 
 * All features that are named with the prefix "TK_" are treated as trees.
 * 
 */
@Beta
public class TreeFeatureVector {
  private LinkedHashMap<String, String> trees = null;
  private FeatureVector features = null;
  
  /**
   * Get the trees from the feature vector (i.e. all features named with the prefix "TK_").
   * @return A LinkedHashMap of all the trees.
   */
  public LinkedHashMap<String, String> getTrees() {
    return trees;
  }

  /**
   * Set the trees.
   * @param trees A LinkedHashMap of all the trees.
   */
  public void setTrees(LinkedHashMap<String, String> trees) {
    this.trees = trees;
  }

  /**
   * Get the standard (non-tree) features.
   * @return A feature vector of all the standard features.
   */
  public FeatureVector getFeatures() {
    return features;
  }

  /**
   * Sets the non-tree features.
   * @param features The features to set the non standard features to.
   */
  public void setFeatures(FeatureVector features) {
    this.features = features;
  }  
}
