
/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.type;

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
public class Sentence_Type extends ContiguousAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Sentence_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Sentence_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Sentence(addr, Sentence_Type.this);
  			   Sentence_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Sentence(addr, Sentence_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Sentence.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.type.Sentence");
 
  /** @generated */
  final Feature casFeat_constituentParse;
  /** @generated */
  final int     casFeatCode_constituentParse;
  /** @generated */ 
  public int getConstituentParse(int addr) {
        if (featOkTst && casFeat_constituentParse == null)
      jcas.throwFeatMissing("constituentParse", "org.cleartk.type.Sentence");
    return ll_cas.ll_getRefValue(addr, casFeatCode_constituentParse);
  }
  /** @generated */    
  public void setConstituentParse(int addr, int v) {
        if (featOkTst && casFeat_constituentParse == null)
      jcas.throwFeatMissing("constituentParse", "org.cleartk.type.Sentence");
    ll_cas.ll_setRefValue(addr, casFeatCode_constituentParse, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Sentence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_constituentParse = jcas.getRequiredFeatureDE(casType, "constituentParse", "org.cleartk.syntax.treebank.type.TopTreebankNode", featOkTst);
    casFeatCode_constituentParse  = (null == casFeat_constituentParse) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_constituentParse).getCode();

  }
}



    