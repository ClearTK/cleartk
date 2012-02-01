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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */

public class WekaFeaturesEncoder implements FeaturesEncoder<Instance> {

	private static final long serialVersionUID = 1L;

	private FastVector attributes;
	private Map<String, Attribute> attributeMap;
	
	public WekaFeaturesEncoder() {
		attributes = new FastVector(1000);
		attributeMap = new HashMap<String, Attribute>();
	}
	
	public Instance encodeAll(Iterable<Feature> features)  {
		List<Feature> featureList = (List<Feature>) features;
		
		Instance instance = new Instance(attributes.size()+featureList.size());
		for(Feature feature : features) {
			Attribute attribute = featureToAttribute(feature);
			Object featureValue = feature.getValue();
			
			if(featureValue instanceof Number) {
				double attributeValue = ((Number)feature.getValue()).doubleValue();
				instance.setValue(attribute, attributeValue);
			} else if(featureValue instanceof Boolean) {
				double attributeValue = (Boolean) featureValue ? 1.0d : -1.0d;
				instance.setValue(attribute, attributeValue);
			} else {
				instance.setValue(attribute, featureValue.toString());
			}
		}
		
		return instance;
	}

	/**
	 * @param feature
	 * @return
	 */
	private Attribute featureToAttribute(Feature feature) {
		String name = feature.getName();
		name = Utils.quote(name);
		Attribute attribute = attributeMap.get(name);
		if(attribute == null) {
			attribute = featureToAttribute(feature, attributes.size());
			attributes.addElement(attribute);
			attributeMap.put(name, attribute);
		}
		return attribute;
	}

	public static Attribute featureToAttribute(Feature feature, int attributeIndex) {
		Object value = feature.getValue();
		String name = feature.getName();
		name = Utils.quote(name);
		Attribute attribute;
		// if value is a number then create a numeric attribute
		if (value instanceof Number) {
			attribute = new Attribute(name, attributeIndex);
		}// if value is a boolean then create a numeric attribute
		if (value instanceof Boolean) {
			attribute = new Attribute(name, attributeIndex);
		}
		// if value is an Enum thene create a nominal attribute
		else if (value instanceof Enum) {
			Object[] enumConstants = value.getClass().getEnumConstants();
			FastVector attributeValues = new FastVector(enumConstants.length);
			for (Object enumConstant : enumConstants) {
				attributeValues.addElement(enumConstant.toString());
			}
			attribute = new Attribute(name, attributeValues, attributeIndex);
		}
		// if value is not a number, boolean, or enum, then we will create a
		// string attribute
		else {
			attribute = new Attribute(name, (FastVector) null, attributeIndex);
		}
		return attribute;
	}

	public void finalizeFeatureSet(File outputDirectory)  {	}
	
	public FastVector getWekaAttributes() {
		return attributes;
	}

}
