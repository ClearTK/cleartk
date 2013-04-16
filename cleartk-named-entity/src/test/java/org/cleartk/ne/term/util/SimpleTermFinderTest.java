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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.cleartk.token.tokenizer.PennTreebankTokenizer;
import org.cleartk.token.tokenizer.Token;
import org.cleartk.token.tokenizer.Tokenizer;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * @deprecated to be removed for 2.0
 */
@Deprecated
public class SimpleTermFinderTest {

  @Test
  public void testAddTermList() throws IOException {
    TermList usStatesList = TermList.loadSimpleFile("US States", new File(
        "src/test/resources/data/term/termlist/US_States.txt"));
    SimpleTermFinder termFinder = new SimpleTermFinder();
    termFinder.addTermList(usStatesList);

    SimpleTermFinder.Node topNode = termFinder.topNode;
    assertEquals(54, termFinder.topNode.children.size());
    assertNull(topNode.entries);

    Token alabamaToken = new Token(0, 7, "Alabama");
    assertTrue(topNode.containsChild(alabamaToken));
    SimpleTermFinder.Node alabamaNode = topNode.getChild(alabamaToken);
    assertNull(alabamaNode.children);
    Term alabamaTerm = alabamaNode.entries.get(0);
    assertEquals("1", alabamaTerm.getId());
    assertEquals("Alabama", alabamaTerm.getTermText());

    Token newToken = new Token(0, 3, "New");
    assertTrue(topNode.containsChild(newToken));
    SimpleTermFinder.Node newNode = topNode.getChild(newToken);
    assertEquals(4, newNode.children.size());
    assertTrue(newNode.containsChild(new Token(4, 13, "Hampshire")));
    assertTrue(newNode.containsChild(new Token(4, 10, "Mexico")));
    assertTrue(newNode.containsChild(new Token(4, 8, "York")));

    Token jerseyToken = new Token(4, 10, "Jersey");
    assertTrue(newNode.containsChild(jerseyToken));
    SimpleTermFinder.Node jerseyNode = newNode.getChild(jerseyToken);
    Term newJerseyTerm = jerseyNode.entries.get(0);
    assertEquals("35", newJerseyTerm.getId());
    assertEquals("New Jersey", newJerseyTerm.getTermText());
    assertEquals("US States", newJerseyTerm.getTermList().getName());

    SimpleTermFinder.Node minnesotaNode = topNode.getChild(new Token(0, 9, "Minnesota"));
    assertNull(minnesotaNode.children);
    Term minnesotaTerm = minnesotaNode.entries.get(0);
    assertEquals("28", minnesotaTerm.getId());
    assertEquals("Minnesota", minnesotaTerm.getTermText());
    assertEquals("US States", minnesotaTerm.getTermList().getName());

    TermList nstatesList = TermList.loadSimpleFile("N States", new File(
        "src/test/resources/data/term/termlist/N_States.txt"));
    termFinder.addTermList(nstatesList);

    assertEquals(54, termFinder.topNode.children.size());
    assertNull(topNode.entries);

    assertTrue(topNode.containsChild(alabamaToken));
    alabamaNode = topNode.getChild(alabamaToken);
    assertNull(alabamaNode.children);
    alabamaTerm = alabamaNode.entries.get(0);
    assertEquals("1", alabamaTerm.getId());
    assertEquals("Alabama", alabamaTerm.getTermText());
    assertEquals("US States", alabamaTerm.getTermList().getName());

    assertTrue(topNode.containsChild(newToken));
    newNode = topNode.getChild(newToken);
    assertEquals(4, newNode.children.size());
    assertTrue(newNode.containsChild(new Token(4, 13, "Hampshire")));
    assertTrue(newNode.containsChild(new Token(4, 10, "Mexico")));
    assertTrue(newNode.containsChild(new Token(4, 8, "York")));

    assertTrue(newNode.containsChild(jerseyToken));
    jerseyNode = newNode.getChild(jerseyToken);
    newJerseyTerm = jerseyNode.entries.get(0);
    assertEquals("35", newJerseyTerm.getId());
    assertEquals("New Jersey", newJerseyTerm.getTermText());
    assertEquals("US States", newJerseyTerm.getTermList().getName());
    Term newJerseyTerm2 = jerseyNode.entries.get(1);
    assertEquals("4", newJerseyTerm2.getId());
    assertEquals("New Jersey", newJerseyTerm2.getTermText());
    assertEquals("N States", newJerseyTerm2.getTermList().getName());
    Term newJerseyTerm3 = jerseyNode.entries.get(2);
    assertEquals("10", newJerseyTerm3.getId());
    assertEquals("New Jersey", newJerseyTerm3.getTermText());
    assertEquals("N States", newJerseyTerm3.getTermList().getName());

    SimpleTermFinder.Node nevadaNode = topNode.getChild(new Token(0, 6, "Nevada"));
    assertNull(nevadaNode.children);
    Term nevadaTerm1 = nevadaNode.entries.get(0);
    assertEquals("33", nevadaTerm1.getId());
    assertEquals("Nevada", nevadaTerm1.getTermText());
    assertEquals("US States", nevadaTerm1.getTermList().getName());
    Term nevadaTerm2 = nevadaNode.entries.get(1);
    assertEquals("2", nevadaTerm2.getId());
    assertEquals("Nevada", nevadaTerm2.getTermText());
    assertEquals("N States", nevadaTerm2.getTermList().getName());

  }

  @Test
  public void testGetMatches() {

    Tokenizer tokenizer = new PennTreebankTokenizer();

    TermList termList = new TermList("test term list");
    termList.add(new Term("1", "New", termList));
    termList.add(new Term("2", "New Jersey", termList));
    termList.add(new Term("3", "New Jersey", termList));
    termList.add(new Term("4", "New Mexico", termList));
    termList.add(new Term("5", "New York", termList));
    termList.add(new Term("6", "New Hampshire", termList));
    termList.add(new Term("7", "New York, New Hampshire", termList));
    termList.add(new Term("8", "Mexico, New York, New Hampshire", termList));

    TermFinder termFinder = new SimpleTermFinder(true, new PennTreebankTokenizer());
    termFinder.addTermList(termList);

    List<TermMatch> termMatches = termFinder.getMatches(tokenizer.getTokens("New"));
    assertEquals(1, termMatches.size());
    assertEquals("New", termMatches.get(0).getTerm().getTermText());

    termMatches = termFinder.getMatches(tokenizer.getTokens("New Mexico"));
    assertEquals(2, termMatches.size());
    assertEquals("New", termMatches.get(0).getTerm().getTermText());
    assertEquals("New Mexico", termMatches.get(1).getTerm().getTermText());

    termMatches = termFinder.getMatches(tokenizer
        .getTokens("She drove from New Jersey to New Mexico."));

    assertEquals(5, termMatches.size());
    assertEquals("New", termMatches.get(0).getTerm().getTermText());
    assertEquals("New Jersey", termMatches.get(1).getTerm().getTermText());
    assertEquals("2", termMatches.get(1).getTerm().getId());
    assertEquals("New Jersey", termMatches.get(2).getTerm().getTermText());
    assertEquals("3", termMatches.get(2).getTerm().getId());
    assertEquals("New", termMatches.get(3).getTerm().getTermText());
    assertEquals("New Mexico", termMatches.get(4).getTerm().getTermText());

    termMatches = termFinder.getMatches(tokenizer
        .getTokens("New Jersey, New Mexico, New York, New Hampshire."));
    assertEquals(11, termMatches.size());
    assertEquals("New", termMatches.get(0).getTerm().getTermText());
    assertEquals("New Jersey", termMatches.get(1).getTerm().getTermText());
    assertEquals("New Jersey", termMatches.get(2).getTerm().getTermText());
    assertEquals("New", termMatches.get(3).getTerm().getTermText());
    assertEquals("New Mexico", termMatches.get(4).getTerm().getTermText());
    assertEquals("New", termMatches.get(5).getTerm().getTermText());
    assertEquals("New York", termMatches.get(6).getTerm().getTermText());
    assertEquals("New", termMatches.get(7).getTerm().getTermText());
    assertEquals("Mexico, New York, New Hampshire", termMatches.get(8).getTerm().getTermText());
    assertEquals("New York, New Hampshire", termMatches.get(9).getTerm().getTermText());
    assertEquals("New Hampshire", termMatches.get(10).getTerm().getTermText());

  }

  @SuppressWarnings({ "unused", "null" })
  @Test
  public void testTime() throws IOException {
    long startLoad = System.nanoTime();

    // change these three variables to run your experiment
    String termListFileName = null;
    String plainTextFileName = null;
    boolean caseSensitive = false;

    if (termListFileName == null || plainTextFileName == null)
      return;
    TermList termList = TermList.loadSimpleFile("names", new File(termListFileName));
    long stopLoad = System.nanoTime();
    float timeElapsed = (float) (stopLoad - startLoad) / 1000000000;
    System.out.println(String.format(
        "loaded %1$d terms in %2$.3f seconds.",
        termList.size(),
        timeElapsed));

    Tokenizer tokenizer = new PennTreebankTokenizer();

    long startInitialize = System.nanoTime();
    TermFinder termFinder = new SimpleTermFinder(caseSensitive, tokenizer);
    termFinder.addTermList(termList);
    long stopInitialize = System.nanoTime();
    timeElapsed = (float) (stopInitialize - startInitialize) / 1000000000;
    System.out.println(String.format(
        "initialized simple term finder in %2$.3f seconds.",
        timeElapsed));

    long start;
    long stop;
    long finderTime = 0;
    long tokenizerTime = 0;

    long startFinder = System.nanoTime();
    int matchesCount = 0;
    int tokensCount = 0;
    BufferedReader input = new BufferedReader(new FileReader(plainTextFileName));
    String line = null;
    while ((line = input.readLine()) != null) {
      start = System.nanoTime();
      List<Token> tokens = tokenizer.getTokens(line);
      stop = System.nanoTime();
      tokenizerTime += (stop - start);
      tokensCount += tokens.size();
      start = System.nanoTime();
      List<TermMatch> matches = termFinder.getMatches(tokens);
      stop = System.nanoTime();
      finderTime += (stop - start);
      matchesCount += matches.size();
    }
    long stopFinder = System.nanoTime();
    float time = (float) (stopFinder - startFinder) / 1000000000;
    System.out.println(String.format(
        "found %1$s term matches in %2$s tokens in %3$.3f seconds.",
        matchesCount,
        tokensCount,
        time));
    time = (float) tokenizerTime / 1000000000;
    System.out.println(String.format("Time spent tokenizing was %3$.3f seconds.", time));
    time = (float) finderTime / 1000000000;
    System.out.println(String.format("Time spent finding was %3$.3f seconds.", time));

  }

  @Test
  public void testname() throws Exception {
    float time = (float) 112300000000l / 1000000000;
    System.out.println(String.format("%.3f", time));
    System.out.println(String.format("loaded %1$d terms in %2$.3f seconds.", 1234, time));

  }

}
