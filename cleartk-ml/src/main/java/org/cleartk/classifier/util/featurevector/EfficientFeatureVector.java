/**
 * Copyright (c) 2010, Regents of the University of Colorado
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

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.cleartk.CleartkException;

/**
 * Implementation of an {@link ImmutableFeatureVector} that is efficient at storing sparse feature
 * vectors with lower memory usage and better performance than {@link SparseFeatureVector}. This
 * class should be used for any sparse feature vector where modification of values is not required
 * after initialization.
 * 
 * @author cairns@colorado.edu (Brian Cairns)
 */
public class EfficientFeatureVector extends ImmutableFeatureVector {
    /** Sorted array of entry indicies. */
    private final int[] indices;
    /** Array of values in the same order as their corresponding indicces. */
    private final double[] values;
    /** Split pattern for fields (precompiled for performance). */
    private static final Pattern FIELD_SPLIT_PATTERN = Pattern.compile(":");

    public EfficientFeatureVector(ImmutableFeatureVector vector) {
        int n = 0;
        Iterator<Entry> countIterator = vector.iterator();
        while (countIterator.hasNext()) {
            countIterator.next();
            ++n;
        }

        indices = new int[n];
        values = new double[n];

        int i = 0;
        Iterator<Entry> entryIterator = vector.orderedIterator();
        while (entryIterator.hasNext()) {
            Entry entry = entryIterator.next();
            indices[i] = entry.index;
            values[i] = entry.value;
            ++i;
        }
    }

    /**
     * Creates a new space-efficient feature vector based on the provided indices and values.
     * Each entry in the value array must correspond to an index value at the corresponding
     * position in the indices array. The entries in indicies must be in order from smallest to
     * largest.
     */
    public EfficientFeatureVector(int[] indices, double[] values) {
        if (indices.length != values.length) {
            throw new InvalidParameterException("Index and value arrays must be of equal length.");
        }
        this.indices = Arrays.copyOf(indices, indices.length);
        this.values = Arrays.copyOf(values, values.length);
    }

    /**
     * Constructs a new {@link EfficientFeatureVector} from an array of colon-separated
     * strings, where the part before the colon of each element is a positive integer index and
     * the part after the colon is a decimal value. This constructor is most efficient if the
     * elements are in order from smallest index to largest index, but any order is acceptable.
     * @param start start position for values in the fields array; values prior to this position
     * are ignored
     */
    public EfficientFeatureVector(String[] fields, int start) throws CleartkException {
        indices = new int[fields.length - start];
        values = new double[fields.length - start];
        boolean inOrder = true;

        for (int i = start; i < fields.length; ++i) {
            String[] parts = FIELD_SPLIT_PATTERN.split(fields[i]);
            this.indices[i - 1] = Integer.valueOf(parts[0]);
            this.values[i - 1] = Double.valueOf(parts[1]);
            if (i > 1) {
                // Verify that indices are increasing and unique
                if (indices[i - 2] >= indices[i - 1]) {
                    inOrder = false;
                }
            }
        }

        if (!inOrder) {
            // Values are not in-order. Use a SparseFeatureVector to sort them.
            SparseFeatureVector sorted = new SparseFeatureVector();
            for (int i = 0; i < indices.length; ++i) {
                sorted.set(indices[i], values[i]);
            }
            int i = 0;
            for (Entry entry : sorted) {
                indices[i] = entry.index;
                values[i] = entry.value;
                ++i;
            }
        }
    }

    @Override public double get(int index) {
        int valueIndex = Arrays.binarySearch(indices, index);
        if (valueIndex > 0) {
            return values[valueIndex];
        } else {
            return 0.0;
        }
    }

    @Override
    public java.util.Iterator<Entry> iterator() {
        return new SpaceEfficientFeatureVectorIterator(indices, values);
    }

    @Override
    public Iterator<Entry> orderedIterator() {
        // The values are always stored in order so the normal iterator can be used.
        return iterator();
    }

    class SpaceEfficientFeatureVectorIterator implements Iterator<Entry> {
        private final int[] indices;
        private final double[] values;
        private int i;

        private SpaceEfficientFeatureVectorIterator(int[] indices, double[] values) {
            this.indices = indices;
            this.values = values;
        }

        public boolean hasNext() {
            return (i < indices.length);
        }

        public Entry next() {
            int key = indices[i];
            double value = values[i];

            ++i;
            return new Entry(key, value);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // Optimized version of innerProduct
    @Override public double innerProduct(ImmutableFeatureVector other) {
        double result = 0.0;

        if (other instanceof EfficientFeatureVector) {
            // Fast version for when both vectors are space-efficient feature vectors, so we have
            // access to the raw arrays and don't need to create Entry objects.
            EfficientFeatureVector o = (EfficientFeatureVector) other;
            int thisPos = 0;
            int thisEnd = indices.length - 1;
            int otherPos = 0;
            int otherEnd = o.indices.length - 1;
            int thisIndex = indices[thisPos];
            int otherIndex = o.indices[otherPos];
            while (true) {
                if (thisIndex == otherIndex) {
                    result += values[thisPos] * o.values[otherPos];
                    if (thisPos >= thisEnd || otherPos >= otherEnd) { break; }
                    ++thisPos;
                    ++otherPos;
                    thisIndex = indices[thisPos];
                    otherIndex = o.indices[otherPos];
                } else if (thisIndex > otherIndex) {
                    if (otherPos >= otherEnd) { break; }
                    ++otherPos;
                    otherIndex = o.indices[otherPos];
                } else {
                    if (thisPos >= thisEnd) { break; }
                    ++thisPos;
                    thisIndex = indices[thisPos];
                }
            }
        } else {
            // Use iterators for the other vector and direct array access for this vector.
            int thisPos = 0;
            int thisEnd = indices.length - 1;
            Iterator<Entry> otherIterator = other.orderedIterator();

            Entry otherEntry = otherIterator.next();
            int thisIndex = indices[thisPos];
            while (true) {
                if (thisIndex == otherEntry.index) {
                    result += values[thisPos] * otherEntry.value;
                    if (thisPos >= thisEnd || !otherIterator.hasNext()) { break; }
                    ++thisPos;
                    thisIndex = indices[thisPos];
                    otherEntry = otherIterator.next();
                } else if (thisIndex > otherEntry.index) {
                    if (!otherIterator.hasNext()) { break; }
                    otherEntry = otherIterator.next();
                } else {
                    if (thisPos >= thisEnd) { break; }
                    ++thisPos;
                    thisIndex = indices[thisPos];
                }
            }
        }

        return result;
    }
}
