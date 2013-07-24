/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.feature.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.annotationpair.FeatureExtractor2;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil.TreebankNodePath;

import com.google.common.base.Joiner;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class SyntacticLeafToLeafPathPartsExtractor<T extends Annotation, U extends Annotation> implements FeatureExtractor2<T, U> {

  public List<Feature> extract(JCas jCas, T source, U target) {
    List<Feature> features = new ArrayList<Feature>();
    TreebankNode sourceNode = TreebankNodeUtil.selectMatchingLeaf(jCas, source);
    TreebankNode targetNode = TreebankNodeUtil.selectMatchingLeaf(jCas, target);
    if (sourceNode != null && targetNode != null) {
      TreebankNodePath path = TreebankNodeUtil.getPath(sourceNode, targetNode);
      TreebankNode ancestor = path.getCommonAncestor();
      features.add(new Feature("CommonAncestor", ancestor == null ? null : ancestor.getNodeType()));
      features.add(new Feature("SourceToAncestor", pathString(path.getSourceToAncestorPath())));
      features.add(new Feature("TargetToAncestor", pathString(path.getTargetToAncestorPath())));
    }
    return features;
  }

  private static String pathString(List<TreebankNode> nodes) {
    // strip the first node from the list
    nodes = nodes.subList(Math.min(1, nodes.size()), nodes.size());

    // join the types with underscores
    List<String> types = new ArrayList<String>();
    for (TreebankNode node : nodes) {
      types.add(node.getNodeType());
    }
    return Joiner.on('_').join(types);
  }

}
