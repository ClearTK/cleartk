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
package org.cleartk.syntax.treebank.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class TreebankNodeUtility {
	public static org.cleartk.syntax.treebank.type.TopTreebankNode convert(TopTreebankNode pojoNode, JCas jCas, boolean addToIndexes) {
		org.cleartk.syntax.treebank.type.TopTreebankNode uimaNode = new org.cleartk.syntax.treebank.type.TopTreebankNode(
				jCas, pojoNode.getTextBegin(), pojoNode.getTextEnd());
		convert(pojoNode, jCas, uimaNode, null, addToIndexes);
		uimaNode.setTreebankParse(pojoNode.getTreebankParse());
		initTerminalNodes(uimaNode, jCas);
		if(addToIndexes)
			uimaNode.addToIndexes();
		return uimaNode;
	}

	public static void initTerminalNodes(org.cleartk.syntax.treebank.type.TopTreebankNode uimaNode, JCas jCas) {
		List<org.cleartk.syntax.treebank.type.TreebankNode> terminals = new ArrayList<org.cleartk.syntax.treebank.type.TreebankNode>();
		_initTerminalNodes(uimaNode, terminals);

		FSArray terminalsFSArray = new FSArray(jCas, terminals.size());
		terminalsFSArray.copyFromArray(terminals.toArray(new FeatureStructure[terminals.size()]), 0, 0, terminals
				.size());
		uimaNode.setTerminals(terminalsFSArray);
	}

	private static void _initTerminalNodes(org.cleartk.syntax.treebank.type.TreebankNode node,
			List<org.cleartk.syntax.treebank.type.TreebankNode> terminals) {
		FSArray children = node.getChildren();
		for (int i = 0; i < children.size(); i++) {
			org.cleartk.syntax.treebank.type.TreebankNode child = (org.cleartk.syntax.treebank.type.TreebankNode) children
					.get(i);
			if (child.getLeaf()) terminals.add(child);
			else _initTerminalNodes(child, terminals);
		}
	}

	public static org.cleartk.syntax.treebank.type.TreebankNode convert(TreebankNode pojoNode, JCas jCas,
			org.cleartk.syntax.treebank.type.TreebankNode uimaNode,
			org.cleartk.syntax.treebank.type.TreebankNode parentNode,
			boolean addToIndexes) {
		uimaNode.setNodeType(pojoNode.getType());
		uimaNode.setNodeTags(UIMAUtil.toStringArray(jCas, pojoNode.getTags()));
		uimaNode.setNodeValue(pojoNode.getValue());
		uimaNode.setLeaf(pojoNode.isLeaf());
		uimaNode.setParent(parentNode);

		List<org.cleartk.syntax.treebank.type.TreebankNode> uimaChildren = new ArrayList<org.cleartk.syntax.treebank.type.TreebankNode>();
		for (TreebankNode child : pojoNode.getChildren()) {
			org.cleartk.syntax.treebank.type.TreebankNode childNode = new org.cleartk.syntax.treebank.type.TreebankNode(
					jCas, child.getTextBegin(), child.getTextEnd());
			uimaChildren.add(convert(child, jCas, childNode, uimaNode, addToIndexes));
			if(addToIndexes)
				childNode.addToIndexes();
		}
		FSArray uimaChildrenFSArray = new FSArray(jCas, uimaChildren.size());
		uimaChildrenFSArray.copyFromArray(uimaChildren.toArray(new FeatureStructure[uimaChildren.size()]), 0, 0,
				uimaChildren.size());
		uimaNode.setChildren(uimaChildrenFSArray);
		return uimaNode;
	}

	public static org.cleartk.syntax.treebank.type.TopTreebankNode getTopNode(org.cleartk.syntax.treebank.type.TreebankNode node){
		if(node instanceof org.cleartk.syntax.treebank.type.TopTreebankNode)
			return (org.cleartk.syntax.treebank.type.TopTreebankNode) node;
		
		org.cleartk.syntax.treebank.type.TreebankNode parent = node.getParent();
		while(parent != null) {
			if(parent instanceof org.cleartk.syntax.treebank.type.TopTreebankNode)
				return (org.cleartk.syntax.treebank.type.TopTreebankNode) parent;
			node = parent;
			parent = node.getParent();
		}
		return null;
	}
}
