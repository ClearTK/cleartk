package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;

import com.googlecode.clearnlp.dependency.DEPArc;

/**
 * Defines common set of token data type operations used to convert output from semantic role labelers into
 * the annotations and properties for {@link SemanticArgument} and {@link Predicate}
 * 
 * @author Lee Becker
 *
 */
public class CleartkSrlOps implements SrlOps<SemanticArgument, Predicate, Token>{

  @Override
  public SemanticArgument createArgument(JCas jCas, DEPArc head, Token token) {
    SemanticArgument argument = new SemanticArgument(jCas, token.getBegin(), token.getEnd());
    argument.setLabel(head.getLabel());
    argument.addToIndexes();
    return argument;
  }

  @Override
  public Predicate createPredicate(JCas jCas, String rolesetId, Token token) {
    Predicate pred = new Predicate(jCas, token.getBegin(), token.getEnd());
    pred.setFrameSet(rolesetId);
    pred.addToIndexes();
    return pred;
  }

  @Override
  public void setPredicateArguments(JCas jCas, Predicate predicate, List<SemanticArgument> arguments) {
    predicate.setArguments(UIMAUtil.toFSArray(jCas, arguments));
  }

}
