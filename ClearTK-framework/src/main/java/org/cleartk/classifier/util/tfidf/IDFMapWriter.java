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
package org.cleartk.classifier.util.tfidf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.cleartk.CleartkException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.Counts;
import org.cleartk.classifier.feature.FeatureCollection;
import org.cleartk.classifier.jar.ClassifierBuilder;


/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philipp G. Wetzler
 *
 */
public class IDFMapWriter<OUTCOME_TYPE> implements DataWriter<OUTCOME_TYPE> {

	public IDFMapWriter(File outputDirectory) throws IOException {
		this.outputDirectory = outputDirectory;		
	}
	
	private File getIDFMapFile(String identifier) {
		if( identifier == null )
			identifier = "default";
		else
			identifier = identifier.toLowerCase();
		
		if( identifier.equals("default") ) {
			return new File(outputDirectory, "idfmap");
		} else {
			return new File(outputDirectory, "idfmap_" + identifier);
		}
	}
	
	private IDFMap getIDFMap(String identifier) throws IOException {
		if( identifier == null )
			identifier = "default";
		else
			identifier = identifier.toLowerCase();

		if( idfMaps.containsKey(identifier) ) {
			return idfMaps.get(identifier);
		} else {
			File idfMapFile = getIDFMapFile(identifier);

			IDFMap idfMap;
			if( idfMapFile.exists() ) {
				logger.info(String.format("load existing idf map \"%s\" from %s", identifier, idfMapFile.toString()));
				idfMap = IDFMap.read(idfMapFile);
			} else {
				logger.info(String.format("initialize new idf map \"%s\"", identifier));
				idfMap = new IDFMap();
			}
			
			idfMaps.put(identifier, idfMap);
			return idfMap;
		}
	}

	public boolean isTraining() {
		return false;
	}

	public Class<? extends ClassifierBuilder<OUTCOME_TYPE>> getDefaultClassifierBuilderClass() {
		return null;
	}

	public void write(Instance<OUTCOME_TYPE> instance) throws CleartkException {
		consumeFeatures(instance.getFeatures());
	}
		
	private void consumeFeatures(Collection<Feature> features) throws CleartkException {
		for( Feature feature : features ) {
			if( feature.getValue() instanceof Counts ) {
				Counts counts = (Counts) feature.getValue();
				
				IDFMap idfMap;
				try {
					idfMap = getIDFMap(counts.getIdentifier());
				} catch (IOException e) {
					throw new CleartkException(e);
				}
				idfMap.consume(counts);
			} else if( feature.getValue() instanceof FeatureCollection ) {
				FeatureCollection fc = (FeatureCollection) feature.getValue();
				consumeFeatures(fc.getFeatures());
			}
		}
	}

	public void finish() throws CleartkException {
		List<Exception> exceptions = new ArrayList<Exception>();
		
		for( String identifier : idfMaps.keySet() ) {
			IDFMap idfMap = idfMaps.get(identifier);
			File idfMapFile = getIDFMapFile(identifier);
			try {
				logger.info(String.format("write idf map \"%s\" to %s", identifier, idfMapFile.toString()));
				idfMap.write(idfMapFile);
			} catch( IOException e1 ) {
				exceptions.add(e1);
				continue;
			}
		}
		
		if( exceptions.size() > 0 ) {
			if( exceptions.size() == 1 ) {
				throw new CleartkException(exceptions.get(0));
			} else {
				throw new CleartkException(String.format("%s and %d others", exceptions.get(0).toString(), exceptions.size()-1));
			}
		}
	}

	private Map<String,IDFMap> idfMaps = new HashMap<String,IDFMap>();
	private File outputDirectory;
	private Logger logger = Logger.getLogger(this.getClass().getName());

}
