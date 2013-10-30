package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

/**
 * Defines common set of dependency graph data type operations used to convert and query ClearTK dependency types
 * such as {@link TopDependencyNode}, {@link DependencyNode} and {@link DependencyRelation}
 * 
 * 
 * @author Lee Becker
 *
 */
@Beta
public class CleartkDependencyOps implements DependencyOps<DependencyNode, Token, TopDependencyNode, Sentence, DependencyRelation> {

  @Override
  public TopDependencyNode selectRootNode(JCas jCas, Annotation coveringAnnotation) {
    List<TopDependencyNode> nodes = JCasUtil.selectCovered(jCas, TopDependencyNode.class, coveringAnnotation);
    if (nodes.size() != 1) {
      throw new IllegalArgumentException("Expected 1 root node, found " + nodes.size());
    }
    return nodes.get(0);
  }

  @Override
  public List<DependencyNode> selectNodes(JCas jCas, Annotation coveringAnnotation) {
    List<DependencyNode> result = Lists.newArrayList();
    for (DependencyNode node : JCasUtil.selectCovered(jCas, DependencyNode.class, coveringAnnotation)) {
      if (!(node instanceof TopDependencyNode)) {
        result.add(node);
      }
    }
    return result;
  }

  @Override
  public List<DependencyRelation> getHeadRelations(JCas jCas, DependencyNode node) {
    return Lists.newArrayList(JCasUtil.select(node.getHeadRelations(), DependencyRelation.class));
  }

  @Override
  public DependencyNode getHead(JCas jCas, DependencyRelation relation) {
    return relation.getHead();
  }

  @Override
  public String getLabel(JCas jCas, DependencyRelation relation) {
    return relation.getRelation();
  }

  @Override
  public TopDependencyNode createRootNode(JCas jCas, Sentence sentence) {
    return new TopDependencyNode(jCas, sentence.getBegin(), sentence.getEnd());
  }

  @Override
  public DependencyNode createNode(JCas jCas, Token token) {
    return new DependencyNode(jCas, token.getBegin(), token.getEnd());
  }

  @Override
  public DependencyRelation createRelation(JCas jCas, DependencyNode head, DependencyNode child, String relation) {
    DependencyRelation rel = new DependencyRelation(jCas);
    rel.setChild(child);
    rel.setHead(head);
    rel.setRelation(relation);
    return rel; 
  }

  @Override
  public void setHeadRelations(
      JCas jCas,
      DependencyNode node,
      List<DependencyRelation> headRelations) {
    node.setHeadRelations(new FSArray(jCas, headRelations.size()));
    FSCollectionFactory.fillArrayFS(node.getHeadRelations(), headRelations);
  }

  @Override
  public void setChildRelations(
      JCas jCas,
      DependencyNode node,
      List<DependencyRelation> childRelations) {
    node.setChildRelations(new FSArray(jCas, childRelations.size()));
    FSCollectionFactory.fillArrayFS(node.getChildRelations(), childRelations);
  }

}
