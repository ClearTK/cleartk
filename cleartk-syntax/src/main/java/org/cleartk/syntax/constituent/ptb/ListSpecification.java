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
package org.cleartk.syntax.constituent.ptb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * @deprecated Use the one in cleartk-corpus instead
 */

@Deprecated
public class ListSpecification implements Iterable<Integer> {

  private List<RangeSpecification> ranges;

  private SortedSet<Integer> numbers = new TreeSet<Integer>();

  public ListSpecification(String s) {
    this.ranges = new ArrayList<RangeSpecification>();

    for (String rangeSpec : s.trim().split(",")) {
      if (rangeSpec.length() > 0)
        this.ranges.add(new RangeSpecification(rangeSpec));
    }

    for (RangeSpecification r : ranges)
      for (Integer i : r)
        numbers.add(i);

  }

  public boolean contains(int i) {
    return numbers.contains(i);
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    boolean first = true;

    for (RangeSpecification range : this.ranges) {
      if (!first)
        buffer.append(",");

      buffer.append(range);
      first = false;
    }

    return buffer.toString();
  }

  public Iterator<Integer> iterator() {
    return numbers.iterator();
  }
}
