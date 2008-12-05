
/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.corpus.timeml.type;

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
public class TemporalLink_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TemporalLink_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TemporalLink_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TemporalLink(addr, TemporalLink_Type.this);
  			   TemporalLink_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TemporalLink(addr, TemporalLink_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = TemporalLink.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.corpus.timeml.type.TemporalLink");
 
  /** @generated */
  final Feature casFeat_relationType;
  /** @generated */
  final int     casFeatCode_relationType;
  /** @generated */ 
  public String getRelationType(int addr) {
        if (featOkTst && casFeat_relationType == null)
      jcas.throwFeatMissing("relationType", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_relationType);
  }
  /** @generated */    
  public void setRelationType(int addr, String v) {
        if (featOkTst && casFeat_relationType == null)
      jcas.throwFeatMissing("relationType", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_relationType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_source;
  /** @generated */
  final int     casFeatCode_source;
  /** @generated */ 
  public int getSource(int addr) {
        if (featOkTst && casFeat_source == null)
      jcas.throwFeatMissing("source", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getRefValue(addr, casFeatCode_source);
  }
  /** @generated */    
  public void setSource(int addr, int v) {
        if (featOkTst && casFeat_source == null)
      jcas.throwFeatMissing("source", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setRefValue(addr, casFeatCode_source, v);}
    
  
 
  /** @generated */
  final Feature casFeat_target;
  /** @generated */
  final int     casFeatCode_target;
  /** @generated */ 
  public int getTarget(int addr) {
        if (featOkTst && casFeat_target == null)
      jcas.throwFeatMissing("target", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getRefValue(addr, casFeatCode_target);
  }
  /** @generated */    
  public void setTarget(int addr, int v) {
        if (featOkTst && casFeat_target == null)
      jcas.throwFeatMissing("target", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setRefValue(addr, casFeatCode_target, v);}
    
  
 
  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated */ 
  public String getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_id);
  }
  /** @generated */    
  public void setId(int addr, String v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_id, v);}
    
  
 
  /** @generated */
  final Feature casFeat_eventInstanceID;
  /** @generated */
  final int     casFeatCode_eventInstanceID;
  /** @generated */ 
  public String getEventInstanceID(int addr) {
        if (featOkTst && casFeat_eventInstanceID == null)
      jcas.throwFeatMissing("eventInstanceID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_eventInstanceID);
  }
  /** @generated */    
  public void setEventInstanceID(int addr, String v) {
        if (featOkTst && casFeat_eventInstanceID == null)
      jcas.throwFeatMissing("eventInstanceID", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_eventInstanceID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_eventID;
  /** @generated */
  final int     casFeatCode_eventID;
  /** @generated */ 
  public String getEventID(int addr) {
        if (featOkTst && casFeat_eventID == null)
      jcas.throwFeatMissing("eventID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_eventID);
  }
  /** @generated */    
  public void setEventID(int addr, String v) {
        if (featOkTst && casFeat_eventID == null)
      jcas.throwFeatMissing("eventID", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_eventID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_timeID;
  /** @generated */
  final int     casFeatCode_timeID;
  /** @generated */ 
  public String getTimeID(int addr) {
        if (featOkTst && casFeat_timeID == null)
      jcas.throwFeatMissing("timeID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_timeID);
  }
  /** @generated */    
  public void setTimeID(int addr, String v) {
        if (featOkTst && casFeat_timeID == null)
      jcas.throwFeatMissing("timeID", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_timeID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relatedToEventInstance;
  /** @generated */
  final int     casFeatCode_relatedToEventInstance;
  /** @generated */ 
  public String getRelatedToEventInstance(int addr) {
        if (featOkTst && casFeat_relatedToEventInstance == null)
      jcas.throwFeatMissing("relatedToEventInstance", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_relatedToEventInstance);
  }
  /** @generated */    
  public void setRelatedToEventInstance(int addr, String v) {
        if (featOkTst && casFeat_relatedToEventInstance == null)
      jcas.throwFeatMissing("relatedToEventInstance", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_relatedToEventInstance, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relatedToEvent;
  /** @generated */
  final int     casFeatCode_relatedToEvent;
  /** @generated */ 
  public String getRelatedToEvent(int addr) {
        if (featOkTst && casFeat_relatedToEvent == null)
      jcas.throwFeatMissing("relatedToEvent", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_relatedToEvent);
  }
  /** @generated */    
  public void setRelatedToEvent(int addr, String v) {
        if (featOkTst && casFeat_relatedToEvent == null)
      jcas.throwFeatMissing("relatedToEvent", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_relatedToEvent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relatedToTime;
  /** @generated */
  final int     casFeatCode_relatedToTime;
  /** @generated */ 
  public String getRelatedToTime(int addr) {
        if (featOkTst && casFeat_relatedToTime == null)
      jcas.throwFeatMissing("relatedToTime", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_relatedToTime);
  }
  /** @generated */    
  public void setRelatedToTime(int addr, String v) {
        if (featOkTst && casFeat_relatedToTime == null)
      jcas.throwFeatMissing("relatedToTime", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_relatedToTime, v);}
    
  
 
  /** @generated */
  final Feature casFeat_signalID;
  /** @generated */
  final int     casFeatCode_signalID;
  /** @generated */ 
  public String getSignalID(int addr) {
        if (featOkTst && casFeat_signalID == null)
      jcas.throwFeatMissing("signalID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return ll_cas.ll_getStringValue(addr, casFeatCode_signalID);
  }
  /** @generated */    
  public void setSignalID(int addr, String v) {
        if (featOkTst && casFeat_signalID == null)
      jcas.throwFeatMissing("signalID", "org.cleartk.corpus.timeml.type.TemporalLink");
    ll_cas.ll_setStringValue(addr, casFeatCode_signalID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public TemporalLink_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_relationType = jcas.getRequiredFeatureDE(casType, "relationType", "uima.cas.String", featOkTst);
    casFeatCode_relationType  = (null == casFeat_relationType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relationType).getCode();

 
    casFeat_source = jcas.getRequiredFeatureDE(casType, "source", "org.cleartk.corpus.timeml.type.Anchor", featOkTst);
    casFeatCode_source  = (null == casFeat_source) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_source).getCode();

 
    casFeat_target = jcas.getRequiredFeatureDE(casType, "target", "org.cleartk.corpus.timeml.type.Anchor", featOkTst);
    casFeatCode_target  = (null == casFeat_target) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_target).getCode();

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.String", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_eventInstanceID = jcas.getRequiredFeatureDE(casType, "eventInstanceID", "uima.cas.String", featOkTst);
    casFeatCode_eventInstanceID  = (null == casFeat_eventInstanceID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_eventInstanceID).getCode();

 
    casFeat_eventID = jcas.getRequiredFeatureDE(casType, "eventID", "uima.cas.String", featOkTst);
    casFeatCode_eventID  = (null == casFeat_eventID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_eventID).getCode();

 
    casFeat_timeID = jcas.getRequiredFeatureDE(casType, "timeID", "uima.cas.String", featOkTst);
    casFeatCode_timeID  = (null == casFeat_timeID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_timeID).getCode();

 
    casFeat_relatedToEventInstance = jcas.getRequiredFeatureDE(casType, "relatedToEventInstance", "uima.cas.String", featOkTst);
    casFeatCode_relatedToEventInstance  = (null == casFeat_relatedToEventInstance) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relatedToEventInstance).getCode();

 
    casFeat_relatedToEvent = jcas.getRequiredFeatureDE(casType, "relatedToEvent", "uima.cas.String", featOkTst);
    casFeatCode_relatedToEvent  = (null == casFeat_relatedToEvent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relatedToEvent).getCode();

 
    casFeat_relatedToTime = jcas.getRequiredFeatureDE(casType, "relatedToTime", "uima.cas.String", featOkTst);
    casFeatCode_relatedToTime  = (null == casFeat_relatedToTime) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relatedToTime).getCode();

 
    casFeat_signalID = jcas.getRequiredFeatureDE(casType, "signalID", "uima.cas.String", featOkTst);
    casFeatCode_signalID  = (null == casFeat_signalID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_signalID).getCode();

  }
}



    