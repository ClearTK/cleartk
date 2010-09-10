

/* First created by JCasGen Fri Sep 10 16:27:54 MDT 2010 */
package org.cleartk.type.test;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Sep 10 16:27:54 MDT 2010
 * XML source: file:C:/Users/Philip/Documents/Academic/workspace/ClearTK-test-util/src/main/resources/org/cleartk/type/test/TestTypeSystem.xml
 * @generated */
public class POSTag extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(POSTag.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected POSTag() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public POSTag(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public POSTag(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public POSTag(JCas jcas, int begin, int end) {
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
  //* Feature: value

  /** getter for value - gets 
   * @generated */
  public String getValue() {
    if (POSTag_Type.featOkTst && ((POSTag_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "org.cleartk.type.test.POSTag");
    return jcasType.ll_cas.ll_getStringValue(addr, ((POSTag_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   * @generated */
  public void setValue(String v) {
    if (POSTag_Type.featOkTst && ((POSTag_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "org.cleartk.type.test.POSTag");
    jcasType.ll_cas.ll_setStringValue(addr, ((POSTag_Type)jcasType).casFeatCode_value, v);}    
   
    
  //*--------------*
  //* Feature: language

  /** getter for language - gets 
   * @generated */
  public String getLanguage() {
    if (POSTag_Type.featOkTst && ((POSTag_Type)jcasType).casFeat_language == null)
      jcasType.jcas.throwFeatMissing("language", "org.cleartk.type.test.POSTag");
    return jcasType.ll_cas.ll_getStringValue(addr, ((POSTag_Type)jcasType).casFeatCode_language);}
    
  /** setter for language - sets  
   * @generated */
  public void setLanguage(String v) {
    if (POSTag_Type.featOkTst && ((POSTag_Type)jcasType).casFeat_language == null)
      jcasType.jcas.throwFeatMissing("language", "org.cleartk.type.test.POSTag");
    jcasType.ll_cas.ll_setStringValue(addr, ((POSTag_Type)jcasType).casFeatCode_language, v);}    
  }

    