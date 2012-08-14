/** 
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

package org.cleartk.classifier.feature.transform;

import java.io.IOException;
import java.net.URI;

import org.cleartk.classifier.Instance;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * TrainableExtractors defines a extractors that can be trained to fix up data based on a set of
 * instances. Prototypical cases include computing statistics for normalization such as mean,
 * standard deviation, min, max, or for computing corpus tf*idf values
 * 
 * <p>
 * TrainableExtractors that have not yet been trained cannot be used as subextractors inside of any
 * other feature extractor, though they can have subextractors of their own. So for example, while
 * the following will work:
 * <p>
 * 
 * <code>
 * new TfidfExtractor(new ContextExtractor<Token>(Token.class, new CoveredTextExtractor(), new
 * Preceding(2)))
 * </code>
 * <p>
 * the following will not:
 * <p>
 * <code>
 * new ContextExtractor<Token>(Token.class, new TfidfExtractor(new CoveredTextExtractor()), new
 * Preceding(2))
 * </code>
 */
public interface TrainableExtractor<OUTCOME_T> {

  /**
   * In the prototypical case, train takes a collection of instances and computes statistics over
   * the values such as computing mean, standard deviation, TF*IDF, etc...
   * 
   * @param instances
   *          - URI pointing to the output location for saving statistics
   */
  public void train(Iterable<Instance<OUTCOME_T>> instances);

  /**
   * Saves statistics from train in location URI
   */
  public void save(URI uri) throws IOException;

  /**
   * Loads statistics from location URI
   */
  public void load(URI uri) throws IOException;

  /**
   * Transforms all features handled by this extractor. Called on an Instance that was created
   * before {@link #train(Iterable)} was called, to complete the processing of the Instance.
   * 
   * @param instance
   *          An instance that was created before {@link #train(Iterable)} was called.
   * @return A copy of the instance, where processing of the instances is complete.
   */
  public Instance<OUTCOME_T> transform(Instance<OUTCOME_T> instance);

}
