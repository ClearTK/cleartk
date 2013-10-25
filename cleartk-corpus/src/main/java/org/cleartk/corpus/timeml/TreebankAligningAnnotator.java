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
package org.cleartk.corpus.timeml;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Level;
import org.cleartk.corpus.penntreebank.TreebankNodeConverter;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.timeml.type.Text;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewUriUtil;
import org.cleartk.util.treebank.TreebankFormatParser;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * 
 * @author Steven Bethard
 */
public class TreebankAligningAnnotator extends JCasAnnotator_ImplBase {

  public static final String PARAM_TREEBANK_DIRECTORY_NAME = "treebankDirectoryName";

  @ConfigurationParameter(
      name = PARAM_TREEBANK_DIRECTORY_NAME,
      mandatory = true,
      description = "the path to the treebank directory containing the XX/wsj_XXXX.mrg files.")
  private String treebankDirectoryName;

  public void setTreebankDirectoryName(String treebankDirectoryName) {
    this.treebankDirectoryName = treebankDirectoryName;
  }

  public static AnalysisEngineDescription getDescription(String treeBankDir)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        TreebankAligningAnnotator.class,
        PARAM_TREEBANK_DIRECTORY_NAME,
        treeBankDir);

  }

  private File treebankDirectory;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.treebankDirectory = new File(this.treebankDirectoryName);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    // determine the appropriate .mrg file name
    String wsjPath = ViewUriUtil.getURI(jCas).getPath();
    String wsjName = new File(wsjPath).getName();
    String subdir = wsjName.substring(4, 6);
    String mrgName = wsjName.replaceAll("\\.tml", ".mrg");
    File mrgFile = new File(new File(this.treebankDirectory, subdir), mrgName);

    // read the parse text
    String mrgText;
    try {
      mrgText = FileUtils.file2String(mrgFile);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }

    // we need a TEXT element to know where to start
    Collection<Text> texts = JCasUtil.select(jCas, Text.class);
    if (texts.size() != 1) {
      throw new IllegalArgumentException("expected 1 Text annotation, found " + texts.size());
    }

    // parse the trees, skipping the document if there are alignment
    // problems
    int offset = texts.iterator().next().getBegin();
    String text = jCas.getDocumentText();
    List<org.cleartk.util.treebank.TopTreebankNode> utilTrees;
    try {
      utilTrees = TreebankFormatParser.parseDocument(mrgText, offset, text);
    } catch (Exception e) {
      this.getContext().getLogger().log(
          Level.WARNING,
          String.format("Skipping %s due to alignment problems", wsjPath),
          e);
      return;
    }

    // add Token, Sentence and TreebankNode annotations for the text
    for (org.cleartk.util.treebank.TopTreebankNode utilTree : utilTrees) {

      // create a Sentence and set its parse
      TopTreebankNode tree = TreebankNodeConverter.convert(utilTree, jCas, true);
      Sentence sentence = new Sentence(jCas, tree.getBegin(), tree.getEnd());
      sentence.addToIndexes();

      // create the Tokens and add them to the Sentence
      for (int i = 0; i < tree.getTerminals().size(); i++) {
        TreebankNode leaf = tree.getTerminals(i);
        if (leaf.getBegin() != leaf.getEnd()) {
          Token token = new Token(jCas, leaf.getBegin(), leaf.getEnd());
          token.setPos(leaf.getNodeType());
          token.addToIndexes();
        }
      }
    }
  }
}
