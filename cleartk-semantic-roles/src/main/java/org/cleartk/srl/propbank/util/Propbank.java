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
package org.cleartk.srl.propbank.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * 
 * <p>
 * A <em>Propbank object</em> represents one entry in Propbank. It closely reflects the structure of
 * one line in <tt>prop.txt</tt>.
 * </p>
 * 
 * @author Philipp Wetzler, Philip Ogren, Steven Bethard
 */
public class Propbank {
  /**
   * Parses one Propbank entry and returns its representation as a <em>Propbank</em> object.
   * 
   * @param propTxt
   *          one line from <tt>prop.txt</tt>
   * 
   * @return a <em>Propbank</em> object representing <b>propTxt</b>
   */
  public static Propbank fromString(String propTxt) {
    String[] columns = propTxt.split(" ");
    Propbank propbank = new Propbank();
    try {
      propbank.setPropTxt(propTxt);
      // set filename, sentence number and predicate leaf number
      propbank.setFilename(columns[0]);
      propbank.setSentenceNumber(Integer.parseInt(columns[1]));
      propbank.setTerminal(PropbankRelation.fromString(columns[2]));

      // NomBank format - labels start in column 5
      int labelsStart;
      if (columns[5].indexOf(':') >= 0) {
        labelsStart = 5;
        propbank.setBaseForm(columns[3]);
        propbank.setFrameSet(columns[4]);
      }

      // PropBank format - labels start in column 6
      else {
        labelsStart = 6;
        propbank.setTaggerName(columns[3]);
        String[] baseFormAndFrameSet = columns[4].split("\\.");
        propbank.setBaseForm(baseFormAndFrameSet[0]);
        propbank.setFrameSet(baseFormAndFrameSet[1]);
        propbank.setInflectionValue(columns[5]);
      }

      // set each of the labels
      for (int i = labelsStart; i < columns.length; i++) {
        propbank.addProplabel(Proplabel.fromString(columns[i]));
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      throw new PropbankFormatException("invalid Propbank entry: " + propTxt);
    } catch (NumberFormatException e) {
      throw new PropbankFormatException("invalid Propbank entry: " + propTxt);
    }
    return propbank;
  }

  /**
   * A convenience funtion to quickly read only the filename portion of a line from
   * <tt>prop.txt</tt>.
   * 
   * @param propTxt
   *          one line from <tt>prop.txt</tt>
   * 
   * @return the filename part of <b>propTxt</b>
   */
  public static String filenameFromString(String propTxt) {
    return propTxt.split(" ")[0];
  }

  protected String filename;

  protected int sentenceNumber;

  protected PropbankRelation terminal;

  protected String taggerName;

  protected String baseForm;

  protected String frameSet;

  protected String inflectionValue;

  protected List<Proplabel> proplabels;

  protected String propTxt;

  public String getPropTxt() {
    return propTxt;
  }

  public void setPropTxt(String propTxt) {
    this.propTxt = propTxt;
  }

  protected Propbank() {
    proplabels = new ArrayList<Proplabel>();
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getBaseForm() {
    return baseForm;
  }

  public void setBaseForm(String baseForm) {
    this.baseForm = baseForm;
  }

  public String getFrameSet() {
    return frameSet;
  }

  public void setFrameSet(String frameSet) {
    this.frameSet = frameSet;
  }

  public String getInflectionValue() {
    return inflectionValue;
  }

  public void setInflectionValue(String inflectionValue) {
    this.inflectionValue = inflectionValue;
  }

  public int getSentenceNumber() {
    return sentenceNumber;
  }

  public void setSentenceNumber(int sentenceNumber) {
    this.sentenceNumber = sentenceNumber;
  }

  public String getTaggerName() {
    return taggerName;
  }

  public void setTaggerName(String taggerName) {
    this.taggerName = taggerName;
  }

  public PropbankRelation getTerminal() {
    return this.terminal;
  }

  public void setTerminal(PropbankRelation terminal) {
    this.terminal = terminal;
  }

  public List<Proplabel> getPropLabels() {
    return Collections.unmodifiableList(proplabels);
  }

  public void setPropLabels(List<Proplabel> proplabels) {
    this.proplabels.clear();
    if (proplabels != null) {
      this.proplabels.addAll(proplabels);
    }
  }

  public void addProplabel(Proplabel proplabel) {
    this.proplabels.add(proplabel);
  }

  /**
   * Convert to ClearTK <em>Predicate</em> / <em>SemanticArgument</em> annotations and add them to
   * <b>view</b>.
   * 
   * @param view
   * @param topNode
   *          the top node annotation of the corresponding Treebank parse
   * @param sentence
   *          the sentence annotation of the corresponding sentence
   * @return the generated <em>Predicate</em> annotation
   */
  public Predicate convert(JCas view, TopTreebankNode topNode, Sentence sentence) {
    Predicate p = new Predicate(view);
    p.setPropTxt(this.propTxt);
    p.setAnnotation(this.terminal.convert(view, topNode));
    p.setBegin(p.getAnnotation().getBegin());
    p.setEnd(p.getAnnotation().getEnd());
    p.setSentence(sentence);
    p.setFrameSet(this.frameSet);
    p.setBaseForm(this.baseForm);

    List<Argument> aList = new ArrayList<Argument>();
    for (Proplabel proplabel : this.proplabels) {
      aList.add(proplabel.convert(view, topNode));
    }
    p.setArguments(UIMAUtil.toFSArray(view, aList));
    p.addToIndexes();

    return p;
  }

  /**
   * Generate an easily readable multi-line description of this Propbank entry.
   */
  public String displayText() {
    StringBuffer text = new StringBuffer(String.format(
        "filename = %s\n" + "sentence number = %s\n" + "terminal = %s\n" + "base form = %s\n"
            + "frame set = %s\n" + "tagger = %s\n" + "inflection value = %s\n",
        this.getFilename(),
        this.getSentenceNumber(),
        this.getTerminal(),
        this.getBaseForm(),
        this.getFrameSet(),
        this.getTaggerName(),
        this.getInflectionValue()));
    for (Proplabel label : getPropLabels()) {
      text.append(String.format(
          "proplabel = %s %s\n" + "text = %s\n",
          label.getLabel(),
          label.getFeature(),
          label.getRelation()));
    }
    return text.toString();
  }

  /**
   * Re-generate the Propbank format line that this object was parsed from.
   */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    buffer.append(String.format(
        "%s %s %s",
        this.getFilename(),
        this.getSentenceNumber(),
        this.getTerminal()));
    // NomBank
    if (this.getTaggerName() == null) {
      buffer.append(String.format(" %s %s", this.getBaseForm(), this.getFrameSet()));
    }
    // PropBank
    else {
      buffer.append(String.format(
          " %s %s.%s %s",
          this.getTaggerName(),
          this.getBaseForm(),
          this.getFrameSet(),
          this.getInflectionValue()));
    }

    for (Proplabel label : getPropLabels()) {
      buffer.append(' ');
      buffer.append(label);
    }

    return buffer.toString();
  }

}
