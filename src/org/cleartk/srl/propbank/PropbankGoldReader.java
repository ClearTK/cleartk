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
package org.cleartk.srl.propbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.ViewNames;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.srl.propbank.util.Propbank;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.ListSpecification;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * <p>
 * PropbankCollectionReader reads all <tt>.mrg</tt> files of the WSJ part of
 * Treebank in lexical order, then reads the corresponding Propbank entries, and
 * populates the "TreebankView" and "PropbankView" SOFAs.
 * </p>
 * 
 * @author Philip Ogren, Philipp Wetzler
 */
public class PropbankGoldReader extends CollectionReader_ImplBase {
	/**
	 * The descriptor file should have a parameter called 'PropbankCorpusFile'
	 * that points to propbank data (e.g. propbank-1.0/props.txt).
	 */
	public static final String PARAM_PROPBANK_CORPUS_FILE = "PropbankCorpusFile";

	public static final String PARAM_TREEBANK_CORPUS_DIRECTORY = "TreebankCorpusDirectory";

	/**
	 * This determines which sections of WSJ will be used. The format allows for
	 * comma-separated section numbers and section ranges, for example
	 * "02,07-12,16".
	 */
	public static final String PARAM_WSJ_SECTIONS = "WSJSections";

	/**
	 * holds all of the propbank data from props.txt. One entry per line in the
	 * file
	 */
	protected LinkedList<String> propbankData;

	protected File treebankDirectory;

	protected LinkedList<File> treebankFiles;

	protected int totalTreebankFiles = 0;

	protected ListSpecification wsjSections;

	public void initialize() throws ResourceInitializationException {
		try {
			this.wsjSections = new ListSpecification(
					(String) getConfigParameterValue(PARAM_WSJ_SECTIONS));

			File propbankFile = new File(
					(String) getConfigParameterValue(PARAM_PROPBANK_CORPUS_FILE));
			BufferedReader reader = new BufferedReader(new FileReader(
					propbankFile));
			propbankData = new LinkedList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				propbankData.add(line);
			}
			Collections.sort(propbankData);

			this.treebankFiles = new LinkedList<File>();
			treebankDirectory = new File(
					((String) getConfigParameterValue(PARAM_TREEBANK_CORPUS_DIRECTORY))
							.trim());
			File wsjDirectory = new File(treebankDirectory.getPath(), "wsj");
			PennTreebankReader.collectSections(wsjDirectory,
					this.treebankFiles, this.wsjSections);
			Collections.sort(treebankFiles);
			this.totalTreebankFiles = treebankFiles.size();
			
			super.initialize();
		} catch (FileNotFoundException fnfe) {
			throw new ResourceInitializationException(fnfe);
		} catch (IOException ioe) {
			throw new ResourceInitializationException(ioe);
		}
	}

	/**
	 * Reads the next file and stores its text in <b>cas</b> as the
	 * "TreebankView" SOFA. Then stores the corresponding Propbank entries in
	 * the "PropbankView" SOFA.
	 * 
	 * @param cas
	 * 
	 * @throws IOException
	 * @throws CollectionException
	 */
	public void getNext(CAS cas) throws IOException, CollectionException {
		try {
			JCas tbView = cas.createView(ViewNames.TREEBANK).getJCas();
			JCas pbView = cas.createView(ViewNames.PROPBANK).getJCas();

			File treebankFile = treebankFiles.removeFirst().getCanonicalFile();
			tbView.setSofaDataURI(treebankFile.toURI().toASCIIString(),
					"text/plain");

			DocumentUtil.createDocument(tbView, treebankFile.getName(), treebankFile.getName());

			StringBuffer propbankText = new StringBuffer();
			while (propbankData.size() > 0) {
				File nextPbFile = new File(treebankDirectory.getPath()
						+ File.separator
						+ Propbank.filenameFromString(propbankData.getFirst()))
						.getCanonicalFile();

				int c = treebankFile.compareTo(nextPbFile);
				if (c < 0) {
					break;
				} else if (c > 0) {
					propbankData.removeFirst();
					continue;
				}

				propbankText.append(propbankData.removeFirst() + "\n");
			}

			pbView.setSofaDataString(propbankText.toString(), "text/plain");
		} catch (CASException ce) {
			throw new CollectionException(ce);
		}
	}

	public void close() throws IOException {
	}

	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(totalTreebankFiles
				- treebankFiles.size(), totalTreebankFiles, Progress.ENTITIES) };
	}

	public boolean hasNext() throws IOException, CollectionException {
		if (treebankFiles.size() > 0)
			return true;
		else
			return false;
	}

}
