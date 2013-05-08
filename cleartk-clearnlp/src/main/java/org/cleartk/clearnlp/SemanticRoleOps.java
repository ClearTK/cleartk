package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.googlecode.clearnlp.dependency.DEPArc;

/**
 * Defines common set of SRL data type operations used to convert output from semantic role labelers into
 * the target type system
 * 
 * @author Lee Becker
 *
 */
public interface SemanticRoleOps<SEMANTIC_ARGUMENT_TYPE extends Annotation, PREDICATE_TYPE extends Annotation, TOKEN_TYPE extends Annotation> {
  
  SEMANTIC_ARGUMENT_TYPE createArgument(JCas jCas, DEPArc head, TOKEN_TYPE token);

  PREDICATE_TYPE createPredicate(JCas jCas, String rolesetId, TOKEN_TYPE token);
 
  void setPredicateArguments(JCas jCas, PREDICATE_TYPE predicate, List<SEMANTIC_ARGUMENT_TYPE> arguments);
  
}
