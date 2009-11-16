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
package org.cleartk.srl.conll2005;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ViewNames;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Chunk;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.AnnotationUtil;
import org.cleartk.util.UIMAUtil;
import org.uutuc.descriptor.SofaCapability;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
@SofaCapability(inputSofas = { ViewNames.CONLL_2005, ViewNames.DEFAULT }, outputSofas = {})
public class Conll2005GoldAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			JCas conllView = jCas.getView(ViewNames.CONLL_2005);
			JCas initView = jCas.getView(ViewNames.DEFAULT);

			String conllText = conllView.getSofaDataString();

			List<CoNLL2005Line> conll2005Lines = new ArrayList<CoNLL2005Line>();
			for (String line : conllText.split("\n")) {
				conll2005Lines.add(new CoNLL2005Line(line));
			}

			StringBuffer docText = new StringBuffer();

			List<TreebankNode> terminals = new ArrayList<TreebankNode>(
					conll2005Lines.size());
			CharniakParseParser parser = new CharniakParseParser(initView);

			int numberOfPredicates = conll2005Lines.get(0).argumentSegments.length;
			int currentPredicate = 0;
			PredicateParser predicateParsers[] = new PredicateParser[numberOfPredicates];
			for (int i = 0; i < numberOfPredicates; i++)
				predicateParsers[i] = new PredicateParser(initView);

			for (CoNLL2005Line line : conll2005Lines
					.toArray(new CoNLL2005Line[0])) {
				if (line.argumentSegments.length != numberOfPredicates) {
					throw new RuntimeException(String.format(
							"expected %d segments, found %d",
							numberOfPredicates, line.argumentSegments.length));
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

				TreebankNode terminal = new TreebankNode(initView, startIndex,
						endIndex);
				terminal.setNodeType(line.pos);
				terminal.setNodeValue(line.word);
				terminal.setChildren(UIMAUtil.toFSArray(jCas, Collections
						.<TreebankNode> emptyList()));
				terminal.setLeaf(true);
				terminal.addToIndexes();
				terminals.add(terminal);

				parser.feed(line.charniakParseSegment, terminal);

				for (int i = 0; i < numberOfPredicates; i++) {
					predicateParsers[i].feed(line.argumentSegments[i], token);
				}

				if (!line.verbBaseForm.equals("-")) {
					predicateParsers[currentPredicate].feedInfo(line.word,
							line.verbBaseForm, line.verbSenseTag, token);
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

	private static class CoNLL2005Line {
		String word;
		String pos;
		String charniakParseSegment;
		// String neSegment;
		String verbSenseTag;
		String verbBaseForm;
		String argumentSegments[];

		CoNLL2005Line(String line) {
			String fields[] = line.split("\\s+");
			this.word = fields[0].trim();
			this.pos = fields[1].trim();
			this.charniakParseSegment = fields[2].trim();
			// this.neSegment = fields[3].trim();
			this.verbSenseTag = fields[4].trim();
			this.verbBaseForm = fields[5].trim();
			this.argumentSegments = new String[fields.length - 6];
			for (int i = 6; i < fields.length; i++)
				this.argumentSegments[i - 6] = fields[i].trim();
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
				node.setChildren(UIMAUtil.toFSArray(jCas, this.children));
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
					throw new IOException("unexpected character in string: "
							+ String.valueOf(c) + " ("
							+ String.valueOf((int) c) + ")");
				}
			}
		}

		public TopTreebankNode makeParse() {
			int[] span = AnnotationUtil.getAnnotationsExtent(this.terminals);
			TopTreebankNode node = new TopTreebankNode(jCas, span[0], span[1]);
			node.setNodeType("TOP");
			node.setChildren(UIMAUtil.toFSArray(jCas,
					parseStack.peek().children));
			for (TreebankNode child : parseStack.peek().children)
				child.setParent(node);
			node.setTerminals(UIMAUtil.toFSArray(jCas, this.terminals));
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

		void feedInfo(String tokenText, String baseForm, String sense,
				Token token) {
			if (token == null) {
				throw new RuntimeException(String.format(
						"token for \"%s\" is null", tokenText));
			}
			// this.token = tokenText;
			this.baseForm = baseForm;
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
					Annotation relation = AnnotationRetrieval.getMatchingAnnotation(
							jCas, arg, TreebankNode.class);
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
					throw new IOException("unexpected character in string: "
							+ String.valueOf(c) + " ("
							+ String.valueOf((int) c) + ")");
				}
			}
		}

		Predicate makePredicate() {
			if (this.predicateToken == null) {
				throw new RuntimeException("no predicateToken found yet");
			}
			Predicate predicate = new Predicate(jCas, this.predicateToken
					.getBegin(), this.predicateToken.getEnd());
			predicate.setAnnotation(this.predicateToken);
			predicate.setArguments(UIMAUtil.toFSArray(jCas, this.arguments));
			predicate.setBaseForm(this.baseForm);
			predicate.addToIndexes();

			return predicate;
		}

		private static String readArgumentType(BufferedReader r)
				throws IOException {
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
}
