/** 
 * Copyright 2011-2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;

/**
 * <br>
 * Copyright (c) 2011-2012, Technische Universität Darmstadt <br>
 * All rights reserved.
 * 
 * 
 * @author Martin Riedl
 */

public class PlatformDetection {
  private String os;

  private String arch;

  public static String OS_WINDOWS = "windows";

  public static String OS_OSX = "osx";

  public static String OS_SOLARIS = "solaris";

  public static String OS_LINUX = "linux";

  public static String ARCH_PPC = "ppc";

  public static String ARCH_X86_32 = "x86_32";

  public static String ARCH_X86_64 = "x86_64";

  public PlatformDetection() {
    // resolve OS
    if (SystemUtils.IS_OS_WINDOWS) {
      this.os = OS_WINDOWS;
    } else if (SystemUtils.IS_OS_MAC_OSX) {
      this.os = OS_OSX;
    } else if (SystemUtils.IS_OS_SOLARIS) {
      this.os = OS_SOLARIS;
    } else if (SystemUtils.IS_OS_LINUX) {
      this.os = OS_LINUX;
    } else {
      throw new IllegalArgumentException("Unknown operating system " + SystemUtils.OS_NAME);
    }

    // resolve architecture
    Map<String, String> archMap = new HashMap<String, String>();
    archMap.put("x86", ARCH_X86_32);
    archMap.put("i386", ARCH_X86_32);
    archMap.put("i486", ARCH_X86_32);
    archMap.put("i586", ARCH_X86_32);
    archMap.put("i686", ARCH_X86_32);
    archMap.put("x86_64", ARCH_X86_64);
    archMap.put("amd64", ARCH_X86_64);
    archMap.put("powerpc", ARCH_PPC);
    this.arch = archMap.get(SystemUtils.OS_ARCH);
    if (this.arch == null) {
      throw new IllegalArgumentException("Unknown architecture " + SystemUtils.OS_ARCH);
    }
  }

  public String getOs() {
    return os;
  }

  public String getArch() {
    return arch;
  }

  public void setArch(String arch) {
    this.arch = arch;
  }

  public void setOs(String os) {
    this.os = os;
  }

  @Override
  public String toString() {

    return os + "_" + arch;
  }

  public String getExecutableSuffix() {
    if (getOs().equals(OS_WINDOWS))
      return ".exe";
    return "";
  }
}
