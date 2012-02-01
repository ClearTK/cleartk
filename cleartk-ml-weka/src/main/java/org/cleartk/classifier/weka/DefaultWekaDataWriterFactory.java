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

import java.io.IOException;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.outcome.StringToStringOutcomeEncoder;
import org.cleartk.classifier.jar.DataWriterFactory_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */

public class DefaultWekaDataWriterFactory extends DataWriterFactory_ImplBase<Iterable<Feature>, String, String>{

	  public static final String PARAM_RELATION_TAG = ConfigurationParameterFactory.createConfigurationParameterName(
			  DefaultWekaDataWriterFactory.class,
		      "relationTag");

		  @ConfigurationParameter(
		      mandatory = true,
		      description = "determines the value that appears after @RELATION at the top of the ARFF file")
		  protected String relationTag;

	
	public DataWriter<String> createDataWriter() throws IOException {
		WekaDataWriter swdw = new WekaDataWriter(outputDirectory, relationTag);
		
		if(!this.setEncodersFromFileSystem(swdw)) {
			swdw.setFeaturesEncoder(new WekaFeaturesEncoder());
			swdw.setOutcomeEncoder(new StringToStringOutcomeEncoder());
		}
		
		return swdw;
	}
	
}
