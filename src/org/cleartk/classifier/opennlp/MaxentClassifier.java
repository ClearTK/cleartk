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
package org.cleartk.classifier.opennlp;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;

import opennlp.maxent.MaxentModel;
import opennlp.maxent.io.BinaryGISModelReader;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.Initializable;
import org.cleartk.classifier.Classifier_ImplBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredValue;
import org.cleartk.classifier.Viterbi;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class MaxentClassifier extends Classifier_ImplBase<String, String, List<NameNumber>> implements Initializable {

	protected MaxentModel model;
	int sequenceBeamSize = 1;
	
	public MaxentClassifier(JarFile modelFile) throws IOException {
		super(modelFile);
		ZipEntry modelEntry = modelFile.getEntry("model.maxent");
		this.model = new BinaryGISModelReader(new DataInputStream(new GZIPInputStream(modelFile
				.getInputStream(modelEntry)))).getModel();
	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
		sequenceBeamSize = (Integer) UIMAUtil.getDefaultingConfigParameterValue(context, Viterbi.PARAM_BEAM_SIZE, 1);
	}

	
	@Override
	public String classify(List<Feature> features) {
		EvalParams evalParams = convertToEvalParams(features);
		String encodedOutcome = this.model.getBestOutcome(this.model.eval(evalParams.getContext(), evalParams
				.getValues()));
		return outcomeEncoder.decode(encodedOutcome);
	}

	@Override
	public List<ScoredValue<String>> score(List<Feature> features, int maxResults) {
		EvalParams evalParams = convertToEvalParams(features);
		double[] evalResults = this.model.eval(evalParams.getContext(), evalParams.getValues());
		String[] encodedOutcomes = (String[]) this.model.getDataStructures()[2];

		List<ScoredValue<String>> returnValues = new ArrayList<ScoredValue<String>>();

		if (maxResults == 1) {
			String encodedBestOutcome = outcomeEncoder.decode(this.model.getBestOutcome(evalResults));
			double bestResult = evalResults[this.model.getIndex(encodedBestOutcome)];
			returnValues.add(new ScoredValue<String>(encodedBestOutcome, bestResult));
			return returnValues;
		}
		else {
				
			for (int i = 0; i < evalResults.length; i++) {
				returnValues.add(new ScoredValue<String>(outcomeEncoder.decode(encodedOutcomes[i]), evalResults[i]));
			}

			Collections.sort(returnValues);
			if (returnValues.size() > maxResults) {
				return returnValues.subList(0, maxResults);
			}
			else {
				return returnValues;
			}
		}
	}

	@Override
	public List<String> classifySequence(List<List<Feature>> features){
		if(sequenceBeamSize == 1)
			return super.classifySequence(features);
		else {
			return Viterbi.classifySequence(features, sequenceBeamSize, String.class, this, outcomeFeatureExtractors, false);	
		}
	}

	/**
	 * @return false
	 */
	@Override
	public boolean isSequential() {
		return false;
	}

	private EvalParams convertToEvalParams(List<Feature> features) {
		String[] context = new String[features.size()];
		float[] values = new float[features.size()];

		List<NameNumber> contexts = featuresEncoder.encodeAll(features);

		for (int i = 0; i < contexts.size(); i++) {
			NameNumber contextValue = contexts.get(i);
			context[i] = contextValue.name;
			values[i] = contextValue.number.floatValue();
		}

		return new EvalParams(context, values);
	}

	public class EvalParams {
		private String[] context;

		private float[] values;

		public String[] getContext() {
			return context;
		}

		public float[] getValues() {
			return values;
		}

		public EvalParams(String[] context, float[] values) {
			this.context = context;
			this.values = values;
		}

	}

}






//List<ScoredValue<List<String>>> returnValues = new ArrayList<ScoredValue<List<String>>>();
//
//if (features == null || features.size() == 0) {
//	return Collections.emptyList();
//}
//
//List<ScoredValue<String>> scoredOutcomes = score(features.get(0), maxResults);
//for (ScoredValue<String> scoredOutcome : scoredOutcomes) {
//	double score = scoredOutcome.getScore();
//	List<String> sequence = new ArrayList<String>();
//	sequence.add(scoredOutcome.getValue());
//	returnValues.add(new ScoredValue<List<String>>(sequence, score));
//}
//
//Map<String, Double> l = new HashMap<String, Double>();
//Map<String, List<String>> m = new HashMap<String, List<String>>();
//
//for (int i = 1; i < features.size(); i++) {
//
//	List<Feature> instanceFeatures = features.get(i);
//	l.clear();
//	m.clear();
//	for (ScoredValue<List<String>> scoredSequence : returnValues) {
//		// add features from previous outcomes from each scoredSequence
//		// in returnValues
//		int outcomeFeaturesCount = 0;
//		List<Object> previousOutcomes = new ArrayList<Object>(scoredSequence.getValue());
//		for (OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
//			List<Feature> outcomeFeatures = outcomeFeatureExtractor.extractFeatures(previousOutcomes);
//			instanceFeatures.addAll(outcomeFeatures);
//			outcomeFeaturesCount += outcomeFeatures.size();
//		}
//		// score the instance features using the features added by the
//		// outcomeFeatureExtractors
//		scoredOutcomes = score(instanceFeatures, maxResults);
//		// remove the added features from previous outcomes for this
//		// scoredSequence
//		instanceFeatures = instanceFeatures.subList(0, instanceFeatures.size() - outcomeFeaturesCount);
//
//		for (ScoredValue<String> scoredOutcome : scoredOutcomes) {
//			if (!l.containsKey(scoredOutcome.getValue())) {
//				double score = scoredSequence.getScore() + scoredOutcome.getScore();
//				l.put(scoredOutcome.getValue(), score);
//				m.put(scoredOutcome.getValue(), new ArrayList<String>(scoredSequence.getValue()));
//			}
//			else {
//				double newScore = scoredSequence.getScore() + scoredOutcome.getScore();
//				double bestScore = l.get(scoredOutcome.getValue());
//
//				if (newScore > bestScore) {
//					l.put(scoredOutcome.getValue(), newScore);
//					m.put(scoredOutcome.getValue(), new ArrayList<String>(scoredSequence.getValue()));
//				}
//			}
//		}
//	}
//
//	returnValues.clear();
//	for (String outcome : l.keySet()) {
//		List<String> outcomeSequence = m.get(outcome);
//		outcomeSequence.add(outcome);
//		double score = l.get(outcome);
//		ScoredValue<List<String>> returnValue = new ScoredValue<List<String>>(outcomeSequence, score);
//		returnValues.add(returnValue);
//	}
//
//	Collections.sort(returnValues);
//}
//
//Collections.sort(returnValues);
//return returnValues;
