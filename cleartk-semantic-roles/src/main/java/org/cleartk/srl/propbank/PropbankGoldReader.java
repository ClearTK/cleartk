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

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.srl.propbank.util.Propbank;
import org.cleartk.syntax.constituent.ptb.ListSpecification;
import org.cleartk.syntax.constituent.ptb.PennTreebankReader;
import org.cleartk.util.ViewNames;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.ConfigurationParameterFactory;


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

@SofaCapability(outputSofas= {ViewNames.PROPBANK, ViewNames.TREEBANK, ViewNames.URI})
public class PropbankGoldReader extends JCasCollectionReader_ImplBase {
	
	public static final String PARAM_PROPBANK_FILE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(PropbankGoldReader.class, "propbankFileName");

	@ConfigurationParameter(
			description = "points to propbank data file",
			mandatory = true)
	private String propbankFileName;
	
	public static final String PARAM_PENNTREEBANK_DIRECTORY_NAME = ConfigurationParameterFactory.createConfigurationParameterName(PropbankGoldReader.class, "penntreebankDirectoryName");

	private static final String PENN_TREEBANK_DIRECTORY_DESCRIPTION = "points to the PennTreebank corpus. " +
			"The directory should contain subdirectories corresponding to the sections (e.g. \"00\", \"01\", etc.)  " +
			"That is, if a local copy of PennTreebank sits at C:/Data/PTB/wsj/mrg, then the subdirectory C:/Data/PTB/wsj/mrg/00 should exist. " +
			"There are 24 sections in PTB corresponding to the directories 00, 01, 02, ... 24.";
	
	@ConfigurationParameter(
			description = PENN_TREEBANK_DIRECTORY_DESCRIPTION,
			mandatory = true)
	private String penntreebankDirectoryName;
	
	public static final String PARAM_WSJ_SECTIONS = ConfigurationParameterFactory.createConfigurationParameterName(PropbankGoldReader.class, "wsjSections");

	@ConfigurationParameter(
			description = "Determines which sections of WSJ will be used.  The format allows for comma-separated section numbers and section ranges, for example \"02,07-12,16\".",
			mandatory = true)
	private String wsjSections;

	/**
	 * holds all of the propbank data from props.txt. One entry per line in the
	 * file
	 */
	protected LinkedList<String> propbankData;

	protected File treebankDirectory;

	protected LinkedList<File> treebankFiles;

	protected int totalTreebankFiles = 0;

	protected ListSpecification wsjSpecification;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			this.wsjSpecification = new ListSpecification(wsjSections);

			File propbankFile = new File(propbankFileName);
			if(!propbankFile.exists()) {
				throw new ResourceInitializationException(new IllegalArgumentException("could not find file: "+propbankFile.getPath()));
			}
			BufferedReader reader = new BufferedReader(new FileReader(propbankFile));
			propbankData = new LinkedList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				propbankData.add(line);
			}
			Collections.sort(propbankData);

			this.treebankFiles = new LinkedList<File>();
			treebankDirectory = new File(penntreebankDirectoryName);
			//don't forget that the paths in props.txt have "wsj" in the name.
			File wsjDirectory = new File(treebankDirectory, "wsj");
			if(!wsjDirectory.exists()) {
				throw new ResourceInitializationException(new IllegalArgumentException("could not find file: "+treebankDirectory.getPath()+" or this directory does not contain a sub-directory named 'wsj' as expected by propbank data."));
			}
			PennTreebankReader.collectSections(wsjDirectory, this.treebankFiles, this.wsjSpecification);
			Collections.sort(treebankFiles);
			this.totalTreebankFiles = treebankFiles.size();
			
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
	public void getNext(JCas jCas) throws IOException, CollectionException {
		JCas tbView, pbView;
		try {
			tbView = jCas.createView(ViewNames.TREEBANK);
			pbView = jCas.createView(ViewNames.PROPBANK);
		} catch (CASException ce) {
			throw new CollectionException(ce);
		}

		File treebankFile = treebankFiles.removeFirst();
		ViewURIUtil.setURI(jCas, treebankFile.getPath());

		StringBuffer propbankText = new StringBuffer();

		/*
		 * The logic here is rather fragile and should be rewritten and/or unit tested.
		 * I changed the code so that the comparison is between the canonical paths.  (PVO) 
		 */
		while (propbankData.size() > 0) {
			File nextPbFile = new File(treebankDirectory.getPath()
					+ File.separator
					+ Propbank.filenameFromString(propbankData.getFirst()))
					.getCanonicalFile();

			int c = treebankFile.getCanonicalFile().compareTo(nextPbFile);
			if (c < 0) {
				break;
			} else if (c > 0) {
				propbankData.removeFirst();
				continue;
			}

			propbankText.append(propbankData.removeFirst() + "\n");
		}

		tbView.setSofaDataString(FileUtils.file2String(treebankFile), "text/plain");
		pbView.setSofaDataString(propbankText.toString(), "text/plain");
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

	public void setPropbankFileName(String propbankFileName) {
		this.propbankFileName = propbankFileName;
	}

	public void setPenntreebankDirectoryName(String treebankDirectoryName) {
		this.penntreebankDirectoryName = treebankDirectoryName;
	}

	public void setWsjSections(String wsjSections) {
		this.wsjSections = wsjSections;
	}

}
