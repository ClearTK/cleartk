package org.cleartk.classifier.feature.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.NoSuchElementException;

import org.cleartk.classifier.Instance;

/**
 * 
 * This class wraps the data written out by an InstanceDataWriter into an Iterable collection
 * 
 * @author Lee Becker
 * 
 * @param <OUTCOME_T>
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
   * 
   * @param <OUTCOME_T>
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
