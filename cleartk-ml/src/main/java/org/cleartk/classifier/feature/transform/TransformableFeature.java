/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.classifier.feature.transform;

import java.util.List;

import org.cleartk.classifier.Feature;

/**
 * Abstract base class for transformable features.
 * <P>
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 */
public class TransformableFeature extends Feature {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected List<Feature> features;

  private static final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "This is a temporary "
      + "place-holder feature that cannot be written by most data writers. Instead, it must be "
      + "written with an " + InstanceDataWriter.class.getName() + "and then the instances should"
      + "be transformed by the appropriate " + TrainableExtractor.class.getName();

  public TransformableFeature(String name, List<Feature> features) {
    super();
    this.setName(name);
    this.features = features;
  }

  // Following are unsupported to prevent anything but InstanceDataWriter from writing transformable
  // features.
  @Override
  public Object getValue() {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  @Override
  public void setValue(Object value) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  public List<Feature> getFeatures() {
    return this.features;
  }

}
