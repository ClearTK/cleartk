package org.cleartk.classifier.feature.transform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Instance;

/**
 * 
 * <br>
 * Copyright (c) 2011-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * This training data (instance) consumer produces intermediate storage of instances for later use
 * in feature transformation (i.e. normalization, clustering, etc...)
 * 
 * @author Lee Becker
 * 
 * @param <OUTCOME_T>
 *          - The outcome type of the instances it writes out
 */
public class InstanceDataWriter<OUTCOME_T> implements DataWriter<OUTCOME_T> {

  FileOutputStream fileout;

  ObjectOutputStream objout;

  public static String INSTANCES_OUTPUT_FILENAME = "training-data.instances";

  public InstanceDataWriter(File outputDirectory) {
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    // Initialize Object Serializer
    File outputFile = new File(outputDirectory, INSTANCES_OUTPUT_FILENAME);
    try {
      this.fileout = new FileOutputStream(outputFile);
      this.objout = new ObjectOutputStream(this.fileout);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void write(Instance<OUTCOME_T> instance) throws CleartkProcessingException {
    try {
      this.objout.writeObject(instance);
    } catch (IOException e) {
      throw new CleartkProcessingException("", "Unable to write Instance", e);
    }
  }

  @Override
  public void finish() throws CleartkProcessingException {
    try {
      // We need to add a "null" instance terminator to gracefully handle the iteration while
      // reading in serialized objects from file
      InstanceStream.Terminator<OUTCOME_T> terminator = new InstanceStream.Terminator<OUTCOME_T>();
      this.objout.writeObject(terminator);

      this.objout.close();
      this.fileout.close();
    } catch (IOException e) {
      throw new CleartkProcessingException("", "Unable to write terminal instance", e);
    }

  }

}
