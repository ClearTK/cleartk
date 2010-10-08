
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
public class Header_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Header_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Header_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Header(addr, Header_Type.this);
  			   Header_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Header(addr, Header_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Header.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.type.test.Header");
 
  /** @generated */
  final Feature casFeat_authors;
  /** @generated */
  final int     casFeatCode_authors;
  /** @generated */ 
  public int getAuthors(int addr) {
        if (featOkTst && casFeat_authors == null)
      jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    return ll_cas.ll_getRefValue(addr, casFeatCode_authors);
  }
  /** @generated */    
  public void setAuthors(int addr, int v) {
        if (featOkTst && casFeat_authors == null)
      jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    ll_cas.ll_setRefValue(addr, casFeatCode_authors, v);}
    
   /** @generated */
  public int getAuthors(int addr, int i) {
        if (featOkTst && casFeat_authors == null)
      jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_authors), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_authors), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_authors), i);
  }
   
  /** @generated */ 
  public void setAuthors(int addr, int i, int v) {
        if (featOkTst && casFeat_authors == null)
      jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_authors), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_authors), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_authors), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Header_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_authors = jcas.getRequiredFeatureDE(casType, "authors", "uima.cas.FSArray", featOkTst);
    casFeatCode_authors  = (null == casFeat_authors) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_authors).getCode();

  }
}



    