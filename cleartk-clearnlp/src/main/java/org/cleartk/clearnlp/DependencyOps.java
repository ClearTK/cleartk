package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.annotations.Beta;

/**
 * Defines common set of dependency graph data type operations used to convert to and query in
 * the target type system
 * 
 * @author Lee Becker
 *
 */
@Beta
public interface DependencyOps<
    NODE_TYPE extends TOP,
    NODE_SPAN_TYPE extends Annotation,
    ROOT_NODE_TYPE extends NODE_TYPE,
    ROOT_NODE_SPAN_TYPE extends Annotation,
    RELATION_TYPE extends TOP> {
  
  /**
   * Selects the single root node within the annotation. Should error on more than one root node.
   */
  ROOT_NODE_TYPE selectRootNode(JCas jCas, Annotation coveringAnnotation);

  /**
   * Selects all dependency nodes within the annotation, excluding the root node.
   * 
   * Nodes should be ordered by their offsets. 
   */
  List<NODE_TYPE> selectNodes(JCas jCas, Annotation coveringAnnotation);

  List<RELATION_TYPE> getHeadRelations(JCas jCas, NODE_TYPE node);
  NODE_TYPE getHead(JCas jCas, RELATION_TYPE relation); 
  String getLabel(JCas jCas, RELATION_TYPE relation); 
  
  /**
   * Creates a new dependency node for the specified span
   */
  NODE_TYPE createNode(JCas jCas, NODE_SPAN_TYPE span);
  ROOT_NODE_TYPE createRootNode(JCas jCas, ROOT_NODE_SPAN_TYPE span);
  
  /**
   * Creates a relation between the two nodes.  If dealing with a type system where the relation is a separate object,
   * this will likely create a new relation object.  For graphs that have the relation built into the node, this will likely
   * just set labels on head and child relation fields
   */
  RELATION_TYPE createRelation(JCas jCas, NODE_TYPE head, NODE_TYPE child, String relation);
  
  /**
   * Sets the head relations for a given node.  This is only important if the type system supports multi-head dependency parses
   */
  void setHeadRelations(JCas jCas, NODE_TYPE node, List<RELATION_TYPE> headRelations);
  
  /**
   * Sets the head relations for a given node.  This is only important if the type system supports multi-child dependency parses
   */
  void setChildRelations(JCas jCas, NODE_TYPE node, List<RELATION_TYPE> childRelations);

}
