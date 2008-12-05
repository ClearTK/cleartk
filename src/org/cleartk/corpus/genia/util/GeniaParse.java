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
package org.cleartk.corpus.genia.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class GeniaParse {

	String medline;
	String text;
	String xml;
	List<GeniaTag> posTags;
	List<GeniaTag> semTags;
	List<GeniaSentence> sentences;

	public GeniaParse() {
		posTags = new ArrayList<GeniaTag>();
		semTags = new ArrayList<GeniaTag>();
		sentences = new ArrayList<GeniaSentence>();
	}
	public String getMedline() {
		return medline;
	}
	
	public void setMedline(String medline) {
		this.medline = medline;
	}
	
	public List<GeniaTag> getPosTags() {
		return Collections.unmodifiableList(posTags);
	}
	
	public void addPosTags(List<GeniaTag> posTags) {
		this.posTags.addAll(posTags);
	}
	
	public List<GeniaTag> getSemTags() {
		return Collections.unmodifiableList(semTags);
	}
	
	public void addSemTags(List<GeniaTag> semTags) {
		this.semTags.addAll(semTags);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public void addSentence(GeniaSentence sentence) {
		sentences.add(sentence);
	}
	
	public List<GeniaSentence> getSentences(){
		return Collections.unmodifiableList(sentences);
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
}
