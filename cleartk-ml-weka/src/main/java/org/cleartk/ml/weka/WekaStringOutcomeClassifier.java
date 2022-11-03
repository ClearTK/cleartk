/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.ml.weka;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;
import org.cleartk.ml.jar.Classifier_ImplBase;

import weka.core.Instance;

import com.google.common.annotations.Beta;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */
@Beta
public abstract class WekaStringOutcomeClassifier extends Classifier_ImplBase<Instance, String, String> {

	//TODO need to add the Weka model as a parameter
	public WekaStringOutcomeClassifier(       FeaturesEncoder<Instance> featuresEncoder,
		      OutcomeEncoder<String, String> outcomeEncoder) throws Exception {
		   		super(featuresEncoder, outcomeEncoder);
     }
	
	//TODO no implementation of classify method
	public String classify(List<Feature> features) throws UnsupportedOperationException {
		throw new NotImplementedException();
	}
	
	//TODO no implementation of the score method
	@Override
  public Map<String, Double> score(List<Feature> features) throws CleartkProcessingException {
		throw new NotImplementedException();
	}


}
