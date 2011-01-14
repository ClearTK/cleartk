/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * @author Philipp Wetzler
 * @author Steven Bethard
 */

public class Feature {

  protected String name;

  protected Object value;

  public Feature() {
  }

  public Feature(Object value) {
    this.value = value;
  }

  public Feature(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public static Feature createFeature(String namePrefix, Feature feature) {
    return new Feature(createName(namePrefix, feature.name), feature.value);
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static String createName(String... names) {
    StringBuffer buffer = new StringBuffer();
    for (String name : names) {
      if (name != null) {
        buffer.append(name);
        buffer.append('_');
      }
    }
    if (buffer.length() > 0) {
      buffer.deleteCharAt(buffer.length() - 1);
    }
    return buffer.toString();
  }

  public String toString() {
    String className = Feature.class.getSimpleName();
    return String.format("%s(<%s>, <%s>)", className, this.name, this.value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Feature) {
      Feature other = (Feature) obj;
      boolean nameMatch = (this.name == null && other.name == null)
              || (this.name != null && this.name.equals(other.name));
      boolean valueMatch = (this.value == null && other.value == null)
              || (this.value != null && this.value.equals(other.value));
      return nameMatch && valueMatch;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + (this.name == null ? 0 : this.name.hashCode());
    hash = hash * 31 + (this.value == null ? 0 : this.value.hashCode());
    return hash;
  }

}
