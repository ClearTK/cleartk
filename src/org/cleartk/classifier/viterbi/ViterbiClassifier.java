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
package org.cleartk.classifier.viterbi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.Initializable;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ClassifierFactory;
import org.cleartk.classifier.Classifier_ImplBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.SequentialClassifier;
import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public abstract class ViterbiClassifier<OUTCOME_TYPE> implements SequentialClassifier<OUTCOME_TYPE>,
		Initializable {

	/**
	 * "org.cleartk.classifier.ViterbiSequentialClassifier.PARAM_STACK_SIZE" is an optional, single,
	 * integer parameter that specifies the maximum number of candidate paths to
	 * keep track of. In general, this number should be higher than the number
	 * of possible classifications at any given point in the sequence. This
	 * guarantees that highest-possible scoring sequence will be returned. If,
	 * however, the number of possible classifications is quite high and/or you
	 * are concerned about throughput performance, then you may want to reduce the number
	 * of candidate paths to maintain.
	 */
	public static String PARAM_STACK_SIZE = "org.cleartk.classifier.ViterbiSequentialClassifier.PARAM_STACK_SIZE";

	/**
	 * "org.cleartk.classifier.ViterbiSequentialClassifier.PARAM_ADD_SCORES" is an optional, single,
	 * boolean parameter that specifies whether the scores of candidate sequence
	 * classifications should be calculated by summing classfication scores for
	 * each member of the sequence or by multiplying them. A value of true means
	 * that the scores will be summed. A value of false means that the scores
	 * will be multiplied. 
	 */
	public static String PARAM_ADD_SCORES = "org.cleartk.classifier.ViterbiSequentialClassifier.PARAM_ADD_SCORES";

	
	protected Classifier<OUTCOME_TYPE> classifier;

	protected OutcomeFeatureExtractor[] outcomeFeatureExtractors;

	int viterbiStackSize = 1;

	boolean viterbiAddScores = false;

	public ViterbiClassifier(JarFile modelFile) throws IOException {

		classifier = ReflectionUtil.uncheckedCast(ClassifierFactory.createClassifierFromJar(modelFile.getName()));

		ZipEntry zipEntry = modelFile.getEntry(ViterbiDataWriter.OUTCOME_FEATURE_EXTRACTOR_FILE_NAME);
		if (zipEntry == null) {
			outcomeFeatureExtractors = new OutcomeFeatureExtractor[0];
		}
		else {
			ObjectInputStream is = new ObjectInputStream(modelFile.getInputStream(zipEntry));

			try {
				outcomeFeatureExtractors = (OutcomeFeatureExtractor[]) is.readObject();
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
		viterbiStackSize = (Integer) UIMAUtil.getDefaultingConfigParameterValue(context, PARAM_STACK_SIZE, 1);
		if (viterbiStackSize < 1) {
			throw new ResourceInitializationException(new IllegalArgumentException(String.format(
					"the parameter '%1$s' must be greater than 0.", PARAM_STACK_SIZE)));
		}
		viterbiAddScores = (Boolean) UIMAUtil.getDefaultingConfigParameterValue(context, PARAM_ADD_SCORES,
				false);
	}

	public List<OUTCOME_TYPE> classifySequence(List<List<Feature>> features) {
		if (viterbiStackSize == 1) {
			List<Object> outcomes = new ArrayList<Object>();
			List<OUTCOME_TYPE> returnValues = new ArrayList<OUTCOME_TYPE>();
			for (List<Feature> instanceFeatures : features) {
				for (OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
					instanceFeatures.addAll(outcomeFeatureExtractor.extractFeatures(outcomes));
				}
				OUTCOME_TYPE outcome = classifier.classify(instanceFeatures);
				outcomes.add(outcome);
				returnValues.add(outcome);
			}
			return returnValues;
		}
		else {
			return classifySequence(features, viterbiStackSize, outcomeFeatureExtractors,
					viterbiAddScores);
		}

	}

	/**
	 * This implementation of Viterbi requires at most stackSize * sequenceLength
	 * calls to the classifier. If this proves to be to expensive, then consider
	 * using a smaller stack size.
	 * 
	 * @param <OUTCOME_TYPE>
	 *            the type of outcome that should be returned
	 * @param features
	 *            a sequence-worth of features. Each List<Feature> in features
	 *            should corresond to all of the features for a given element in
	 *            a sequence to be classified.
	 * @param stackSize
	 *            the number of candidate paths through the space of possible
	 *            sequence paths. See note for PARAM_STACK_SIZE above.
	 * @param cls
	 *            the class of the outcome type that should be returned
	 * @param classifier
	 *            a classifier that implements the score(List<Feature>, int)
	 *            method.
	 * @param outcomeFeatureExtractors
	 *            an array of feature extractors that create features from
	 *            previous classifications.
	 * @param addScores
	 *            if true, then candidate sequence classification scores will be
	 *            calculated by summing the scores for each classification of
	 *            each member of the sequence. If false, then the scores will be
	 *            calculated by multiplying the scores together.
	 * @return a list of outcomes (classifications) - one classification for
	 *         each member of the sequence.
	 * @see #PARAM_STACK_SIZE
	 * @see OutcomeFeatureExtractor
	 * @see Classifier_ImplBase#classifySequence(List)
	 * @see MaxentClassifier#classifySequence(List)
	 */
	private List<OUTCOME_TYPE> classifySequence(List<List<Feature>> features, int stackSize, 
			OutcomeFeatureExtractor[] outcomeFeatureExtractors, boolean addScores) {

		List<ScoredOutcome<List<OUTCOME_TYPE>>> nbestSequences = new ArrayList<ScoredOutcome<List<OUTCOME_TYPE>>>();

		if (features == null || features.size() == 0) {
			return Collections.emptyList();
		}

		List<ScoredOutcome<OUTCOME_TYPE>> scoredOutcomes = classifier.score(features.get(0), stackSize);
		for (ScoredOutcome<OUTCOME_TYPE> scoredOutcome : scoredOutcomes) {
			double score = scoredOutcome.getScore();
			List<OUTCOME_TYPE> sequence = new ArrayList<OUTCOME_TYPE>();
			sequence.add(scoredOutcome.getValue());
			nbestSequences.add(new ScoredOutcome<List<OUTCOME_TYPE>>(sequence, score));
		}

		Map<OUTCOME_TYPE, Double> l = new HashMap<OUTCOME_TYPE, Double>();
		Map<OUTCOME_TYPE, List<OUTCOME_TYPE>> m = new HashMap<OUTCOME_TYPE, List<OUTCOME_TYPE>>();

		for (int i = 1; i < features.size(); i++) {

			List<Feature> instanceFeatures = features.get(i);
			l.clear();
			m.clear();
			for (ScoredOutcome<List<OUTCOME_TYPE>> scoredSequence : nbestSequences) {
				// add features from previous outcomes from each scoredSequence
				// in returnValues
				int outcomeFeaturesCount = 0;
				List<Object> previousOutcomes = new ArrayList<Object>(scoredSequence.getValue());
				for (OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
					List<Feature> outcomeFeatures = outcomeFeatureExtractor.extractFeatures(previousOutcomes);
					instanceFeatures.addAll(outcomeFeatures);
					outcomeFeaturesCount += outcomeFeatures.size();
				}
				// score the instance features using the features added by the
				// outcomeFeatureExtractors
				scoredOutcomes = classifier.score(instanceFeatures, stackSize);
				// remove the added features from previous outcomes for this
				// scoredSequence
				instanceFeatures = instanceFeatures.subList(0, instanceFeatures.size() - outcomeFeaturesCount);

				for (ScoredOutcome<OUTCOME_TYPE> scoredOutcome : scoredOutcomes) {

					if (!l.containsKey(scoredOutcome.getValue())) {
						double score = scoredSequence.getScore();
						if (addScores) {
							score = score + scoredOutcome.getScore();
						}
						else {
							score = score * scoredOutcome.getScore();
						}
						l.put(scoredOutcome.getValue(), score);
						m.put(scoredOutcome.getValue(), new ArrayList<OUTCOME_TYPE>(scoredSequence.getValue()));
					}
					else {
						double newScore = scoredSequence.getScore();
						if (addScores) {
							newScore = newScore + scoredOutcome.getScore();
						}
						else {
							newScore = newScore * scoredOutcome.getScore();
						}
						double bestScore = l.get(scoredOutcome.getValue());

						if (newScore > bestScore) {
							l.put(scoredOutcome.getValue(), newScore);
							m.put(scoredOutcome.getValue(), new ArrayList<OUTCOME_TYPE>(scoredSequence.getValue()));
						}
					}
				}
			}

			nbestSequences.clear();
			for (OUTCOME_TYPE outcome : l.keySet()) {
				List<OUTCOME_TYPE> outcomeSequence = m.get(outcome);
				outcomeSequence.add(outcome);
				double score = l.get(outcome);
				ScoredOutcome<List<OUTCOME_TYPE>> returnValue = new ScoredOutcome<List<OUTCOME_TYPE>>(outcomeSequence,
						score);
				nbestSequences.add(returnValue);
			}

			Collections.sort(nbestSequences);
		}

		Collections.sort(nbestSequences);
		if (nbestSequences.size() > 0) {
			return nbestSequences.get(0).getValue();
		}

		return null;
	}

}
