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

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.ViewNames;
import org.cleartk.syntax.treebank.type.TerminalTreebankNode;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.util.TreebankFormatParser;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.InitializeUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * <p>
 * The TreebankFormatParser AnalysisEngine populates the "_InitialView" SOFA
 * from the "TreebankView" SOFA, creating all the appropriate Treebank
 * annotations.
 * </p>
 * 
 * @author Philipp Wetzler
 */

@SofaCapability(inputSofas = {ViewNames.TREEBANK, ViewNames.DEFAULT}, outputSofas = {})
public class TreebankGoldAnnotator extends JCasAnnotator_ImplBase {
	
	public static AnalysisEngineDescription getDescription()
	throws ResourceInitializationException {
		return CleartkComponents.createPrimitiveDescription(TreebankGoldAnnotator.class);
	}
	public static AnalysisEngineDescription getDescriptionPOSTagsOnly()
	throws ResourceInitializationException {
		return CleartkComponents.createPrimitiveDescription(TreebankGoldAnnotator.class,
				TreebankGoldAnnotator.PARAM_POST_TREES, false);
	}

	public static final String PARAM_POST_TREES = ConfigurationParameterFactory.createConfigurationParameterName(TreebankGoldAnnotator.class, "postTrees");

	private static final String POST_TREES_DESCRIPTION = "specifies whether or not to post trees (i.e. annotations of type TreebankNode) to the CAS.  " +
			"Sometimes treebank data is used only for the part-of-speech data that it contains.  " +
			"For such uses, it is not necessary to post the entire constituent parse to the CAS. " +
			"Instead, this parameter can be set to false which results in  only the part-of-speech data being added.";

	@ConfigurationParameter(
			description = POST_TREES_DESCRIPTION, 
			mandatory = false, 
			defaultValue = "true")
	private boolean postTrees;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		InitializeUtil.initialize(this, context);
		super.initialize(context);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		JCas docView;
		String tbText;
		try {
			docView = jCas.getView(ViewNames.DEFAULT);
			tbText = jCas.getView(ViewNames.TREEBANK).getDocumentText();
		}
		catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		String docText = jCas.getDocumentText();

		if (docText == null) {
			docText = TreebankFormatParser.inferPlainText(tbText);
			docView.setSofaDataString(docText, "text/plain");
		}
		List<org.cleartk.syntax.treebank.util.TopTreebankNode> topNodes;
		topNodes = TreebankFormatParser.parseDocument(tbText, 0, docText);

		for (org.cleartk.syntax.treebank.util.TopTreebankNode topNode : topNodes) {
			TopTreebankNode uimaNode = org.cleartk.syntax.treebank.util.TreebankNodeUtility.convert(topNode, docView,
					postTrees);
			Sentence uimaSentence = new Sentence(docView, uimaNode.getBegin(), uimaNode.getEnd());
			uimaSentence.addToIndexes();

			int tokenIndex = 0;
			for (TerminalTreebankNode terminal : UIMAUtil.toList(uimaNode.getTerminals(), TerminalTreebankNode.class)) {
				if (terminal.getBegin() != terminal.getEnd()) {
					terminal.setTokenIndex(tokenIndex++);
					Token uimaToken = new Token(docView, terminal.getBegin(), terminal.getEnd());
					uimaToken.setPos(terminal.getNodeType());
					uimaToken.addToIndexes();
				} else {
					terminal.setTokenIndex(-1);
				}
			}
		}
	}
}
