

/* First created by JCasGen Fri Oct 08 11:18:15 MDT 2010 */
package org.cleartk.type.test;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.StringArray;


/** 
 * Updated by JCasGen Fri Oct 08 11:18:15 MDT 2010
 * XML source: file:C:/Users/Philip/Documents/Academic/workspace/cleartk-ml-grmm/src/test/resources/org/cleartk/TestTypeSystem.xml
 * @generated */
public class Lemma extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Lemma.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Lemma() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Lemma(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Lemma(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Lemma(JCas jcas, int begin, int end) {
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
    if (Lemma_Type.featOkTst && ((Lemma_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "org.cleartk.type.test.Lemma");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Lemma_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   * @generated */
  public void setValue(String v) {
    if (Lemma_Type.featOkTst && ((Lemma_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "org.cleartk.type.test.Lemma");
    jcasType.ll_cas.ll_setStringValue(addr, ((Lemma_Type)jcasType).casFeatCode_value, v);}    
   
    
  //*--------------*
  //* Feature: testFS

  /** getter for testFS - gets 
   * @generated */
  public StringArray getTestFS() {
    if (Lemma_Type.featOkTst && ((Lemma_Type)jcasType).casFeat_testFS == null)
      jcasType.jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Lemma_Type)jcasType).casFeatCode_testFS)));}
    
  /** setter for testFS - sets  
   * @generated */
  public void setTestFS(StringArray v) {
    if (Lemma_Type.featOkTst && ((Lemma_Type)jcasType).casFeat_testFS == null)
      jcasType.jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    jcasType.ll_cas.ll_setRefValue(addr, ((Lemma_Type)jcasType).casFeatCode_testFS, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for testFS - gets an indexed value - 
   * @generated */
  public String getTestFS(int i) {
    if (Lemma_Type.featOkTst && ((Lemma_Type)jcasType).casFeat_testFS == null)
      jcasType.jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Lemma_Type)jcasType).casFeatCode_testFS), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Lemma_Type)jcasType).casFeatCode_testFS), i);}

  /** indexed setter for testFS - sets an indexed value - 
   * @generated */
  public void setTestFS(int i, String v) { 
    if (Lemma_Type.featOkTst && ((Lemma_Type)jcasType).casFeat_testFS == null)
      jcasType.jcas.throwFeatMissing("testFS", "org.cleartk.type.test.Lemma");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Lemma_Type)jcasType).casFeatCode_testFS), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Lemma_Type)jcasType).casFeatCode_testFS), i, v);}
  }

    