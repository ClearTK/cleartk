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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.io.Files;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * <p>
 * 
 * A CollectionReader that loads all files in a directory tree.
 * 
 * Files are loaded as plain text and stored in the JCas view selected by the
 * user. ClearTK Document objects are added to the same JCas view to record the
 * file IDs and paths.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public class PlainTextCollectionReader extends CollectionReader_ImplBase {

	/**
	 * "FileOrDirectory" is a single, required, string parameter that takes 
	 *  either the name of a single file or the root
	 * directory containing all the files to be processed.
	 */
	public static final String PARAM_FILE_OR_DIRECTORY = "FileOrDirectory";

	/**
	 * "ViewName" is a single, optional, string parameter that takes the
	 * the name that should be given to the JCas view associated with the document texts.
	 * @see UIMAUtil#createJCasView(CAS, String)
	 */
	public static final String PARAM_VIEW_NAME = "ViewName";

	/**
	 * "Language" is a single, optional, string parameter that takes the
	 * language code corresponding to the language of the documents being
	 * examined. The value of this parameter is simply passed on to
	 * JCas.setDocumentLanguage(String).
	 * 
	 * @see JCas#setDocumentLanguage(String)
	 */
	public static final String PARAM_LANGUAGE = "Language";

	/**
	 * "Encoding" is a single, optional, string parameter that takes the
	 * encoding of the text files (e.g. "UTF-8").
	 * 
	 * @see Charset for a list of encoding names.
	 */
	public static final String PARAM_ENCODING = "Encoding";

	/**
	 * "Suffixes" is a multiple, optional, string parameter that takes suffixes
	 * (e.g. .txt) of the files that should be read in.
	 */
	public static final String PARAM_SUFFIXES = "Suffixes";

	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();

		// get the name of the CAS view to be added
		this.viewName = (String) this.getConfigParameterValue(PlainTextCollectionReader.PARAM_VIEW_NAME);

		// get the language
		this.language = (String) this.getConfigParameterValue(PlainTextCollectionReader.PARAM_LANGUAGE);

		// get the encoding
		this.encoding = (String) this.getConfigParameterValue(PlainTextCollectionReader.PARAM_ENCODING);

		// get the input directory
		String fileName = (String) UIMAUtil.getRequiredConfigParameterValue(this.getUimaContext(),
				PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY);
		
		this.rootFile = new File(fileName);


		// raise an exception if the root file does not exist
		if (!this.rootFile.exists()) {
			String format = "file or directory %s does not exist";
			String message = String.format(format, fileName);
			throw new ResourceInitializationException(new IOException(message));
		}

		String[] suffixNames = (String[]) this.getConfigParameterValue(PARAM_SUFFIXES);
		if(suffixNames != null && suffixNames.length > 0) {
			files = Files.getFiles(rootFile, suffixNames).iterator();
		} else {
			files = Files.getFiles(rootFile).iterator();
		}
	}

	public void getNext(CAS cas) throws IOException, CollectionException {
		// get a JCas object
		JCas view = UIMAUtil.createJCasView(cas, this.viewName);

		// get the next file in the iterator
		File file = this.files.next();

		// set the document's text
		String text = FileUtils.file2String(file, this.encoding);
		view.setSofaDataString(text, "text/plain");

		// set language if it was specified
		if (this.language != null) {
			view.setDocumentLanguage(this.language);
		}

		String path;
		if (this.rootFile.isDirectory()) {
			path = Files.stripRootDir(rootFile, file);
		} else {
			path = file.getPath();
		}
		
		// create the ClearTK Document object
		DocumentUtil.createDocument(view, file.getName(), path);
		
		completed++;
	}


	public Progress[] getProgress() {
		Progress progress = new ProgressImpl(completed, 1000000, Progress.ENTITIES);
		return new Progress[] { progress };
	}

	public boolean hasNext() throws IOException, CollectionException {
		return this.files.hasNext();
	}

	protected File rootFile;

	private String viewName;

	private String language;

	private String encoding;

	protected Iterator<File> files;

	protected int completed = 0; 
	public void close() throws IOException { }
}

