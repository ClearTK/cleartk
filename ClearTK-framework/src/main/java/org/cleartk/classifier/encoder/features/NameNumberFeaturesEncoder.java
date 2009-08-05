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
package org.cleartk.classifier.encoder.features;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.FeatureEncoderUtil;
import org.cleartk.util.collection.CompressedStringBidiMap;


/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
*/

public class NameNumberFeaturesEncoder extends FeaturesEncoder_ImplBase<List<NameNumber>, NameNumber> {

	private static final long serialVersionUID = 7508330794260661987L;

	public static final String LOOKUP_FILE_NAME = "names-lookup.txt";

	private CompressedStringBidiMap csbm;

	private boolean compressFeatures;

	public boolean isCompressFeatures() {
		return compressFeatures;
	}

	private boolean allowNewFeatures;

	private boolean sortNameLookup;

	public NameNumberFeaturesEncoder(boolean compressFeatures, boolean sortNameLookup) {
		super();
		this.compressFeatures = compressFeatures;
		this.sortNameLookup = sortNameLookup;
		if (compressFeatures) {
			csbm = new CompressedStringBidiMap();
		}
	}

	@Override
	public List<NameNumber> encodeAll(Iterable<Feature> features) {
		List<NameNumber> returnValues = new ArrayList<NameNumber>();

		for (Feature feature : features) {
			for (NameNumber nameNumber : this.encode(feature)) {
				nameNumber.name = compress(escape(nameNumber.name));
				if (nameNumber.name != null) {
					returnValues.add(nameNumber);
				} 
			}
		}
		return returnValues;
	}

	@Override
	public void allowNewFeatures(boolean flag) {
		this.allowNewFeatures = flag;
	}

	private String escape(String string) {
		return FeatureEncoderUtil.escape(string, new char[] { '=' });
	}

	private String compress(String featureString) {
		if (compressFeatures) {
			if (allowNewFeatures) {
				return csbm.getOrGenerateKey(featureString);
			}
			else {
				return csbm.getKey(featureString);
			}
		}
		return featureString;
	}

	public void writeNameLookup(Writer writer) throws IOException {
		if (compressFeatures) {
			csbm.write(writer, sortNameLookup);
		}
	}
}
