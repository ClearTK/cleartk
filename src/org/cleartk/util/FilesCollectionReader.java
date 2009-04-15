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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.io.Files;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
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
public class FilesCollectionReader extends CollectionReader_ImplBase {

	/**
	 * "org.cleartk.util.PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY" is a
	 * single, required, string parameter that takes either the name of a single
	 * file or the root directory containing all the files to be processed.
	 */
	public static final String PARAM_FILE_OR_DIRECTORY = "org.cleartk.util.PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY";

	/**
	 * "org.cleartk.util.PlainTextCollectionReader.PARAM_VIEW_NAME" is a single,
	 * optional, string parameter that takes the the name that should be given
	 * to the JCas view associated with the document texts.
	 * 
	 * @see UIMAUtil#createJCasView(CAS, String)
	 */
	public static final String PARAM_VIEW_NAME = "org.cleartk.util.PlainTextCollectionReader.PARAM_VIEW_NAME";

	/**
	 * "org.cleartk.util.PlainTextCollectionReader.PARAM_LANGUAGE" is a single,
	 * optional, string parameter that takes the language code corresponding to
	 * the language of the documents being examined. The value of this parameter
	 * is simply passed on to JCas.setDocumentLanguage(String).
	 * 
	 * @see JCas#setDocumentLanguage(String)
	 */
	public static final String PARAM_LANGUAGE = "org.cleartk.util.PlainTextCollectionReader.PARAM_LANGUAGE";

	/**
	 * "org.cleartk.util.PlainTextCollectionReader.PARAM_ENCODING" is a single,
	 * optional, string parameter that takes the encoding of the text files
	 * (e.g. "UTF-8").
	 * 
	 * @see Charset for a list of encoding names.
	 */
	public static final String PARAM_ENCODING = "org.cleartk.util.PlainTextCollectionReader.PARAM_ENCODING";

	/**
	 * "org.cleartk.util.PlainTextCollectionReader.PARAM_SUFFIXES" is a
	 * multiple, optional, string parameter that takes suffixes (e.g. .txt) of
	 * the files that should be read in. This parameter can only be set if there
	 * is no value for PARAM_FILE_NAMES_FILES or PARAM_FILE_NAMES.
	 */
	public static final String PARAM_SUFFIXES = "org.cleartk.util.PlainTextCollectionReader.PARAM_SUFFIXES";

	/**
	 * "org.cleartk.util.PlainTextCollectionReader.PARAM_FILE_NAMES_FILES" is a
	 * multiple, optional, string parameter that names files which contain lists
	 * of file names. For example, if the value "mydata/mylist.txt" is provided,
	 * then the file "mylist.txt" should contain a line delimited list of file
	 * names. The file names in the list should not have directory information
	 * but should just be the names of the files. The directory is determined by
	 * PARAM_FILE_OR_DIRECTORY and the files that are processed result from
	 * traversing the directory structure provided and looking for files with a
	 * name found in the lists of file names. That is, no exception will be
	 * thrown if a file name in the list does not actually correspond to a file.
	 * This parameter can only be set if there is no value for PARAM_SUFFIXES or
	 * PARAM_FILE_NAMES.
	 */
	public static final String PARAM_FILE_NAMES_FILES = "org.cleartk.util.PlainTextCollectionReader.PARAM_FILE_NAMES_FILES";

	/**
	 * "org.cleartk.util.PlainTextCollectionReader.PARAM_FILE_NAMES" is a
	 * multiple, optional, string parameter that provides a list of file names
	 * that should be read in. The directory of the file names is determined by
	 * PARAM_FILE_OR_DIRECTORY and the files that are processed result from
	 * traversing the directory structure provided and looking for files with a
	 * name found in the list of file names. That is, no exception will be
	 * thrown if a file name in the list does not actually correspond to a file.
	 * This parameter can only be set if there is no value for PARAM_SUFFIXES or
	 * PARAM_FILE_NAMES_FILES.
	 */
	public static final String PARAM_FILE_NAMES = "org.cleartk.util.PlainTextCollectionReader.PARAM_FILE_NAMES";

	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();

		// get the name of the CAS view to be added
		this.viewName = (String) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(),
				FilesCollectionReader.PARAM_VIEW_NAME, null);

		// get the language
		this.language = (String) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(),
				FilesCollectionReader.PARAM_LANGUAGE, null);

		// get the encoding
		this.encoding = (String) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(),
				FilesCollectionReader.PARAM_ENCODING, null);

		// get the input directory
		String fileName = (String) UIMAUtil.getRequiredConfigParameterValue(this.getUimaContext(),
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY);

		this.rootFile = new File(fileName);

		// raise an exception if the root file does not exist
		if (!this.rootFile.exists()) {
			String format = "file or directory %s does not exist";
			String message = String.format(format, fileName);
			throw new ResourceInitializationException(new IOException(message));
		}

		String[] suffixNames = (String[]) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_SUFFIXES,
				null);
		String[] fileNamesLists = (String[]) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(),
				PARAM_FILE_NAMES_FILES, null);
		String[] fileNames = (String[]) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_FILE_NAMES,
				null);

		if (!(suffixNames != null ^ fileNamesLists != null ^ fileNames != null)
				&& (suffixNames != null || fileNamesLists != null || fileNames != null)) {
			String message = String.format(
					"One of the parameters %1$s, %2$s, or %3$s may be set but not more than one of them.",
					PARAM_SUFFIXES, PARAM_FILE_NAMES_FILES, PARAM_FILE_NAMES);
			throw new ResourceInitializationException(new IllegalArgumentException(message));
		}

		if (suffixNames != null) {
			files = Files.getFiles(rootFile, suffixNames).iterator();
			filesCount = countFiles(Files.getFiles(rootFile, suffixNames).iterator());
		}
		else if (fileNamesLists != null) {
			Set<String> fileNamesFromLists = new HashSet<String>();
			try {
				for (String fileNamesList : fileNamesLists) {
					fileNamesFromLists.addAll(Arrays.asList(FileUtil.loadListOfStrings(new File(fileNamesList))));
				}
				files = Files.getFiles(rootFile, fileNamesFromLists).iterator();
				filesCount = countFiles(Files.getFiles(rootFile, fileNamesFromLists).iterator());
			}
			catch (IOException ioe) {
				throw new ResourceInitializationException(ioe);
			}
		}
		else if (fileNames != null) {
			files = Files.getFiles(rootFile, new HashSet<String>(Arrays.asList(fileNames))).iterator();
		}
		else {
			files = Files.getFiles(rootFile).iterator();
			filesCount = countFiles(Files.getFiles(rootFile).iterator());
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
		}
		else {
			path = file.getPath();
		}

		// set the document URI
		ViewURIUtil.setURI(cas, path);

		completed++;
	}

	protected int countFiles(Iterator<File> tempFiles) {
		int count = 0;
		while (tempFiles.hasNext()) {
			tempFiles.next();
			count++;
		}
		return count;
	}

	public Progress[] getProgress() {
		Progress progress = new ProgressImpl(completed, filesCount, Progress.ENTITIES);
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

	protected int filesCount = 0;

	public void close() throws IOException {
	}
}
