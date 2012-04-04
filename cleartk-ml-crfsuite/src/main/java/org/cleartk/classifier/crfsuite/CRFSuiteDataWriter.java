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
import java.io.FileNotFoundException;
import java.util.List;

import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.jar.SequenceDataWriter_ImplBase;

/**
 * <br>
 * Copyright (c) 2011-2012, Technische Universität Darmstadt <br>
 * All rights reserved.
 * 
 * 
 * @author Martin Riedl
 */

public class CRFSuiteDataWriter
		extends
		SequenceDataWriter_ImplBase<CRFSuiteClassifierBuilder, List<NameNumber>, String, String> {

	private String featureSeparator = "\t";

	public CRFSuiteDataWriter(File outputDirectory)
			throws FileNotFoundException {
		super(outputDirectory);
	}

	@Override
	public void writeEncoded(List<NameNumber> features, String outcome) {
		this.trainingDataWriter.print(outcome);
		for (NameNumber nameNumber : features) {
			this.trainingDataWriter.print(featureSeparator);
			this.trainingDataWriter.print(nameNumber.name);
		}
		this.trainingDataWriter.println();
	}

	@Override
	public void writeEndSequence() {
		this.trainingDataWriter.println();
	}

	@Override
	protected CRFSuiteClassifierBuilder newClassifierBuilder() {
		return new CRFSuiteClassifierBuilder();
	}

}
