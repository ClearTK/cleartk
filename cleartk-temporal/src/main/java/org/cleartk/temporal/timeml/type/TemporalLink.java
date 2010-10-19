

/* First created by JCasGen Fri Oct 08 16:36:23 MDT 2010 */
package org.cleartk.temporal.timeml.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Oct 08 16:36:23 MDT 2010
 * XML source: file:C:/Users/Philip/Documents/Academic/workspace/cleartk-toolkit/src/main/resources/org/cleartk/TypeSystem.xml
 * @generated */
public class TemporalLink extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(TemporalLink.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TemporalLink() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TemporalLink(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TemporalLink(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TemporalLink(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: relationType

  /** getter for relationType - gets 
   * @generated */
  public String getRelationType() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relationType == null)
      jcasType.jcas.throwFeatMissing("relationType", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relationType);}
    
  /** setter for relationType - sets  
   * @generated */
  public void setRelationType(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relationType == null)
      jcasType.jcas.throwFeatMissing("relationType", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relationType, v);}    
   
    
  //*--------------*
  //* Feature: source

  /** getter for source - gets 
   * @generated */
  public Anchor getSource() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "org.cleartk.corpus.timeml.type.TemporalLink");
    return (Anchor)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_source)));}
    
  /** setter for source - sets  
   * @generated */
  public void setSource(Anchor v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setRefValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_source, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: target

  /** getter for target - gets 
   * @generated */
  public Anchor getTarget() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "org.cleartk.corpus.timeml.type.TemporalLink");
    return (Anchor)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_target)));}
    
  /** setter for target - sets  
   * @generated */
  public void setTarget(Anchor v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setRefValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_target, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public String getId() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: eventInstanceID

  /** getter for eventInstanceID - gets 
   * @generated */
  public String getEventInstanceID() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_eventInstanceID == null)
      jcasType.jcas.throwFeatMissing("eventInstanceID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_eventInstanceID);}
    
  /** setter for eventInstanceID - sets  
   * @generated */
  public void setEventInstanceID(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_eventInstanceID == null)
      jcasType.jcas.throwFeatMissing("eventInstanceID", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_eventInstanceID, v);}    
   
    
  //*--------------*
  //* Feature: eventID

  /** getter for eventID - gets 
   * @generated */
  public String getEventID() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_eventID == null)
      jcasType.jcas.throwFeatMissing("eventID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_eventID);}
    
  /** setter for eventID - sets  
   * @generated */
  public void setEventID(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_eventID == null)
      jcasType.jcas.throwFeatMissing("eventID", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_eventID, v);}    
   
    
  //*--------------*
  //* Feature: timeID

  /** getter for timeID - gets 
   * @generated */
  public String getTimeID() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_timeID == null)
      jcasType.jcas.throwFeatMissing("timeID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_timeID);}
    
  /** setter for timeID - sets  
   * @generated */
  public void setTimeID(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_timeID == null)
      jcasType.jcas.throwFeatMissing("timeID", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_timeID, v);}    
   
    
  //*--------------*
  //* Feature: relatedToEventInstance

  /** getter for relatedToEventInstance - gets 
   * @generated */
  public String getRelatedToEventInstance() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relatedToEventInstance == null)
      jcasType.jcas.throwFeatMissing("relatedToEventInstance", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relatedToEventInstance);}
    
  /** setter for relatedToEventInstance - sets  
   * @generated */
  public void setRelatedToEventInstance(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relatedToEventInstance == null)
      jcasType.jcas.throwFeatMissing("relatedToEventInstance", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relatedToEventInstance, v);}    
   
    
  //*--------------*
  //* Feature: relatedToEvent

  /** getter for relatedToEvent - gets 
   * @generated */
  public String getRelatedToEvent() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relatedToEvent == null)
      jcasType.jcas.throwFeatMissing("relatedToEvent", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relatedToEvent);}
    
  /** setter for relatedToEvent - sets  
   * @generated */
  public void setRelatedToEvent(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relatedToEvent == null)
      jcasType.jcas.throwFeatMissing("relatedToEvent", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relatedToEvent, v);}    
   
    
  //*--------------*
  //* Feature: relatedToTime

  /** getter for relatedToTime - gets 
   * @generated */
  public String getRelatedToTime() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relatedToTime == null)
      jcasType.jcas.throwFeatMissing("relatedToTime", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relatedToTime);}
    
  /** setter for relatedToTime - sets  
   * @generated */
  public void setRelatedToTime(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_relatedToTime == null)
      jcasType.jcas.throwFeatMissing("relatedToTime", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_relatedToTime, v);}    
   
    
  //*--------------*
  //* Feature: signalID

  /** getter for signalID - gets 
   * @generated */
  public String getSignalID() {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_signalID == null)
      jcasType.jcas.throwFeatMissing("signalID", "org.cleartk.corpus.timeml.type.TemporalLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_signalID);}
    
  /** setter for signalID - sets  
   * @generated */
  public void setSignalID(String v) {
    if (TemporalLink_Type.featOkTst && ((TemporalLink_Type)jcasType).casFeat_signalID == null)
      jcasType.jcas.throwFeatMissing("signalID", "org.cleartk.corpus.timeml.type.TemporalLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemporalLink_Type)jcasType).casFeatCode_signalID, v);}    
  }

    