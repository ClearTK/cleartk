package org.cleartk.classifier.svmlight.rank;

import org.cleartk.classifier.Instance;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 *
 * @param <OUTCOME>
 * 
 * This subclass of Instance is used to pass in the Qid when ranking objects over an 
 * item using SVMlight's svm_rank_learn executable.
 */
public class QidInstance<OUTCOME> extends Instance<OUTCOME> {
  
  private static final long serialVersionUID = -4948613739946956458L;
  
  private String qid;
  
  public String getQid() {
    return this.qid;
  }
  
  public void setQid(String qid) {
    this.qid = qid;
  }
  

}
