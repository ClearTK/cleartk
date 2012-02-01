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
package org.cleartk.classifier.weka;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.jar.DataWriter_ImplBase;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 * http://weka.wikispaces.com/Creating+an+ARFF+file
 * 
 */

public class WekaDataWriter extends DataWriter_ImplBase<WekaClassifierBuilder, Instance, String, String> {

	private PrintWriter trainingDataWriter;
	
	private final String relationTag; 
	
	List<Instance> instanceList;
	List<String> outcomes;
	Set<String> outcomeValues;
	
	public WekaDataWriter(File outputDirectory, String relationTag) throws IOException {
		super(outputDirectory);
		this.relationTag = relationTag;
		instanceList = new ArrayList<Instance>();
		outcomes = new ArrayList<String>();
		outcomeValues = new HashSet<String>();
	}

	@Override
	public void writeEncoded(Instance instance, String outcome)  {
		instanceList.add(instance);
		outcomes.add(outcome);
		outcomeValues.add(outcome);
	}

	@Override
	public void finish() throws CleartkProcessingException {
		FastVector attributes = ((WekaFeaturesEncoder) this.classifierBuilder.getFeaturesEncoder()).getWekaAttributes();
		Attribute outcomeAttribute = createOutcomeAttribute(attributes.size());
		attributes.addElement(outcomeAttribute);
	
		Instances instances = new Instances(relationTag, attributes, instanceList.size());
		instances.setClass(outcomeAttribute);
	
		for(int i=0; i < instanceList.size(); i++) {
			Instance instance = instanceList.get(i);
			
			instance.setValue(outcomeAttribute, outcomes.get(i));
			instances.add(instance);
		}
		
		
		trainingDataWriter.write(instances.toString());
		super.finish();
	}

	private Attribute createOutcomeAttribute(int attributeIndex) {
		FastVector attributeValues = new FastVector(outcomeValues.size());
		for (String outcome : outcomeValues) {
			attributeValues.addElement(outcome);
		}
		Attribute attribute = new Attribute("outcome", attributeValues, attributeIndex);
		return attribute; 
	}

	@Override
	protected WekaClassifierBuilder newClassifierBuilder() {
	    return new WekaClassifierBuilder();
	}

}
