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
package org.cleartk.corpus.ace2005;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.ViewNames;
import org.cleartk.util.ViewURIUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.ConfigurationParameterFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */

@SofaCapability(outputSofas= {ViewNames.ACE_APF_URI, ViewNames.URI})
public class Ace2005GoldReader extends CollectionReader_ImplBase
{
	public static final String PARAM_ACE_DIRECTORY_NAME = ConfigurationParameterFactory.createConfigurationParameterName(Ace2005GoldReader.class, "aceDirectoryName");

	@ConfigurationParameter(
			mandatory = true,
			description = "Takes the name of directory that contains ACE data.  Typically, a folder such as \".../ACE_2005/optimization/English/all\".  The folder should contain files that come in pairs - i.e. for each .sgm file there should be a corresponding .apf.xml file.")
	private String aceDirectoryName;

	private static final String PARAM_ACE_FILE_NAMES_DESCRIPTION = "takes a file that contains the names of the files to read.   \n" +
			"The file should contain a list of the files in AceCorpusDir (one file name per line) \n" +
			"that you want read in. File names should not include the last suffix(es) (e.g. \".sgm\" or \"apf.xml\") \n" +
			"If parameter value is not given, then all files will be read in. An example file might look like this: \n\n" +
			"AFP_ENG_20030304.0250\n" +
			"AFP_ENG_20030305.0918\n" +
			"...\n";

	public static final String PARAM_ACE_FILE_NAMES_FILE = ConfigurationParameterFactory.createConfigurationParameterName(Ace2005GoldReader.class, "aceFileNamesFile");
	
	@ConfigurationParameter(
			description = PARAM_ACE_FILE_NAMES_DESCRIPTION) 
	private String aceFileNamesFile;
	
	File[] aceFiles;
	int aceFileIndex;
	int aceFileCount;
	File currentSGMFile = null;
	
	public static final String TAG_REGEX = "<.*?>";
	Pattern tagPattern;
	
	public void initialize() throws ResourceInitializationException	{
		ConfigurationParameterInitializer.initialize(this, getUimaContext());
		
		if (!new File(aceDirectoryName).exists()) {
			throw new ResourceInitializationException(new IOException(String.format(
					"directory %s does not exist", aceDirectoryName)));
		}
		File aceDirectory = new File(aceDirectoryName);

		if(aceFileNamesFile != null && !aceFileNamesFile.trim().equals("")) {
			try {
				List<File> files = new ArrayList<File>();
				BufferedReader reader = new BufferedReader(new FileReader(aceFileNamesFile));
				String line;
				while((line = reader.readLine()) != null) {
					line = line.trim();
					if(line.endsWith(".sgm"))
						files.add(new File(aceDirectory, line));
					else
						files.add(new File(aceDirectory, line+".sgm"));
				}
				aceFiles = files.toArray(new File[files.size()]);
			}catch(IOException ioe) {
				throw new ResourceInitializationException(ioe);
			}
			for(File file : aceFiles) {
				if(!file.exists())
					throw new ResourceInitializationException(ResourceInitializationException.COULD_NOT_ACCESS_DATA, new Object[] {file});
			}
		}
		else {
			aceFiles = aceDirectory.listFiles();
		}
		aceFileIndex = 0;
		aceFileCount = 0;
		
		tagPattern = Pattern.compile(TAG_REGEX, Pattern.MULTILINE | Pattern.DOTALL);
	}
	
	private File getNextSGMFile() {
		if(currentSGMFile != null)
			return currentSGMFile;
		while(aceFileIndex < aceFiles.length) {
			File sgmFile = aceFiles[aceFileIndex++];
			if(sgmFile.getName().endsWith(".sgm")) {
				currentSGMFile = sgmFile;
				return sgmFile;
			}
		}
		return null;
	}
	
	private File getAPFFile(File sgmFile)
	{
		String apfFileName = sgmFile.getPath();
		apfFileName = sgmFile.getPath().substring(0, apfFileName.length()-3)+"apf.xml";
		if(new File(apfFileName).exists())
			return new File(apfFileName);
		
		apfFileName = sgmFile.getPath();
		apfFileName = sgmFile.getPath().substring(0, apfFileName.length()-3)+"entities.apf.xml";
		if(new File(apfFileName).exists())
			return new File(apfFileName);
		
		apfFileName = sgmFile.getPath();
		apfFileName = sgmFile.getPath().substring(0, apfFileName.length()-3)+"mentions.apf.xml";
		if(new File(apfFileName).exists())
			return new File(apfFileName);
		
		return null;
	}
	
	private String getDocumentText(String sgmText) throws IOException
	{
		StringBuffer rawDocumentText = new StringBuffer(sgmText);
		Matcher tagMatcher = tagPattern.matcher(rawDocumentText);
		String documentText = tagMatcher.replaceAll("");
		return documentText;
	}
	
	// make note about moving local dtd file into directory
	public void getNext(CAS cas) throws IOException, CollectionException
	{
		try
		{
			JCas jCas = cas.getJCas(); 
			
			//we need the next sgm file which will typically be 'currentSGMFile' - but we
			//will call getNextSGMFile() to be safe
			File sgmFile = getNextSGMFile();
			//setting currentSGMFile to null tells getNextSGMFile to get the next sgm file
			//rather than simply returning the current value.
			currentSGMFile = null;
			
			String sgmText = FileUtils.file2String(sgmFile);
			
			JCas initialView = jCas.getView(ViewNames.DEFAULT);
			initialView.setDocumentText(getDocumentText(sgmText));

//			org.cleartk.type.Document sgmDocument = new org.cleartk.type.Document(initialView);
//		    sgmDocument.setIdentifier(sgmFile.getName());
//		    sgmDocument.setPath(sgmFile.getName());
//		    sgmDocument.addToIndexes();

			File apfFile = getAPFFile(sgmFile);
			
			SAXBuilder builder = new SAXBuilder();
			builder.setDTDHandler(null);
			Document doc = builder.build(apfFile);
			
			Element apfSource = doc.getRootElement();
			String uri = apfSource.getAttributeValue("URI");
			String source = apfSource.getAttributeValue("SOURCE");
			String type = apfSource.getAttributeValue("TYPE");
			
			ViewURIUtil.setURI(cas, sgmFile.getName());
			org.cleartk.corpus.ace2005.type.Document document = new org.cleartk.corpus.ace2005.type.Document(initialView);
			document.setAceUri(uri);
			document.setAceSource(source);
			document.setAceType(type);
			document.addToIndexes();
		    
			CAS apfUriView = cas.createView(ViewNames.ACE_APF_URI);
			apfUriView.setSofaDataURI(apfFile.toURI().toString(), null);

		}
		catch(CASException ce)
		{
			throw new CollectionException(ce);
		}
		catch(JDOMException je)
		{
			throw new CollectionException(je);
		}
	}

	public void close() throws IOException
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Progress is measured by the number of files in the target directory - not by the number
	 * of times getNext has been (and will be) called.  This means that the total number of 
	 * entities to completion is typically going to be 2 or 4 times as many 'documents' that 
	 * are found depending on what kinds of files exist in the target directory 
	 * (e.g. *.ag.xml, *.apf.xml, *.sgm, *.tab)
	 */
	public Progress[] getProgress()
	{
	    return new Progress[] { new ProgressImpl(aceFileIndex, aceFiles.length, Progress.ENTITIES) };
	}

	public boolean hasNext() throws IOException, CollectionException
	{
		return getNextSGMFile() != null;
	}

	public void setAceDirectoryName(String aceDirectoryName) {
		this.aceDirectoryName = aceDirectoryName;
	}

	public void setAceFileNamesFile(String aceFileNamesFile) {
		this.aceFileNamesFile = aceFileNamesFile;
	}


}





