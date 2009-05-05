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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.UIMAUtil;
import org.cleartk.util.io.Files;


/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * LineReader is collection reader for cases when you want to read in files
 * line-by-line such that there is one JCas per line.
 * 
 * <p>
 * This class has no relation to LineWriter - i.e. LineReader does not provide
 * "reverse functionality" of LineWriter.
 * 
 * <p>
 * This class is very similar to PlainTextCollectionReader in that it allows you
 * to specify a file or directory from which to read in plain text into a named
 * view with a specified language and encoding. However, instead of reading in
 * entire files as plain text, this collection reader reads in a file
 * line-by-line where each line gets its own JCas.
 * <p>
 * LineReader uses an interface LineHandler which determines how lines from a
 * file are used to initialize a JCas. The default implementation,
 * DefaultLineHandler, simply expects each line to be plain text and the id of
 * the document will be the number of lines read up to that point (across all
 * files that are being read in.) A second implementation, SimpleLineHandler,
 * assumes that an id for each line is provided in the text of the line and
 * parses it out.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public class LineReader extends CollectionReader_ImplBase {

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_FILE_OR_DIRECTORY"
	 * is a single, required, string parameter that takes either the name of a
	 * single file or the root directory containing all the files to be processed.
	 */
	public static final String PARAM_FILE_OR_DIRECTORY = "org.cleartk.util.linereader.LineReader.PARAM_FILE_OR_DIRECTORY";

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_VIEW_NAME"
	 * is a single, optional, string parameter that takes the the name that should
	 * be given to the JCas view associated with the document texts.
	 * 
	 * @see UIMAUtil#createJCasView(CAS, String)
	 */
	public static final String PARAM_VIEW_NAME = "org.cleartk.util.linereader.LineReader.PARAM_VIEW_NAME";

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_LANGUAGE"
	 * is a single, optional, string parameter that takes the
	 * language code corresponding to the language of the documents being
	 * examined. The value of this parameter is simply passed on to
	 * JCas.setDocumentLanguage(String).
	 * 
	 * @see JCas#setDocumentLanguage(String)
	 */
	public static final String PARAM_LANGUAGE = "org.cleartk.util.linereader.LineReader.PARAM_LANGUAGE";

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_ENCODING"
	 * is a single, optional, string parameter that takes the
	 * encoding of the text files (e.g. "UTF-8").
	 * 
	 * @see Charset for a list of encoding names.
	 */
	public static final String PARAM_ENCODING = "org.cleartk.util.linereader.LineReader.PARAM_ENCODING";

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_SUFFIXES"
	 * is a multiple, optional, string parameter that takes suffixes
	 * (e.g. .txt) of the files that should be read in.
	 */
	public static final String PARAM_SUFFIXES = "org.cleartk.util.linereader.LineReader.PARAM_SUFFIXES";

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_LINE_HANDLER"
	 * is a single, optional, string parameter that specifies the
	 * class name of the LineHandler. If one is not specified, then the
	 * SimpleLineHandler will be used.
	 * 
	 * @see SimpleLineHandler
	 * 
	 */
	public static final String PARAM_LINE_HANDLER = "org.cleartk.util.linereader.LineReader.PARAM_LINE_HANDLER";

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_COMMENT_SPECIFIER"
	 * is a multiple, optional, string parameter that
	 * specifies lines that should be considered "comments" - i.e. lines that
	 * should be skipped. Commented lines are those the start with one of the
	 * values of this parameter.
	 */
	public static final String PARAM_COMMENT_SPECIFIER = "org.cleartk.util.linereader.LineReader.PARAM_COMMENT_SPECIFIER";

	/**
	 * "org.cleartk.util.linereader.LineReader.PARAM_SKIP_BLANK_LINES"
	 * is a single, optional, boolean parameter that specifies
	 * whether blank lines should be skipped or not. The default value is true
	 * if no value is given. If this parameter is set to false, then blank lines
	 * that appear in the text files will be read in and given their own JCas.
	 * Blank lines are those that consist of only whitespace.
	 */
	public static final String PARAM_SKIP_BLANK_LINES = "org.cleartk.util.linereader.LineReader.PARAM_SKIP_BLANK_LINES";
	
	File file;

	int lineNumber;

	String line;

	BufferedReader input;

	LineHandler lineHandler;

	String[] commentSpecifiers;

	boolean skipBlankLines;
	@Override
	public void initialize() throws ResourceInitializationException {
		try {
			// get the name of the CAS view to be added
			this.viewName = (String) this.getConfigParameterValue(LineReader.PARAM_VIEW_NAME);

			// get the language
			this.language = (String) this.getConfigParameterValue(LineReader.PARAM_LANGUAGE);

			// get the encoding
			this.encoding = (String) this.getConfigParameterValue(LineReader.PARAM_ENCODING);

			// get the input directory
			String fileName = (String) UIMAUtil.getRequiredConfigParameterValue(this.getUimaContext(),
					LineReader.PARAM_FILE_OR_DIRECTORY);

			this.rootFile = new File(fileName);

			// raise an exception if the root file does not exist
			if (!this.rootFile.exists()) {
				String format = "file or directory %s does not exist";
				String message = String.format(format, fileName);
				throw new ResourceInitializationException(new IOException(message));
			}

			String[] suffixNames = (String[]) this.getConfigParameterValue(PARAM_SUFFIXES);
			if (suffixNames != null && suffixNames.length > 0) {
				files = Files.getFiles(rootFile, suffixNames).iterator();
			}
			else {
				files = Files.getFiles(rootFile).iterator();
			}

			commentSpecifiers = (String[]) this.getConfigParameterValue(PARAM_COMMENT_SPECIFIER);
			if (commentSpecifiers == null) {
				commentSpecifiers = new String[0];
			}

			String lineHandlerClassName = (String) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(),
					PARAM_LINE_HANDLER, "org.cleartk.util.linereader.DefaultLineHandler");
			Class<?> cls = Class.forName(lineHandlerClassName);
			Class<? extends LineHandler> lineHandlerClass = cls.asSubclass(LineHandler.class);
			lineHandler = lineHandlerClass.newInstance();
			lineHandler.initialize(getUimaContext());

			skipBlankLines = (Boolean) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_SKIP_BLANK_LINES, true);
			
			moveToNextFile();
		}
		catch (Exception fnfe) {
			throw new ResourceInitializationException(fnfe);
		}
	}

	public void getNext(CAS cas) throws IOException, CollectionException {
		hasNext();

		JCas view = UIMAUtil.createJCasView(cas, this.viewName);

		lineHandler.handleLine(view, rootFile, file, line);

		// set language if it was specified
		if (this.language != null) {
			view.setDocumentLanguage(this.language);
		}

		completed++;
		line = null;
	}

	private boolean moveToNextFile() throws FileNotFoundException, UnsupportedEncodingException {
		if (files.hasNext()) {
			file = files.next();
			if(encoding != null)
				input = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
			else
				input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			lineNumber = 0;
			return true;
		}
		return false;
	}

	public Progress[] getProgress() {
		Progress progress = new ProgressImpl(completed, 1000000, Progress.ENTITIES);
		return new Progress[] { progress };
	}

	public boolean hasNext() throws IOException, CollectionException {
		if (line == null) {
			line = input.readLine();
			if(line != null) {
				for (String commentSpecifier : commentSpecifiers) {
					if (line.startsWith(commentSpecifier)) {
						line = null;
						return hasNext();
					}
				}
				if(skipBlankLines && line.trim().equals("")) {
					line = null;
					return hasNext();
				}
			}
		}

		if (line == null) {
			if (moveToNextFile()) return hasNext();
			else return false;
		}
		return true;
	}

	private File rootFile;

	private String viewName;

	private String language;

	private String encoding;

	private Iterator<File> files;

	private int completed = 0;

	public void close() throws IOException {
	}
}
