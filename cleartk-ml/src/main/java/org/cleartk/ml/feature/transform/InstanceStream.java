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
package org.cleartk.ml.feature.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.NoSuchElementException;

import org.cleartk.ml.Instance;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class wraps the data written out by an InstanceDataWriter into an Iterable collection
 * 
 * @author Lee Becker
 */
public class InstanceStream<OUTCOME_T> implements Iterable<Instance<OUTCOME_T>> {

  private URI uri;

  public static <OUTCOME_T> Iterable<Instance<OUTCOME_T>> loadFromDirectory(File dir) {

    File instancesFile = new File(dir, InstanceDataWriter.INSTANCES_OUTPUT_FILENAME);
    InstanceStream<OUTCOME_T> instanceStream = new InstanceStream<OUTCOME_T>(instancesFile.toURI());
    return instanceStream;
  }

  // public static java.util.Iterator<Instance> loadFromURI(URI uri) {
  public static <OUTCOME_T> Iterable<Instance<OUTCOME_T>> loadFromURI(URI uri) {

    InstanceStream<OUTCOME_T> instanceStream = new InstanceStream<OUTCOME_T>(uri);
    return instanceStream;
  }

  public static class Terminator<OUTCOME_T> extends Instance<OUTCOME_T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public Terminator() {
    }
  }

  /**
   * 
   * @author Lee Becker
   */
  public static class Iterator<OUTCOME_T> implements java.util.Iterator<Instance<OUTCOME_T>> {

    private URI instancesURI;

    private ObjectInputStream input;

    private Instance<OUTCOME_T> lastRead;

    private boolean done;

    public Iterator(URI uri) {
      this.instancesURI = uri;
      this.lastRead = null;
      this.done = false;

      try {
        FileInputStream fis = new FileInputStream(this.instancesURI.getPath());
        this.input = new ObjectInputStream(fis);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public boolean hasNext() {
      if (this.done) {
        return false;
      }

      try {
        @SuppressWarnings("unchecked")
        Instance<OUTCOME_T> inst = (Instance<OUTCOME_T>) this.input.readObject();
        if (inst instanceof InstanceStream.Terminator) {
          this.done = true;
        } else {
          this.lastRead = inst;
          this.done = false;
        }
      } catch (IOException e) {
        this.done = true;
      } catch (ClassNotFoundException e) {
        this.done = true;
      }

      return !this.done;

    }

    @SuppressWarnings("unchecked")
    @Override
    public Instance<OUTCOME_T> next() {
      if (this.lastRead != null) {
        Instance<OUTCOME_T> nextInst = this.lastRead;
        this.lastRead = null;
        return nextInst;
      }

      try {
        this.lastRead = null;
        return (Instance<OUTCOME_T>) this.input.readObject();
      } catch (IOException e) {
        throw new NoSuchElementException();
      } catch (ClassNotFoundException e) {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  public InstanceStream(URI uri) {
    this.uri = uri;
  }

  @Override
  public java.util.Iterator<Instance<OUTCOME_T>> iterator() {
    return new InstanceStream.Iterator<OUTCOME_T>(this.uri);
  }

}
