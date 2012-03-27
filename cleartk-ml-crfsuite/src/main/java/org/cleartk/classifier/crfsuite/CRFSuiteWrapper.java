/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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


package org.cleartk.classifier.crfsuite;

import static org.cleartk.util.PlatformDetection.ARCH_X86_32;
import static org.cleartk.util.PlatformDetection.ARCH_X86_64;
import static org.cleartk.util.PlatformDetection.OS_WINDOWS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.CleartkEncoderException;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.InputStreamHandler;
import org.cleartk.util.PlatformDetection;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;


/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Martin Riedl
 */



public class CRFSuiteWrapper {
	static Logger logger = UIMAFramework.getLogger(CRFSuiteWrapper.class);
	private File executable;

	public CRFSuiteWrapper() {
		Executables exec = new Executables();
		if (exec.isInstalled()) {
			logger.log(Level.INFO, "The CRFSuite is installed on the system");
			executable = new File(exec.getExecutableName());
		} else {
			
			this.executable = exec.getExecutable();
			if(!exec.isInstalled(this.executable.getAbsolutePath())){
				logger.log(Level.WARNING, "The CRFSuite binary is not available for the current operation system, please install it!");	
			}else{
				logger.log(Level.INFO, "The CRFSuite binary is successfully extracted");
			}
		}
	}

	class Executables {
		PlatformDetection pd = new PlatformDetection();
		public Executables() {
			
			
			// windows 64 should also be able to execute win32 files --> change
			// it to win 32, this can be fixed, when a win 64 Bit executable is available
			if (pd.getOs().equals(OS_WINDOWS)
					&& pd.getArch().equals(ARCH_X86_64)) {
				pd.setArch(ARCH_X86_32);
			}
		}

		public String getExecutablePath() {

			String[] path = new String[] { "crfsuite", pd.toString(),
					"bin" };
			String sep = "/";
			String p = "";
			for (String s : path) {
				p += s + sep;
			}
			return p;

		}

		public boolean isInstalled() {
			return isInstalled("crfsuite");
		}

		public boolean isInstalled(String path) {
			ProcessBuilder builder = new ProcessBuilder(path, "-h");
			builder.redirectErrorStream();
			Process p;

			try {
				p = builder.start();
				InputStreamHandler<StringBuffer> output = InputStreamHandler
						.getInputStreamAsBufferedString(p.getInputStream());
				try {
					p.waitFor();
					output.join();
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, e.getMessage());
				}
				StringBuffer buffer = output.getBuffer();
				if(buffer.length()<8){
					logger.log(Level.WARNING, "CRFSuite could not be executed!");
				}
				return (buffer.substring(0, 8).equals("CRFSuite"));
			} catch (IOException e) {
				//Path is not available
				logger.log(Level.FINE, e.getMessage());
			}
			return false;
		}

		
		public String getExecutableName() {
			return "crfsuite" + pd.getExecutableSuffix();
		}

		public File getExecutable() {
			String loc = getExecutablePath() + getExecutableName();
			URL crfExecUrl = getClass().getResource(loc);
			crfExecUrl = ClassLoader.getSystemResource(loc);
			logger.log(Level.FINE, "CrfSuite Location " + loc);
			logger.log(Level.FINE, "CrfSuite Url: " + crfExecUrl);
			File f;
			try {
				if (crfExecUrl != null) {
					
					f = new File(ResourceUtils.getUrlAsFile(crfExecUrl, true)
							.toURI().getPath());
					if (!f.exists()) {
						f = new File(URLDecoder.decode(f.getAbsolutePath(),
								("UTF-8")));
					}
					f.setExecutable(true);
					return f;

				}
				logger.log(Level.WARNING,
						"The executable could not be found at " + loc);
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				
				return null;
			}

		}

	}

	public void trainClassifier(String model, String trainingDataFile,
			String[] args) throws IOException {
		
		StringBuffer cmd = new StringBuffer();
		cmd.append(executable.getPath());cmd.append(" ");
		cmd.append("learn");cmd.append(" ");
		cmd.append("-m");cmd.append(" ");
		cmd.append(model);cmd.append(" ");
		for (String a : args) {
			cmd.append(a);cmd.append(" ");
		}
		cmd.append(trainingDataFile);
		Process p = Runtime.getRuntime().exec(cmd.toString());

		
		InputStream stdIn = p.getInputStream();
		InputStreamHandler<List<String>> ishIn = InputStreamHandler
				.getInputStreamAsList(stdIn);

		InputStream stdErr = p.getErrorStream();
		InputStreamHandler<StringBuffer> ishErr = InputStreamHandler
				.getInputStreamAsBufferedString(stdErr);

		try {
			p.waitFor();
			ishIn.join();
			ishErr.join();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	
		logger.log(Level.WARNING, ishErr.getBuffer().toString().replaceAll("(^\\[)|([,])|(]$)", "\n"));
		logger.log(Level.INFO, ishIn.getBuffer().toString().replaceAll("(^\\[)|([,])|(]$)", "\n"));
		stdErr.close();
		stdIn.close();
		
		
	}

	public List<String> classifyFeatures(String featureFile, String modelFile,
			int featureSize) throws IOException {
		return classifyFeatures(new File(featureFile), new File(modelFile),
				featureSize);
	}

	public List<String> classifyFeatures(File featureFile, File modelFile,
			int featureSize) throws IOException {
		List<String> result = new ArrayList<String>();
		result = classifyFeatures(modelFile, featureFile);
		if (result.size() != featureSize) {
			throw new IllegalStateException(
					"The number of extracted classified labels is not equivalent with the number of instanzes ("
							+ result.size() + "!=" + featureSize + ")");
		}

		return result;

	}

	public List<String> classifyFeatures(List<List<Feature>> features,
			OutcomeEncoder<String, String> outcomeEncoder,
			FeaturesEncoder<List<NameNumber>> featuresEncoder, File modelFile)
			throws IOException {

		File featureFile = File.createTempFile("features", ".crfsuite");
		featureFile.deleteOnExit();
		logger.log(
				Level.FINE,
				"Write features to classify to "
						+ featureFile.getAbsolutePath());
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(featureFile));
			for (List<Feature> f : features) {
				List<NameNumber> fe;
				fe = featuresEncoder.encodeAll(f);
				for (NameNumber nn : fe) {
					out.append(nn.name);
					out.append("\t");
				}
				out.append("\n");
			}

			out.close();
			return classifyFeatures(featureFile, modelFile, features.size());
		} catch (CleartkEncoderException e) {
			logger.log(Level.WARNING, e.getMessage());
		}
		return null;

	}

	private List<String> classifyFeatures(File modelFile, File featureFile)
			throws IOException {
		List<String> posTags = new ArrayList<String>();
		StringBuffer cmd = new StringBuffer();
		
		cmd.append(executable.getPath());
		cmd.append(" tag -m ");
		cmd.append(modelFile.getAbsolutePath());cmd.append(" ");
		cmd.append(featureFile.getAbsolutePath());cmd.append(" ");
		Process p = Runtime.getRuntime().exec(cmd.toString());
		
		InputStream stdIn = p.getInputStream();
		InputStreamHandler<List<String>> ishIn = InputStreamHandler
				.getInputStreamAsList(stdIn);

		InputStream stdErr = p.getErrorStream();
		InputStreamHandler<StringBuffer> ishErr = InputStreamHandler
				.getInputStreamAsBufferedString(stdErr);

		try {
			p.waitFor();
			ishIn.join();
			ishErr.join();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage());
		}
		logger.log(Level.WARNING, ishErr.getBuffer().toString().replaceAll("(^\\[)|([,])|(]$)", "\n"));
		posTags = ishIn.getBuffer();
		if (posTags.size() > 0) {
			if (posTags.get(posTags.size() - 1).trim().length() == 0) {
				posTags.remove(posTags.size() - 1);
			}
		}
		stdErr.close();
		stdIn.close();
		return posTags;
	}

}
