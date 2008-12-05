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
package org.cleartk.ne.term;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.cleartk.ne.term.TermFinderAnnotator;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.TestsUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class TermFinderAnnotatorTests {
	
	@Test
	public void test() throws UIMAException, IOException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				TermFinderAnnotator.class,
				TestsUtil.getTypeSystem(Sentence.class, Token.class, NamedEntityMention.class),
				TermFinderAnnotator.PARAM_TERM_LIST_LISTING, "data/termlist/lexicons.txt",
				TermFinderAnnotator.PARAM_TOKEN_CLASS, Token.class.getName(),
				TermFinderAnnotator.PARAM_TERM_MATCH_ANNOTATION_CLASS, NamedEntityMention.class.getName());
		JCas jCas = engine.newJCas();
		TestsUtil.createTokens(jCas,
				"Smith is a nice guy.", 
				"Smith is a nice guy .", null, null);
		engine.process(jCas);
		engine.collectionProcessComplete();
		List<NamedEntityMention> mentions = AnnotationRetrieval.getAnnotations(
				jCas, NamedEntityMention.class);
		Assert.assertEquals(mentions.size(), 1);
		NamedEntityMention mention = mentions.get(0);
		Assert.assertEquals("Smith", mention.getCoveredText());
	}
	
	@Test
	public void testTermFinderAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				"desc/ne/term/TermFinderAnnotator.xml");
		Object handler = engine.getConfigParameterValue(
				TermFinderAnnotator.PARAM_TOKEN_CLASS);
		Assert.assertEquals(Token.class.getName(), handler);
		
		engine.collectionProcessComplete();
	}

}
