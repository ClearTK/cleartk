package org.cleartk.classifier;

import java.util.List;

import org.cleartk.classifier.mallet.MalletCRFClassifier;

public interface SequentialClassifier<OUTCOME_TYPE> {

	
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
	 * Provides a way to get scores for the best N sequence classifications. The
	 * 'score' in the scored value could be anything but is often something
	 * similar to a probability. The only expectation is that a higher score
	 * should correspond to higher confidence in the classification. No other
	 * assumptions should be made.
	 * 
	 * @param features
	 *            features for a sequence - i.e. for each member in the sequence
	 *            there is a list of features.
	 * @param maxResults
	 *            the maximum number of classifications to return.
	 * @return a sorted list of the best N sequence classifications with their
	 *         scores.
	 */
	public List<ScoredOutcome<List<OUTCOME_TYPE>>> scoreSequence(List<List<Feature>> features, int maxResults);

}
