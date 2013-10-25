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
package org.cleartk.corpus.conll2005;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.srl.type.Chunk;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationUtil;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.fit.util.FSCollectionFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
@SofaCapability(
    inputSofas = { Conll2005Constants.CONLL_2005_VIEW, CAS.NAME_DEFAULT_SOFA },
    outputSofas = {})
public class Conll2005GoldAnnotator extends JCasAnnotator_ImplBase {

  @ConfigurationParameter(
      name = PARAM_HAS_VERB_SENSES,
      mandatory = true,
      description = "does the data file contain verb sense tags")
  private Boolean hasVerbSenses;

  public static final String PARAM_HAS_VERB_SENSES = "hasVerbSenses";

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      JCas conllView = jCas.getView(Conll2005Constants.CONLL_2005_VIEW);
      JCas initView = jCas.getView(CAS.NAME_DEFAULT_SOFA);

      String conllText = conllView.getSofaDataString();

      List<Conll2005Line> conll2005Lines = new ArrayList<Conll2005Line>();
      for (String line : conllText.split("\n")) {
        conll2005Lines.add(new Conll2005Line(line, hasVerbSenses));
        // System.err.println(line);
      }
      // System.err.println();

      StringBuffer docText = new StringBuffer();

      List<TreebankNode> terminals = new ArrayList<TreebankNode>(conll2005Lines.size());
      CharniakParseParser parser = new CharniakParseParser(initView);

      int numberOfPredicates = 0;
      for (Conll2005Line line : conll2005Lines)
        if (!line.targetVerb.equals("-"))
          numberOfPredicates += 1;

      int currentPredicate = 0;
      PredicateParser predicateParsers[] = new PredicateParser[numberOfPredicates];
      for (int i = 0; i < numberOfPredicates; i++)
        predicateParsers[i] = new PredicateParser(initView);

      NamedEntityParser namedEntityParser = new NamedEntityParser(initView);

      for (Conll2005Line line : conll2005Lines.toArray(new Conll2005Line[0])) {
        if (line.argumentSegments.length != 0 && line.argumentSegments.length != numberOfPredicates) {
          throw new RuntimeException(String.format(
              "expected 0 or %d segments, found %d",
              numberOfPredicates,
              line.argumentSegments.length));
        }

        if (docText.length() > 0 && line.word.length() > 0) {
          docText.append(" ");
        }
        int startIndex = docText.length();
        docText.append(line.word);
        int endIndex = docText.length();

        Token token = new Token(initView, startIndex, endIndex);
        token.setPos(line.pos);
        token.addToIndexes();

        TreebankNode terminal = new TreebankNode(initView, startIndex, endIndex);
        terminal.setNodeType(line.pos);
        terminal.setNodeValue(line.word);
        terminal.setChildren(new FSArray(jCas, 0));
        terminal.setLeaf(true);
        terminal.addToIndexes();
        terminals.add(terminal);

        parser.feed(line.charniakParseSegment, terminal);

        namedEntityParser.feed(line.neSegment, token);

        if (line.argumentSegments.length > 0) {
          for (int i = 0; i < numberOfPredicates; i++) {
            predicateParsers[i].feed(line.argumentSegments[i], token);
          }
        }

        if (!line.targetVerb.equals("-")) {
          predicateParsers[currentPredicate].feedInfo(
              line.word,
              line.targetVerb,
              line.verbSenseTag,
              token);
          currentPredicate += 1;
        }
      }
      initView.setSofaDataString(docText.toString(), "text/plain");

      Sentence sentence = new Sentence(initView, 0, docText.toString().length());
      sentence.addToIndexes();

      parser.makeParse();

      for (PredicateParser predicateParser : predicateParsers)
        predicateParser.makePredicate();

    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  private static class Conll2005Line {
    String word;

    String pos;

    String charniakParseSegment;

    String neSegment;

    String verbSenseTag;

    String targetVerb;

    String argumentSegments[];

    Conll2005Line(String line, boolean hasSenseTag) {
      String fields[] = line.split("\\s+");
      int i = 0;
      this.word = fields[i++].trim();
      this.pos = fields[i++].trim();
      this.charniakParseSegment = fields[i++].trim();
      this.neSegment = fields[i++].trim();

      if (hasSenseTag) {
        this.verbSenseTag = fields[i++].trim();
      } else {
        this.verbSenseTag = null;
      }

      this.targetVerb = fields[i++].trim();

      this.argumentSegments = new String[fields.length - i];
      for (int j = 0; j < argumentSegments.length; j++) {
        this.argumentSegments[j] = fields[i++].trim();
      }
    }
  }

  private static class Constituent {
    String type;

    List<TreebankNode> children;

    Constituent(String type) {
      this.type = type;
      this.children = new ArrayList<TreebankNode>();
    }

    // Constituent() {
    // this(null);
    // }

    public void addChild(TreebankNode newChild) {
      this.children.add(newChild);
    }

    public TreebankNode makeTreebankNode(JCas jCas) {
      if (this.type.equals("S1")) {
        return this.children.get(0);
      } else {
        int[] span = AnnotationUtil.getAnnotationsExtent(this.children);
        TreebankNode node = new TreebankNode(jCas, span[0], span[1]);
        node.setNodeType(this.type);
        node.setChildren(new FSArray(jCas, this.children.size()));
        FSCollectionFactory.fillArrayFS(node.getChildren(), this.children);
        for (TreebankNode child : this.children)
          child.setParent(node);
        node.addToIndexes();
        return node;
      }
    }
  }

  private static class CharniakParseParser {
    Stack<Constituent> parseStack;

    List<TreebankNode> terminals;

    JCas jCas;

    CharniakParseParser(JCas jCas) {
      parseStack = new Stack<Constituent>();
      parseStack.push(new Constituent("TOP"));
      terminals = new ArrayList<TreebankNode>();
      this.jCas = jCas;
    }

    void feed(String segment, TreebankNode terminal) throws IOException {
      BufferedReader r = new BufferedReader(new StringReader(segment));

      terminals.add(terminal);

      for (int i = r.read(); i != -1; i = r.read()) {
        char c = (char) i;
        switch (c) {
          case '*':
            parseStack.peek().addChild(terminal);
            break;
          case '(':
            parseStack.push(new Constituent(readNodeType(r)));
            break;
          case ')':
            TreebankNode node = parseStack.pop().makeTreebankNode(jCas);
            parseStack.peek().addChild(node);
            break;
          default:
            throw new IOException("unexpected character in string: " + String.valueOf(c) + " ("
                + String.valueOf((int) c) + ")");
        }
      }
    }

    public TopTreebankNode makeParse() {
      int[] span = AnnotationUtil.getAnnotationsExtent(this.terminals);
      TopTreebankNode node = new TopTreebankNode(jCas, span[0], span[1]);
      node.setNodeType("TOP");
      List<TreebankNode> children = parseStack.peek().children;
      node.setChildren(new FSArray(jCas, children.size()));
      FSCollectionFactory.fillArrayFS(node.getChildren(), children);
      for (TreebankNode child : parseStack.peek().children)
        child.setParent(node);
      node.setTerminals(new FSArray(jCas, this.terminals.size()));
      FSCollectionFactory.fillArrayFS(node.getTerminals(), this.terminals);
      node.addToIndexes();
      parseStack.pop();
      return node;
    }

    private static String readNodeType(BufferedReader r) throws IOException {
      StringBuffer b = new StringBuffer();

      while (r.ready()) {
        r.mark(1);
        char c = (char) r.read();
        if (c == '(' || c == ')' || c == '*') {
          r.reset();
          break;
        } else {
          b.append(c);
        }
      }

      return b.toString();
    }
  }

  private static class PredicateParser {
    JCas jCas;

    // String token;
    String baseForm;

    // String sense;
    Token predicateToken;

    List<SemanticArgument> arguments;

    List<Token> argumentTokens;

    String argumentType;

    PredicateParser(JCas jCas) {
      this.jCas = jCas;
      this.arguments = new ArrayList<SemanticArgument>();
    }

    void feedInfo(String tokenText, String bForm, String sense, Token token) {
      if (token == null) {
        throw new RuntimeException(String.format("token for \"%s\" is null", tokenText));
      }
      // this.token = tokenText;
      this.baseForm = bForm;
      // this.sense = sense;
      this.predicateToken = token;
    }

    void feed(String segment, Token token) throws IOException {
      BufferedReader r = new BufferedReader(new StringReader(segment));

      for (int i = r.read(); i != -1; i = r.read()) {
        char c = (char) i;

        switch (c) {
          case '(':
            this.argumentTokens = new ArrayList<Token>();
            this.argumentType = readArgumentType(r);
            break;
          case ')':
            int[] span = AnnotationUtil.getAnnotationsExtent(this.argumentTokens);
            SemanticArgument arg = new SemanticArgument(jCas, span[0], span[1]);
            arg.addToIndexes();
            Annotation relation = TreebankNodeUtil.selectHighestMatchingTreebankNode(jCas, arg);
            if (relation == null) {
              Chunk chunk = new Chunk(jCas, span[0], span[1]);
              relation = chunk;
            }
            arg.setAnnotation(relation);
            arg.setLabel(this.argumentType);
            arg.addToIndexes();
            this.arguments.add(arg);

            this.argumentTokens = null;
            break;
          case '*':
            if (this.argumentTokens != null)
              this.argumentTokens.add(token);
            break;
          default:
            throw new IOException("unexpected character in string: " + String.valueOf(c) + " ("
                + String.valueOf((int) c) + ")");
        }
      }
    }

    Predicate makePredicate() {
      if (this.predicateToken == null) {
        throw new RuntimeException("no predicateToken found yet");
      }
      Predicate predicate = new Predicate(
          jCas,
          this.predicateToken.getBegin(),
          this.predicateToken.getEnd());
      predicate.setAnnotation(this.predicateToken);
      predicate.setArguments(new FSArray(jCas, this.arguments.size()));
      FSCollectionFactory.fillArrayFS(predicate.getArguments(), this.arguments);
      predicate.setBaseForm(this.baseForm);
      predicate.addToIndexes();

      return predicate;
    }

    private static String readArgumentType(BufferedReader r) throws IOException {
      StringBuffer b = new StringBuffer();

      while (true) {
        r.mark(1);
        int i = r.read();
        if (i == -1)
          break;

        char c = (char) i;
        if (c == '(' || c == ')' || c == '*') {
          r.reset();
          break;
        }

        b.append(c);
      }

      return b.toString();
    }
  }

  private static class NamedEntityParser {

    public NamedEntityParser(JCas view) {
      this.view = view;
    }

    void feed(String segment, Token token) throws IOException {
      BufferedReader r = new BufferedReader(new StringReader(segment));

      for (int i = r.read(); i != -1; i = r.read()) {
        char c = (char) i;

        switch (c) {
          case '(':
            this.currentAnnotation = new NamedEntityAnnotation();
            this.currentAnnotation.begin = token.getBegin();
            this.currentAnnotation.name = readName(r);
            break;
          case ')':
            this.currentAnnotation.end = token.getEnd();

            NamedEntityMention nem = new NamedEntityMention(
                view,
                this.currentAnnotation.begin,
                this.currentAnnotation.end);
            Annotation relation = null;
            try {
              relation = TreebankNodeUtil.selectHighestMatchingTreebankNode(view, nem);
            } catch (NoSuchElementException e) {
            }
            nem.setAnnotation(relation);
            nem.setMentionType(this.currentAnnotation.name);
            nem.addToIndexes();

            this.currentAnnotation = null;
            break;
          case '*':
            break;
          default:
            throw new IOException("unexpected character in string: " + String.valueOf(c) + " ("
                + String.valueOf((int) c) + ")");
        }
      }
    }

    private static String readName(BufferedReader r) throws IOException {
      StringBuffer b = new StringBuffer();

      while (true) {
        r.mark(1);
        int i = r.read();
        if (i == -1)
          break;

        char c = (char) i;
        if (c == '*') {
          r.reset();
          break;
        }

        b.append(c);
      }

      return b.toString();
    }

    JCas view;

    NamedEntityAnnotation currentAnnotation = null;

    private static class NamedEntityAnnotation {
      public NamedEntityAnnotation() {
      }

      int begin;

      int end;

      String name;
    }
  }
}
