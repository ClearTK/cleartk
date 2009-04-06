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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
*/
public class XReader extends PlainTextCollectionReader {

	/**
	 * "org.cleartk.util.XReader.PARAM_XML_SCHEME" is a single, optional, string parameter that specifies the
	 * UIMA XML serialization scheme that should be used. Valid values for this
	 * parameter are "XMI" (default) and "XCAS".
	 * 
	 * @see XmiCasSerializer
	 * @see XCASSerializer
	 */
	public static final String PARAM_XML_SCHEME = "org.cleartk.util.XReader.PARAM_XML_SCHEME";

	public static final String XMI = "XMI";

	public static final String XCAS = "XCAS";

	private boolean useXMI = true;
	
	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();
		
		String xmlScheme = (String) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_XML_SCHEME, XMI);

		if (xmlScheme.equals(XMI)) useXMI = true;
		else if (xmlScheme.equals(XCAS)) useXMI = false;
		else throw new ResourceInitializationException(String.format(
				"parameter '%1$s' must be either '%2$s' or '%3$s' or left empty.", PARAM_XML_SCHEME, XMI, XCAS), null);

		
	}

	public void getNext(CAS cas) throws IOException, CollectionException {
		File currentFile = this.files.next();
		FileInputStream inputStream = new FileInputStream(currentFile);
		
		try {
			if(useXMI)
				XmiCasDeserializer.deserialize(inputStream, cas);
			else
				XCASDeserializer.deserialize(inputStream, cas);
		}
		catch (SAXException e) {
			throw new CollectionException(e);
		}
		finally {
			inputStream.close();
		}

		completed++;
	}

}
