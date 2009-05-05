//  Copyright (c) 1998-2008 Adrian Kuhn <akuhn(a)students.unibe.ch>
//  
//  This file is part of ch.akuhn.util.
//  
//  ch.akuhn.util is free software: you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation, either version 3 of the License, or (at your
//  option) any later version.
//  
//  ch.akuhn.util is distributed in the hope that it will be useful, but
//  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
//  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
//  License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public License
//  along with ch.akuhn.util. If not, see <http://www.gnu.org/licenses/>.
// 

/*
 * The original code in this class was obtained from 
 * 
 * https://www.iam.unibe.ch/scg/svn_repos/Sources/ch.akuhn.util/src/magic/Files.java
 * 
 * Major modifications to this file include:
 * <ul><li>removal of unwanted methods endsWith, openWrite, openRead, and close</li>
 * 	   <li>rewrite of main method.</li>
 * 	   <li>added method createSuffixFilter(String[])</li>
 * 	   <li>renamed 'all' methods to 'getFiles'.
 * 	   <li>does not add hidden files to the queue.
 * </ul>
 */

package org.cleartk.util.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;



public class Files {
	public static FileFilter createSuffixFilter(final String[] suffixes) {
		return new FileFilter() {

			public boolean accept(File file) {
				if(suffixes == null)
					return true;
				if(suffixes.length == 0)
					return true;
				else {
					for(String suffix : suffixes) {
						if(file.getName().endsWith(suffix))
							return true;
					}
				}
				return false;
			}
		};
	}

	public static Iterable<File> getFiles(final File folder, final Set<String> fileNames) {
		return new Iterable<File>() {

			public Iterator<File> iterator() {
				return new Iterator<File>() {
					private LinkedList<File> queue = new LinkedList<File>();
					{
						queue.offer(folder);
						processDirectories();
					}

					public boolean hasNext() {
						this.processDirectories();
						return !queue.isEmpty();
					}

					public File next() {
						this.processDirectories();
						if (queue.isEmpty())
							throw new NoSuchElementException();
						return queue.poll();
					}

					private void processDirectories() {
						while (!queue.isEmpty()) {
							if (!queue.peek().isDirectory())
								break;
							File next = queue.poll();
							for (File file : next.listFiles()) {
								String fileName = file.getName();
								if(file.getName().startsWith("."))
									continue;
								if (file.isDirectory() || fileNames.contains(fileName)) {
									queue.offer(file);
								}
							}
						}
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

				};
			}
		};

	}
	public static Iterable<File> getFiles(final File folder, final FileFilter filter) {
		return new Iterable<File>() {

			public Iterator<File> iterator() {
				return new Iterator<File>() {
					private LinkedList<File> queue = new LinkedList<File>();
					{
						queue.offer(folder);
						processDirectories();
					}

					public boolean hasNext() {
						this.processDirectories();
						return !queue.isEmpty();
					}

					public File next() {
						this.processDirectories();
						if (queue.isEmpty())
							throw new NoSuchElementException();
						return queue.poll();
					}

					private void processDirectories() {
						while (!queue.isEmpty()) {
							if (!queue.peek().isDirectory())
								break;
							File next = queue.poll();
							for (File each : next.listFiles()) {
								if(each.getName().startsWith("."))
									continue;
								if (each.isDirectory() || filter == null || filter.accept(each)) {
									queue.offer(each);
								}
							}
						}
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

				};
			}
		};
	}

	public static Iterable<File> getFiles(String filename) {
		return getFiles(new File(filename));
	}

	public static Iterable<File> getFiles(File file) {
		return getFiles(file, (FileFilter) null);
	}

	public static Iterable<File> getFiles(String filename, FileFilter filter) {
		return getFiles(new File(filename), filter);
	}

	public static Iterable<File> getFiles(String filename, String[] suffixes) {
		return getFiles(new File(filename), suffixes);
	}

	public static Iterable<File> getFiles(File file, String[] suffixes) {
		return getFiles(file, createSuffixFilter(suffixes));
	}


	/**
	 * Makes the file name relative to the root directory by stripping the input
	 * directory prefix from the file name.
	 * 
	 * @param file
	 *            The file whose name is to be relativized.
	 * @return The relativized path.
	 */
	public static String stripRootDir(File rootFile, File file) {
		// get absolute paths for the root directory and the file
		String dirPath = rootFile.getAbsolutePath();
		String filePath = file.getAbsolutePath();

		// strip the directory path from the beginning of the file path
		if (!filePath.startsWith(dirPath)) {
			String format = "%s does not start with %s";
			String message = String.format(format, filePath, dirPath);
			throw new IllegalArgumentException(message);
		}
		return filePath.substring(dirPath.length());
	}

}















