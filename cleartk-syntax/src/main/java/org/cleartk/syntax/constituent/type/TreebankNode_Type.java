
/* First created by JCasGen Fri Oct 08 16:36:23 MDT 2010 */
package org.cleartk.syntax.constituent.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Fri Oct 15 15:46:11 MDT 2010
 * @generated */
public class TreebankNode_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TreebankNode_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TreebankNode_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TreebankNode(addr, TreebankNode_Type.this);
  			   TreebankNode_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TreebankNode(addr, TreebankNode_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = TreebankNode.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.syntax.constituent.type.TreebankNode");
 
  /** @generated */
  final Feature casFeat_nodeType;
  /** @generated */
  final int     casFeatCode_nodeType;
  /** @generated */ 
  public String getNodeType(int addr) {
        if (featOkTst && casFeat_nodeType == null)
      jcas.throwFeatMissing("nodeType", "org.cleartk.syntax.constituent.type.TreebankNode");
    return ll_cas.ll_getStringValue(addr, casFeatCode_nodeType);
  }
  /** @generated */    
  public void setNodeType(int addr, String v) {
        if (featOkTst && casFeat_nodeType == null)
      jcas.throwFeatMissing("nodeType", "org.cleartk.syntax.constituent.type.TreebankNode");
    ll_cas.ll_setStringValue(addr, casFeatCode_nodeType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_nodeValue;
  /** @generated */
  final int     casFeatCode_nodeValue;
  /** @generated */ 
  public String getNodeValue(int addr) {
        if (featOkTst && casFeat_nodeValue == null)
      jcas.throwFeatMissing("nodeValue", "org.cleartk.syntax.constituent.type.TreebankNode");
    return ll_cas.ll_getStringValue(addr, casFeatCode_nodeValue);
  }
  /** @generated */    
  public void setNodeValue(int addr, String v) {
        if (featOkTst && casFeat_nodeValue == null)
      jcas.throwFeatMissing("nodeValue", "org.cleartk.syntax.constituent.type.TreebankNode");
    ll_cas.ll_setStringValue(addr, casFeatCode_nodeValue, v);}
    
  
 
  /** @generated */
  final Feature casFeat_leaf;
  /** @generated */
  final int     casFeatCode_leaf;
  /** @generated */ 
  public boolean getLeaf(int addr) {
        if (featOkTst && casFeat_leaf == null)
      jcas.throwFeatMissing("leaf", "org.cleartk.syntax.constituent.type.TreebankNode");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_leaf);
  }
  /** @generated */    
  public void setLeaf(int addr, boolean v) {
        if (featOkTst && casFeat_leaf == null)
      jcas.throwFeatMissing("leaf", "org.cleartk.syntax.constituent.type.TreebankNode");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_leaf, v);}
    
  
 
  /** @generated */
  final Feature casFeat_parent;
  /** @generated */
  final int     casFeatCode_parent;
  /** @generated */ 
  public int getParent(int addr) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "org.cleartk.syntax.constituent.type.TreebankNode");
    return ll_cas.ll_getRefValue(addr, casFeatCode_parent);
  }
  /** @generated */    
  public void setParent(int addr, int v) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "org.cleartk.syntax.constituent.type.TreebankNode");
    ll_cas.ll_setRefValue(addr, casFeatCode_parent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_children;
  /** @generated */
  final int     casFeatCode_children;
  /** @generated */ 
  public int getChildren(int addr) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "org.cleartk.syntax.constituent.type.TreebankNode");
    return ll_cas.ll_getRefValue(addr, casFeatCode_children);
  }
  /** @generated */    
  public void setChildren(int addr, int v) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "org.cleartk.syntax.constituent.type.TreebankNode");
    ll_cas.ll_setRefValue(addr, casFeatCode_children, v);}
    
   /** @generated */
  public int getChildren(int addr, int i) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "org.cleartk.syntax.constituent.type.TreebankNode");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_children), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i);
  }
   
  /** @generated */ 
  public void setChildren(int addr, int i, int v) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "org.cleartk.syntax.constituent.type.TreebankNode");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_children), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_nodeTags;
  /** @generated */
  final int     casFeatCode_nodeTags;
  /** @generated */ 
  public int getNodeTags(int addr) {
        if (featOkTst && casFeat_nodeTags == null)
      jcas.throwFeatMissing("nodeTags", "org.cleartk.syntax.constituent.type.TreebankNode");
    return ll_cas.ll_getRefValue(addr, casFeatCode_nodeTags);
  }
  /** @generated */    
  public void setNodeTags(int addr, int v) {
        if (featOkTst && casFeat_nodeTags == null)
      jcas.throwFeatMissing("nodeTags", "org.cleartk.syntax.constituent.type.TreebankNode");
    ll_cas.ll_setRefValue(addr, casFeatCode_nodeTags, v);}
    
   /** @generated */
  public String getNodeTags(int addr, int i) {
        if (featOkTst && casFeat_nodeTags == null)
      jcas.throwFeatMissing("nodeTags", "org.cleartk.syntax.constituent.type.TreebankNode");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_nodeTags), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_nodeTags), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_nodeTags), i);
  }
   
  /** @generated */ 
  public void setNodeTags(int addr, int i, String v) {
        if (featOkTst && casFeat_nodeTags == null)
      jcas.throwFeatMissing("nodeTags", "org.cleartk.syntax.constituent.type.TreebankNode");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_nodeTags), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_nodeTags), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_nodeTags), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public TreebankNode_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_nodeType = jcas.getRequiredFeatureDE(casType, "nodeType", "uima.cas.String", featOkTst);
    casFeatCode_nodeType  = (null == casFeat_nodeType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_nodeType).getCode();

 
    casFeat_nodeValue = jcas.getRequiredFeatureDE(casType, "nodeValue", "uima.cas.String", featOkTst);
    casFeatCode_nodeValue  = (null == casFeat_nodeValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_nodeValue).getCode();

 
    casFeat_leaf = jcas.getRequiredFeatureDE(casType, "leaf", "uima.cas.Boolean", featOkTst);
    casFeatCode_leaf  = (null == casFeat_leaf) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_leaf).getCode();

 
    casFeat_parent = jcas.getRequiredFeatureDE(casType, "parent", "org.cleartk.syntax.constituent.type.TreebankNode", featOkTst);
    casFeatCode_parent  = (null == casFeat_parent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_parent).getCode();

 
    casFeat_children = jcas.getRequiredFeatureDE(casType, "children", "uima.cas.FSArray", featOkTst);
    casFeatCode_children  = (null == casFeat_children) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_children).getCode();

 
    casFeat_nodeTags = jcas.getRequiredFeatureDE(casType, "nodeTags", "uima.cas.StringArray", featOkTst);
    casFeatCode_nodeTags  = (null == casFeat_nodeTags) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_nodeTags).getCode();

  }
}



    