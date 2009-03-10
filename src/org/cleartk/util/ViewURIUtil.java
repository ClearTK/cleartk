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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.ViewNames;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 * @author Steven Bethard
 *
 */
public class ViewURIUtil {
	
	/**
	 * Set the primary Uniform Resource Identifier for this CAS and all its views.
	 * This creates the view {@link ViewNames.URI} and assigns the URI there. 
	 * 
	 * @param cas  The CAS object.
	 * @param uri  The primary URI for the CAS and all its views.
	 */
	public static void setURI(CAS cas, String uri) {
		CAS view = cas.createView(ViewNames.URI);
		view.setSofaDataURI(uri, null);
	}
	
	/**
	 * Set the primary Uniform Resource Identifier for this JCas and all its views.
	 * This creates the view {@link ViewNames.URI} and assigns the URI there. 
	 * 
	 * @param jCas  The CAS object.
	 * @param uri   The primary URI for the CAS and all its views.
	 */
	public static void setURI(JCas jCas, String uri) {
		ViewURIUtil.setURI(jCas.getCas(), uri);
	}
	
	/**
	 * Get the primary Uniform Resource Identifier for this JCas and all its views.
	 * This is obtained from the {@link ViewNames.URI} view of the JCas.
	 * 
	 * @param jCas   The JCas object.
	 * @return       The primary URI for the JCas and all its views.
	 * @throws CASException 
	 */
	public static String getURI(JCas jCas) throws AnalysisEngineProcessException {
		try {
			return jCas.getView(ViewNames.URI).getSofaDataURI();
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
