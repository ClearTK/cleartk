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

import java.util.ArrayList;
import java.util.List;

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
 * <p>
 * A <em>PropbankCorefRelation object</em> represents a reference to multiple relations that are
 * part of a coreferenced argument in Propbank.
 * </p>
 * 
 * @author Philipp Wetzler
 */
@Beta
public class PropbankCorefRelation extends PropbankRelation {

  public static PropbankCorefRelation fromString(String s) {
    PropbankCorefRelation rel = new PropbankCorefRelation();
    String[] fields = s.split("\\*");
    for (String field : fields) {
      rel.addCorefRelation(PropbankRelation.fromString(field));
    }
    return rel;
  }

  protected List<PropbankRelation> corefRelations;

  public PropbankCorefRelation() {
    this.corefRelations = new ArrayList<PropbankRelation>();
  }

  public List<PropbankRelation> getCorefRelations() {
    return this.corefRelations;
  }

  public void addCorefRelation(PropbankRelation rel) {
    this.corefRelations.add(rel);
  }

  /**
   * This is not implemented for PropbankCorefRelation and will always throw
   * {@link UnsupportedOperationException}. Instead, the conversion of a PropbankCorefRelation
   * happens during Predicate conversion.
   */
  @Override
  public Annotation convert(JCas jCas, TopTreebankNode topNode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    boolean first = true;
    for (PropbankRelation rel : getCorefRelations()) {
      if (!first)
        buffer.append("*");
      buffer.append(rel.toString());
      first = false;
    }

    return buffer.toString();
  }

}
