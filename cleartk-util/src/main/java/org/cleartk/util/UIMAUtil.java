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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * @author Philipp Wetzler
 * @author Steven Bethard
 * 
 */
public class UIMAUtil {
	public static FSArray toFSArray(JCas jCas, List<? extends FeatureStructure> fsList) {
		if (fsList == null) {
			return new FSArray(jCas, 0);
		}
		FSArray fsArray = new FSArray(jCas, fsList.size());
		fsArray.copyFromArray(fsList.toArray(new FeatureStructure[fsList.size()]), 0, 0, fsList.size());
		return fsArray;
	}

	public static StringArray toStringArray(JCas jCas, String[] sArray) {
		StringArray uimaSArray = new StringArray(jCas, sArray.length);
		uimaSArray.copyFromArray(sArray, 0, 0, sArray.length);
		return uimaSArray;
	}
	
	public static List<String> toList(StringArray sArray) {
		List<String> result = new ArrayList<String>(sArray.size());
		for( int i=0; i<sArray.size(); i++ ) {
			result.add(sArray.get(i));
		}
		
		return result;
	}

	public static <T extends FeatureStructure> List<T> toList(FSArray fsArray, Class<T> cls) {
		List<T> list = new ArrayList<T>();

		if (fsArray == null) {
			return list;
		}

		for (FeatureStructure fs : fsArray.toArray()) {
			list.add(cls.cast(fs));
		}
		return list;

	}

	public static Type getCasType(JCas jCas, Class<? extends TOP> cls) {
		try {
			return jCas.getCasType(cls.getField("type").getInt(null));
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String readSofa(JCas view) throws IOException {
		InputStream in = view.getSofaDataStream();
		StringBuffer tmp = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			tmp.append(new String(b, 0, n));
		}
		return tmp.toString();
	}


	


}
