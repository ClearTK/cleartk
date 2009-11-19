package org.cleartk.util.collection;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

public interface Writable {

	public void write(File file) throws IOException;
	
	public void write(Writer writer) throws IOException;
	
}
