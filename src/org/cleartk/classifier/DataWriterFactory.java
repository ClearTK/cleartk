package org.cleartk.classifier;

import java.io.File;

import org.cleartk.Initializable;

public interface DataWriterFactory extends Initializable {
	
	public DataWriter<?> getDataWriter(File outputDirectory);

}
