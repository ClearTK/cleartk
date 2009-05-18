/*
 * This file was copied from the Apache UIMA examples source code base from
 * org.apache.uima.examples.cpe.XCasWriterCasConsumer and org.apache.uima.examples.xmi.XmiWriterCasConsumer 
 * and modified.  The ASF license that applies to the original work and this one is provided in the next comment.  
 * The following are the major changes to the original code:
 * <ul>
 * <li>removed dependency on class SourceDocumentInformation in favor of a simpler file naming procedure.
 * <li>combined both types of xml serialization into a single class
 * </ul>
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.cleartk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.ClearTKComponents;
import org.uutuc.factory.AnalysisEngineFactory;
import org.xml.sax.SAXException;

/**
 * 
 * A simple CAS consumer that generates XCAS (XML representation of the CAS)
 * files in the filesystem.
 * 
 * @author Philip Ogren
 */

public class XWriter extends JCasAnnotator_ImplBase {
	/**
	 * "org.cleartk.util.XWriter.PARAM_OUTPUT_DIRECTORY"
	 * is a single, required string parameter that takes a
	 * path to directory into which output files will be written.
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "org.cleartk.util.XWriter.PARAM_OUTPUT_DIRECTORY";

	/**
	 * "org.cleartk.util.XWriter.PARAM_XML_SCHEME"
	 * is a single, optional, string parameter that specifies the
	 * UIMA XML serialization scheme that should be used. Valid values for this
	 * parameter are "XMI" (default) and "XCAS".
	 * 
	 * @see XmiCasSerializer
	 * @see XCASSerializer
	 */
	public static final String PARAM_XML_SCHEME = "org.cleartk.util.XWriter.PARAM_XML_SCHEME";

	public static final String XMI = "XMI";

	public static final String XCAS = "XCAS";

	private File outputDirectory;

	private boolean useXMI = true;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		outputDirectory = new File((String) context.getConfigParameterValue(PARAM_OUTPUT_DIRECTORY));
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		String xmlScheme = (String) UIMAUtil.getDefaultingConfigParameterValue(context, PARAM_XML_SCHEME, XMI);

		if (xmlScheme.equals(XMI)) useXMI = true;
		else if (xmlScheme.equals(XCAS)) useXMI = false;
		else throw new ResourceInitializationException(String.format(
				"parameter '%1$s' must be either '%2$s' or '%3$s' or left empty.", PARAM_XML_SCHEME, XMI, XCAS), null);

	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String id = ViewURIUtil.getURI(jcas);
		int index = Math.max(id.lastIndexOf("/"), id.lastIndexOf("\\")) + 1;
		id = id.substring(index);
		try {
			if (useXMI) writeXmi(jcas.getCas(), id);
			else writeXCas(jcas.getCas(), id);
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		catch (SAXException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void writeXCas(CAS aCas, String id) throws IOException, SAXException {
		File outFile = new File(outputDirectory, id + ".xcas");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			XCASSerializer ser = new XCASSerializer(aCas.getTypeSystem());
			XMLSerializer xmlSer = new XMLSerializer(out, false);
			ser.serialize(aCas, xmlSer.getContentHandler());
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private void writeXmi(CAS aCas, String id) throws IOException, SAXException {
		File outFile = new File(outputDirectory, id + ".xmi");
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(outFile);
			XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
			XMLSerializer xmlSer = new XMLSerializer(out, false);
			ser.serialize(aCas, xmlSer.getContentHandler());
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static AnalysisEngineDescription getDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(
				XWriter.class, ClearTKComponents.TYPE_SYSTEM_DESCRIPTION, 
				XWriter.PARAM_OUTPUT_DIRECTORY, outputDirectory);
	}
}
