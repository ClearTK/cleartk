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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.ViewURIUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */

public class Ace2005Writer extends JCasAnnotator_ImplBase {

	public static final String PARAM_OUTPUT_DIRECTORY_NAME = ConfigurationParameterFactory.createConfigurationParameterName(Ace2005Writer.class, "outputDirectoryName");

	@ConfigurationParameter(
			mandatory = true, 
			description = "provides the path of the directory where the XML files should be written.")
	private String outputDirectoryName;

	private File outputDirectory;

	private int idIndex = 0;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		outputDirectory = new File(outputDirectoryName);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
	}

	private Element createExtentElement(String elementName, Annotation annotation) {
		Element extent = new Element(elementName);
		Element charseq = new Element("charseq");
		charseq.setAttribute("START", "" + annotation.getBegin());
		charseq.setAttribute("END", "" + (annotation.getEnd() - 1));
		charseq.setText(annotation.getCoveredText());
		extent.addContent(charseq);
		return extent;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String uri = ViewURIUtil.getURI(jCas);
		String docId = uri.substring(0, uri.indexOf(".sgm"));
		org.cleartk.corpus.ace2005.type.Document document;
		document = AnnotationRetrieval.getAnnotations(jCas, org.cleartk.corpus.ace2005.type.Document.class).iterator()
				.next();

		Document xml = new Document();

		Element sourceFileElement = new Element("source_file");
		sourceFileElement.setAttribute("URI", uri);
		sourceFileElement.setAttribute("SOURCE", document.getAceSource());
		sourceFileElement.setAttribute("TYPE", document.getAceType());
		xml.addContent(sourceFileElement);

		Element documentElement = new Element("document");
		documentElement.setAttribute("DOCID", docId);
		sourceFileElement.addContent(documentElement);

		FSIterator<FeatureStructure> namedEntities = jCas.getFSIndexRepository().getAllIndexedFS(jCas.getCasType(NamedEntity.type));
		while (namedEntities.hasNext()) {
			NamedEntity namedEntity = (NamedEntity) namedEntities.next();
			Element namedEntityElement = new Element("entity");
			namedEntityElement.setAttribute("ID", "" + idIndex++);
			namedEntityElement.setAttribute("TYPE", namedEntity.getEntityType());
			String entitySubtype = namedEntity.getEntitySubtype();
			if (entitySubtype != null) namedEntityElement.setAttribute("SUBTYPE", entitySubtype);
			String entityClass = namedEntity.getEntityClass();
			if (entityClass != null) namedEntityElement.setAttribute("CLASS", entityClass);

			FSArray namedEntityMentions = namedEntity.getMentions();
			for (int i = 0; i < namedEntityMentions.size(); i++) {
				NamedEntityMention namedEntityMention = (NamedEntityMention) namedEntityMentions.get(i);
				Element namedEntityMentionElement = new Element("entity_mention");
				namedEntityMentionElement.setAttribute("ID", "" + idIndex++);
				namedEntityMentionElement.setAttribute("TYPE", namedEntityMention.getMentionType());

				namedEntityMentionElement.addContent(createExtentElement("extent", namedEntityMention.getAnnotation()));
				namedEntityMentionElement.addContent(createExtentElement("head", namedEntityMention.getHead()));
				namedEntityElement.addContent(namedEntityMentionElement);
			}
			documentElement.addContent(namedEntityElement);
		}

		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileOutputStream stream = new FileOutputStream(new File(outputDirectory, docId + ".cleartk.xml"));
			xmlOut.output(xml, stream);
			stream.close();
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	public void setOutputDirectoryName(String outputDirectoryName) {
		this.outputDirectoryName = outputDirectoryName;
	}


}
