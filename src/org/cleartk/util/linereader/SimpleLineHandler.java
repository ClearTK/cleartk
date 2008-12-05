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
package org.cleartk.util.linereader;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.UIMAUtil;
import org.cleartk.util.io.Files;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * <p>
*/
public class SimpleLineHandler implements LineHandler {

	/**
	 * "Delimiter" is a single, optional, string parameter that specifies a
	 * string that delimits the id from the text. If no value is given, then a
	 * vertical bar '|' is expected.
	 */
	public static final String PARAM_DELIMITER = "Delimiter";

	private String delimiter;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		delimiter = (String) UIMAUtil.getDefaultingConfigParameterValue(context, PARAM_DELIMITER, "|");
	}

	public void handleLine(JCas jCas, File rootFile, File file, String line)  throws IOException, CollectionException{
		String id = line.substring(0, line.indexOf(delimiter));
		String text = line.substring(line.indexOf(delimiter) + 1);
		jCas.setSofaDataString(text, "text/plain");

		String path;
		if (rootFile.isDirectory()) {
			path = Files.stripRootDir(rootFile, file);
		} else {
			path = file.getPath();
		}
		DocumentUtil.createDocument(jCas, id, path);
	}

}
