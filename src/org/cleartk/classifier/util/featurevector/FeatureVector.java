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
package org.cleartk.classifier.util.featurevector;

import java.util.Iterator;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philipp Wetzler
 *
 */
public abstract class FeatureVector implements Iterable<FeatureVector.Entry>{
	
	public void add(FeatureVector other) {
		for( FeatureVector.Entry entry : other ) {
			this.set(entry.index, this.get(entry.index) + entry.value);
		}
	}
	
	public void multiply(double factor) {
		for( FeatureVector.Entry entry : this ) {
			this.set(entry.index, this.get(entry.index) * factor);
		}
	}
	
	public double l2Norm() {
		double l = 0.0;
		
		for( FeatureVector.Entry entry : this ) {
			l += entry.value * entry.value;
		}
		
		return Math.sqrt(l);
	}
	
	public abstract void set(int index, double value);
	
	public abstract double get(int index);
	
	public abstract Iterator<Entry> iterator();
	
	public double innerProduct(FeatureVector other) {
		double result = 0.0;
		
		for( FeatureVector.Entry entry : other ) {
			result += this.get(entry.index) * entry.value;
		}

		return result;
	}

	public static class Entry {
		public Entry(int index, double value) {
			this.index = index;
			this.value = value;
		}
		
		public final int index;
		public final double value;
	}

}
