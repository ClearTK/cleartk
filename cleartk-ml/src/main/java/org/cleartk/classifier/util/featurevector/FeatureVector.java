/**
 * Copyright (c) 2007-2010, Regents of the University of Colorado
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

import org.cleartk.CleartkException;

/**
 * Abstract class for feature vectors that support assignment and modification.
 *
 * @author Philipp Wetzler
 * @author cairns@colorado.edu (Brian Cairns)
 */
public abstract class FeatureVector extends ImmutableFeatureVector {

    public void add(FeatureVector other) throws CleartkException {
        for( FeatureVector.Entry entry : other ) {
            this.set(entry.index, this.get(entry.index) + entry.value);
        }
    }

    public void multiply(double factor) throws CleartkException {
        for( FeatureVector.Entry entry : this ) {
            this.set(entry.index, this.get(entry.index) * factor);
        }
    }

    /**
     * Set the feature at index to value.
     * 
     * @param index
     * @param value
     */
    public abstract void set(int index, double value) throws CleartkException;
}
