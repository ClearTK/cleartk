
/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.srl.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * @generated */
public class SemanticArgument_Type extends Argument_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SemanticArgument_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SemanticArgument_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SemanticArgument(addr, SemanticArgument_Type.this);
  			   SemanticArgument_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SemanticArgument(addr, SemanticArgument_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SemanticArgument.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.srl.type.SemanticArgument");
 
  /** @generated */
  final Feature casFeat_label;
  /** @generated */
  final int     casFeatCode_label;
  /** @generated */ 
  public String getLabel(int addr) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "org.cleartk.srl.type.SemanticArgument");
    return ll_cas.ll_getStringValue(addr, casFeatCode_label);
  }
  /** @generated */    
  public void setLabel(int addr, String v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "org.cleartk.srl.type.SemanticArgument");
    ll_cas.ll_setStringValue(addr, casFeatCode_label, v);}
    
  
 
  /** @generated */
  final Feature casFeat_feature;
  /** @generated */
  final int     casFeatCode_feature;
  /** @generated */ 
  public String getFeature(int addr) {
        if (featOkTst && casFeat_feature == null)
      jcas.throwFeatMissing("feature", "org.cleartk.srl.type.SemanticArgument");
    return ll_cas.ll_getStringValue(addr, casFeatCode_feature);
  }
  /** @generated */    
  public void setFeature(int addr, String v) {
        if (featOkTst && casFeat_feature == null)
      jcas.throwFeatMissing("feature", "org.cleartk.srl.type.SemanticArgument");
    ll_cas.ll_setStringValue(addr, casFeatCode_feature, v);}
    
  
 
  /** @generated */
  final Feature casFeat_coreferenceAnnotations;
  /** @generated */
  final int     casFeatCode_coreferenceAnnotations;
  /** @generated */ 
  public int getCoreferenceAnnotations(int addr) {
        if (featOkTst && casFeat_coreferenceAnnotations == null)
      jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    return ll_cas.ll_getRefValue(addr, casFeatCode_coreferenceAnnotations);
  }
  /** @generated */    
  public void setCoreferenceAnnotations(int addr, int v) {
        if (featOkTst && casFeat_coreferenceAnnotations == null)
      jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    ll_cas.ll_setRefValue(addr, casFeatCode_coreferenceAnnotations, v);}
    
   /** @generated */
  public int getCoreferenceAnnotations(int addr, int i) {
        if (featOkTst && casFeat_coreferenceAnnotations == null)
      jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_coreferenceAnnotations), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_coreferenceAnnotations), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_coreferenceAnnotations), i);
  }
   
  /** @generated */ 
  public void setCoreferenceAnnotations(int addr, int i, int v) {
        if (featOkTst && casFeat_coreferenceAnnotations == null)
      jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_coreferenceAnnotations), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_coreferenceAnnotations), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_coreferenceAnnotations), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_preposition;
  /** @generated */
  final int     casFeatCode_preposition;
  /** @generated */ 
  public String getPreposition(int addr) {
        if (featOkTst && casFeat_preposition == null)
      jcas.throwFeatMissing("preposition", "org.cleartk.srl.type.SemanticArgument");
    return ll_cas.ll_getStringValue(addr, casFeatCode_preposition);
  }
  /** @generated */    
  public void setPreposition(int addr, String v) {
        if (featOkTst && casFeat_preposition == null)
      jcas.throwFeatMissing("preposition", "org.cleartk.srl.type.SemanticArgument");
    ll_cas.ll_setStringValue(addr, casFeatCode_preposition, v);}
    
  
 
  /** @generated */
  final Feature casFeat_hyphenTag;
  /** @generated */
  final int     casFeatCode_hyphenTag;
  /** @generated */ 
  public String getHyphenTag(int addr) {
        if (featOkTst && casFeat_hyphenTag == null)
      jcas.throwFeatMissing("hyphenTag", "org.cleartk.srl.type.SemanticArgument");
    return ll_cas.ll_getStringValue(addr, casFeatCode_hyphenTag);
  }
  /** @generated */    
  public void setHyphenTag(int addr, String v) {
        if (featOkTst && casFeat_hyphenTag == null)
      jcas.throwFeatMissing("hyphenTag", "org.cleartk.srl.type.SemanticArgument");
    ll_cas.ll_setStringValue(addr, casFeatCode_hyphenTag, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SemanticArgument_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.String", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

 
    casFeat_feature = jcas.getRequiredFeatureDE(casType, "feature", "uima.cas.String", featOkTst);
    casFeatCode_feature  = (null == casFeat_feature) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_feature).getCode();

 
    casFeat_coreferenceAnnotations = jcas.getRequiredFeatureDE(casType, "coreferenceAnnotations", "uima.cas.FSArray", featOkTst);
    casFeatCode_coreferenceAnnotations  = (null == casFeat_coreferenceAnnotations) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_coreferenceAnnotations).getCode();

 
    casFeat_preposition = jcas.getRequiredFeatureDE(casType, "preposition", "uima.cas.String", featOkTst);
    casFeatCode_preposition  = (null == casFeat_preposition) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_preposition).getCode();

 
    casFeat_hyphenTag = jcas.getRequiredFeatureDE(casType, "hyphenTag", "uima.cas.String", featOkTst);
    casFeatCode_hyphenTag  = (null == casFeat_hyphenTag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_hyphenTag).getCode();

  }
}



    