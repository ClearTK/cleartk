 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.jar.JarClassifier;
import org.cleartk.classifier.util.featurevector.FeatureVector;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philipp Wetzler
 *
 */
public abstract class LIBSVMClassifier<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE> extends JarClassifier<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE,FeatureVector> {
	
	public static final String MODEL_NAME = "model.libsvm";
	public static final String ATTRIBUTES_NAME = "LIBSVM";
	public static final String SCALE_FEATURES_KEY = "scaleFeatures";
	public static final String SCALE_FEATURES_VALUE_NORMALIZEL2 = "normalizeL2";
	
	public LIBSVMClassifier(JarFile modelFile) throws IOException {
		super(modelFile);
		
		ZipEntry modelEntry = modelFile.getEntry(LIBSVMClassifier.MODEL_NAME);
		File tmpFile = File.createTempFile("tmp", ".mdl");
		try {
			LIBSVMClassifier.copy(modelFile.getInputStream(modelEntry), new FileOutputStream(tmpFile));
			this.model = libsvm.svm.svm_load_model(tmpFile.getPath());
		} finally {
			tmpFile.delete();
		}
	}

	public INPUTOUTCOME_TYPE classify(List<Feature> features) throws CleartkException {
		FeatureVector featureVector = this.featuresEncoder.encodeAll(features);

		OUTPUTOUTCOME_TYPE encodedOutcome = decodePrediction(libsvm.svm.svm_predict(this.model, convertToLIBSVM(featureVector)));
		
		return outcomeEncoder.decode(encodedOutcome);
	}
	
	
	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		int len = 0;
		
		while( (len = in.read(buffer)) > 0 ) {
			out.write(buffer, 0, len);
		}
	}
	
	protected static libsvm.svm_node[] convertToLIBSVM(FeatureVector featureVector) {
		List<libsvm.svm_node> nodes = new ArrayList<libsvm.svm_node>();
		
		for( FeatureVector.Entry entry : featureVector ) {
			libsvm.svm_node node = new libsvm.svm_node();
			node.index = entry.index;
			node.value = entry.value;
			nodes.add(node);
		}
		
		return nodes.toArray(new libsvm.svm_node[nodes.size()]);
	}
	
	protected abstract OUTPUTOUTCOME_TYPE decodePrediction(double prediction);

	protected libsvm.svm_model model;
}
