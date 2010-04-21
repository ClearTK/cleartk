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
package org.cleartk.corpus.timeml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasToInlineXml;
import org.cleartk.CleartkComponents;
import org.cleartk.corpus.timeml.type.Event;
import org.cleartk.corpus.timeml.type.TemporalLink;
import org.cleartk.corpus.timeml.type.Time;
import org.cleartk.corpus.timeml.util.TimeMLUtil;
import org.cleartk.util.ViewURIUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.InitializeUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Steven Bethard
 *
 */
public class TimeMLWriter extends JCasAnnotator_ImplBase {
	
	public static final String PARAM_OUTPUT_DIRECTORY_NAME = ConfigurationParameterFactory.createConfigurationParameterName(TimeMLWriter.class, "outputDirectoryName");
	
	@ConfigurationParameter(
			description = "Provides the path where the TimeML documents should be written.",
			mandatory = true)
	private String outputDirectoryName;
	
	public static AnalysisEngineDescription getDescription(String outputDir)
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				TimeMLWriter.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION,
				CleartkComponents.TYPE_PRIORITIES,
				PARAM_OUTPUT_DIRECTORY_NAME, outputDir);
	}

	private File outputDirectory;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		InitializeUtil.initialize(this, context);
		
		this.outputDirectory = new File(outputDirectoryName);
		if (!this.outputDirectory.exists()) {
			this.outputDirectory.mkdirs();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		// the set of types to be output as XML
		final Set<String> includedTypes = new HashSet<String>();
		includedTypes.add(Event.class.getName());
		includedTypes.add(Time.class.getName());
		includedTypes.add(TemporalLink.class.getName());
		
		// convert the CAS to inline XML - this will use UIMA style XML names,
		// not TimeML names, so we'll have to fix this up afterwards
		String xmlString;
		try {
			xmlString = new CasToInlineXml().generateXML(jCas.getCas(), new FSMatchConstraint() {
				private static final long serialVersionUID = 1L;
				public boolean match(FeatureStructure fs) {
					return includedTypes.contains(fs.getType().getName());
				}
			});
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}

		// parse the inline XML into a jDom document
		SAXBuilder builder = new SAXBuilder();
		builder.setDTDHandler(null);
		Document document;
		try {
			document = builder.build(new StringReader(xmlString));
		} catch (JDOMException e) {
			throw new AnalysisEngineProcessException(e);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		
		// reformat each element, converting UIMA names to TimeML names
		for (Iterator<?> iter = document.getDescendants(); iter.hasNext();) {
			Object contentObj = iter.next();
			if (contentObj instanceof Element) {
				TimeMLUtil.uimaToTimemlNames((Element)contentObj);
			}
		}
		xmlString = new XMLOutputter().outputString(document);

		// write the TimeML to the output file
		String filePath = ViewURIUtil.getURI(jCas);
		String fileName = new File(filePath).getName();
		if (!fileName.endsWith(".tml")) {
			fileName += ".tml";
		}
		File outputFile = new File(this.outputDirectory, fileName);
		try {
			FileWriter writer = new FileWriter(outputFile);
			writer.write(xmlString);
			writer.close();
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	public void setOutputDirectoryName(String outputDirectoryName) {
		this.outputDirectoryName = outputDirectoryName;
	}
}
