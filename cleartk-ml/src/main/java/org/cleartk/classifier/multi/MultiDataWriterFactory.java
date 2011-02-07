package org.cleartk.classifier.multi;

import java.io.IOException;

import org.cleartk.classifier.DataWriter;

/**
 * This class is used by CleartkMultiAnnotator to manage the creation of {@link DataWriter} objects.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 */
public interface MultiDataWriterFactory<OUTCOME_TYPE> {
  /**
   * Creates a {@link DataWriter} associated with name. Methods that implement this method must
   * ensure creation of a unique DataWriter for each name
   * 
   * @param name
   * @return
   * @throws IOException
   */
  public DataWriter<OUTCOME_TYPE> createDataWriter(String name) throws IOException;
}
