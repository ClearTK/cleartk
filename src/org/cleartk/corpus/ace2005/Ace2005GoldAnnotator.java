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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ViewNames;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.type.Chunk;
import org.cleartk.util.UIMAUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * This parser works with the ACE2005CollectionReader to pre-populate the JCas with 
 * ACE named entities.  Currently, it only loads information from the entity and 
 * entity_mention tags and adds NamedEntity and NamedEntityMention objects to the 
 * JCas.  
 * 
 * @author Philip Ogren
 *
 */
public class Ace2005GoldAnnotator extends JCasAnnotator_ImplBase
{

	Pattern ampPattern;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException
	{
		ampPattern = Pattern.compile(Pattern.quote("&amp;"));
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException
	{
		try
		{
			String apfUri = jCas.getView(ViewNames.ACE_APF_URI).getSofaDataURI();
			JCas initialView = jCas.getView(ViewNames.DEFAULT);
			String documentText = initialView.getDocumentText();
			SAXBuilder builder = new SAXBuilder();
			builder.setDTDHandler(null);
			URI sofaDataURI = new URI(apfUri);
			Document doc = builder.build(new File(sofaDataURI));
			
			Element apfSource = doc.getRootElement();
			Element apfDocument = apfSource.getChild("document");
			List<?> apfEntities = apfDocument.getChildren("entity");
			for(int i=0; i<apfEntities.size(); i++)
			{
				Element apfEntity = (Element) apfEntities.get(i);
				NamedEntity namedEntity = new NamedEntity(initialView);
				namedEntity.setEntityType(apfEntity.getAttributeValue("TYPE"));
				namedEntity.setEntitySubtype(apfEntity.getAttributeValue("SUBTYPE"));
				namedEntity.setEntityClass(apfEntity.getAttributeValue("CLASS"));
				namedEntity.setEntityId(apfEntity.getAttributeValue("ID"));
				namedEntity.addToIndexes();
				
				List<NamedEntityMention> mentions = new ArrayList<NamedEntityMention>();
				
				List<?> entityMentions = apfEntity.getChildren("entity_mention");
				for(int j=0; j<entityMentions.size(); j++)
				{
					Element entityMention = (Element) entityMentions.get(j);
					int start = Integer.parseInt(entityMention.getChild("extent").getChild("charseq").getAttributeValue("START"));
					int end = Integer.parseInt(entityMention.getChild("extent").getChild("charseq").getAttributeValue("END"));
					String givenText = entityMention.getChild("extent").getChild("charseq").getText();
					String parsedText = documentText.substring(start, end+1);
					Matcher ampMatcher = ampPattern.matcher(parsedText);
					parsedText = ampMatcher.replaceAll("&");

					NamedEntityMention mention = new NamedEntityMention(initialView, start, end+1);
					mention.setMentionType(entityMention.getAttributeValue("TYPE"));
					mention.setMentionedEntity(namedEntity);
					
					Chunk chunk = new Chunk(initialView, start, end+1);
					mention.setAnnotation(chunk);
					
					int headStart = Integer.parseInt(entityMention.getChild("head").getChild("charseq").getAttributeValue("START"));
					int headEnd = Integer.parseInt(entityMention.getChild("head").getChild("charseq").getAttributeValue("END"));
					Chunk head = new Chunk(initialView, headStart, headEnd+1);
					mention.setHead(head);
					
					mention.addToIndexes();
					mentions.add(mention);

					givenText = givenText.replaceAll("\\s+", " ");
					parsedText = givenText.replaceAll("\\s+", " ");

//					if(!givenText.equals(parsedText))
//					{
//						
//						System.out.println("given text and parsed text differ.");
//						System.out.println(givenText);
//						System.out.println(parsedText);
//						System.out.println(apfDocument.getAttributeValue("DOCID"));
//						System.out.println(apfEntity.getAttributeValue("ID"));
//						System.out.println(documentText);
//					}
				}
				namedEntity.setMentions(UIMAUtil.toFSArray(jCas, mentions));
			}

		}
		catch(CASException ce)
		{
			throw new AnalysisEngineProcessException(ce);
		}
		catch(IOException ioe)
		{
			throw new AnalysisEngineProcessException(ioe);
		}
		catch(JDOMException je)
		{
			throw new AnalysisEngineProcessException(je);
		}
		catch(URISyntaxException use)
		{
			throw new AnalysisEngineProcessException(use);
		}
	}

}
