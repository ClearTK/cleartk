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

import java.util.List;


/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * This class provides an abstraction for interfacing with various ML learning libraries such as
 * Mallet, Libsvm, OpenNLP Maxent, etc. Each subclass must override the abstract methods
 * (naturally!) each of which is documented below. Each subclass must also be able to be
 * instantiated from a single jar file that contains all the necessary meta-data and data (e.g. the
 * model). The contents of the jar file will vary for each implementation. However, each jar file
 * should have a manifest file with two attributes, "returnType" and "classifier". The jar command
 * can be used to set the manifest of a jar file. A manifest file should look something like this:
 * 
 * <pre>
 *  Manifest-Version: 1.0
 *  classifier: org.cleartk.classifier.OpenNLPMaxentClassifier
 *  returnType: string
 * </pre>
 * 
 * @author Philipp Wetzler
 * @author Philip Ogren
 * @author Steven Bethard
 */

public interface Classifier<OUTCOME_TYPE> {
  /**
   * Classifies a list of features.
   * 
   * @param features
   *          a list of features to be classified
   * @return the classification made
   */
  public OUTCOME_TYPE classify(List<Feature> features) throws CleartkProcessingException;

  /**
   * Get the N best classifications along with their scores.
   * 
   * @param features
   *          a list of features to be classified
   * @param maxResults
   *          the maximum number of classifications to return
   * @return a sorted list of the best N classifications with their scores
   */
  public List<ScoredOutcome<OUTCOME_TYPE>> score(List<Feature> features, int maxResults)
      throws CleartkProcessingException;

}
