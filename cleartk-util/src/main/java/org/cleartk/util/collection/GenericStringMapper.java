/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
package org.cleartk.util.collection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 */
public class GenericStringMapper implements StringMapper, Writable {

  private static final long serialVersionUID = -9129249759791539649L;

  public GenericStringMapper(int cutoff) {
    this.cutoff = cutoff;
  }

  public int getOrGenerateInteger(String s) {
    if (expandMap) {
      if (countingMap.containsKey(s)) {
        Entry e = countingMap.get(s);
        e.increment();
        return e.i;
      } else {
        Entry e = new Entry(nextValue++);
        countingMap.put(s, e);
        return e.i;
      }
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public int getInteger(String s) throws UnknownKeyException {
    if (expandMap) {
      throw new UnsupportedOperationException();
    } else {
      if (stringIntMap.containsKey(s))
        return stringIntMap.get(s);
      else
        throw new UnknownKeyException(s);
    }
  }

  public void finalizeMap() {
    int total = 0;
    int kept = 0;

    stringIntMap = new WrappedHPPCStringIntMap();
    for (String s : countingMap.keySet()) {
      Entry e = countingMap.get(s);
      total += 1;

      if (e.count >= cutoff) {
        stringIntMap.put(s, e.i);
        kept += 1;
      }
    }

    Logger.getLogger(this.getClass().getName())
            .info(String
                    .format("discarded %d features that occurred less than %d times; %d features remaining",
                            total - kept, cutoff, kept));

    countingMap = null;
    expandMap = false;
  }

  public void write(File file) throws IOException {
    Writer writer = new FileWriter(file);
    write(writer);
    writer.close();
  }

  public void write(Writer writer) throws IOException {
    if (expandMap)
      throw new UnsupportedOperationException();

    for (String key : stringIntMap.keySet()) {
      writer.append(String.format("%d %s\n", stringIntMap.get(key), key));
    }

    writer.flush();
  }

  boolean expandMap = true;

  int nextValue = 1;

  int cutoff;

  Map<String, Entry> countingMap = new HashMap<String, Entry>();

  Map<String, Integer> stringIntMap = null;

  private static class Entry {
    public Entry(int i) {
      this.i = i;
      this.count = 1;
    }

    public void increment() {
      count += 1;
    }

    public int i;

    public int count;
  }

}
