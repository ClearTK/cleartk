/*
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.clearnlp;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.dependency.DEPFeat;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.AbstractReader;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP dependency parser. A typical
 * pipeline preceding this analysis engine would consist of a tokenizer, sentence segmenter,
 * POS tagger, and lemmatizer (mp analyzer).
 * <p>
 * This parser is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
* 
 * @author Lee Becker
 * 
 */
@TypeCapability(
    inputs = { 
        "org.cleartk.token.type.Sentence", 
        "org.cleartk.token.type.Token:pos", 
        "org.cleartk.token.type.Token:lemma" },
    outputs = { 
        "org.cleartk.syntax.dependency.type.TopDependencyNode",
        "org.cleartk.syntax.dependency.type.DependencyNode", 
        "org.cleartk.syntax.dependency.type.DependencyNode"})
public class DependencyParser extends JCasAnnotator_ImplBase {
	

	public static final String DEFAULT_MODEL_FILE_NAME = "ontonotes-en-dep-1.3.0.tgz";
	
	public static final String PARAM_PARSER_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			DependencyParser.class,
			"parserModelUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the file name of the dependency parser model required by the factory method provided by ClearParserUtil.")
	private URI parserModelUri;
	

	public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
	    PosTagger.class, 
	    "languageCode");

	@ConfigurationParameter(
	    description = "Language code for the pos tagger (default value=en).",
	    defaultValue = AbstractReader.LANG_EN)
	private String languageCode;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		
		try {
			URL parserModelURL = (this.parserModelUri == null)
					? DependencyParser.class.getResource(DEFAULT_MODEL_FILE_NAME).toURI().toURL()
					: this.parserModelUri.toURL();
					
			this.parser = EngineGetter.getComponent(parserModelURL.openStream(), this.languageCode, NLPLib.MODE_DEP);
			
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	/**
	 * Convenience method for creating Analysis Engine for ClearNLP's dependency parser using default English model files
	 */
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(DependencyParser.class);
		
	}
	

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			
			DEPTree tree = new DEPTree();
			for (int i = 0; i < tokens.size(); i++) {
			  Token token = tokens.get(i);
				DEPNode node = new DEPNode(i+1, token.getCoveredText(), token.getLemma(), token.getPos(), new DEPFeat());
				tree.add(node);
			}
			
			this.parser.process(tree);
			this.addTreeToCas(jCas, tree, sentence, tokens);
		}
	}
	
	/**
	 * Takes parsed tree from ClearNLP and converts it into ClearTK's dependency type system.
	 * @param jCas
	 * @param tree
	 * @param sentence
	 * @param tokens
	 */
	private void addTreeToCas(JCas jCas, DEPTree tree, Sentence sentence, List<Token> tokens) {
	    DependencyNode[] nodes = new DependencyNode[tree.size()];
	    DependencyNode rootNode = new TopDependencyNode(jCas, sentence.getBegin(), sentence.getEnd());
	    rootNode.addToIndexes();
	    nodes[0] = rootNode; 
	    
	    for (int i = 0; i < tokens.size(); i++) {
	        Token token = tokens.get(i);
	        nodes[i + 1] = new DependencyNode(jCas, token.getBegin(), token.getEnd());
	      }
	    
	    Map<DependencyNode, List<DependencyRelation>> headRelations;
	    headRelations = new HashMap<DependencyNode, List<DependencyRelation>>();
	    Map<DependencyNode, List<DependencyRelation>> childRelations;
	    childRelations = new HashMap<DependencyNode, List<DependencyRelation>>();
	    for (int i = 0; i < tree.size(); i++) {
	      DEPNode parserNode = tree.get(i);
	      if (parserNode.hasHead()) {
	        int headIndex = parserNode.getHead().id;
	        DependencyNode node = nodes[i];
	        DependencyNode headNode = nodes[headIndex];
	        DependencyRelation rel = new DependencyRelation(jCas);
	        rel.setChild(node);
	        rel.setHead(headNode);
	        rel.setRelation(parserNode.getLabel());

	        if (!headRelations.containsKey(node)) {
	          headRelations.put(node, new ArrayList<DependencyRelation>());
	        }
	        headRelations.get(node).add(rel);
	        if (!childRelations.containsKey(headNode)) {
	          childRelations.put(headNode, new ArrayList<DependencyRelation>());
	        }
	        childRelations.get(headNode).add(rel);
	      }
	    } 
	    
	    // finalize nodes: add links between nodes and relations
	    for (DependencyNode node : nodes) {
	      node.setHeadRelations(UIMAUtil.toFSArray(jCas, headRelations.get(node)));
	      node.setChildRelations(UIMAUtil.toFSArray(jCas, childRelations.get(node)));
	      node.addToIndexes();
	    }
	}
	
	private AbstractComponent parser;
}
