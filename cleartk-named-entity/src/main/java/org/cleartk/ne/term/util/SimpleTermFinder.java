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
package org.cleartk.ne.term.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.cleartk.token.tokenizer.PennTreebankTokenizer;
import org.cleartk.token.tokenizer.Token;
import org.cleartk.token.tokenizer.Tokenizer;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * This class provides a very simple and fast term finder. It performs a simple left to right term
 * matching doing a single pass through the text token by token. It finds only exact matches where
 * the terms are found in the text exactly as they appear (with the option of case insensitive
 * matching) in the term list.
 * 
 * I performed a very simple test with throughput and got the following results:
 * <ul>
 * <li>loaded 2 million terms in 10.182 seconds using TermList.loadSimpleFile()
 * <li>initialize instance of SimpleTermFinder with list by calling addTermList(). This took 96.416
 * seconds.
 * <li>A file containing 495,503 tokens was read in line by line (about 4000 lines) and tokenized.
 * Time spent tokenizing was 8.567 seconds.
 * <li>Each line was passed into the term finder getMatches() method one after the next after being
 * tokenized. 8,633 term matches were found in the 495,503 tokens in .243 seconds.
 * </ul>
 * This experiment was run on a dual-core 1.99GHz Pentium. The code for this experiment can be found
 * in SimpleTermFinderTests.testTime()
 * 
 * @author Philip Ogren
 * @deprecated to be removed in 2.0
 */
@Deprecated
public class SimpleTermFinder implements TermFinder {

  Tokenizer tokenizer;

  boolean caseSensitive = true;

  Node topNode;

  public SimpleTermFinder(boolean caseSensitive, Tokenizer tokenizer) {

    this.caseSensitive = caseSensitive;
    this.tokenizer = tokenizer;

    topNode = new Node();
  }

  public SimpleTermFinder() {
    this(true, new PennTreebankTokenizer());
  }

  public void addTermList(TermList termList) {
    for (Term entry : termList.getTerms()) {

      List<Token> tokens = tokenizer.getTokens(entry.getTermText());

      if (!caseSensitive) {
        List<Token> lowerTokens = new ArrayList<Token>(tokens.size());
        for (Token token : tokens) {
          lowerTokens.add(new Token(token.getBegin(), token.getEnd(), token
              .getTokenText()
              .toLowerCase()));
        }
        tokens = lowerTokens;
      }

      Node node = topNode;
      for (Token token : tokens) {
        if (!node.containsChild(token)) {
          node.addChild(token);
        }
        node = node.getChild(token);
      }
      node.addEntry(entry);
    }
  }

  private List<TermMatch> getMatches(Token token, LinkedList<Candidate> candidates) {
    List<TermMatch> returnValues = new ArrayList<TermMatch>();

    String tokenText = token.getTokenText();
    if (!caseSensitive)
      tokenText = tokenText.toLowerCase();

    if (topNode.containsChild(token)) {
      candidates.add(new Candidate());
    }

    ListIterator<Candidate> candidatesIterator = candidates.listIterator();
    while (candidatesIterator.hasNext()) {
      Candidate candidate = candidatesIterator.next();
      if (candidate.node.containsChild(token)) {
        candidate.addToken(token);
        Node childNode = candidate.node.getChild(token);

        List<Term> entries = childNode.entries;
        if (entries != null) {
          for (Term entry : entries) {
            returnValues.add(createTermMatch(candidate.tokens, entry));
          }
        }
        if (childNode.hasChildren())
          candidate.node = childNode;
        else
          candidatesIterator.remove();
      } else {
        candidatesIterator.remove();
      }
    }
    return returnValues;
  }

  /**
   * @param tokens
   *          It is the responsibility of the caller of this method to be sure that the same
   *          tokenizer is used to create the tokens passed in that is passed into the constructor.
   * @see #SimpleTermFinder(boolean, Tokenizer)
   */
  public List<TermMatch> getMatches(List<Token> tokens) {
    LinkedList<Candidate> candidates = new LinkedList<Candidate>();
    List<TermMatch> returnValues = new ArrayList<TermMatch>();

    for (Token token : tokens) {
      returnValues.addAll(getMatches(token, candidates));
    }
    return returnValues;
  }

  private TermMatch createTermMatch(List<Token> tokens, Term entry) {

    if (tokens == null || tokens.size() == 0)
      return null;

    int begin = tokens.get(0).getBegin();
    int end = tokens.get(tokens.size() - 1).getEnd();
    return new TermMatch(begin, end, entry);
  }

  private class Candidate {
    List<Token> tokens;

    Node node;

    Candidate() {
      tokens = new ArrayList<Token>();
      node = topNode;
    }

    public void addToken(Token token) {
      tokens.add(token);
    }
  }

  class Node {
    List<Term> entries;

    Map<String, Node> children;

    public boolean containsChild(Token child) {
      if (children == null) {
        return false;
      } else
        return children.containsKey(child.getTokenText());
    }

    public boolean hasChildren() {
      if (children == null)
        return false;
      else if (children.size() == 0)
        return false;
      else
        return true;
    }

    public void addChild(Token child) {
      if (children == null) {
        children = new HashMap<String, Node>();
      }
      children.put(child.getTokenText(), new Node());
    }

    public Node getChild(Token child) {
      if (children == null || !children.containsKey(child.getTokenText())) {
        return null;
      }
      return children.get(child.getTokenText());
    }

    public void addEntry(Term entry) {
      if (entries == null)
        entries = new ArrayList<Term>();
      entries.add(entry);
    }

  }
}
