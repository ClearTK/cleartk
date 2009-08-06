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
package org.cleartk.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard, Philipp Wetzler
 */
public class ClassifierManifest extends Manifest {
	
	public static final Attributes.Name CLASSIFIER_BUILDER_ATTRIBUTE = new Attributes.Name("classifierBuilderClass");
	
	private ClassifierBuilder<?> classifierBuilder;
	
	public ClassifierManifest() {
		Attributes attributes = this.getMainAttributes();
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
	}
	
	public ClassifierManifest(ClassifierBuilder<?> classifierBuilder) {
		Attributes attributes = this.getMainAttributes();
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		attributes.put(CLASSIFIER_BUILDER_ATTRIBUTE, classifierBuilder.getClass().getName());
		this.classifierBuilder = classifierBuilder;
	}
	
	private ClassifierManifest(FileInputStream stream) throws IOException {
		super(stream);
		stream.close();
	}
	
	public ClassifierManifest(File dir) throws IOException {
		this(new FileInputStream(getFile(dir)));
		this.loadClassifierBuilder(getFile(dir).getPath());
	}
	
	public ClassifierManifest(JarFile jarFile) throws IOException {
		super(jarFile.getManifest());
		this.loadClassifierBuilder(jarFile.getName());
	}
	
	public void write(File dir) throws IOException {
		FileOutputStream manifestStream = new FileOutputStream(getFile(dir));
		this.write(manifestStream);
		manifestStream.close();
	}
	
	public void setClassifierBuilder(ClassifierBuilder<?> classifierBuilder) {
		Attributes attributes = this.getMainAttributes();
		attributes.put(CLASSIFIER_BUILDER_ATTRIBUTE, classifierBuilder.getClass().getName());
		this.classifierBuilder = classifierBuilder;
	}
	
	public ClassifierBuilder<?> getClassifierBuilder() {
		return this.classifierBuilder;
	}
	
	private void loadClassifierBuilder(String path) throws IOException {
		Attributes attributes = this.getMainAttributes();
		String classifierBuilderClassName = attributes.getValue(CLASSIFIER_BUILDER_ATTRIBUTE);
		if (classifierBuilderClassName == null) {
			throw new IOException(String.format("Missing %s attribute in manifest %s",
					CLASSIFIER_BUILDER_ATTRIBUTE, path));
		}
		Exception exception = null;
		try {
			this.classifierBuilder = Class.forName(classifierBuilderClassName).asSubclass(ClassifierBuilder.class).newInstance();
		} catch (ClassNotFoundException e) {
			exception = e;
		} catch (InstantiationException e) {
			exception = e;
		} catch (IllegalAccessException e) {
			exception = e;
		}
		if (exception != null) {
			throw new IOException(String.format("Invalid %s attribute in manifest %s",
					CLASSIFIER_BUILDER_ATTRIBUTE, path));
		}
	}
	
	private static File getFile(File dir) {
		return new File(dir, "MANIFEST.MF");
	}
	
}
