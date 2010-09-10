
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
public class AuthorInfo_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (AuthorInfo_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = AuthorInfo_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new AuthorInfo(addr, AuthorInfo_Type.this);
  			   AuthorInfo_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new AuthorInfo(addr, AuthorInfo_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = AuthorInfo.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.type.test.AuthorInfo");
 
  /** @generated */
  final Feature casFeat_lastName;
  /** @generated */
  final int     casFeatCode_lastName;
  /** @generated */ 
  public String getLastName(int addr) {
        if (featOkTst && casFeat_lastName == null)
      jcas.throwFeatMissing("lastName", "org.cleartk.type.test.AuthorInfo");
    return ll_cas.ll_getStringValue(addr, casFeatCode_lastName);
  }
  /** @generated */    
  public void setLastName(int addr, String v) {
        if (featOkTst && casFeat_lastName == null)
      jcas.throwFeatMissing("lastName", "org.cleartk.type.test.AuthorInfo");
    ll_cas.ll_setStringValue(addr, casFeatCode_lastName, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public AuthorInfo_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_lastName = jcas.getRequiredFeatureDE(casType, "lastName", "uima.cas.String", featOkTst);
    casFeatCode_lastName  = (null == casFeat_lastName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lastName).getCode();

  }
}



    