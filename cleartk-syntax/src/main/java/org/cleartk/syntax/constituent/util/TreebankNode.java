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
package org.cleartk.syntax.constituent.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class TreebankNode implements TreebankObject {
  private TreebankNode parent = null;

  private List<TreebankNode> children;

  private TopTreebankNode topNode = null;

  public TopTreebankNode getTopNode() {
    return topNode;
  }

  public void setTopNode(TopTreebankNode top) {
    this.topNode = top;
  }

  public String getTreebankParse() {
    return getTopNode().getTreebankParse().substring(parseBegin, parseEnd);
  }

  private int parseBegin;

  private int parseEnd;

  private int textBegin;

  private int textEnd;

  private String type;

  private String value;

  private String text;

  private String[] tags;

  private boolean leaf;

  public boolean isLeaf() {
    return leaf;
  }

  public void setLeaf(boolean leafNode) {
    this.leaf = leafNode;
  }

  public TreebankNode() {
    children = new ArrayList<TreebankNode>();
  }

  public List<TreebankNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  public void setChildren(List<TreebankNode> children) {
    this.children.clear();
    if (children != null)
      this.children.addAll(children);
  }

  public void addChild(TreebankNode child) {
    children.add(0, child);
  }

  public TreebankNode getParent() {
    return parent;
  }

  public TreebankNode getAncestor(int levels) {
    if (levels < 0)
      return null;
    TreebankNode returnNode = this;
    for (int i = 0; i < levels; i++) {
      returnNode = returnNode.getParent();
      if (returnNode == null)
        break;
    }
    return returnNode;
  }

  public void setParent(TreebankNode parent) {
    this.parent = parent;
  }

  /**
   * @return the offset of the parse text for this node with respect to the sentence parse. For
   *         example, if the entire sentence parse was "(NP-LOC (NNP Calif.) )" and this node
   *         corresponded to only "(NNP Calif.)", then this method would return 8.
   */
  public int getParseBegin() {
    return parseBegin;
  }

  public int getParseEnd() {
    return parseEnd;
  }

  public void setParseBegin(int begin) {
    this.parseBegin = begin;
  }

  public void setParseEnd(int end) {
    this.parseEnd = end;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return a "pretty print" of this node that may be useful for e.g. debugging.
   */
  public String displayText() {
    return displayText(0);
  }

  private String displayText(int tabs) {
    StringBuffer returnValue = new StringBuffer();
    String tabString = getTabs(tabs);
    returnValue.append(tabString + getType());
    if (getValue() != null)
      returnValue.append(":" + getValue() + "\n");
    else {
      returnValue.append(":" + getText() + "\n");
    }
    if (getChildren().size() > 0) {
      for (TreebankNode child : getChildren()) {
        returnValue.append(child.displayText(tabs + 1));
      }
    }
    return returnValue.toString();
  }

  private String getTabs(int tabs) {
    StringBuffer returnValue = new StringBuffer();
    for (int i = 0; i < tabs; i++) {
      returnValue.append("  ");
    }
    return returnValue.toString();
  }

  public String[] getTags() {
    return tags;
  }

  public void setTags(String[] tags) {
    this.tags = tags;
  }

  public int getTextBegin() {
    return textBegin;
  }

  /**
   * the offset of the plain text for this node with respect to the sentence text adjusted by the
   * textOffset passed into the parse method. For example, if the entire sentence parse was
   * "(NP-LOC (NNP Calif.) )" and this node corresponded to only "(NNP Calif.)", then
   * {@link #getText()} would return "Calif." and this method would return 0 plus the textOffset
   * given by {@link TreebankFormatParser#parse(String, String, int)}.
   */
  public void setTextBegin(int textBegin) {
    this.textBegin = textBegin;
  }

  public int getTextEnd() {
    return textEnd;
  }

  public void setTextEnd(int textEnd) {
    this.textEnd = textEnd;
  }

}
