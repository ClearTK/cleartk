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
package org.cleartk.syntax.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.annotationpair.AnnotationPairFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.syntax.constituent.type.TreebankNode;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * 
 */
public class SyntacticPathExtractor implements AnnotationPairFeatureExtractor {
  protected final String UP_SEPARATOR = "::";

  protected final String DOWN_SEPARATOR = ";;";

  protected SimpleFeatureExtractor pathMemberExtractor;

  protected String name;

  protected boolean isPartial;

  /**
   * 
   * @param pathMemberExtractor
   *          this extractor will be used to get a feature for every node on the path, which will
   *          then be combined to form a single string. The extractor should preferably generate
   *          exactly one <tt>StringFeature</tt>, but must generate at least one
   *          <tt>StringFeature</tt>, <tt>LongFeature</tt>, <tt>DoubleFeature</tt>, or
   *          <tt>BooleanFeature</tt>. Only the first feature will then be used and naively
   *          converted to a string.
   * @param partial
   *          if true, generate a partial path only, i.e. from the first node up to the lowest
   *          common ancestor of the two
   */
  public SyntacticPathExtractor(SimpleFeatureExtractor pathMemberExtractor, boolean partial) {
    this.pathMemberExtractor = pathMemberExtractor;
    this.isPartial = partial;
  }

  /**
   * This constructor defaults to a full rather than a partial path.
   */
  public SyntacticPathExtractor(SimpleFeatureExtractor pathMemberExtractor) {
    this(pathMemberExtractor, false);
  }

  /**
   * Extract a string representation of a path feature.
   * 
   * @param leftAnnotation
   *          the first node of the path
   * 
   * @param rightAnnotation
   *          the last node of the path
   * 
   * @return List of one <em>StringFeature</em>, which contains a string representation of the path
   *         between the two nodes.
   * 
   */
  public List<Feature> extract(JCas view, Annotation leftAnnotation, Annotation rightAnnotation)
      throws CleartkException {
    TreebankNode leftConstituent;
    TreebankNode rightConstituent;
    try {
      leftConstituent = (TreebankNode) leftAnnotation;
      rightConstituent = (TreebankNode) rightAnnotation;
    } catch (ClassCastException e) {
      throw new CleartkException("annotation is not of type TreebankNode");
    }

    List<TreebankNode> fromStart = getPathToRoot(leftConstituent);
    List<TreebankNode> fromEnd = getPathToRoot(rightConstituent);
    String pathFeatureName = null;
    String lengthFeatureName = null;

    fromEnd.remove(fromEnd.size() - 1);
    while (fromStart.size() > 1 && fromEnd.size() > 0
        && fromStart.get(fromStart.size() - 2) == fromEnd.get(fromEnd.size() - 1)) {
      fromStart.remove(fromStart.size() - 1);
      fromEnd.remove(fromEnd.size() - 1);
    }

    int length = fromStart.size();
    if (!isPartial)
      length += fromEnd.size();

    try {
      ListIterator<TreebankNode> it = fromStart.listIterator();
      StringBuffer pathBuffer = new StringBuffer();
      boolean first = true;
      while (it.hasNext()) {
        Feature feature = this.pathMemberExtractor.extract(view, it.next()).get(0);
        if (first) {
          String s = feature.getName();
          if (isPartial) {
            pathFeatureName = Feature.createName(name, "PartialSyntacticPath(" + s + ")");
            lengthFeatureName = Feature.createName(name, "PartialSyntacticPath", "Length");
          } else {
            pathFeatureName = Feature.createName(name, "SyntacticPath(" + s + ")");
            lengthFeatureName = Feature.createName(name, "SyntacticPath", "Length");
          }
          first = false;
        } else {
          pathBuffer.append(this.UP_SEPARATOR);
        }
        pathBuffer.append(feature.getValue().toString());
      }

      if (!isPartial) {
        it = fromEnd.listIterator(fromEnd.size());
        while (it.hasPrevious()) {
          Feature feature = this.pathMemberExtractor.extract(view, it.previous()).get(0);
          pathBuffer.append(this.DOWN_SEPARATOR);
          pathBuffer.append(feature.getValue().toString());
        }
      }

      List<Feature> features = new ArrayList<Feature>(2);
      features.add(new Feature(pathFeatureName, pathBuffer.toString()));
      features.add(new Feature(lengthFeatureName, (long) length));

      return features;

    } catch (IndexOutOfBoundsException e) {
      return new ArrayList<Feature>(0);
    }
  }

  /**
   * Find the path from a TreebankNode to the root of the tree it belongs to.
   * 
   * @param startNode
   *          The start node of the path
   * 
   * @return A list of TreebankNodes that make up the path from <b>startNode</b> to the root of the
   *         tree
   */
  protected static List<TreebankNode> getPathToRoot(TreebankNode startNode) {
    List<TreebankNode> nlist = new ArrayList<TreebankNode>(20);
    TreebankNode cursorNode = startNode;

    while (cursorNode != null) {
      nlist.add(cursorNode);
      cursorNode = cursorNode.getParent();
    }

    return nlist;
  }
}
