 /** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.svmlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.cleartk.CleartkException;
import org.cleartk.classifier.sigmoid.LinWengPlatt;
import org.cleartk.classifier.sigmoid.LinWengPlatt.ConvergenceFailure;
import org.cleartk.classifier.sigmoid.Sigmoid;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.SparseFeatureVector;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 * 
 * This class implements an algorithm to fit a sigmoid function to the output
 * of an SVM classifier. The algorithm is the one introduced by Hsuan-Tien Lin,
 * Chih-Jen Lin, and Ruby C. Weng (who were in turn extending work by J. Platt),
 * and this implementation is a direct translation of their pseudo-code as 
 * presented in
 * 
 * Lin, Lin, Weng. A note on Platt's probabilistic outputs for support vector 
 * machines. In Machine Learning, vol. 68, pp. 267-276, 2007.
 * 
 * @author Philipp G. Wetzler
 */
public class FitSigmoid {
	
	public static Sigmoid fit(File svmlightModelFile, File trainingDataFile) throws ConvergenceFailure, IOException, CleartkException {
		SVMlightModel model = SVMlightModel.fromFile(svmlightModelFile);
		
		BufferedReader r = new BufferedReader(new FileReader(trainingDataFile));
		int lines = 0;
		while( r.readLine() != null )
			lines += 1;
		r.close();
		
		boolean[] labels = new boolean[lines];
		double[] decisionValues = new double[lines];
		int i=0;
		r = new BufferedReader(new FileReader(trainingDataFile));
		String line = r.readLine();
		while( line != null ) {
			TrainingInstance ti = parseTI(line.trim());
			labels[i] = ti.getLabel();
			decisionValues[i] = model.evaluate(ti.getFeatureVector());
			i += 1;
			line = r.readLine();
		}
		r.close();
		
		return LinWengPlatt.fit(decisionValues, labels);

	}

	private static TrainingInstance parseTI(String line) throws IOException, CleartkException {
		String[] fields = line.split(" ");
		
		boolean label = fields[0].trim().equals("+1");
		
		FeatureVector fv = new SparseFeatureVector();
		
		for( int i=1; i<fields.length; i++ ) {
			String[] parts = fields[i].split(":");
			int featureIndex = Integer.valueOf(parts[0]);
			double featureValue = Double.valueOf(parts[1]);
			fv.set(featureIndex, featureValue);
		}
		
		return new TrainingInstance(label, fv);
	}
	
	private static class TrainingInstance {
		
		public TrainingInstance(boolean label, FeatureVector featureVector) {
			this.label = label;
			this.featureVector = featureVector;
		}
		
		public boolean getLabel() {
			return label;
		}
		
		public FeatureVector getFeatureVector() {
			return featureVector;
		}
		
		private boolean label;
		private FeatureVector featureVector;
	}
}
