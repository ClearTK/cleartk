

/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.cleartk.syntax.treebank.type.TopTreebankNode;


/** 
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * XML source: C:/Documents and Settings/Philip/My Documents/CSLR/workspace/ClearTK/desc/TypeSystem.xml
 * @generated */
public class Sentence extends ContiguousAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Sentence.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Sentence() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Sentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Sentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Sentence(JCas jcas, int begin, int end) {
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
  //* Feature: constituentParse

  /** getter for constituentParse - gets 
   * @generated */
  public TopTreebankNode getConstituentParse() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_constituentParse == null)
      jcasType.jcas.throwFeatMissing("constituentParse", "org.cleartk.type.Sentence");
    return (TopTreebankNode)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_constituentParse)));}
    
  /** setter for constituentParse - sets  
   * @generated */
  public void setConstituentParse(TopTreebankNode v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_constituentParse == null)
      jcasType.jcas.throwFeatMissing("constituentParse", "org.cleartk.type.Sentence");
    jcasType.ll_cas.ll_setRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_constituentParse, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    