package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.annotations.Beta;

/**
 * Defines common set of SRL data type operations used to convert output from semantic role labelers into
 * the target type system
 * 
 * @author Lee Becker
 *
 */
@Beta
public interface SemanticRoleOps<
    ARGUMENT_TYPE extends TOP,
    ARGUMENT_SPAN_TYPE extends Annotation,
    PREDICATE_TYPE extends TOP,
    PREDICATE_SPAN_TYPE extends Annotation> {
  
  ARGUMENT_TYPE createArgument(JCas jCas, ARGUMENT_SPAN_TYPE span, String label);

  PREDICATE_TYPE createPredicate(JCas jCas, PREDICATE_SPAN_TYPE span, String rolesetId);
 
  void setPredicateArguments(JCas jCas, PREDICATE_TYPE predicate, List<ARGUMENT_TYPE> arguments);
  
}
