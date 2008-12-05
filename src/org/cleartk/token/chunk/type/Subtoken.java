

/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.token.chunk.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * XML source: C:/Documents and Settings/Philip/My Documents/CSLR/workspace/ClearTK/desc/TypeSystem.xml
 * @generated */
public class Subtoken extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Subtoken.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Subtoken() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Subtoken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Subtoken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Subtoken(JCas jcas, int begin, int end) {
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
  //* Feature: position

  /** getter for position - gets 
   * @generated */
  public int getPosition() {
    if (Subtoken_Type.featOkTst && ((Subtoken_Type)jcasType).casFeat_position == null)
      jcasType.jcas.throwFeatMissing("position", "org.cleartk.token.chunk.type.Subtoken");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Subtoken_Type)jcasType).casFeatCode_position);}
    
  /** setter for position - sets  
   * @generated */
  public void setPosition(int v) {
    if (Subtoken_Type.featOkTst && ((Subtoken_Type)jcasType).casFeat_position == null)
      jcasType.jcas.throwFeatMissing("position", "org.cleartk.token.chunk.type.Subtoken");
    jcasType.ll_cas.ll_setIntValue(addr, ((Subtoken_Type)jcasType).casFeatCode_position, v);}    
  }

    