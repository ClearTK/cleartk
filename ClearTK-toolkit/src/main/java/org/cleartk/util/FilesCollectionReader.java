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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.CleartkComponents;
import org.cleartk.ViewNames;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.InitializeUtil;
import org.uimafit.util.io.Files;

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
@SofaCapability(outputSofas=ViewNames.URI)
public class FilesCollectionReader extends CollectionReader_ImplBase {

	public static CollectionReader getCollectionReader(String fileOrDir)
	throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION, PARAM_ROOT_FILE, fileOrDir);
	}

	public static CollectionReader getCollectionReaderWithView(
			String dir, String viewName) throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION,
				PARAM_ROOT_FILE, dir, PARAM_VIEW_NAME, viewName);
	}

	public static CollectionReader getCollectionReaderWithPatterns(
			String dir, String viewName, String... patterns) throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION,
				PARAM_ROOT_FILE, dir, PARAM_VIEW_NAME, viewName, PARAM_PATTERNS, patterns);
	}

	public static CollectionReader getCollectionReaderWithSuffixes(
			String dir, String viewName, String... suffixes) throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION,
				PARAM_ROOT_FILE, dir, PARAM_VIEW_NAME, viewName, PARAM_SUFFIXES, suffixes);
	}

	public static final String PARAM_ROOT_FILE = ConfigurationParameterFactory.createConfigurationParameterName(
			FilesCollectionReader.class, "rootFile");
	@ConfigurationParameter(mandatory = true, description = "takes either the name of a single file or the root directory containing all the files to be processed.")
	protected File rootFile;

	public static final String PARAM_VIEW_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
			FilesCollectionReader.class, "viewName");
	@ConfigurationParameter(description = "takes the the name that should be given to the JCas view that the document texts should be set to.")
	private String viewName;

	public static final String PARAM_LANGUAGE = ConfigurationParameterFactory.createConfigurationParameterName(
			FilesCollectionReader.class, "language");
	@ConfigurationParameter(description = "takes the language code corresponding to the language of the documents being examined.  The value of this parameter "
			+ "is simply passed on to JCas.setDocumentLanguage(String).")
	private String language;

	public static final String PARAM_ENCODING = ConfigurationParameterFactory.createConfigurationParameterName(
			FilesCollectionReader.class, "encoding");
	@ConfigurationParameter(description = "takes the encoding of the text files (e.g. \"UTF-8\").  See javadoc for java.nio.charset.Charset for a list of encoding names.")
	private String encoding;

	public static final String PARAM_SUFFIXES = ConfigurationParameterFactory.createConfigurationParameterName(
			FilesCollectionReader.class, "suffixes");
	@ConfigurationParameter(description = "takes suffixes (e.g. .txt) of the files that should be read in.  This parameter can only be set if there"
			+ " is no value for 'nameFilesFileNames', 'fileNames', or 'patterns'.")
	private String[] suffixes;

	public static final String PARAM_PATTERNS = ConfigurationParameterFactory.createConfigurationParameterName(
			FilesCollectionReader.class, "patterns");
	@ConfigurationParameter(description = "	takes regular expressions for matching the files that should be read in. Note that these will be searched for"
			+ " using java.util. regex.Matcher.find, so if you want to make sure the entire file name matches a pattern, you should start the string with ^ and end the"
			+ " string with $. This parameter can only be set if there is no value for 'nameFilesFileNames', 'fileNames', or 'suffixes'.")
	private String[] patterns;

	public static final String PARAM_NAME_FILES_FILE_NAMES = ConfigurationParameterFactory
			.createConfigurationParameterName(FilesCollectionReader.class, "nameFilesFileNames");
	@ConfigurationParameter(description = "names files which contain lists of file names. For example, if the value 'mydata/mylist.txt' is provided, "
			+ "then the file 'mylist.txt' should contain a line delimited list of file names.  The file names in the list should not have directory information "
			+ "but should just be the names of the files. The directory is determined by 'rootFile' and the files that are processed result from "
			+ "traversing the directory structure provided and looking for files with a name found in the lists of file names. That is, no exception will be "
			+ "thrown if a file name in the list does not actually correspond to a file.  This parameter can only be set if there is no value for 'suffixes', 'patterns', or 'fileNames'.")
	private String[] nameFilesFileNames;

	public static final String PARAM_FILE_NAMES = ConfigurationParameterFactory.createConfigurationParameterName(
			FilesCollectionReader.class, "fileNames");
	@ConfigurationParameter(description = "provides a list of file names that should be read in. The directory of the file names is determined by "
			+ "'rootFile' and the files that are processed result from traversing the directory structure provided and looking for files with a name found in the list of file names. "
			+ "That is, no exception will be thrown if a file name in the list does not actually correspond to a file.  This parameter can only be set if there is no value for 'suffixes', "
			+ "'patterns', or 'nameFilesFileNames'.")
	private String[] fileNames;

	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();
		InitializeUtil.initialize(this, getUimaContext());

		// raise an exception if the root file does not exist
		if (!this.rootFile.exists()) {
			String format = "file or directory %s does not exist";
			String message = String.format(format, rootFile.getPath());
			throw new ResourceInitializationException(new IOException(message));
		}

		if (!(suffixes != null ^ patterns != null ^ nameFilesFileNames != null ^ fileNames != null)
				&& (suffixes != null || patterns != null || nameFilesFileNames != null || fileNames != null)) {
			String message = String.format(
					"One of the parameters %1$s, %2$s, %3$s or %4$s may be set but not more than one of them.",
					PARAM_SUFFIXES, PARAM_PATTERNS, PARAM_NAME_FILES_FILE_NAMES, PARAM_FILE_NAMES);
			throw new ResourceInitializationException(new IllegalArgumentException(message));
		}

		if (suffixes != null) {
			files = Files.getFiles(rootFile, suffixes).iterator();
			filesCount = countFiles(Files.getFiles(rootFile, suffixes).iterator());
		}
		else if (patterns != null) {
			FileFilter patternFilter = Files.createPatternFilter(patterns);
			files = Files.getFiles(rootFile, patternFilter).iterator();
			filesCount = countFiles(Files.getFiles(rootFile, patternFilter).iterator());
		}
		else if (nameFilesFileNames != null) {
			Set<String> fileNamesFromLists = new HashSet<String>();
			try {
				for (String fileNamesList : nameFilesFileNames) {
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

		// set the document URI
		ViewURIUtil.setURI(cas, file.toURI().toString());

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

	protected Iterator<File> files;

	protected int completed = 0;

	protected int filesCount = 0;

	public void close() throws IOException {
	}
}
