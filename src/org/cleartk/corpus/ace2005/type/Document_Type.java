
/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.corpus.ace2005.type;

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
public class Document_Type extends org.cleartk.type.Document_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Document_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Document_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Document(addr, Document_Type.this);
  			   Document_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Document(addr, Document_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Document.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.corpus.ace2005.type.Document");
 
  /** @generated */
  final Feature casFeat_aceSource;
  /** @generated */
  final int     casFeatCode_aceSource;
  /** @generated */ 
  public String getAceSource(int addr) {
        if (featOkTst && casFeat_aceSource == null)
      jcas.throwFeatMissing("aceSource", "org.cleartk.corpus.ace2005.type.Document");
    return ll_cas.ll_getStringValue(addr, casFeatCode_aceSource);
  }
  /** @generated */    
  public void setAceSource(int addr, String v) {
        if (featOkTst && casFeat_aceSource == null)
      jcas.throwFeatMissing("aceSource", "org.cleartk.corpus.ace2005.type.Document");
    ll_cas.ll_setStringValue(addr, casFeatCode_aceSource, v);}
    
  
 
  /** @generated */
  final Feature casFeat_aceUri;
  /** @generated */
  final int     casFeatCode_aceUri;
  /** @generated */ 
  public String getAceUri(int addr) {
        if (featOkTst && casFeat_aceUri == null)
      jcas.throwFeatMissing("aceUri", "org.cleartk.corpus.ace2005.type.Document");
    return ll_cas.ll_getStringValue(addr, casFeatCode_aceUri);
  }
  /** @generated */    
  public void setAceUri(int addr, String v) {
        if (featOkTst && casFeat_aceUri == null)
      jcas.throwFeatMissing("aceUri", "org.cleartk.corpus.ace2005.type.Document");
    ll_cas.ll_setStringValue(addr, casFeatCode_aceUri, v);}
    
  
 
  /** @generated */
  final Feature casFeat_aceType;
  /** @generated */
  final int     casFeatCode_aceType;
  /** @generated */ 
  public String getAceType(int addr) {
        if (featOkTst && casFeat_aceType == null)
      jcas.throwFeatMissing("aceType", "org.cleartk.corpus.ace2005.type.Document");
    return ll_cas.ll_getStringValue(addr, casFeatCode_aceType);
  }
  /** @generated */    
  public void setAceType(int addr, String v) {
        if (featOkTst && casFeat_aceType == null)
      jcas.throwFeatMissing("aceType", "org.cleartk.corpus.ace2005.type.Document");
    ll_cas.ll_setStringValue(addr, casFeatCode_aceType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Document_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_aceSource = jcas.getRequiredFeatureDE(casType, "aceSource", "uima.cas.String", featOkTst);
    casFeatCode_aceSource  = (null == casFeat_aceSource) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_aceSource).getCode();

 
    casFeat_aceUri = jcas.getRequiredFeatureDE(casType, "aceUri", "uima.cas.String", featOkTst);
    casFeatCode_aceUri  = (null == casFeat_aceUri) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_aceUri).getCode();

 
    casFeat_aceType = jcas.getRequiredFeatureDE(casType, "aceType", "uima.cas.String", featOkTst);
    casFeatCode_aceType  = (null == casFeat_aceType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_aceType).getCode();

  }
}



    