
/* First created by JCasGen Fri Sep 10 16:27:54 MDT 2010 */
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
 * Updated by JCasGen Fri Sep 10 16:27:54 MDT 2010
 * @generated */
public class Lemma_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Lemma_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Lemma_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Lemma(addr, Lemma_Type.this);
  			   Lemma_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Lemma(addr, Lemma_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Lemma.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.type.test.Lemma");
 
  /** @generated */
  final Feature casFeat_value;
  /** @generated */
  final int     casFeatCode_value;
  /** @generated */ 
  public String getValue(int addr) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "org.cleartk.type.test.Lemma");
    return ll_cas.ll_getStringValue(addr, casFeatCode_value);
  }
  /** @generated */    
  public void setValue(int addr, String v) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "org.cleartk.type.test.Lemma");
    ll_cas.ll_setStringValue(addr, casFeatCode_value, v);}
    
  
 
  /** @generated */
  final Feature casFeat_testFS;
  /** @generated */
  final int     casFeatCode_testFS;
  /** @generated */ 
  public int getTestFS(int addr) {
        if (featOkTst && casFeat_testFS == null)
      jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    return ll_cas.ll_getRefValue(addr, casFeatCode_testFS);
  }
  /** @generated */    
  public void setTestFS(int addr, int v) {
        if (featOkTst && casFeat_testFS == null)
      jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    ll_cas.ll_setRefValue(addr, casFeatCode_testFS, v);}
    
   /** @generated */
  public String getTestFS(int addr, int i) {
        if (featOkTst && casFeat_testFS == null)
      jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_testFS), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_testFS), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_testFS), i);
  }
   
  /** @generated */ 
  public void setTestFS(int addr, int i, String v) {
        if (featOkTst && casFeat_testFS == null)
      jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_testFS), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_testFS), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_testFS), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Lemma_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_value = jcas.getRequiredFeatureDE(casType, "value", "uima.cas.String", featOkTst);
    casFeatCode_value  = (null == casFeat_value) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_value).getCode();

 
    casFeat_testFS = jcas.getRequiredFeatureDE(casType, "testFS", "uima.cas.StringArray", featOkTst);
    casFeatCode_testFS  = (null == casFeat_testFS) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_testFS).getCode();

  }
}



    