

/* First created by JCasGen Tue Oct 19 17:14:09 MDT 2010 */
package org.cleartk.temporal.timeml.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Oct 19 17:14:09 MDT 2010
 * XML source: file:C:/Users/Philip/Documents/Academic/workspace/cleartk-temporal/src/main/resources/org/cleartk/temporal/TypeSystem.xml
 * @generated */
public class Anchor extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Anchor.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Anchor() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Anchor(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Anchor(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Anchor(JCas jcas, int begin, int end) {
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
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public String getId() {
    if (Anchor_Type.featOkTst && ((Anchor_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "org.cleartk.temporal.timeml.type.Anchor");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Anchor_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (Anchor_Type.featOkTst && ((Anchor_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "org.cleartk.temporal.timeml.type.Anchor");
    jcasType.ll_cas.ll_setStringValue(addr, ((Anchor_Type)jcasType).casFeatCode_id, v);}    
  }

    