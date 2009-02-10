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

import java.util.Iterator;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.cleartk.type.Document;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */
public class DocumentUtil
{
	public static String getPath(JCas jCas)
	{
		Document document = getDocument(jCas);
		return document == null ? null : document.getPath();
	}
	
	public static String getIdentifier(JCas jCas)
	{
		Document document = getDocument(jCas);
		return document == null ? null : document.getIdentifier();
	}

	/**
	 * Get the ClearTK Document object for this JCas. 
	 * @param jCas The JCas for the document.
	 * @return     The ClearTK Document object.
	 */
	public static Document getDocument(JCas jCas)
	{
		AnnotationIndex index = jCas.getAnnotationIndex(Document.type);
		Iterator<?> iter = index.iterator();
		Document doc = iter.hasNext() ? (Document)iter.next() : null;
		if( doc != null && doc.getEnd() == 0 ) {
			if( jCas.getSofaDataString() != null ) {
				doc.removeFromIndexes();
				doc.setEnd(jCas.getSofaDataString().length());
				doc.addToIndexes();
			}
		}
		return doc;
	}
	
	/**
	 * Create the ClearTK Document object for this JCas. 
	 * @param jCas The JCas for the document.
	 * @param identifier The identifier attribute of the document.
	 * @param path The path attribute of the document.
	 */
	public static void createDocument(JCas jCas, String identifier, String path)
	{
		Document doc = new Document(jCas, 0, 0);
		doc.setIdentifier(identifier);
		doc.setPath(path);
		if( jCas.getSofaDataString() != null ) {
			doc.setEnd(jCas.getSofaDataString().length());
		}
		doc.addToIndexes();
		
		try {
			JCas initialView = jCas.getView("_InitialView");
			if(!initialView.equals(jCas))
				DocumentUtil.createDocument(initialView, identifier, path);
		}
		catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Update the ClearTK Document to reflect the boundaries of the SOFA.
	 * @param jCas The JCas for the document.
	 */
	public static void updateDocument(JCas jCas)
	{
		String s = jCas.getSofaDataString();
		if( s != null ) {
			Document doc = getDocument(jCas);
			doc.removeFromIndexes();
			doc.setEnd(s.length());
			doc.addToIndexes();
		}
	}
	
	/**
	 * Copy a ClearTK Document from one view to another.
	 * @param sourceView The JCas view to copy from.
	 * @param targetView The JCas view to copy to.
	 */
	public static void copyDocument(JCas sourceView, JCas targetView) {
		Document doc = getDocument(sourceView);
		createDocument(targetView, doc.getIdentifier(), doc.getPath());
	}
}
