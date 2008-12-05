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
package org.cleartk.classifier.encoder.features.contextvalue;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.FeatureEncoderUtil;
import org.cleartk.classifier.encoder.features.FeatureEncoder;
import org.cleartk.util.collection.CompressedStringBidiMap;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * For discussion on the name of this class, please see the javadoc for
 * ContextValue.
 * 
 * @author Philip Ogren
 * @see ContextValue
 */
public class ContextValueFeatureEncoder implements FeatureEncoder<ContextValue> {

	private static final long serialVersionUID = -6220304359945707422L;

	private boolean _compressFeatures;

	public boolean isCompressFeatures() {
		return _compressFeatures;
	}

	private boolean _allowNewFeatures;

	private CompressedStringBidiMap csbm;

	public ContextValueFeatureEncoder() {
		this(false, true);
	}

	public ContextValueFeatureEncoder(boolean compressFeatures) {
		this(compressFeatures, true);
	}

	public ContextValueFeatureEncoder(boolean compressFeatures, boolean allowNewFeatures) {
		_compressFeatures = compressFeatures;
		_allowNewFeatures = allowNewFeatures;

		if (_compressFeatures) {
			csbm = new CompressedStringBidiMap();
			((CompressedStringBidiMap) csbm).setSortWriter(true);
		}
	}

	private ContextValue makeContextValue(String context) {
		context = compress(escape(context));
		if (context != null) return new ContextValue(context, 1.0f);
		else return null;
	}

	private List<ContextValue> makeContextValueSingletonList(String context) {
		ContextValue contextValue = makeContextValue(context);
		if(contextValue != null)
			return Collections.singletonList(contextValue);
		return Collections.emptyList();
	}

	private String escape(String string) {
		return FeatureEncoderUtil.escape(string, new char[] { '=' });
	}

	private String compress(String featureString) {
		if (_compressFeatures) {
			if (_allowNewFeatures) return csbm.getOrGenerateKey(featureString);
			else return csbm.getKey(featureString);
		}
		return featureString;
	}

	public void writeKeys(Writer writer) throws IOException {
		if (_compressFeatures) {
			csbm.write(writer);
		}
	}

	public List<ContextValue> encode(Feature feature) throws IllegalArgumentException {
		if (feature == null) return Collections.emptyList();

		String featureName = feature.getName();
		Object featureValue = feature.getValue();

		if ("".equals(featureName)) featureName = null;

		if ("".equals(featureValue)) featureValue = null;

		if (featureName == null && featureValue == null) return Collections.emptyList();

		/*
		 * If featureValue is null then the context will be the featureName and
		 * the value will be 1.0f
		 */
		if (featureValue == null) {
			return makeContextValueSingletonList(featureName);
		}

		/*
		 * If featureName is null, then the context will be the featureValue as
		 * long as it isn't a number.
		 */
		if (featureName == null) {
			if (featureValue instanceof Number) {
				return Collections.emptyList();
			}
			else {
				return makeContextValueSingletonList(featureValue.toString());
			}
		}

		/*
		 * If neither featureName nor featureValue are null, then the context
		 * will start with the featureName. If the featureValue is numeric, then
		 * the context will be the featureName and the value will be the
		 * featureValue. If the featureValue is not numeric, then the context
		 * will be the featureName plus the featureValue and the value will be
		 * 1.
		 */
		String context = feature.getName();

		if (featureValue instanceof Number) {
			ContextValue returnValue = makeContextValue(context);
			if (returnValue != null) {
				float value = ((Number) featureValue).floatValue();
				returnValue.setValue(value);
				return Collections.singletonList(returnValue);
			}
			else
				return Collections.emptyList();
		}
		else {
			return makeContextValueSingletonList(context + "_" + featureValue.toString());
		}
	}

	public boolean encodes(Feature feature) {
		return true;
	}

	public void allowNewFeatures(boolean flag) {
		_allowNewFeatures = flag;
	}
}
