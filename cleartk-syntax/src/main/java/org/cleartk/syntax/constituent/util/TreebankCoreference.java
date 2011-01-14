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
public class TreebankCoreference implements TreebankObject {
  List<TreebankObject> chainedNodes;

  public TreebankCoreference() {
    chainedNodes = new ArrayList<TreebankObject>();
  }

  public List<TreebankObject> getNodes() {
    return Collections.unmodifiableList(chainedNodes);
  }

  public void setNodes(List<TreebankObject> relatedNodes) {
    this.chainedNodes.clear();
    if (relatedNodes != null) {
      this.chainedNodes.addAll(relatedNodes);
    }
  }

  public void addNode(TreebankObject relatedNode) {
    this.chainedNodes.add(relatedNode);
  }

  public String getText() {
    StringBuffer text = new StringBuffer("(");
    for (int i = 0; i < chainedNodes.size(); i++) {
      TreebankObject chainedNode = chainedNodes.get(i);
      String txt = chainedNode.getText();
      if (txt.equals("") && chainedNode instanceof TreebankNode)
        txt = ((TreebankNode) chainedNode).getValue();
      text.append(txt);
      if (i < chainedNodes.size() - 1)
        text.append(" || ");
    }
    text.append(")");
    return text.toString();
  }

}
