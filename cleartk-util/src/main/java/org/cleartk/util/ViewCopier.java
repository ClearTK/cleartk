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
package org.cleartk.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.ByteArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.DoubleArrayFS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.FloatArrayFS;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.cas.LongArrayFS;
import org.apache.uima.cas.ShortArrayFS;
import org.apache.uima.cas.SofaFS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.Type;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Wetzler
 *
 */
public class ViewCopier {

	public ViewCopier(CAS sourceView, CAS targetView) {
		this.sourceView = sourceView;
		this.targetView = targetView;
	}

	public FeatureStructure copyFS(FeatureStructure source) {
		assert(source.getCAS() == sourceView);
		
		if( copiedFSs.containsKey(source) )
			return copiedFSs.get(source);

		if( source instanceof SofaFS ) {
			return targetView.getSofa();
		}

		Type type = source.getType();

		FeatureStructure copy;
		if( type.isArray() ) {
			copy = copyFSArray(source);
			copiedFSs.put(source, copy);
		} else {
			copy = targetView.createFS(type);
			copiedFSs.put(source, copy);
			copyFeatures(source, copy);
		}

		return copy;
	}

	private FeatureStructure copyFSArray(FeatureStructure source) {
		if (source instanceof StringArrayFS) {
			StringArrayFS array = (StringArrayFS) source;
			int len = array.size();
			StringArrayFS copy = targetView.createStringArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else if (source instanceof IntArrayFS) {
			IntArrayFS array = (IntArrayFS) source;
			int len = array.size();
			IntArrayFS copy = targetView.createIntArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else if (source instanceof ByteArrayFS) {
			ByteArrayFS array = (ByteArrayFS) source;
			int len = array.size();
			ByteArrayFS copy = targetView.createByteArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else if (source instanceof ShortArrayFS) {
			ShortArrayFS array = (ShortArrayFS) source;
			int len = array.size();
			ShortArrayFS copy = targetView.createShortArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else if (source instanceof LongArrayFS) {
			LongArrayFS array = (LongArrayFS) source;
			int len = array.size();
			LongArrayFS copy = targetView.createLongArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else if (source instanceof FloatArrayFS) {
			FloatArrayFS array = (FloatArrayFS) source;
			int len = array.size();
			FloatArrayFS copy = targetView.createFloatArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else if (source instanceof DoubleArrayFS) {
			DoubleArrayFS array = (DoubleArrayFS) source;
			int len = array.size();
			DoubleArrayFS copy = targetView.createDoubleArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else if (source instanceof BooleanArrayFS) {
			BooleanArrayFS array = (BooleanArrayFS) source;
			int len = array.size();
			BooleanArrayFS copy = targetView.createBooleanArrayFS(len);
			for (int i = 0; i < len; i++) {
				copy.set(i, array.get(i));
			}
			return copy;
		} else {
			ArrayFS array = (ArrayFS) source;
			int len = array.size();
			ArrayFS copy = targetView.createArrayFS(len);
			for (int i = 0; i < len; i++) {
				FeatureStructure sourceElement = array.get(i);
				if( sourceElement != null )
					copy.set(i, copyFS(sourceElement));
			}
			return copy;
		}
	}

	private void copyFeatures(FeatureStructure source, FeatureStructure target) {
		Type type = source.getType();
		assert(target.getType() == type);

		List<?> features = type.getFeatures();
		for( Object o : features ) {
			Feature feature = (Feature) o;

			if( feature.getRange().isPrimitive() ) {
				String s = source.getFeatureValueAsString(feature);
				target.setFeatureValueFromString(feature, s);
			} else {
				FeatureStructure refFS = source.getFeatureValue(feature);
				if( refFS != null ) {
					FeatureStructure cpFS = copyFS(refFS);
					target.setFeatureValue(feature, cpFS);
				}
			}
		}
	}

	private Map<FeatureStructure,FeatureStructure> copiedFSs = new HashMap<FeatureStructure,FeatureStructure>();

	private CAS sourceView;
	private CAS targetView;
}
