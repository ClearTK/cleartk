/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.ml;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides static methods for working with {@link Instance} objects.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class Instances {
  /**
   * Create a list of Instances from a list of outcomes and a list of feature-lists.
   * 
   * There must be exactly one outcome for each feature list.
   * 
   * @param outcomes
   *          The list of classifier outcomes.
   * @param featureLists
   *          The list of classifier feature-lists.
   * @return A list of Instances produced by matchin the outcomes and feature-lists pairwise.
   */
  public static <OUTCOME_TYPE> List<Instance<OUTCOME_TYPE>> toInstances(
      List<OUTCOME_TYPE> outcomes,
      List<List<Feature>> featureLists) {
    int nOutcomes = outcomes.size();
    int nFeatureLists = featureLists.size();
    if (nOutcomes != nFeatureLists) {
      String message = "expected the same number of outcomes (%d) as featureLists (%d)";
      throw new IllegalArgumentException(String.format(message, nOutcomes, nFeatureLists));
    }

    List<Instance<OUTCOME_TYPE>> instances = new ArrayList<Instance<OUTCOME_TYPE>>();
    for (int i = 0; i < nOutcomes; ++i) {
      instances.add(new Instance<OUTCOME_TYPE>(outcomes.get(i), featureLists.get(i)));
    }
    return instances;
  }

}
