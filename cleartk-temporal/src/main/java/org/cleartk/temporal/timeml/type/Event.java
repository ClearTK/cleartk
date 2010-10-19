

/* First created by JCasGen Tue Oct 19 17:14:09 MDT 2010 */
package org.cleartk.temporal.timeml.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Tue Oct 19 17:14:09 MDT 2010
 * XML source: file:C:/Users/Philip/Documents/Academic/workspace/cleartk-temporal/src/main/resources/org/cleartk/temporal/TypeSystem.xml
 * @generated */
public class Event extends Anchor {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Event.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Event() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Event(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Event(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Event(JCas jcas, int begin, int end) {
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
  //* Feature: eventClass

  /** getter for eventClass - gets 
   * @generated */
  public String getEventClass() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_eventClass == null)
      jcasType.jcas.throwFeatMissing("eventClass", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_eventClass);}
    
  /** setter for eventClass - sets  
   * @generated */
  public void setEventClass(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_eventClass == null)
      jcasType.jcas.throwFeatMissing("eventClass", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_eventClass, v);}    
   
    
  //*--------------*
  //* Feature: eventInstanceID

  /** getter for eventInstanceID - gets 
   * @generated */
  public String getEventInstanceID() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_eventInstanceID == null)
      jcasType.jcas.throwFeatMissing("eventInstanceID", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_eventInstanceID);}
    
  /** setter for eventInstanceID - sets  
   * @generated */
  public void setEventInstanceID(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_eventInstanceID == null)
      jcasType.jcas.throwFeatMissing("eventInstanceID", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_eventInstanceID, v);}    
   
    
  //*--------------*
  //* Feature: signalID

  /** getter for signalID - gets 
   * @generated */
  public String getSignalID() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_signalID == null)
      jcasType.jcas.throwFeatMissing("signalID", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_signalID);}
    
  /** setter for signalID - sets  
   * @generated */
  public void setSignalID(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_signalID == null)
      jcasType.jcas.throwFeatMissing("signalID", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_signalID, v);}    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated */
  public String getStem() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_stem);}
    
  /** setter for stem - sets  
   * @generated */
  public void setStem(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_stem, v);}    
   
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated */
  public String getPos() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_pos);}
    
  /** setter for pos - sets  
   * @generated */
  public void setPos(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_pos, v);}    
   
    
  //*--------------*
  //* Feature: tense

  /** getter for tense - gets 
   * @generated */
  public String getTense() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_tense);}
    
  /** setter for tense - sets  
   * @generated */
  public void setTense(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_tense, v);}    
   
    
  //*--------------*
  //* Feature: aspect

  /** getter for aspect - gets 
   * @generated */
  public String getAspect() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_aspect == null)
      jcasType.jcas.throwFeatMissing("aspect", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_aspect);}
    
  /** setter for aspect - sets  
   * @generated */
  public void setAspect(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_aspect == null)
      jcasType.jcas.throwFeatMissing("aspect", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_aspect, v);}    
   
    
  //*--------------*
  //* Feature: cardinality

  /** getter for cardinality - gets 
   * @generated */
  public String getCardinality() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_cardinality == null)
      jcasType.jcas.throwFeatMissing("cardinality", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_cardinality);}
    
  /** setter for cardinality - sets  
   * @generated */
  public void setCardinality(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_cardinality == null)
      jcasType.jcas.throwFeatMissing("cardinality", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_cardinality, v);}    
   
    
  //*--------------*
  //* Feature: polarity

  /** getter for polarity - gets 
   * @generated */
  public String getPolarity() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_polarity == null)
      jcasType.jcas.throwFeatMissing("polarity", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_polarity);}
    
  /** setter for polarity - sets  
   * @generated */
  public void setPolarity(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_polarity == null)
      jcasType.jcas.throwFeatMissing("polarity", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_polarity, v);}    
   
    
  //*--------------*
  //* Feature: modality

  /** getter for modality - gets 
   * @generated */
  public String getModality() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_modality == null)
      jcasType.jcas.throwFeatMissing("modality", "org.cleartk.temporal.timeml.type.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_modality);}
    
  /** setter for modality - sets  
   * @generated */
  public void setModality(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_modality == null)
      jcasType.jcas.throwFeatMissing("modality", "org.cleartk.temporal.timeml.type.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_modality, v);}    
  }

    