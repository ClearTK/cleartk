/* Copyright (c) 2012, Regents of the University of Colorado 
* All rights reserved.
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* For a complete copy of the license please see the file LICENSE distributed 
* with the cleartk-syntax-berkeley project or visit 
* http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
*/

package org.cleartk.berkeleyparser;

import java.util.List;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.constituent.type.TerminalTreebankNode;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.token.type.Token;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Majid Laali
 */
public class ParseTreePosTagSetter extends JCasAnnotator_ImplBase{
  public static final String PARAM_INPUT_TYPES_HELPER_CLASS_NAME = "inputTypesHelperClassName";

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException{
    return AnalysisEngineFactory.createEngineDescription(ParseTreePosTagSetter.class, 
        PARAM_INPUT_TYPES_HELPER_CLASS_NAME, DefaultInputTypesHelper.class.getName());
  }

  @ConfigurationParameter(
      name = PARAM_INPUT_TYPES_HELPER_CLASS_NAME,
      defaultValue = "org.cleartk.berkeleyparser.DefaultInputTypesHelper",
      mandatory = true)
  protected String inputTypesHelperClassName;


  
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    for (TopTreebankNode topTreebankNode: JCasUtil.select(aJCas, TopTreebankNode.class)){
      FSArray terminals = topTreebankNode.getTerminals();
      List<Token> tokens = JCasUtil.selectCovered(Token.class, topTreebankNode);
      for (int i = 0; i < terminals.size(); i++){
        TerminalTreebankNode terminalTreebankNode = (TerminalTreebankNode) terminals.get(i);
        tokens.get(i).setPos(terminalTreebankNode.getNodeType());
      }
    }
  }

}
