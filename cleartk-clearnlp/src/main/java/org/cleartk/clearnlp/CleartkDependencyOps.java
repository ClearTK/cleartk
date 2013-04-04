package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.util.JCasUtil;

/**
 * Defines common set of dependency graph data type operations used to convert and query ClearTK dependency types
 * such as {@link TopDependencyNode}, {@link DependencyNode} and {@link DependencyRelation}
 * 
 * 
 * @author Lee Becker
 *
 */
public class CleartkDependencyOps implements DependencyOps<DependencyNode, TopDependencyNode, DependencyRelation, Token> {

  @Override
  public boolean isTopNode(JCas jCas, DependencyNode depNode) {
    return depNode instanceof TopDependencyNode;
  }

  @Override
  public boolean hasHeadRelation(JCas jCas, DependencyNode depNode) {
    return depNode.getHeadRelations().size() != 0;
  }

  @Override
  public List<DependencyNode> selectDependencyNodes(JCas jCas, Annotation coveringAnnotation) {
    return JCasUtil.selectCovered(jCas, DependencyNode.class, coveringAnnotation);
  }

  @Override
  public DependencyNode getDependencyNode(JCas jCas, Token token) {
    return JCasUtil.selectCovered(jCas, DependencyNode.class, token).get(0);
  }
  
  @Override
  public String getHeadRelation(JCas jCas, DependencyNode node) {
    return node.getHeadRelations(0).getRelation();
  }

  @Override
  public DependencyNode getHead(JCas jCas, DependencyNode node) {
    return node.getHeadRelations(0).getHead();
  }

  @Override
  public TopDependencyNode createTopDependencyNode(JCas jCas, int begin, int end) {
    return new TopDependencyNode(jCas, begin, end);
  }

  @Override
  public DependencyNode createDependencyNode(JCas jCas, int begin, int end) {
    return new DependencyNode(jCas, begin, end);
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
    node.setHeadRelations(UIMAUtil.toFSArray(jCas, headRelations));
  }

  @Override
  public void setChildRelations(
      JCas jCas,
      DependencyNode node,
      List<DependencyRelation> childRelations) {
    node.setChildRelations(UIMAUtil.toFSArray(jCas, childRelations));
  }

}
