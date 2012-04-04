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
 */

package org.cleartk.util;
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
		os = System.getProperties().getProperty("os.name").toLowerCase();
		String arch = System.getProperties().getProperty("os.arch");
		//resolve architecture
    	if (
    			arch.equals("x86") ||
    			arch.equals("i386") ||
    			arch.equals("i486") ||
    			arch.equals("i586") ||
    			arch.equals("i686")
    	) {
    		arch = ARCH_X86_32;
    	}
    	if (
    			arch.equals("amd64")
    	) {
    		arch = ARCH_X86_64;
    	}
    	if (arch.equals("powerpc")) {
    		arch = ARCH_PPC;
    	}
    	this.arch = arch;

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
