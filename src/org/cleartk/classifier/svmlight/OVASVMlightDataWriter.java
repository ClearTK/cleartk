package org.cleartk.classifier.svmlight;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.util.featurevector.FeatureVector;

public class OVASVMlightDataWriter extends DataWriter_ImplBase<String,Integer,FeatureVector> {

	public OVASVMlightDataWriter(File outputDirectory) throws IOException {
		super(outputDirectory);

		// prepare output files
		allFalseFile = getFile("training-data-allfalse.svmlight");
		allFalseFile.delete();		

		// create the output writers
		allFalseWriter = new PrintWriter(allFalseFile);
		trainingDataWriters = new TreeMap<Integer,PrintWriter>();
	}

	@Override
	public void writeEncoded(FeatureVector features, Integer outcome) {
		if( ! trainingDataWriters.containsKey(outcome) ) {
			addClass(outcome);
		}
		
		StringBuffer featureString = new StringBuffer();
		for( FeatureVector.Entry entry : features ) {
			featureString.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
		}
		
		StringBuffer output = new StringBuffer();
		output.append("-1");
		output.append(featureString);
		allFalseWriter.println(output);
		
		for( int i : trainingDataWriters.keySet()) {
			output = new StringBuffer();
			if( outcome == i )
				output.append("+1");
			else
				output.append("-1");

			output.append(featureString);
			
			trainingDataWriters.get(i).println(output);
		}
	}
	

	@Override
	public void finish() throws IOException {
		super.finish();

		// close and remove all-false file
		allFalseWriter.close();
		allFalseFile.delete();

		// flush and close all training data writers
		for( PrintWriter pw : trainingDataWriters.values() ) {
			pw.flush();
			pw.close();
		}
	}

	public Class<? extends ClassifierBuilder<String>> getDefaultClassifierBuilderClass() {
		return OVASVMlightClassifierBuilder.class;
	}
	

	private void addClass(int label) {
		File newTDFile = getFile(String.format("training-data-%d.svmlight", label));
		newTDFile.delete();

		allFalseWriter.flush();
		try {
			copyFile(allFalseFile, newTDFile);
			trainingDataWriters.put(label, new PrintWriter(new BufferedWriter(new FileWriter(newTDFile, true))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	private void copyFile(File source, File target) throws IOException {
	     // Create channel on the source
       FileChannel srcChannel = new FileInputStream(source).getChannel();
   
       // Create channel on the destination
       FileChannel dstChannel = new FileOutputStream(target).getChannel();
   
       // Copy file contents from source to destination
       dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
   
       // Close the channels
       srcChannel.close();
       dstChannel.close();
   }

	private File allFalseFile;
	private PrintWriter allFalseWriter;
	private Map<Integer, PrintWriter> trainingDataWriters;
}
