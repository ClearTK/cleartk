package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.util.FSCollectionFactory;

import com.google.common.annotations.Beta;

/**
 * Defines common set of token data type operations used to convert output from semantic role labelers into
 * the annotations and properties for {@link SemanticArgument} and {@link Predicate}
 * 
 * @author Lee Becker
 *
 */
@Beta
public class CleartkSemanticRoleOps implements SemanticRoleOps<SemanticArgument, Token, Predicate, Token>{

  @Override
  public SemanticArgument createArgument(JCas jCas, Token span, String label) {
    SemanticArgument argument = new SemanticArgument(jCas, span.getBegin(), span.getEnd());
    argument.setLabel(label);
    argument.addToIndexes();
    return argument;
  }

  @Override
  public Predicate createPredicate(JCas jCas, Token span, String rolesetId) {
    Predicate pred = new Predicate(jCas, span.getBegin(), span.getEnd());
    pred.setFrameSet(rolesetId);
    pred.addToIndexes();
    return pred;
  }

  @Override
  public void setPredicateArguments(JCas jCas, Predicate predicate, List<SemanticArgument> arguments) {
    predicate.setArguments(new FSArray(jCas, arguments.size()));
    FSCollectionFactory.fillArrayFS(predicate.getArguments(), arguments);
  }

}
