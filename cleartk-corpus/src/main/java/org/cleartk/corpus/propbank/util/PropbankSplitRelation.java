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
import org.cleartk.util.AnnotationUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * <p>
 * A <em>PropbankSplitRelation object</em> represents a reference to multiple relations that are
 * part of a split argument in Propbank.
 * </p>
 * 
 * @author Philipp Wetzler
 */
public class PropbankSplitRelation extends PropbankRelation {

  public static PropbankSplitRelation fromString(String s) {
    PropbankSplitRelation rel = new PropbankSplitRelation();
    String[] fields = s.split(",");
    for (String field : fields) {
      rel.addRelation(PropbankRelation.fromString(field));
    }
    return rel;
  }

  protected List<PropbankRelation> relations;

  public PropbankSplitRelation() {
    this.relations = new ArrayList<PropbankRelation>();
  }

  public List<PropbankRelation> getRelations() {
    return this.relations;
  }

  public void addRelation(PropbankRelation rel) {
    this.relations.add(rel);
  }

  @Override
  public Annotation convert(JCas view, TopTreebankNode topNode) {
    Annotation annotation = new Annotation(view);

    List<Annotation> subAnnotations = new ArrayList<Annotation>();
    for (PropbankRelation rel : this.relations) {
      subAnnotations.add(rel.convert(view, topNode));
    }
    // annotation.setAnnotations(UIMAUtil.toFSArray(view, subAnnotations));
    int[] span = AnnotationUtil.getAnnotationsExtent(subAnnotations);
    annotation.setBegin(span[0]);
    annotation.setEnd(span[1]);
    annotation.addToIndexes();

    return annotation;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    boolean first = true;
    for (PropbankRelation rel : getRelations()) {
      if (!first)
        buffer.append(",");
      buffer.append(rel.toString());
      first = false;
    }

    return buffer.toString();
  }
}
