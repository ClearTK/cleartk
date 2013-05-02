/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
package org.cleartk.timeml.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.syntax.constituent.type.TreebankNode;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 * @deprecated Use the one in cleartk-feature instead.
 */
@Deprecated
public class TargetPathExtractor {
  public List<Feature> extract(JCas jCas, TreebankNode source, TreebankNode target) {
    List<TreebankNode> sourceToRoot = this.pathToRoot(source);
    List<TreebankNode> targetToRoot = this.pathToRoot(target);
    // TreebankNode commonParent = null;
    while (!sourceToRoot.isEmpty() && !targetToRoot.isEmpty()
        && sourceToRoot.get(sourceToRoot.size() - 1) == targetToRoot.get(targetToRoot.size() - 1)) {
      // commonParent = sourceToRoot.get(sourceToRoot.size() - 1);
      sourceToRoot.remove(sourceToRoot.size() - 1);
      targetToRoot.remove(targetToRoot.size() - 1);
    }
    String value = this.toTagString(targetToRoot, ">");
    return Collections.singletonList(new Feature("TargetPath", value));
  }

  private List<TreebankNode> pathToRoot(TreebankNode leaf) {
    List<TreebankNode> result = new ArrayList<TreebankNode>();
    TreebankNode curr = leaf;
    while (curr != null) {
      result.add(curr);
      curr = curr.getParent();
    }
    return result;
  }

  private String toTagString(List<TreebankNode> nodes, String join) {
    StringBuilder builder = new StringBuilder();
    for (TreebankNode node : nodes) {
      if (builder.length() > 0) {
        builder.append(join);
      }
      builder.append(node.getNodeType());
    }
    return builder.toString();
  }
}
