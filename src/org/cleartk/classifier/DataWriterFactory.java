package org.cleartk.classifier;

import java.io.File;
import java.io.IOException;

import org.cleartk.Initializable;

public interface DataWriterFactory extends Initializable {
	
	public DataWriter<?> createDataWriter(File outputDirectory) throws IOException;

}
