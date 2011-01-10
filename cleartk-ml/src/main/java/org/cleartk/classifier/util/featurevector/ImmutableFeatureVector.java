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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for feature vectors that does not allow the feature vector to be modified.
 * 
 * @author Philipp Wetzler
 * @author cairns@colorado.edu (Brian Cairns)
 */
public abstract class ImmutableFeatureVector implements Iterable<ImmutableFeatureVector.Entry> {
    public double l2Norm() {
        double l = 0.0;

        for( ImmutableFeatureVector.Entry entry : this ) {
            l += entry.value * entry.value;
        }

        return Math.sqrt(l);
    }

    public double innerProduct(ImmutableFeatureVector other) {
        double result = 0.0;
        Iterator<Entry> thisIterator = orderedIterator();
        Iterator<Entry> otherIterator = other.orderedIterator();

        Entry thisEntry = thisIterator.next();
        Entry otherEntry = otherIterator.next();
        while (true) {
            if (thisEntry.index == otherEntry.index) {
                result += thisEntry.value * otherEntry.value;
                if (!thisIterator.hasNext() || !otherIterator.hasNext()) { break; }
                thisEntry = thisIterator.next();
                otherEntry = otherIterator.next();
            } else if (thisEntry.index > otherEntry.index) {
                if (!otherIterator.hasNext()) { break; }
                otherEntry = otherIterator.next();
            } else if (thisEntry.index < otherEntry.index) {
                if (!thisIterator.hasNext()) { break; }
                thisEntry = thisIterator.next();
            }
        }

        return result;
    }


    @Override
    public boolean equals(Object o) {
        ImmutableFeatureVector other;
        try {
            other = (ImmutableFeatureVector) o;
        } catch( ClassCastException e ) {
            return false;
        }

        Iterator<Entry> thisIt = this.iterator();
        Iterator<Entry> otherIt = other.iterator();
        while( thisIt.hasNext() || otherIt.hasNext() ) {
            Entry thisEntry;
            Entry otherEntry;
            try {
                thisEntry = thisIt.next();
                otherEntry = otherIt.next();
            } catch( NoSuchElementException e ) {
                return false;
            }

            if( ! thisEntry.equals(otherEntry) )
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1451;
        for( Entry e : this ) {
            result = 757 * result + e.hashCode();
        }
        return result;
    }

    /**
     * Return the value at index.
     * 
     * @param index
     * @return value at index
     */
    public abstract double get(int index);

    /**
     * Gives an iterator over the non-zero features.
     */
    public abstract Iterator<Entry> iterator();

    /**
     * Gives an iterator oover the non-zero features that returns features in order from lowest
     * index to highest index.
     */
    public abstract Iterator<Entry> orderedIterator();

    public static class Entry {
        public Entry(int index, double value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            Entry other;
            try {
                other = (Entry) o;
            } catch( ClassCastException e ) {
                return false;
            }

            if( this.index != other.index )
                return false;

            if( this.value != other.value )
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = 83;
            result = 47 * result + this.index;
            result = 47 * result + new Double(this.value).hashCode();
            return result;
        }

        public final int index;
        public final double value;
    }

}
