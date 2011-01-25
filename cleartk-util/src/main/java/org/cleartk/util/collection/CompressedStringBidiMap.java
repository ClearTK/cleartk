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
package org.cleartk.util.collection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.cleartk.util.BaseConversion;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip
 * 
 */
public class CompressedStringBidiMap extends DualHashBidiMap<String, String> implements
    GenKeyBidiMap<String, String> {

  private static final long serialVersionUID = 5362169827584657941L;

  private int count = 0;

  public String getOrGenerateKey(String value) {
    if (containsValue(value))
      return getKey(value);

    synchronized (this) {
      String key = BaseConversion.convertBase(count, 62);
      put(key, value);
      return key;
    }
  }

  public String put(String key, String value) {
    count++;
    return super.put(key, value);
  }

  public void read(Reader reader) throws IOException {
    clear();

    BufferedReader input = new BufferedReader(reader);

    String line = input.readLine();

    if (line == null)
      return;

    int tempcount = Integer.parseInt(line);

    while ((line = input.readLine()) != null) {
      int tabLocation = line.lastIndexOf('\t');
      String value = line.substring(0, tabLocation);
      String key = line.substring(tabLocation + 1);
      put(key, value);
    }

    count = tempcount;
    input.close();

  }

  public void write(Writer writer) throws IOException {
    write(writer, false);
  }

  public void write(Writer writer, boolean sortOutput) throws IOException {
    PrintWriter out = new PrintWriter(new BufferedWriter(writer));
    out.println(count);
    if (sortOutput) {
      List<String> lookupList = new ArrayList<String>();
      for (Map.Entry<String, String> entry : entrySet()) {
        lookupList.add(String.format("%s\t%s", entry.getValue(), entry.getKey()));
      }
      Collections.sort(lookupList);

      // write the sorted key value pairs
      for (String lookupString : lookupList) {
        out.println(lookupString);
      }
    } else {
      for (Map.Entry<String, String> entry : entrySet()) {
        out.println(String.format("%s\t%s", entry.getValue(), entry.getKey()));
      }
    }
    out.close();
  }
}
