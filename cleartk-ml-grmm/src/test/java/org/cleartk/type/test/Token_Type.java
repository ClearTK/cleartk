
/* First created by JCasGen Fri Oct 08 11:18:15 MDT 2010 */
package org.cleartk.type.test;

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
 * Updated by JCasGen Fri Oct 08 11:18:15 MDT 2010
 * @generated */
public class Token_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Token_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Token_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Token(addr, Token_Type.this);
  			   Token_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Token(addr, Token_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Token.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.type.test.Token");
 
  /** @generated */
  final Feature casFeat_pos;
  /** @generated */
  final int     casFeatCode_pos;
  /** @generated */ 
  public String getPos(int addr) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "org.cleartk.type.test.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_pos);
  }
  /** @generated */    
  public void setPos(int addr, String v) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "org.cleartk.type.test.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_pos, v);}
    
  
 
  /** @generated */
  final Feature casFeat_lemma;
  /** @generated */
  final int     casFeatCode_lemma;
  /** @generated */ 
  public int getLemma(int addr) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "org.cleartk.type.test.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_lemma);
  }
  /** @generated */    
  public void setLemma(int addr, int v) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "org.cleartk.type.test.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_lemma, v);}
    
  
 
  /** @generated */
  final Feature casFeat_posTag;
  /** @generated */
  final int     casFeatCode_posTag;
  /** @generated */ 
  public int getPosTag(int addr) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_posTag);
  }
  /** @generated */    
  public void setPosTag(int addr, int v) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_posTag, v);}
    
   /** @generated */
  public int getPosTag(int addr, int i) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i);
  }
   
  /** @generated */ 
  public void setPosTag(int addr, int i, int v) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_depRel;
  /** @generated */
  final int     casFeatCode_depRel;
  /** @generated */ 
  public int getDepRel(int addr) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_depRel);
  }
  /** @generated */    
  public void setDepRel(int addr, int v) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_depRel, v);}
    
   /** @generated */
  public int getDepRel(int addr, int i) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i);
  }
   
  /** @generated */ 
  public void setDepRel(int addr, int i, int v) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_orthogr;
  /** @generated */
  final int     casFeatCode_orthogr;
  /** @generated */ 
  public int getOrthogr(int addr) {
        if (featOkTst && casFeat_orthogr == null)
      jcas.throwFeatMissing("orthogr", "org.cleartk.type.test.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_orthogr);
  }
  /** @generated */    
  public void setOrthogr(int addr, int v) {
        if (featOkTst && casFeat_orthogr == null)
      jcas.throwFeatMissing("orthogr", "org.cleartk.type.test.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_orthogr, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Token_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_pos = jcas.getRequiredFeatureDE(casType, "pos", "uima.cas.String", featOkTst);
    casFeatCode_pos  = (null == casFeat_pos) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pos).getCode();

 
    casFeat_lemma = jcas.getRequiredFeatureDE(casType, "lemma", "org.cleartk.type.test.Lemma", featOkTst);
    casFeatCode_lemma  = (null == casFeat_lemma) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lemma).getCode();

 
    casFeat_posTag = jcas.getRequiredFeatureDE(casType, "posTag", "uima.cas.FSArray", featOkTst);
    casFeatCode_posTag  = (null == casFeat_posTag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_posTag).getCode();

 
    casFeat_depRel = jcas.getRequiredFeatureDE(casType, "depRel", "uima.cas.FSArray", featOkTst);
    casFeatCode_depRel  = (null == casFeat_depRel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_depRel).getCode();

 
    casFeat_orthogr = jcas.getRequiredFeatureDE(casType, "orthogr", "org.cleartk.type.test.Orthography", featOkTst);
    casFeatCode_orthogr  = (null == casFeat_orthogr) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_orthogr).getCode();

  }
}



    