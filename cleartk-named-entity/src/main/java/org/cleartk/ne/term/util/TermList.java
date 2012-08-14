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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * 
 *         This class provides a very simple data structure for a list of terms. There is no real
 *         expectation that a TermFinder implementation will make any meaningful use of this class
 *         other than to obtain the terms as a collection. A term finder should determine how to
 *         optimally access the contents of a list with its own data structures.
 */
public class TermList {
  private String name;

  private List<Term> terms;

  public TermList(String name) {
    super();
    this.name = name;
    this.terms = new ArrayList<Term>();
  }

  public String getName() {
    return name;
  }

  /**
   * A term finder is not expected to automatically update its internal representation of a term
   * list when this method is called. This method should be called when populating the term list
   * before a term finder calls getTerms().
   */
  public void add(Term term) {
    this.terms.add(term);
  }

  public Collection<Term> getTerms() {
    return Collections.unmodifiableCollection(terms);
  }

  public int size() {
    return terms.size();
  }

  /**
   * Calls loadSimpleFile(listName, file, null).
   * 
   * @see #loadSimpleFile(String, File, String)
   */
  public static TermList loadSimpleFile(String listName, File file) throws IOException {
    return loadSimpleFile(listName, file, null);
  }

  /**
   * This method expects a very simple format. Each line should contain an id and a term delimited
   * by some separator. e.g.:
   * <p>
   * 1|Aixas <br>
   * 2|Aixirivali <br>
   * ... <br>
   * 
   * Ids can be omitted in which case there should be no separator and the id of each term in the
   * list will correspond to the line number that the term appears on.
   * 
   * @param listName
   *          the name of the list
   * @param file
   *          a file that contains one term per line as described above.
   * @param columnSeparator
   *          the string that separates the id from the term. If null is passed in then the id of
   *          each term will be the line number as it appears in the file.
   * @return a term list populated with the data in the provided file
   */
  public static TermList loadSimpleFile(String listName, File file, String columnSeparator)
      throws IOException {

    TermList termList = new TermList(listName);

    BufferedReader input = new BufferedReader(new FileReader(file));
    String line;
    int i = 1;
    try {
      while ((line = input.readLine()) != null) {
        line = line.trim();
        String id;
        String termText;
        if (columnSeparator == null) {
          id = "" + i++;
          termText = line.trim();
        } else {
          String[] columns = line.split(Pattern.quote(columnSeparator));
          id = columns[0];
          termText = columns[1].trim();
        }
        Term term = new Term(id, termText, termList);
        termList.add(term);
        // if (i % 100000 == 0) System.out.println("loaded " + i + " terms from term list: " +
        // file.getName() + ".");
      }
      // System.out.println("loaded " + (i - 1) + " terms from term list: " + file.getName() + ".");
    } finally {
      input.close();
    }
    return termList;
  }
}
