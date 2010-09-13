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
package org.cleartk.classifier.svmlight.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.cleartk.CleartkException;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.SparseFeatureVector;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class SVMlightModel {
	public static SVMlightModel fromFile(File modelFile) throws IOException, CleartkException {
		InputStream modelStream = new FileInputStream(modelFile);
		SVMlightModel model = fromInputStream(modelStream);
		modelStream.close();
		return model;
	}
	
	public static SVMlightModel fromInputStream(InputStream modelStream) throws IOException, CleartkException {
		SVMlightModel model = new SVMlightModel();
		
		SVMlightReader in = new SVMlightReader(modelStream);
		String buffer;
		
		// Model File Version
		buffer = in.readLine();
		model.version = buffer;
		
		// Kernel Type and Parameters
		int ktype = Integer.valueOf(in.readLine());
		int kpar_degree = Integer.valueOf(in.readLine()); // kpar_degree
		double kpar_gamma = Double.valueOf(in.readLine()); // kpar_gamma
		double kpar_s = Double.valueOf(in.readLine()); // kpar_s
		double kpar_c = Double.valueOf(in.readLine()); // kpar_c
		in.readLine(); // kpar_u
		switch( ktype ) {
		case 0:
			// linear kernel
			model.kernel = new LinearKernel();
			break;
		case 1:
			// polynomial kernel
			model.kernel = new PolynomialKernel(kpar_s, kpar_c, kpar_degree);
			break;
		case 2:
			// rbf kernel
			model.kernel = new RBFKernel(kpar_gamma);
			break;
		case 3:
			// sigmoid kernel
			model.kernel = new SigmoidKernel(kpar_s, kpar_c);
			break;
		default:
			throw new UnsupportedKernelError();
		}
		
		// Highest Feature Index
		in.readLine();
		
		// Number of Training Documents
		in.readLine();
		
		// Number of Support Vectors
		int numberOfSupportVectors = Integer.valueOf(in.readLine()) - 1;
		
		// Threshold b
		model.bias = Double.valueOf(in.readLine());
		
		// Support Vectors
		model.supportVectors = new SupportVector[numberOfSupportVectors];
		for( int i=0; i<numberOfSupportVectors; i++ ) {
			model.supportVectors[i] = readSV(in);
		}
		
		model.compress();
		
		return model;
	}
	
	SupportVector[] supportVectors;
	double bias;
	Kernel kernel;
	String version;

	private SVMlightModel() {
	}
	
	public double evaluate(FeatureVector fv) {
		double result = -bias;
		
		for( SupportVector sv : supportVectors ) {
			result += sv.alpha_y * kernel.evaluate(fv, sv.featureVector);
		}
		
		return result;
	}
	
	private static SupportVector readSV(SVMlightReader in) throws IOException, CleartkException {
		String[] fields = in.readLine().split(" ");
		double alpha_y = Double.valueOf(fields[0]);
		
		FeatureVector fv = new SparseFeatureVector();
		
		for( int i=1; i<fields.length; i++ ) {
			String[] parts = fields[i].split(":");
			int featureIndex = Integer.valueOf(parts[0]);
			double featureValue = Double.valueOf(parts[1]);
			fv.set(featureIndex, featureValue);
		}
		
		return new SupportVector(alpha_y, fv);
	}
	
	private void compress() throws CleartkException {
		if( ! (kernel instanceof LinearKernel) )
			return;
		
		FeatureVector newFV = new SparseFeatureVector();
		for( SupportVector sv : supportVectors ) {
			FeatureVector fv = new SparseFeatureVector(sv.featureVector);
			fv.multiply(sv.alpha_y);
			newFV.add(fv);
		}
		SupportVector newSVs[] = {new SupportVector(1, newFV)};
		supportVectors = newSVs;
	}
	
	private static class SVMlightReader {
		BufferedReader reader;
		
		SVMlightReader(InputStream modelStream) {
			this.reader = new BufferedReader(new InputStreamReader(modelStream));
		}
		
		String readLine() throws IOException {
			String line = reader.readLine().trim();
			return line.split("#")[0].trim();
		}
	}

}
