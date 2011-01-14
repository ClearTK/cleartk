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

import org.apache.uima.jcas.JCas;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 *         <p>
 *         A <em>PropbankNodeRelation object</em> represents a reference to one node in Propbank.
 *         </p>
 * 
 * @author Philipp Wetzler
 */
public class PropbankNodeRelation extends PropbankRelation {

  public static PropbankNodeRelation fromString(String s) {
    String[] fields = s.split(":");

    if (fields.length != 2)
      throw new IllegalArgumentException();

    return new PropbankNodeRelation(Integer.valueOf(fields[0]), Integer.valueOf(fields[1]));
  }

  protected int terminalNumber;

  protected int height;

  public PropbankNodeRelation(int terminalNumber, int height) {
    this.terminalNumber = terminalNumber;
    this.height = height;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getTerminalNumber() {
    return terminalNumber;
  }

  public void setTerminalNumber(int terminalNumber) {
    this.terminalNumber = terminalNumber;
  }

  @Override
  public TreebankNode convert(JCas jCas, TopTreebankNode topNode) {
    TreebankNode node = topNode.getTerminals(this.terminalNumber);
    for (int i = 0; i < this.height; i++)
      node = node.getParent();

    return node;
  }

  @Override
  public String toString() {
    return String.valueOf(getTerminalNumber()) + ":" + String.valueOf(getHeight());
  }
}
