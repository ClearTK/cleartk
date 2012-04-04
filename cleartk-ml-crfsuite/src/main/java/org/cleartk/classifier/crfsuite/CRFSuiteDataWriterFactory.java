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

import java.io.IOException;
import java.util.List;

import org.cleartk.classifier.SequenceDataWriter;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.outcome.StringToStringOutcomeEncoder;
import org.cleartk.classifier.jar.SequenceDataWriterFactory_ImplBase;

/**
 * <br>
 * Copyright (c) 2011-2012, Technische Universität Darmstadt <br>
 * All rights reserved.
 * 
 * 
 * @author Martin Riedl
 */

public class CRFSuiteDataWriterFactory extends
		SequenceDataWriterFactory_ImplBase<List<NameNumber>, String, String> {

	public SequenceDataWriter<String> createDataWriter() throws IOException {
		CRFSuiteDataWriter mdw = new CRFSuiteDataWriter(outputDirectory);
		if (!this.setEncodersFromFileSystem(mdw)) {
			NameNumberFeaturesEncoder fe = new NameNumberFeaturesEncoder(false,
					false);
			fe.addEncoder(new NumberEncoder());
			fe.addEncoder(new BooleanEncoder());
			fe.addEncoder(new StringEncoder());
			mdw.setFeaturesEncoder(fe);

			mdw.setOutcomeEncoder(new StringToStringOutcomeEncoder());
		}

		return mdw;
	}

}
