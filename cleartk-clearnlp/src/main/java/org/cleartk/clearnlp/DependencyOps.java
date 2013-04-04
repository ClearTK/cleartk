package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Defines common set of dependency graph data type operations used to convert to and query in
 * the target type system
 * 
 * @author Lee Becker
 *
 */
public interface DependencyOps<DEPENDENCY_NODE_TYPE extends Annotation, 
    TOP_DEPENDENCY_NODE_TYPE extends DEPENDENCY_NODE_TYPE, 
    DEPENDENCY_RELATION_TYPE extends 
    FeatureStructure, 
    TOKEN_TYPE extends Annotation> {
  
  boolean isTopNode(JCas jCas, DEPENDENCY_NODE_TYPE depNode);
  
  
  boolean hasHeadRelation(JCas jCas, DEPENDENCY_NODE_TYPE depNode);
  
  List<DEPENDENCY_NODE_TYPE> selectDependencyNodes(JCas jCas, Annotation coveringAnnotation);

  DEPENDENCY_NODE_TYPE getDependencyNode(JCas jCas, TOKEN_TYPE token);
  
  abstract String getHeadRelation(JCas jCas, DEPENDENCY_NODE_TYPE node);
  
  abstract DEPENDENCY_NODE_TYPE getHead(JCas jCas, DEPENDENCY_NODE_TYPE node); 
  
  TOP_DEPENDENCY_NODE_TYPE createTopDependencyNode(JCas jCas, int begin, int end);
  
  /**
   * Creates a new dependency node for the specified span
   */
  DEPENDENCY_NODE_TYPE createDependencyNode(JCas jCas, int begin, int end);
  
  /**
   * Creates a relation between the two nodes.  If dealing with a type system where the relation is a separate object,
   * this will likely create a new relation object.  For graphs that have the relation built into the node, this will likely
   * just set labels on head and child relation fields
   */
  DEPENDENCY_RELATION_TYPE createRelation(JCas jCas, DEPENDENCY_NODE_TYPE head, DEPENDENCY_NODE_TYPE child, String relation);
  
  /**
   * Sets the head relations for a given node.  This is only important if the type system supports multi-head dependency parses
   */
  void setHeadRelations(JCas jCas, DEPENDENCY_NODE_TYPE node, List<DEPENDENCY_RELATION_TYPE> headRelations);
  
  /**
   * Sets the head relations for a given node.  This is only important if the type system supports multi-child dependency parses
   */
  void setChildRelations(JCas jCas, DEPENDENCY_NODE_TYPE node, List<DEPENDENCY_RELATION_TYPE> childRelations);

}
