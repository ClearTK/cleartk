/** 
 * Copyright (c) 2007-2015, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.ml.tksvmlight.model;

import java.util.Map;

/**
 * <br>
 * Copyright (c) 2007-2015, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Tim Miller
 */

public class ContinuousCosineLexicalSimilarity implements LexicalFunctionModel {

  /**
   * 
   */
  private static final long serialVersionUID = -6282134536439718560L;
  Map<String,double[]> wordMap = null;
  private static final String unk = "unk";
  
  public ContinuousCosineLexicalSimilarity(Map<String,double[]> map){
    this.wordMap = map;
    
    // set vectors to unit length to speed computation.
    for(double[] vec : wordMap.values()){
      double sum = 0.0;
      for(double val : vec){
        sum += val*val;
      }
      for(int i = 0; i < vec.length; i++){
        vec[i] /= Math.sqrt(sum);
      }
    }
  }
  
  public double getLexicalSimilarity(String w1, String w2) {
    double sim;
    if(w1 == w2 || w1.equals(w2)){
      return 1.0;
    }
    
    // if we would need the unk word but it doesn't even contain unk then nothing we can
    // do but return 0.
    if(!(wordMap.containsKey(w1) && wordMap.containsKey(w2)) && !wordMap.containsKey(unk)){
      return 0.0;
    }
    
    double[] v1 = wordMap.containsKey(w1) ? wordMap.get(w1) : wordMap.get(unk);
    double[] v2 = wordMap.containsKey(w2) ? wordMap.get(w2) : wordMap.get(unk);
    
    if(v1 == null || v2 == null){
      if(w1.equals(w2)) return 1.0;
      else return 0.0;
    }
    
    sim = 0.0;
    for(int i = 0; i < v1.length; i++){
      sim += v1[i] * v2[i];      
    }
    return sim;
  }

}
