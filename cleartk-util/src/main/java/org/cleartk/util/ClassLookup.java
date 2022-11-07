/*
 * Copyright (c) 2022, Contributors to the ClearTK project
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
package org.cleartk.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.UimaContextHolder;

import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;

/**
 * <br>
 * Copyright (c) 2022, Contributors to the ClearTK project <br>
 * All rights reserved.
 */
public class ClassLookup {
  private ClassLookup() {
    // No instances
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> lookupClass(String aClass) throws ClassNotFoundException {
    List<ClassLoader> classLoaders = collectClassLoaders();

    StringBuilder errorMessage = new StringBuilder();
    errorMessage.append("Could not find class [" + aClass + "] in any of these classloaders:\n");
    for (ClassLoader cl : classLoaders) {
      try {
        return (Class<T>) cl.loadClass(aClass);
      } catch (ClassNotFoundException e) {
        errorMessage.append("- " + cl + "\n");
      }
    }

    throw new ClassNotFoundException(errorMessage.toString());
  }

  private static List<ClassLoader> collectClassLoaders() {
    List<ClassLoader> classLoaders = new ArrayList<>();

    UimaContext ctx = UimaContextHolder.getContext();
    if (ctx != null) {
      UimaContextAdmin ctxAdmin = (UimaContextAdmin) ctx;
      classLoaders.add(ctxAdmin.getResourceManager().getExtensionClassLoader());
    }
    classLoaders.add(Thread.currentThread().getContextClassLoader());
    classLoaders.add(ClassLookup.class.getClassLoader());
    classLoaders.removeIf(cl -> cl == null);
    return classLoaders;
  }

  public static ObjectInputStream streamObjects(InputStream aIs)
          throws StreamCorruptedException, IOException {
    return new ClassLoaderObjectInputStream(new MultipleParentClassLoader(collectClassLoaders()),
            aIs);
  }
}
