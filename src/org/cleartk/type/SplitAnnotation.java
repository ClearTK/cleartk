

/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;


/** 
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * XML source: C:/Documents and Settings/Philip/My Documents/CSLR/workspace/ClearTK/desc/TypeSystem.xml
 * @generated */
public class SplitAnnotation extends SimpleAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SplitAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SplitAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SplitAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SplitAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SplitAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: annotations

  /** getter for annotations - gets 
   * @generated */
  public FSArray getAnnotations() {
    if (SplitAnnotation_Type.featOkTst && ((SplitAnnotation_Type)jcasType).casFeat_annotations == null)
      jcasType.jcas.throwFeatMissing("annotations", "org.cleartk.type.SplitAnnotation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SplitAnnotation_Type)jcasType).casFeatCode_annotations)));}
    
  /** setter for annotations - sets  
   * @generated */
  public void setAnnotations(FSArray v) {
    if (SplitAnnotation_Type.featOkTst && ((SplitAnnotation_Type)jcasType).casFeat_annotations == null)
      jcasType.jcas.throwFeatMissing("annotations", "org.cleartk.type.SplitAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((SplitAnnotation_Type)jcasType).casFeatCode_annotations, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for annotations - gets an indexed value - 
   * @generated */
  public ContiguousAnnotation getAnnotations(int i) {
    if (SplitAnnotation_Type.featOkTst && ((SplitAnnotation_Type)jcasType).casFeat_annotations == null)
      jcasType.jcas.throwFeatMissing("annotations", "org.cleartk.type.SplitAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SplitAnnotation_Type)jcasType).casFeatCode_annotations), i);
    return (ContiguousAnnotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SplitAnnotation_Type)jcasType).casFeatCode_annotations), i)));}

  /** indexed setter for annotations - sets an indexed value - 
   * @generated */
  public void setAnnotations(int i, ContiguousAnnotation v) { 
    if (SplitAnnotation_Type.featOkTst && ((SplitAnnotation_Type)jcasType).casFeat_annotations == null)
      jcasType.jcas.throwFeatMissing("annotations", "org.cleartk.type.SplitAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SplitAnnotation_Type)jcasType).casFeatCode_annotations), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SplitAnnotation_Type)jcasType).casFeatCode_annotations), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    