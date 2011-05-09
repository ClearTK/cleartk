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
package org.cleartk.srl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.constituent.ptb.ListSpecification;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

/**
 * /** <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * <p>
 * SRLWriter generates a file that is similar to Propbank's <tt>prop.txt</tt>. But unlike
 * <tt>prop.txt</tt> all relations are given in token ranges instead of Treebank nodes, so the file
 * can be interpreted without knowing the Treebank parses.
 * </p>
 * 
 * <p>
 * ClearTK includes a script which takes two files of this type, a gold standard file and a test
 * file, and prints a report on the performance.
 * </p>
 * 
 * @author Philipp Wetzler, Philip Ogren
 */
public class SRLWriter extends JCasAnnotator_ImplBase {

  @ConfigurationParameter(
      mandatory = true,
      description = "path where the PropBank-style file should be written")
  private File outputFile;

  public static final String PARAM_OUTPUT_FILE = ConfigurationParameterFactory.createConfigurationParameterName(
      SRLWriter.class,
      "outputFile");

  private PrintWriter output;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if (!this.outputFile.getParentFile().exists()) {
      this.outputFile.getParentFile().mkdirs();
    }
    try {
      this.output = new PrintWriter(this.outputFile);
    } catch (FileNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    DocumentAnnotation doc = JCasUtil.selectSingle(jCas, DocumentAnnotation.class);

    List<Sentence> sentences = JCasUtil.selectCovered(jCas, Sentence.class, doc);
    int sentenceIndex = 0;
    for (Sentence sentence : sentences) {
      sentenceIndex += 1;

      List<Token> sentenceTokens = JCasUtil.selectCovered(jCas, Token.class, sentence);

      List<Predicate> predicates = JCasUtil.selectCovered(jCas, Predicate.class, sentence);
      for (Predicate predicate : predicates) {
        ListSpecification predicateTokenList = tokenList(
            JCasUtil.selectCovered(jCas, Token.class, predicate.getAnnotation()),
            sentenceTokens);

        StringBuffer line = new StringBuffer();
        line.append(ViewURIUtil.getURI(jCas));
        line.append(" ");
        line.append(sentenceIndex);
        line.append(" ");
        line.append(predicateTokenList);

        List<Argument> args = predicate.getArguments() == null
            ? new ArrayList<Argument>()
            : UIMAUtil.toList(predicate.getArguments(), Argument.class);
        for (Argument arg : args) {
          SemanticArgument sArg;
          try {
            sArg = (SemanticArgument) arg;
          } catch (ClassCastException e) {
            continue;
          }

          if (sArg.getLabel().equals("rel"))
            continue;

          line.append(" " + sArg.getLabel());
          if (sArg.getFeature() != null)
            line.append("-" + sArg.getFeature());
          line.append(":");
          if (sArg.getAnnotation() != null) {
            List<Token> argTokens = JCasUtil.selectCovered(jCas, Token.class, sArg.getAnnotation());
            if (argTokens.size() > 0)
              line.append(tokenList(argTokens, sentenceTokens));
          } else {
            List<Annotation> corefAnnotations = UIMAUtil.toList(
                sArg.getCoreferenceAnnotations(),
                Annotation.class);
            boolean first = true;
            for (Annotation corefAnnotation : corefAnnotations) {
              List<Token> argTokens = JCasUtil.selectCovered(jCas, Token.class, corefAnnotation);
              if (argTokens.size() > 0) {
                if (!first)
                  line.append("*");
                line.append(tokenList(argTokens, sentenceTokens));
                first = false;
              }
            }
          }
        }

        output.println(line);
      }

    }
    output.flush();
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    output.flush();
    output.close();
  }

  private ListSpecification tokenList(List<Token> listTokens, List<Token> allTokens) {
    StringBuffer spec = new StringBuffer();
    ListIterator<Token> it = listTokens.listIterator();

    Token cursor = it.next();
    int cursorIndex = allTokens.indexOf(cursor);
    spec.append(cursorIndex);
    int lastBegin = cursorIndex;
    while (it.hasNext()) {
      cursor = it.next();
      int lastIndex = cursorIndex;
      cursorIndex = allTokens.indexOf(cursor);

      if (cursorIndex > lastIndex + 1) {
        if (lastIndex > lastBegin) {
          spec.append("-");
          spec.append(lastIndex);
        }
        spec.append(",");
        spec.append(cursorIndex);
        lastBegin = cursorIndex;
      }
    }
    if (cursorIndex > lastBegin) {
      spec.append("-");
      spec.append(cursorIndex);
    }

    return new ListSpecification(spec.toString());
  }

}
