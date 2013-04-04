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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import com.google.common.collect.Maps;
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
 * This class provides the base implementation class for the UIMA/ClearTK wrapper for the ClearNLP dependency parser. 
 * Sub classes should override methods for creating and setting properties on dependency annotations
 * 
 * <p>
 * This parser is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
* 
 * @author Lee Becker
 * 
 */
public abstract class DependencyParser_ImplBase<WINDOW_TYPE extends Annotation, 
   TOKEN_TYPE extends Annotation, 
   DEPENDENCY_NODE_TYPE extends Annotation, 
   TOP_DEPENDENCY_NODE_TYPE extends DEPENDENCY_NODE_TYPE,
   DEPENDENCY_RELATION_TYPE extends FeatureStructure> extends JCasAnnotator_ImplBase {

	public static final String DEFAULT_MODEL_FILE_NAME = "ontonotes-en-dep-1.3.0.tgz";
	
	public static final String PARAM_PARSER_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			DependencyParser_ImplBase.class,
			"parserModelUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the file name of the dependency parser model required by the factory method provided by ClearParserUtil.")
	private URI parserModelUri;
	

	public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
	    DependencyParser_ImplBase.class, 
	    "languageCode");

	@ConfigurationParameter(
	    description = "Language code for the dependency parser (default value=en).",
	    defaultValue = AbstractReader.LANG_EN)
	private String languageCode;
	
	
	/**
	 * Provides a list of windows (spans) to be parsed
	 */
  protected abstract Collection<WINDOW_TYPE> selectWindows(JCas jCas);

  /**
   * Provides a list of tokens within a window
   */
  protected abstract List<TOKEN_TYPE> selectTokens(JCas jCas, WINDOW_TYPE window);

  /**
   * Gets the lemma for the specified token
   */
  protected abstract String getLemma(JCas jCas, TOKEN_TYPE token);

  /**
   * Gets the POS tag for the specified token
   */
  protected abstract String getPos(JCas jCas, TOKEN_TYPE token);
  
  /**
   * Creates a root node for the parse tree
   */
  protected abstract TOP_DEPENDENCY_NODE_TYPE createTopDependencyNode(JCas jCas, int begin, int end);
	
  /**
   * Creates a new dependency node for the specified span
   */
  protected abstract DEPENDENCY_NODE_TYPE createDependencyNode(JCas jCas, int begin, int end);
  
  /**
   * Creates a relation between the two nodes.  If dealing with a type system where the relation is a separate object,
   * this will likely create a new relation object.  For graphs that have the relation built into the node, this will likely
   * just set labels on head and child relation fields
   */
  protected abstract DEPENDENCY_RELATION_TYPE createRelation(JCas jCas, DEPENDENCY_NODE_TYPE head, DEPENDENCY_NODE_TYPE child, String relation);
  
  /**
   * Sets the head relations for a given node.  This is only important if the type system supports multi-head dependency parses
   */
  protected abstract void setHeadRelations(JCas jCas, DEPENDENCY_NODE_TYPE node, List<DEPENDENCY_RELATION_TYPE> headRelations);
  
  /**
   * Sets the head relations for a given node.  This is only important if the type system supports multi-child dependency parses
   */
  protected abstract void setChildRelations(JCas jCas, DEPENDENCY_NODE_TYPE node, List<DEPENDENCY_RELATION_TYPE> childRelations);
  
	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		
		try {
			URL parserModelURL = (this.parserModelUri == null)
					? DependencyParser_ImplBase.class.getResource(DEFAULT_MODEL_FILE_NAME).toURI().toURL()
					: this.parserModelUri.toURL();
					
			this.parser = EngineGetter.getComponent(parserModelURL.openStream(), this.languageCode, NLPLib.MODE_DEP);
			
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		for (WINDOW_TYPE window : this.selectWindows(jCas)) {
			List<TOKEN_TYPE> tokens = this.selectTokens(jCas, window);
			
			// Extract data from CAS and stuff it into ClearNLP data structures
			DEPTree tree = new DEPTree();
			for (int i = 0; i < tokens.size(); i++) {
			  TOKEN_TYPE token = tokens.get(i);
			  String lemma = this.getLemma(jCas, token);
			  String pos = this.getPos(jCas, token);
				DEPNode node = new DEPNode(i+1, token.getCoveredText(), lemma, pos, new DEPFeat());
				tree.add(node);
			}
			
			// Run the parser
			this.parser.process(tree);
			
			// convert ClearNLP output back into CAS type system annotation
			this.addTreeToCas(jCas, tree, window, tokens);
		}
	}
	
	/**
	 * Takes parsed tree from ClearNLP and converts it into dependency type system.
	 * @param jCas
	 * @param tree
	 * @param window
	 * @param tokens
	 */
	private void addTreeToCas(JCas jCas, DEPTree tree, WINDOW_TYPE window, List<TOKEN_TYPE> tokens) {
	  
	    ArrayList<DEPENDENCY_NODE_TYPE> nodes = new ArrayList<DEPENDENCY_NODE_TYPE>(tree.size());
	    TOP_DEPENDENCY_NODE_TYPE rootNode = this.createTopDependencyNode(jCas, window.getBegin(), window.getEnd());
	    rootNode.addToIndexes();
	    nodes.add(rootNode); 
	    
	    for (int i = 0; i < tokens.size(); i++) {
	        TOKEN_TYPE token = tokens.get(i);
	        nodes.add(this.createDependencyNode(jCas, token.getBegin(), token.getEnd()));
	    }
	    
	    Map<DEPENDENCY_NODE_TYPE, List<DEPENDENCY_RELATION_TYPE>> headRelations = Maps.newHashMap();
	    Map<DEPENDENCY_NODE_TYPE, List<DEPENDENCY_RELATION_TYPE>> childRelations = Maps.newHashMap();
	    // extract relation arcs from ClearNLP parse tree
	    for (int i = 0; i < tree.size(); i++) {
	      DEPNode parserNode = tree.get(i);
	      if (parserNode.hasHead()) {
	        int headIndex = parserNode.getHead().id;
	        DEPENDENCY_NODE_TYPE node = nodes.get(i);
	        DEPENDENCY_NODE_TYPE headNode = nodes.get(headIndex);
	        DEPENDENCY_RELATION_TYPE rel = this.createRelation(jCas, headNode, node, parserNode.getLabel());
	    
	        if (!headRelations.containsKey(node)) {
	          headRelations.put(node, new ArrayList<DEPENDENCY_RELATION_TYPE>());
	        }
	        headRelations.get(node).add(rel);
	        if (!childRelations.containsKey(headNode)) {
	          childRelations.put(headNode, new ArrayList<DEPENDENCY_RELATION_TYPE>());
	        }
	        childRelations.get(headNode).add(rel);
	      }
	    } 
	    
	    // finalize nodes: add links between nodes and relations 
	    for (DEPENDENCY_NODE_TYPE node : nodes) {
	      this.setHeadRelations(jCas, node, headRelations.get(node));
	      this.setChildRelations(jCas, node, childRelations.get(node));
	      node.addToIndexes();
	    }
	}
	
	private AbstractComponent parser;
}
