/** 
 * Copyright 2011-2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
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
package org.cleartk.classifier.crfsuite;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.SequenceClassifier_ImplBase;
/**
 * <br>
 * Copyright (c) 2011-2012, Technische Universität Darmstadt <br>
 * All rights reserved.
 * 
 * 
 * @author Martin Riedl
 */

public class CRFSuiteClassifier
	extends SequenceClassifier_ImplBase<List<NameNumber>, String, String> {


	private File modelFile;
	CRFSuiteWrapper wrapper;
	public CRFSuiteClassifier(
			FeaturesEncoder<List<NameNumber>> featuresEncoder,
			OutcomeEncoder<String, String> outcomeEncoder, File modelFile) {
		super(featuresEncoder, outcomeEncoder);
		this.modelFile = modelFile;
		this.wrapper = new CRFSuiteWrapper();
	}

	
	public List<String> classify(List<List<Feature>> features)
		throws CleartkProcessingException {
		List<String> posTags = null;

		try {
			
			posTags = wrapper.classifyFeatures(features,
					outcomeEncoder, featuresEncoder, modelFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return posTags;
	}

}
