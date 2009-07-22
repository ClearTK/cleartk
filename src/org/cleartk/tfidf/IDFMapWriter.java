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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;
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
import org.cleartk.classifier.feature.FeatureCollection;
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
	
	/**
	 * "org.cleartk.tfidf.IDFMapWriter.PARAM_IDFMAP_IDENTIFIER"
	 * is a single, optional string parameter. If it is given, it restricts the
	 * Counts features used for populating the IDF map to only those that have
	 * that specific identifier.
	 */
	public static final String PARAM_IDFMAP_IDENTIFIER = "org.cleartk.tfidf.IDFMapWriter.PARAM_IDFMAP_IDENTIFIER";

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			super.initialize(context);

			idfMapFile = new File((String)UIMAUtil.getRequiredConfigParameterValue(context, PARAM_IDFMAP_FILE)).getAbsoluteFile();

			if( ! idfMapFile.getParentFile().exists() )
				throw new ResourceInitializationException();
			
			if( idfMapFile.exists() )
				counter = readMap(idfMapFile);
			else
				counter = new IDFCounter();
			
			identifier = (String) UIMAUtil.getDefaultingConfigParameterValue(context, PARAM_IDFMAP_IDENTIFIER, null);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public OUTCOME_TYPE consume(Instance<OUTCOME_TYPE> instance) {
		consumeFeatures(instance.getFeatures());

		return null;
	}
	
	public void consumeFeatures(Collection<Feature> features) {
		for( Feature feature : features ) {
			if( feature.getValue() instanceof Counts ) {
				Counts counts = (Counts) feature.getValue();
				
				if( identifier == null || identifier.equals(counts.getIdentifier()) )
					consumeCountsFeature(counts);
			} else if( feature.getValue() instanceof FeatureCollection ) {
				FeatureCollection fc = (FeatureCollection) feature.getValue();
				consumeFeatures(fc.getFeatures());
			}
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		try {
			writeMap(counter, idfMapFile);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	private static IDFCounter readMap(File inputFile) throws IOException {
		ObjectInput input = new ObjectInputStream(new FileInputStream(inputFile));
		try {
			Map<String, Double> idfMap = (Map<String, Double>) input.readObject();
			IDFCounter c = IDFCounter.fromIDFMap(idfMap);
			return c;
		} catch( ClassNotFoundException e ) {
			throw new IOException(e.toString());
		} finally {
			input.close();
		}
	}

	private static void writeMap(IDFCounter counter, File outputFile) throws IOException {
		if( outputFile.exists() )
			outputFile.delete();
		
		ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(outputFile)));
		try {
			output.writeObject(counter.getIDFMap());
		} finally {
			output.close();
		}
	}

	private void consumeCountsFeature(Counts counts) {
		for( Object o : counts.getValues() ) {
			String key = o.toString();
			int count = counts.getCount(o);
			counter.incrementBy(key, count);
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

	private IDFCounter counter;
	private File idfMapFile;
	private String identifier;

	private static class IDFCounter {
		
		public static IDFCounter fromIDFMap(Map<String, Double> idfMap) {
			double documentCount = Math.exp(idfMap.get(null));
			IDFCounter counter = new IDFCounter();
			for( String key : idfMap.keySet() ) {
				double inverseDocumentFrequency = idfMap.get(key);
				int documentFrequency = (int) (documentCount / inverseDocumentFrequency);
				counter.incrementBy(key, documentFrequency);
			}
			
			return counter;
		}

		public void incrementBy(String key, int c) {
			int count = 0;
			if( documentFrequencies.containsKey(key) )
				count = documentFrequencies.get(key);

			count += c;
			documentCount += c;

			documentFrequencies.put(key, count);
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
