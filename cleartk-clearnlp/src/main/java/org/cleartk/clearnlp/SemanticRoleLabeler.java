package org.cleartk.clearnlp;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.dependency.srl.AbstractSRLabeler;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.engine.EngineProcess;
import com.googlecode.clearnlp.pos.POSNode;
import com.googlecode.clearnlp.predicate.AbstractPredIdentifier;

public class SemanticRoleLabeler extends JCasAnnotator_ImplBase {


  public static final String DEFAULT_PRED_ID_MODEL_FILE_NAME = "ontonotes-en-pred-1.2.0.jar";
  public static final String DEFAULT_SRL_MODEL_FILE_NAME = "ontonotes-en-srl-1.2.0b3.jar";

  public static final String PARAM_SRL_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler.class, 
      "srlModelUri");

  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the semantic role labeler model.  If none is specified it will use the default ontonotes model.")
  private URI srlModelUri;

  public static final String PARAM_PRED_ID_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler.class,
      "predIdModelUri");

  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the predicate identifier model.  If none is specified it will use the default ontonotes model.")
  private URI predIdModelUri;


  @Override
  public void initialize(UimaContext aContext)
      throws ResourceInitializationException {
    super.initialize(aContext);

    try {
      URL predIdModelURL = (this.predIdModelUri == null) ? SemanticRoleLabeler.class.getResource(DEFAULT_PRED_ID_MODEL_FILE_NAME).toURI().toURL() : this.predIdModelUri.toURL();
      this.predIdentifier = EngineGetter.getPredIdentifier(predIdModelURL.openStream());

      URL srlModelURL = (this.srlModelUri == null) ? SemanticRoleLabeler.class.getResource(DEFAULT_SRL_MODEL_FILE_NAME).toURI().toURL() : this.srlModelUri.toURL();
      this.srlabeler = EngineGetter.getSRLabeler(srlModelURL.openStream());

    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  /**
   * Convenience method for creating Analysis Engine for ClearNLP's dependency parser using default English model files
   */
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(SemanticRoleLabeler.class);

  }


  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);

      // Build dependency tree from token information
      DEPTree tree = new DEPTree();
      for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
        POSNode posNode = new POSNode(token.getCoveredText(), token.getPos(), token.getLemma());
        DEPNode node = new DEPNode(i+1, posNode);
        tree.add(node);
      }

      // Build map between CAS dependency node and id for later creation of
      // ClearParser dependency node/tree
      Map<DependencyNode, Integer> depNodeToID = Maps.newHashMap();
      int nodeId = 1;
      for (DependencyNode depNode : JCasUtil.selectCovered(jCas, DependencyNode.class, sentence)) {
        if (depNode instanceof TopDependencyNode) {
          depNodeToID.put(depNode, 0);
        } else {
          depNodeToID.put(depNode, nodeId);
          nodeId++;
        }
      }
      
      // Initialize Dependency Relations for ClearNLP input
      for (int i = 0; i < tokens.size(); i++) {
        Token token = tokens.get(i);

        // Determine node and head
        DependencyNode casDepNode = JCasUtil.selectCovered(jCas, DependencyNode.class, token).get(0);
        DependencyRelation headRelation = (DependencyRelation) casDepNode.getHeadRelations().get(0);
        DependencyNode head = headRelation.getHead();

        int id = i + 1;
        DEPNode node = tree.get(id);
        
        int headId = depNodeToID.get(head);
        DEPNode headNode = tree.get(headId);
        node.setHead(headNode, headRelation.getRelation());
      }
      
      // Run the SRL
      EngineProcess.predictSRL(this.predIdentifier, this.srlabeler, tree);
      
      //System.out.println(tree.toStringDEP());
      //System.out.println(tree.toStringSRL());

      //this.extractSrlInfoToCas(jCas, tree, sentence, tokens);
      this.extractSRLInfo(jCas, tokens, tree);

    }
  }

  /**
   * Takes parsed tree from ClearNLP and converts it into ClearTK's dependency type system.
   * @param jCas
   * @param tree
   * @param sentence
   * @param tokens
   */
  private void extractSrlInfoToCas(JCas jCas, DEPTree tree, Sentence sentence, List<Token> tokens) {
    DependencyNode[] nodes = new DependencyNode[tree.size()];
    DependencyNode rootNode = new TopDependencyNode(jCas, sentence.getBegin(), sentence.getEnd());
    rootNode.addToIndexes();
    nodes[0] = rootNode; 

    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      nodes[i + 1] = new DependencyNode(jCas, token.getBegin(), token.getEnd());
    }

    Map<DependencyNode, List<DependencyRelation>> headRelations;
    headRelations = new HashMap<DependencyNode, List<DependencyRelation>>();
    Map<DependencyNode, List<DependencyRelation>> childRelations;
    childRelations = new HashMap<DependencyNode, List<DependencyRelation>>();
    for (int i = 0; i < tree.size(); i++) {
      DEPNode parserNode = tree.get(i);
      List<DEPArc> sheads = parserNode.getSHeads();
      for (DEPArc shead : sheads) {
        System.out.println(shead.getLabel() + "(" + shead.getNode().form + "," + parserNode.form + ")" + shead.getNode().getFeat(DEPLib.FEAT_PB));
      }
      
      
      if (parserNode.hasHead()) {
        int headIndex = parserNode.getHead().id;
        DependencyNode node = nodes[i];
        DependencyNode headNode = nodes[headIndex];
        DependencyRelation rel = new DependencyRelation(jCas);
        rel.setChild(node);
        rel.setHead(headNode);
        rel.setRelation(parserNode.getLabel());

        if (!headRelations.containsKey(node)) {
          headRelations.put(node, new ArrayList<DependencyRelation>());
        }
        headRelations.get(node).add(rel);
        if (!childRelations.containsKey(headNode)) {
          childRelations.put(headNode, new ArrayList<DependencyRelation>());
        }
        childRelations.get(headNode).add(rel);
      }
    } 

    // finalize nodes: add links between nodes and relations
    for (DependencyNode node : nodes) {
      node.setHeadRelations(UIMAUtil.toFSArray(jCas, headRelations.get(node)));
      node.setChildRelations(UIMAUtil.toFSArray(jCas, childRelations.get(node)));
      node.addToIndexes();
    }
  }

  /**
   * Converts the output from the ClearParser Semantic Role Labeler to the ClearTK Predicate and
   * SemanticArgument Types.
   * 
   * @param jCas
   * @param tokens
   *          - In order list of tokens
   * @param tree
   *          - DepdendencyTree output by ClearParser SRLPredict
   */
  private void extractSRLInfo(JCas jCas, List<Token> tokens, DEPTree tree) {
    Map<Integer, Predicate> headIdToPredicate = Maps.newHashMap();
    Map<Predicate, List<SemanticArgument>> predicateArguments = Maps.newHashMap();

    // Start at node 1, since node 0 is considered the head of the sentence
    for (int i = 1; i < tree.size(); i++) {
      // Every ClearParser parserNode will contain an srlInfo field.
      DEPNode parserNode = tree.get(i);
      Token token = tokens.get(i - 1);
      
      List<DEPArc> semanticHeads = parserNode.getSHeads();
      if (semanticHeads.isEmpty()) { continue; }
      
      // Parse semantic head relations to get SRL triplets
      for (DEPArc shead : semanticHeads) {
        int headId = shead.getNode().id;
        Token headToken = tokens.get(headId - 1);
        Predicate pred;
        List<SemanticArgument> args;
        if (!headIdToPredicate.containsKey(headId)) {
          String rolesetId = shead.getNode().getFeat(DEPLib.FEAT_PB);
          pred = this.createPredicate(jCas, rolesetId, headToken);
          headIdToPredicate.put(headId, pred);
          args = Lists.newArrayList();
          predicateArguments.put(pred, args);
        } else {
          pred = headIdToPredicate.get(headId);
          args = predicateArguments.get(pred);
        }
        args.add(this.createArgument(jCas, shead, token));
      }
    }    
    
    // Store Arguments in Predicate
    for (Map.Entry<Predicate, List<SemanticArgument>> entry : predicateArguments.entrySet()) {
      Predicate predicate = entry.getKey();
      List<SemanticArgument> arguments = entry.getValue();
      predicate.setArguments(UIMAUtil.toFSArray(jCas, arguments));
    }
    
      
  }

  private Predicate createPredicate(JCas jCas, String rolesetId, Token token) {
    Predicate pred = new Predicate(jCas, token.getBegin(), token.getEnd());
    pred.setFrameSet(rolesetId);
    pred.addToIndexes();
    return pred;
  }

  private SemanticArgument createArgument(JCas jCas, DEPArc head, Token token) {
    SemanticArgument argument = new SemanticArgument(jCas, token.getBegin(), token.getEnd());
    argument.setLabel(head.getLabel());
    argument.addToIndexes();
    return argument;
  }

  private AbstractSRLabeler srlabeler;
  private AbstractPredIdentifier predIdentifier;
}
