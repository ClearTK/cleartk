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
package org.cleartk.syntax.treebank;

import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.syntax.treebank.util.TreebankFormatParser;
import org.cleartk.type.Document;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * <p>
 * The TreebankFormatParser AnalysisEngine populates the "_InitialView" SOFA from the
 * "TreebankView" SOFA, creating all the appropriate Treebank annotations.
 * </p>
 * 
 * @author Philipp Wetzler
 */
public class TreebankGoldAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			JCas tbView = jCas.getView("TreebankView");
			JCas docView = jCas.getView("_InitialView");

			String tbText = UIMAUtil.readSofa(tbView);
			String docText = jCas.getDocumentText(); 

			Document tbDoc = DocumentUtil.getDocument(tbView);

			
			List<org.cleartk.syntax.treebank.util.TopTreebankNode> topNodes;
			if(docText == null) {
				topNodes =  TreebankFormatParser.parseDocument(tbText);
				StringBuilder documentText = new StringBuilder();
				for (org.cleartk.syntax.treebank.util.TopTreebankNode topNode : topNodes) {
					documentText.append(topNode.getText() + "\n");
				}
				docView.setSofaDataString(documentText.toString(), "text/plain");
			} else {
				topNodes =  TreebankFormatParser.parseDocument(tbText, 0, docText);
			}

			for (org.cleartk.syntax.treebank.util.TopTreebankNode topNode : topNodes) {
				TopTreebankNode uimaNode = org.cleartk.syntax.treebank.util.TreebankNodeUtility
						.convert(topNode, docView);
				Sentence uimaSentence = new Sentence(docView, uimaNode
						.getBegin(), uimaNode.getEnd());
				uimaSentence.setConstituentParse(uimaNode);
				uimaSentence.addToIndexes();

				for (TreebankNode terminal : UIMAUtil.toList(uimaNode
						.getTerminals(), TreebankNode.class)) {
					if (terminal.getBegin() != terminal.getEnd()) {
						Token uimaToken = new Token(docView, terminal
								.getBegin(), terminal.getEnd());
						uimaToken.setPos(terminal.getNodeType());
						uimaToken.addToIndexes();
					}
				}
			}


			DocumentUtil.createDocument(docView, tbDoc.getIdentifier(), tbDoc.getPath());

		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

}
