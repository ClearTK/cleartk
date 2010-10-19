
/* First created by JCasGen Fri Oct 08 16:36:23 MDT 2010 */
package org.cleartk.temporal.timeml.type;

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
 * Updated by JCasGen Fri Oct 08 16:36:23 MDT 2010
 * @generated */
public class Event_Type extends Anchor_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Event_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Event_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Event(addr, Event_Type.this);
  			   Event_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Event(addr, Event_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Event.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.corpus.timeml.type.Event");
 
  /** @generated */
  final Feature casFeat_eventClass;
  /** @generated */
  final int     casFeatCode_eventClass;
  /** @generated */ 
  public String getEventClass(int addr) {
        if (featOkTst && casFeat_eventClass == null)
      jcas.throwFeatMissing("eventClass", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_eventClass);
  }
  /** @generated */    
  public void setEventClass(int addr, String v) {
        if (featOkTst && casFeat_eventClass == null)
      jcas.throwFeatMissing("eventClass", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_eventClass, v);}
    
  
 
  /** @generated */
  final Feature casFeat_eventInstanceID;
  /** @generated */
  final int     casFeatCode_eventInstanceID;
  /** @generated */ 
  public String getEventInstanceID(int addr) {
        if (featOkTst && casFeat_eventInstanceID == null)
      jcas.throwFeatMissing("eventInstanceID", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_eventInstanceID);
  }
  /** @generated */    
  public void setEventInstanceID(int addr, String v) {
        if (featOkTst && casFeat_eventInstanceID == null)
      jcas.throwFeatMissing("eventInstanceID", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_eventInstanceID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_signalID;
  /** @generated */
  final int     casFeatCode_signalID;
  /** @generated */ 
  public String getSignalID(int addr) {
        if (featOkTst && casFeat_signalID == null)
      jcas.throwFeatMissing("signalID", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_signalID);
  }
  /** @generated */    
  public void setSignalID(int addr, String v) {
        if (featOkTst && casFeat_signalID == null)
      jcas.throwFeatMissing("signalID", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_signalID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_stem;
  /** @generated */
  final int     casFeatCode_stem;
  /** @generated */ 
  public String getStem(int addr) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_stem);
  }
  /** @generated */    
  public void setStem(int addr, String v) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_stem, v);}
    
  
 
  /** @generated */
  final Feature casFeat_pos;
  /** @generated */
  final int     casFeatCode_pos;
  /** @generated */ 
  public String getPos(int addr) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_pos);
  }
  /** @generated */    
  public void setPos(int addr, String v) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_pos, v);}
    
  
 
  /** @generated */
  final Feature casFeat_tense;
  /** @generated */
  final int     casFeatCode_tense;
  /** @generated */ 
  public String getTense(int addr) {
        if (featOkTst && casFeat_tense == null)
      jcas.throwFeatMissing("tense", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_tense);
  }
  /** @generated */    
  public void setTense(int addr, String v) {
        if (featOkTst && casFeat_tense == null)
      jcas.throwFeatMissing("tense", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_tense, v);}
    
  
 
  /** @generated */
  final Feature casFeat_aspect;
  /** @generated */
  final int     casFeatCode_aspect;
  /** @generated */ 
  public String getAspect(int addr) {
        if (featOkTst && casFeat_aspect == null)
      jcas.throwFeatMissing("aspect", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_aspect);
  }
  /** @generated */    
  public void setAspect(int addr, String v) {
        if (featOkTst && casFeat_aspect == null)
      jcas.throwFeatMissing("aspect", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_aspect, v);}
    
  
 
  /** @generated */
  final Feature casFeat_cardinality;
  /** @generated */
  final int     casFeatCode_cardinality;
  /** @generated */ 
  public String getCardinality(int addr) {
        if (featOkTst && casFeat_cardinality == null)
      jcas.throwFeatMissing("cardinality", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_cardinality);
  }
  /** @generated */    
  public void setCardinality(int addr, String v) {
        if (featOkTst && casFeat_cardinality == null)
      jcas.throwFeatMissing("cardinality", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_cardinality, v);}
    
  
 
  /** @generated */
  final Feature casFeat_polarity;
  /** @generated */
  final int     casFeatCode_polarity;
  /** @generated */ 
  public String getPolarity(int addr) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_polarity);
  }
  /** @generated */    
  public void setPolarity(int addr, String v) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_polarity, v);}
    
  
 
  /** @generated */
  final Feature casFeat_modality;
  /** @generated */
  final int     casFeatCode_modality;
  /** @generated */ 
  public String getModality(int addr) {
        if (featOkTst && casFeat_modality == null)
      jcas.throwFeatMissing("modality", "org.cleartk.corpus.timeml.type.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_modality);
  }
  /** @generated */    
  public void setModality(int addr, String v) {
        if (featOkTst && casFeat_modality == null)
      jcas.throwFeatMissing("modality", "org.cleartk.corpus.timeml.type.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_modality, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Event_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_eventClass = jcas.getRequiredFeatureDE(casType, "eventClass", "uima.cas.String", featOkTst);
    casFeatCode_eventClass  = (null == casFeat_eventClass) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_eventClass).getCode();

 
    casFeat_eventInstanceID = jcas.getRequiredFeatureDE(casType, "eventInstanceID", "uima.cas.String", featOkTst);
    casFeatCode_eventInstanceID  = (null == casFeat_eventInstanceID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_eventInstanceID).getCode();

 
    casFeat_signalID = jcas.getRequiredFeatureDE(casType, "signalID", "uima.cas.String", featOkTst);
    casFeatCode_signalID  = (null == casFeat_signalID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_signalID).getCode();

 
    casFeat_stem = jcas.getRequiredFeatureDE(casType, "stem", "uima.cas.String", featOkTst);
    casFeatCode_stem  = (null == casFeat_stem) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stem).getCode();

 
    casFeat_pos = jcas.getRequiredFeatureDE(casType, "pos", "uima.cas.String", featOkTst);
    casFeatCode_pos  = (null == casFeat_pos) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pos).getCode();

 
    casFeat_tense = jcas.getRequiredFeatureDE(casType, "tense", "uima.cas.String", featOkTst);
    casFeatCode_tense  = (null == casFeat_tense) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tense).getCode();

 
    casFeat_aspect = jcas.getRequiredFeatureDE(casType, "aspect", "uima.cas.String", featOkTst);
    casFeatCode_aspect  = (null == casFeat_aspect) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_aspect).getCode();

 
    casFeat_cardinality = jcas.getRequiredFeatureDE(casType, "cardinality", "uima.cas.String", featOkTst);
    casFeatCode_cardinality  = (null == casFeat_cardinality) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_cardinality).getCode();

 
    casFeat_polarity = jcas.getRequiredFeatureDE(casType, "polarity", "uima.cas.String", featOkTst);
    casFeatCode_polarity  = (null == casFeat_polarity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_polarity).getCode();

 
    casFeat_modality = jcas.getRequiredFeatureDE(casType, "modality", "uima.cas.String", featOkTst);
    casFeatCode_modality  = (null == casFeat_modality) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_modality).getCode();

  }
}



    