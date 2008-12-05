
/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.ne.type;

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
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * @generated */
public class NamedEntityMention_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NamedEntityMention_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NamedEntityMention_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NamedEntityMention(addr, NamedEntityMention_Type.this);
  			   NamedEntityMention_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NamedEntityMention(addr, NamedEntityMention_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NamedEntityMention.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.ne.type.NamedEntityMention");
 
  /** @generated */
  final Feature casFeat_mentionType;
  /** @generated */
  final int     casFeatCode_mentionType;
  /** @generated */ 
  public String getMentionType(int addr) {
        if (featOkTst && casFeat_mentionType == null)
      jcas.throwFeatMissing("mentionType", "org.cleartk.ne.type.NamedEntityMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_mentionType);
  }
  /** @generated */    
  public void setMentionType(int addr, String v) {
        if (featOkTst && casFeat_mentionType == null)
      jcas.throwFeatMissing("mentionType", "org.cleartk.ne.type.NamedEntityMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_mentionType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_mentionedEntity;
  /** @generated */
  final int     casFeatCode_mentionedEntity;
  /** @generated */ 
  public int getMentionedEntity(int addr) {
        if (featOkTst && casFeat_mentionedEntity == null)
      jcas.throwFeatMissing("mentionedEntity", "org.cleartk.ne.type.NamedEntityMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_mentionedEntity);
  }
  /** @generated */    
  public void setMentionedEntity(int addr, int v) {
        if (featOkTst && casFeat_mentionedEntity == null)
      jcas.throwFeatMissing("mentionedEntity", "org.cleartk.ne.type.NamedEntityMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_mentionedEntity, v);}
    
  
 
  /** @generated */
  final Feature casFeat_annotation;
  /** @generated */
  final int     casFeatCode_annotation;
  /** @generated */ 
  public int getAnnotation(int addr) {
        if (featOkTst && casFeat_annotation == null)
      jcas.throwFeatMissing("annotation", "org.cleartk.ne.type.NamedEntityMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_annotation);
  }
  /** @generated */    
  public void setAnnotation(int addr, int v) {
        if (featOkTst && casFeat_annotation == null)
      jcas.throwFeatMissing("annotation", "org.cleartk.ne.type.NamedEntityMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_annotation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_head;
  /** @generated */
  final int     casFeatCode_head;
  /** @generated */ 
  public int getHead(int addr) {
        if (featOkTst && casFeat_head == null)
      jcas.throwFeatMissing("head", "org.cleartk.ne.type.NamedEntityMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_head);
  }
  /** @generated */    
  public void setHead(int addr, int v) {
        if (featOkTst && casFeat_head == null)
      jcas.throwFeatMissing("head", "org.cleartk.ne.type.NamedEntityMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_head, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NamedEntityMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_mentionType = jcas.getRequiredFeatureDE(casType, "mentionType", "uima.cas.String", featOkTst);
    casFeatCode_mentionType  = (null == casFeat_mentionType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mentionType).getCode();

 
    casFeat_mentionedEntity = jcas.getRequiredFeatureDE(casType, "mentionedEntity", "org.cleartk.ne.type.NamedEntity", featOkTst);
    casFeatCode_mentionedEntity  = (null == casFeat_mentionedEntity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mentionedEntity).getCode();

 
    casFeat_annotation = jcas.getRequiredFeatureDE(casType, "annotation", "org.cleartk.type.SimpleAnnotation", featOkTst);
    casFeatCode_annotation  = (null == casFeat_annotation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_annotation).getCode();

 
    casFeat_head = jcas.getRequiredFeatureDE(casType, "head", "org.cleartk.type.SimpleAnnotation", featOkTst);
    casFeatCode_head  = (null == casFeat_head) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_head).getCode();

  }
}



    