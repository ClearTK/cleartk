/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.tfidf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer_ImplBase;
import org.cleartk.classifier.feature.Counts;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philipp G. Wetzler
 *
 */
public class IDFMapWriter<OUTCOME_TYPE> extends InstanceConsumer_ImplBase<OUTCOME_TYPE> {

	/**
	 * "org.cleartk.tfidf.IDFMapWriter.PARAM_IDFMAP_FILE"
	 * is a single, required, string parameter that provides the path where the
	 * Map<String, Double> of inverse document frequencies should be written.
	 */
	public static final String PARAM_IDFMAP_FILE = "org.cleartk.tfidf.IDFMapWriter.PARAM_IDFMAP_FILE";

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		idfMapFile = new File((String)UIMAUtil.getRequiredConfigParameterValue(
				context, PARAM_IDFMAP_FILE)).getAbsoluteFile();

		if( ! idfMapFile.getParentFile().exists() )
			throw new ResourceInitializationException();
	}

	public OUTCOME_TYPE consume(Instance<OUTCOME_TYPE> instance) {
		for( Feature feature : instance.getFeatures() ) {
			if( feature.getValue() instanceof Counts ) {
				String name = feature.getName();
				Counts counts = (Counts) feature.getValue();
				consumeCountsFeature(name, counts);
			}
		}

		return null;
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		try {
			if( counters.size() == 1 ) {
				IDFCounter counter = counters.values().iterator().next();
				writeMap(counter, idfMapFile);
			} else {
				for( String name : counters.keySet() ) {
					File outputFile = new File(idfMapFile.getParentFile(), idfMapFile.getName() + "-" + name);
					IDFCounter counter = counters.get(name);
					writeMap(counter, outputFile);
				}
			}
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void writeMap(IDFCounter counter, File outputFile) throws IOException {
		ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(idfMapFile)));
		try {
			output.writeObject(counter.getIDFMap());
		} finally {
			output.close();
		}
	}

	private void consumeCountsFeature(String name, Counts counts) {
		String indexedName = name + "__" + counts.getFeatureName();

		IDFCounter counter = getCounter(indexedName);
		for( Object o : counts.getValues() ) {
			String key = o.toString();
			int count = counts.getCount(o);
			counter.increment(key, count);
		}
	}

	private IDFCounter getCounter(String name) {
		if( counters.containsKey(name) )
			return counters.get(name);
		else {
			IDFCounter counter = new IDFCounter();
			counters.put(name, counter);
			return counter;
		}
	}

	public List<OUTCOME_TYPE> consumeSequence(List<Instance<OUTCOME_TYPE>> instances) {
		for( Instance<OUTCOME_TYPE> instance : instances )
			consume(instance);
		return null;
	}

	public boolean expectsOutcomes() {
		return false;
	}

	private Map<String, IDFCounter> counters = new HashMap<String, IDFCounter>();
	private File idfMapFile;

	private static class IDFCounter {

		public void increment(String key, int c) {
			int count = 0;
			if( documentFrequencies.containsKey(key) )
				count = documentFrequencies.get(key);

			count += c;
			documentCount += c;

			documentFrequencies.put(key, count);
		}

		public void increment(String key) {
			increment(key, 1);
		}

		public Map<String, Double> getIDFMap() {
			Map<String, Double> idfMap = new HashMap<String, Double>();
			for( String key : documentFrequencies.keySet() ) {
				int documentFrequency = documentFrequencies.get(key);
				double inverseDocumentFrequency = documentCount / documentFrequency;
				idfMap.put(key, inverseDocumentFrequency);
			}
			idfMap.put(null, Math.log(documentCount));

			return idfMap;
		}

		private Map<String, Integer> documentFrequencies = new HashMap<String, Integer>();
		private int documentCount = 0;
	}

}
