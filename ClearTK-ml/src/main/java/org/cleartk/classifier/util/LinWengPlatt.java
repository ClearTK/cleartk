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
package org.cleartk.classifier.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

import org.cleartk.CleartkException;
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
public class LinWengPlatt {
	
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
		
		return fit(decisionValues, labels);

	}

	public static Sigmoid fit(double[] decisionValues, boolean[] labels) throws ConvergenceFailure {
		
		assert(decisionValues.length == labels.length);
		
		int nPlus = 0;
		for( boolean l : labels )
			if( l ) nPlus += 1;
		int nMinus = labels.length - nPlus;
		
		int maxIterations = 100;
		double minimumStepsize = 1e-10;
		double sigma = 1e-12;
		
		double hiTarget = (nPlus+1.0)/(nPlus+2.0);
		double loTarget = 1/(nMinus+2.0);
		int n=nMinus+nPlus;
		
		double t[] = new double[n];
		for( int i=0; i<n; i++ ) {
			if( labels[i] )
				t[i] = hiTarget;
			else
				t[i] = loTarget;
		}
		
		double a = 0.0;
		double b = Math.log((nMinus+1.0)/(nPlus+1.0));
		double f = 0.0;
		
		for( int i=0; i<n; i++ ) {
			double fApB = decisionValues[i]*a+b;
			if( fApB >= 0 )
				f += t[i]*fApB+Math.log(1+Math.exp(-fApB));
			else
				f += (t[i]-1)*fApB+Math.log(1+Math.exp(fApB));
		}
		
		int iterations;
		for( iterations=0; iterations<maxIterations; iterations++ ) {
			double h11 = sigma;
			double h22 = sigma;
			double h21 = 0.0;
			double g1 = 0.0;
			double g2 = 0.0;
			
			for( int i=0; i<n; i++ ) {
				double fApB=decisionValues[i]*a+b;
				double p, q;
				if (fApB >= 0) {
					p=Math.exp(-fApB)/(1.0+Math.exp(-fApB));
					q=1.0/(1.0+Math.exp(-fApB));
				} else {
					p=1.0/(1.0+Math.exp(fApB));
					q=Math.exp(fApB)/(1.0+Math.exp(fApB));
				}
				double d2=p*q;
				h11 += decisionValues[i]*decisionValues[i]*d2;
				h22 += d2;
				h21 += decisionValues[i]*d2;
				double d1=t[i]-p;
				g1 += decisionValues[i]*d1;
				g2 += d1;
			}
			
			if( Math.abs(g1)<1e-5 && Math.abs(g2)<1e-5 )
				break;
			
			double det=h11*h22-h21*h21;
			double dA=-(h22*g1-h21*g2)/det;
			double dB=-(-h21*g1+h11*g2)/det;
			double gd=g1*dA+g2*dB;
			double stepsize=1;
			
			while( stepsize >= minimumStepsize ) {
				double newA = a+stepsize*dA;
				double newB = b+stepsize*dB;
				double newf=0.0;
				
				for( int i=1; i<n; i++ ) {
					double fApB=decisionValues[i]*newA+newB;
					if( fApB >= 0 )
						newf += t[i]*fApB+Math.log(1+Math.exp(-fApB));
					else
						newf += (t[i]-1)*fApB+Math.log(1+Math.exp(fApB));
				}
				
				if( newf<f+0.0001*stepsize*gd ) {
					a = newA;
					b = newB;
					f = newf;
					break;
				} else {
					stepsize /= 2.0;
				}
			}
			
			if (stepsize < minimumStepsize) {
				Logger logger = Logger.getLogger(LinWengPlatt.class.getName());
				logger.fine("line search failure");
				break;
			}
		}
		
		if( iterations >= maxIterations )
			throw new ConvergenceFailure("Reaching maximum iterations");
		
		return new Sigmoid(a,b);
	}

	
	public static class Sigmoid implements Serializable {

		private static final long serialVersionUID = 3780856549630884460L;

		public Sigmoid(double A, double B) {
			this.A = A;
			this.B = B;
		}
		
		public double evaluate(double v) {
			return 1 / (1 + Math.exp(A*v + B));
		}
		
		public double getA() {
			return A;
		}
		
		public double getB() {
			return B;
		}
		
		@Override
		public String toString() {
			if( B < 0 ) 
				return String.format("1 / (1 + exp(%.3f*x - %.3f))", A, -B);
			else
				return String.format("1 / (1 + exp(%.3f*x + %.3f))", A, B);
		}
		
		private double A;
		private double B;
	}
	
	public static class ConvergenceFailure extends Exception {

		private static final long serialVersionUID = -7570320408478887106L;

		public ConvergenceFailure(String message) {
			super(message);
		}
		
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
