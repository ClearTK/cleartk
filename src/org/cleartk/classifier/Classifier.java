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

import org.cleartk.classifier.mallet.MalletCRFClassifier;
import org.cleartk.classifier.opennlp.MaxentClassifier;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * This class provides an abstraction for interfacing with various ML learning
 * libraries such as Mallet, Libsvm, OpenNLP Maxent, etc. Each subclass must
 * override the abstract methods (naturally!) each of which is documented below.
 * Each subclass must also be able to be instantiated from a single jar file
 * that contains all the necessary meta-data and data (e.g. the model). The
 * contents of the jar file will vary for each implementation. However, each jar
 * file should have a manifest file with two attributes, "returnType" and
 * "classifier". The jar command can be used to set the manifest of a jar file.
 * A manifest file should look something like this:
 * 
 * <pre>
 *  Manifest-Version: 1.0
 *  classifier: org.cleartk.classifier.OpenNLPMaxentClassifier
 *  returnType: string
 * </pre>
 * 
 * @author Philipp Wetzler
 * @author Philip Ogren
 */

public interface Classifier<OUTCOME_TYPE> {
	/**
	 * Classifies a list of features. This method may not be supported for
	 * sequential classifiers.
	 * 
	 * @param features
	 * @return A Value holding the classification result
	 */
	public abstract OUTCOME_TYPE classify(List<Feature> features);

	/**
	 * This method provides a default implementation for classifying a bunch of
	 * instances at once. The values returned are the same as they would if
	 * <code>classify(List&lt;Feature&gt;)</code> was called individually on
	 * each list of features. Sequential classifiers should override this
	 * method.
	 * 
	 * @param features
	 *            a list of lists of features.
	 * @return a list of values corresponding to the classifications made.
	 * @see MalletCRFClassifier#classify(List)
	 */
	public List<OUTCOME_TYPE> classifySequence(List<List<Feature>> features);

	/**
	 * Provides a way to get scores for the best N classifications. The 'score'
	 * in the scored value could be anything but is often a probability. The
	 * only expectation is that a higher score should correspond to higher
	 * confidence in the classification. No other assumptions should be made.
	 * 
	 * @param features
	 *            a list of features to be classified
	 * @param maxResults
	 *            the maximum number of classifications to return.
	 * @return a sorted list of the best N classifications with their scores.
	 */
	public List<ScoredOutcome<OUTCOME_TYPE>> score(List<Feature> features, int maxResults);


	/**
	 * A classifier that is based on a sequence of labels (i.e.
	 * computes/optimizes label transition probabilities) such as the
	 * MalletCRFClassifier should return true.
	 * 
	 * Here is the expectation for implementors. If true is returned, then the
	 * classifier may or may not implement
	 * <code>classify(List&lt;Feature&gt;)</code> (usually not we expect) and
	 * must override <code>classify(List&lt;List&lt;Feature&gt;&gt;)</code>. If
	 * false is returned, then the classifier is required to implement
	 * <code>classify(List&lt;Feature&gt;)</code> and may or many not override
	 * <code>classify(List&lt;List&lt;Feature&gt;&gt;)</code>. If you do not
	 * want to implement <code>classify(List&lt;Feature&gt;)</code> for your
	 * sequential classifier, then simply throw an
	 * UnsupportedOperationException.
	 * 
	 * @return true if this is a sequential classifier, false otherwise.
	 * @see MalletCRFClassifier#isSequential()
	 * @see MaxentClassifier#isSequential()
	 */
	public abstract boolean isSequential();

}









///**
// * Provides a way to get scores for the best N sequence classifications. The
// * 'score' in the scored value could be anything but is often something
// * similar to a probability. The only expectation is that a higher score
// * should correspond to higher confidence in the classification. No other
// * assumptions should be made.
// * 
// * @param features
// *            features for a sequence - i.e. for each member in the sequence
// *            there is a list of features.
// * @param maxResults
// *            the maximum number of classifications to return.
// * @return a sorted list of the best N sequence classifications with their
// *         scores.
// */
//public List<ScoredValue<List<OUTCOME_TYPE>>> scoreSequence(List<List<Feature>> features, int maxResults);
