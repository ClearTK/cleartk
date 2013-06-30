/*
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cleartk.util.BaseConversion;

import com.google.common.collect.BiMap;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.HashBiMap;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip
 * 
 */
public class CompressedStringBiMap extends ForwardingMap<String, String> implements
    GenKeyBiMap<String, String>, Serializable {

  private static final long serialVersionUID = 5362169827584657941L;

  private HashBiMap<String, String> delegate;

  private int count;

  public CompressedStringBiMap() {
    this.delegate = HashBiMap.create();
    this.count = 0;
  }

  @Override
  public String getOrGenerateKey(String value) {
    if (this.containsValue(value))
      return this.inverse().get(value);

    synchronized (this) {
      String key = BaseConversion.convertBase(this.count, 62);
      this.put(key, value);
      return key;
    }
  }

  @Override
  public String put(String key, String value) {
    this.count++;
    return this.delegate.put(key, value);
  }

  @Override
  public String forcePut(String key, String value) {
    return this.delegate.forcePut(key, value);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> map) {
    this.standardPutAll(map);
  }

  @Override
  public BiMap<String, String> inverse() {
    return this.delegate.inverse();
  }

  @Override
  protected Map<String, String> delegate() {
    return this.delegate;
  }

  @Override
  public Set<String> values() {
    return this.delegate.values();
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

  public void write(Writer writer) {
    write(writer, false);
  }

  public void write(Writer writer, boolean sortOutput) {
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
