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

import java.io.File;

import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.ClassifierBuilder_ImplBase;

import com.google.common.annotations.Beta;
/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */
@Beta
public class WekaStringOutcomeClassifierBuilder extends ClassifierBuilder_ImplBase<WekaStringOutcomeClassifier, Iterable<Feature>, String, String> {

	public static final String TRAINING_FILE_NAME = "training-data.arff";

  @Override
	public File getTrainingDataFile(File dir) {
	    return new File(dir, TRAINING_FILE_NAME);
	}

	@Override
	public void trainClassifier(File dir, String... args) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected WekaStringOutcomeClassifier newClassifier() {
		// TODO Auto-generated method stub
		return null;
	}


}








