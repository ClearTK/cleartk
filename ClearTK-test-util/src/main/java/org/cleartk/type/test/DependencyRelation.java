

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
public class DependencyRelation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DependencyRelation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DependencyRelation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DependencyRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DependencyRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DependencyRelation(JCas jcas, int begin, int end) {
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
  //* Feature: head

  /** getter for head - gets 
   * @generated */
  public Token getHead() {
    if (DependencyRelation_Type.featOkTst && ((DependencyRelation_Type)jcasType).casFeat_head == null)
      jcasType.jcas.throwFeatMissing("head", "org.cleartk.type.test.DependencyRelation");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DependencyRelation_Type)jcasType).casFeatCode_head)));}
    
  /** setter for head - sets  
   * @generated */
  public void setHead(Token v) {
    if (DependencyRelation_Type.featOkTst && ((DependencyRelation_Type)jcasType).casFeat_head == null)
      jcasType.jcas.throwFeatMissing("head", "org.cleartk.type.test.DependencyRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((DependencyRelation_Type)jcasType).casFeatCode_head, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: projective

  /** getter for projective - gets 
   * @generated */
  public boolean getProjective() {
    if (DependencyRelation_Type.featOkTst && ((DependencyRelation_Type)jcasType).casFeat_projective == null)
      jcasType.jcas.throwFeatMissing("projective", "org.cleartk.type.test.DependencyRelation");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((DependencyRelation_Type)jcasType).casFeatCode_projective);}
    
  /** setter for projective - sets  
   * @generated */
  public void setProjective(boolean v) {
    if (DependencyRelation_Type.featOkTst && ((DependencyRelation_Type)jcasType).casFeat_projective == null)
      jcasType.jcas.throwFeatMissing("projective", "org.cleartk.type.test.DependencyRelation");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((DependencyRelation_Type)jcasType).casFeatCode_projective, v);}    
   
    
  //*--------------*
  //* Feature: label

  /** getter for label - gets 
   * @generated */
  public String getLabel() {
    if (DependencyRelation_Type.featOkTst && ((DependencyRelation_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "org.cleartk.type.test.DependencyRelation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DependencyRelation_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets  
   * @generated */
  public void setLabel(String v) {
    if (DependencyRelation_Type.featOkTst && ((DependencyRelation_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "org.cleartk.type.test.DependencyRelation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DependencyRelation_Type)jcasType).casFeatCode_label, v);}    
  }

    