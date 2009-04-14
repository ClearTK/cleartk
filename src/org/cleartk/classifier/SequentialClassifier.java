package org.cleartk.classifier;

import java.util.List;

public interface SequentialClassifier<OUTCOME_TYPE> {

	/**
	 * Classifies a sequence of feature lists.
	 * 
	 * @param features   a list of features for each member in the sequence
	 * @return           a list of the classifications made.
	 */
	public List<OUTCOME_TYPE> classifySequence(List<List<Feature>> features);

	/**
	 * Get the N best sequence classifications along with their scores.
	 * 
	 * @param features   a list of features for each member in the sequence
	 * @param maxResults the maximum number of classifications to return.
	 * @return           a sorted list of the best N sequence classifications
	 *                   with their scores.
	 */
	public List<ScoredOutcome<List<OUTCOME_TYPE>>> scoreSequence(List<List<Feature>> features, int maxResults);

}
