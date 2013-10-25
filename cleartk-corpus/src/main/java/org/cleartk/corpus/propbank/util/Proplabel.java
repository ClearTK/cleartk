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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.util.AnnotationUtil;
import org.apache.uima.fit.util.FSCollectionFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * <p>
 * A <em>Proplabel object</em> represents one label of an entry in Propbank.
 * </p>
 * 
 * @author Philipp Wetzler, Steven Bethard
 */
public class Proplabel {
  /**
   * Parses one label taken form a Propbank entry and returns its representation as a
   * <em>Proplabel</em> object.
   * 
   * @param lblTxt
   *          one label part of one line from <tt>prop.txt</tt>
   * 
   * @return a <em>Proplabel</em> object representing <b>lblTxt</b>
   */
  static Proplabel fromString(String lblTxt) {
    // split the string by hyphens and catch some simple errors
    String[] columns = lblTxt.split("-");
    if (columns.length < 1) {
      throw new PropbankFormatException(String.format("Missing label: %s", lblTxt));
    }
    if (!Proplabel.labels.contains(columns[1])) {
      throw new PropbankFormatException(String.format("Invalid label: %s", columns[1]));
    }

    // set the relation and label
    Proplabel proplabel = new Proplabel();
    proplabel.setPropTxt(lblTxt);
    proplabel.setRelation(PropbankRelation.fromString(columns[0]));
    proplabel.setLabel(columns[1]);

    // second column may be feature, hyphen tag or preposition
    // third column may only be hyphen tag following feature
    int expectedLength = 2;
    if (columns.length > 2) {
      if (Proplabel.features.contains(columns[2])) {
        proplabel.setFeature(columns[2]);
        if (columns.length > 3) {
          if (Proplabel.hyphenTags.contains(columns[3])) {
            proplabel.setHyphenTag(columns[3]);
          }
          expectedLength = 4;
        } else {
          expectedLength = 3;
        }
      } else if (Proplabel.hyphenTags.contains(columns[2])) {
        proplabel.setHyphenTag(columns[2]);
        expectedLength = 3;
      } else {
        proplabel.setPreposition(columns[2]);
        expectedLength = 3;
      }
    }

    // throw some exceptions for bad input
    if (columns.length != expectedLength) {
      throw new PropbankFormatException(String.format(
          "Expected %d items, found %d",
          expectedLength,
          columns.length));
    }
    if (Proplabel.labelsRequiringFeatures.contains(columns[1])) {
      if (proplabel.getFeature() == null) {
        throw new PropbankFormatException(String.format(
            "Label %s requires a feature",
            proplabel.getLabel()));
      }
    }
    return proplabel;
  }

  protected PropbankRelation relation;

  protected String label;

  protected String feature;

  protected String preposition;

  protected String hyphenTag;

  protected String propTxt;

  protected Proplabel() {
    relation = null;
    label = null;
    feature = null;
    preposition = null;
  }

  public String getFeature() {
    return feature;
  }

  public void setFeature(String feature) {
    this.feature = feature;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getPreposition() {
    return preposition;
  }

  public void setPreposition(String preposition) {
    this.preposition = preposition;
  }

  public String getHyphenTag() {
    return hyphenTag;
  }

  public void setHyphenTag(String hyphenTag) {
    this.hyphenTag = hyphenTag;
  }

  public PropbankRelation getRelation() {
    return relation;
  }

  public void setRelation(PropbankRelation relation) {
    this.relation = relation;
  }

  public String getPropTxt() {
    return propTxt;
  }

  public void setPropTxt(String propTxt) {
    this.propTxt = propTxt;
  }

  /**
   * Convert to ClearTK <em>SemanticArgument</em> annotation and add it to <b>view</b>.
   * 
   * @param view
   *          the view where the annotation will be added
   * @param topNode
   *          the top node annotation of the corresponding Treebank parse
   * @return the generated <em>SemanticArgument</em> annotation
   */
  public SemanticArgument convert(JCas view, TopTreebankNode topNode) {
    SemanticArgument argument = new SemanticArgument(view);
    argument.setPropTxt(this.propTxt);
    argument.setLabel(this.label);
    argument.setFeature(this.feature);
    argument.setPreposition(this.preposition);
    argument.setHyphenTag(this.hyphenTag);
    if (this.relation instanceof PropbankCorefRelation) {
      List<Annotation> annotations = new ArrayList<Annotation>();
      List<Annotation> substantiveAnnotations = new ArrayList<Annotation>();

      for (PropbankRelation rel : ((PropbankCorefRelation) this.relation).getCorefRelations()) {
        Annotation a = rel.convert(view, topNode);
        annotations.add(a);
        if (a.getBegin() != a.getEnd())
          substantiveAnnotations.add(a);
      }
      argument.setCoreferenceAnnotations(new FSArray(view, annotations.size()));
      FSCollectionFactory.fillArrayFS(argument.getCoreferenceAnnotations(), annotations);

      int[] extent = AnnotationUtil.getAnnotationsExtent(substantiveAnnotations);
      argument.setBegin(extent[0]);
      argument.setEnd(extent[1]);

      if (substantiveAnnotations.size() == 1) {
        argument.setAnnotation(substantiveAnnotations.get(0));
      }
    } else {
      argument.setAnnotation(this.relation.convert(view, topNode));
      argument.setBegin(argument.getAnnotation().getBegin());
      argument.setEnd(argument.getAnnotation().getEnd());
    }
    argument.addToIndexes();

    return argument;
  }

  /**
   * Re-generate the Propbank text that this object was parsed from.
   */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    buffer.append(getRelation().toString());
    buffer.append("-" + getLabel());
    if (getFeature() != null)
      buffer.append("-" + getFeature());
    if (getHyphenTag() != null)
      buffer.append("-" + getHyphenTag());
    if (getPreposition() != null)
      buffer.append("-" + getPreposition());

    return buffer.toString();
  }

  private static final Set<String> labels = new HashSet<String>(
      Arrays.asList("rel|Support|ARG0|ARG1|ARG2|ARG3|ARG4|ARG5|ARGA|ARGM".split("\\|")));

  private static final Set<String> labelsRequiringFeatures = new HashSet<String>(
      Arrays.asList(new String[] { "ARGM" }));

  private static final Set<String> features = new HashSet<String>(
      Arrays.asList("ADV|CAU|DIR|DIS|EXT|LOC|MNR|MOD|NEG|PNC|PRD|REC|TMP".split("\\|")));

  private static final Set<String> hyphenTags = new HashSet<String>(
      Arrays.asList("H0|H1|H2|H3|H4|H5|H6|H7|H8|H9|XX".split("\\|")));

}
