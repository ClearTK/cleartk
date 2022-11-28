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
package org.cleartk.corpus.propbank.util;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.constituent.type.TopTreebankNode;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * 
 * <p>
 * A <em>PropbankRelation object</em> represents the relation (or a sub-relation) of one label of an
 * entry in Propbank.
 * </p>
 * 
 * @author Philipp Wetzler
 */
@Beta
public abstract class PropbankRelation {
  /**
   * Parses a relation taken form a Propbank entry and returns its representation as the appropriate
   * <em>PropbankRelation</em> object.
   * 
   * @param s
   *          the textual representation of a relation taken from {@code prop.txt}
   * 
   * @return a <em>PropbankRelation</em> object representing <b>s</b>
   */
  public static PropbankRelation fromString(String s) {
    if (s.contains("*")) {
      return PropbankCorefRelation.fromString(s);
    } else if (s.contains(",")) {
      return PropbankSplitRelation.fromString(s);
    } else if (s.contains(":")) {
      return PropbankNodeRelation.fromString(s);
    } else {
      return PropbankTerminalRelation.fromString(s);
    }
  }

  /**
   * Convert to an appropriate ClearTK annotation and add it to <b>jCas</b> if necessary.
   * 
   * @param jCas
   *          the view where the annotation will be added
   * @param topNode
   *          the top node annotation of the corresponding Treebank parse
   * @return the corresponding annotation
   */
  public abstract Annotation convert(JCas jCas, TopTreebankNode topNode);

  /**
   * Re-generate the text that this object was parsed from.
   */
  @Override
  public abstract String toString();
}
