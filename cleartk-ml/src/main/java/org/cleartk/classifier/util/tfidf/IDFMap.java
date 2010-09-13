/** 
  * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.util.tfidf;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cleartk.classifier.feature.Counts;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philipp Wetzler
 */

public class IDFMap implements Serializable {

	private static final long serialVersionUID = 8053199003361771143L;
	
	public static IDFMap read(File file) throws IOException {
		Connection connection;
		PreparedStatement stat;
		ResultSet rs;
		IDFMap idfMap = new IDFMap();
		
		try {
			connection = openDB(file);
			
			stat = connection.prepareStatement("select value from globals "
					+ "where name = ?");
			try {
				stat.setString(1, "totalDocumentCount");
				rs = stat.executeQuery();
				try {
					rs.next();
					idfMap.totalDocumentCount = rs.getInt(1);
				} finally { rs.close(); }
			} finally { stat.close(); }
			
			stat = connection.prepareStatement("select key, count from documentcounts ");
			try {
				rs = stat.executeQuery();
				try {
					while( rs.next() ) {
						String key = rs.getString("key");
						int count = rs.getInt("count");
						idfMap.documentCounts.put(key, count);
					}
				} finally { rs.close(); }
			} finally { stat.close(); }
			
			connection.close();
			
			return idfMap;
		} catch (SQLException e) {
			throw new IOException(e.toString());
		}
	}

	public IDFMap() throws IOException {
		this.totalDocumentCount = 0;
		this.documentCounts = new HashMap<String, Integer>();
	}
	
	public void consume(Counts counts) {
		totalDocumentCount += 1;
		
		for( Object value : counts.getValues() ) {
			if( counts.getCount(value) == 0 )
				continue;
			
			String valueString = value.toString();
			if( documentCounts.containsKey(valueString) )
				documentCounts.put(valueString, documentCounts.get(valueString) + 1);
			else
				documentCounts.put(valueString, 1);
		}
	}
	
	public Double getIDF(Object key) {
		String keyString = key.toString();
		Double documentCount = documentCounts.containsKey(keyString) ?
				documentCounts.get(keyString) : 0.0;
				
		return Math.log((totalDocumentCount + 1)/ (documentCount + 1));
	}
	
	public int getTotalDocumentCount() {
		return totalDocumentCount;
	}
	
	public int getDocumentCount(Object key) {
		String keyString = key.toString();
		return documentCounts.containsKey(keyString) ?
				documentCounts.get(keyString) : 0;
	}

	public Iterator<?> getValues() {
		return documentCounts.keySet().iterator();
	}

	
	public void write(File file) throws IOException {
		try {
			File tempFile = new File(file.toString() + "_temp");
			if( tempFile.exists() )
				tempFile.delete();
			
			Connection connection = createDB(tempFile);
			
			PreparedStatement stat = connection.prepareStatement("insert into globals "
					+ "(name, value) "
					+ "values "
					+ "(?, ?)");
			try {
				stat.setString(1, "totalDocumentCount");
				stat.setInt(2, totalDocumentCount);
				stat.execute();
			} finally { stat.close(); }
			
			for( String key : documentCounts.keySet() ) {
				stat = connection.prepareStatement("insert into documentcounts "
						+ "(key, count) "
						+ "values "
						+ "(?, ?)");
				try {
					stat.setString(1, key);
					stat.setInt(2, documentCounts.get(key));
					stat.execute();
				} finally { stat.close(); }
			}
			
			connection.commit();
			connection.close();
			
			if( file.exists() )
				file.delete();
			
			tempFile.renameTo(file);
		} catch (SQLException e) {
			throw new IOException(e.toString());
		}
	}

	private static Connection createDB(File file) throws SQLException {
		Connection connection = null;
		PreparedStatement stat;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + file.toString());
			connection.setAutoCommit(false);

			stat = connection.prepareStatement("create table documentcounts "
					+ "(key text, "
					+ "count integer)");
			try { stat.execute(); }
			finally { stat.close(); }
			
			stat = connection.prepareStatement("create table globals "
					+ "(name text, "
					+ "value)");
			try { stat.execute(); }
			finally { stat.close(); }
			
			connection.commit();
			return connection;
		} catch (SQLException e) {
			if( connection != null )
				try { connection.rollback(); }
				catch( SQLException e1 ) {}
			throw e;
		} catch (ClassNotFoundException e) {
			throw new TypeNotPresentException("org.sqlite.JDBC", e);
		}
	}

	private static Connection openDB(File file) throws SQLException {
		Connection connection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + file.toString());
			connection.setAutoCommit(false);
			
			return connection;
		} catch (SQLException e) {
			if( connection != null )
				try { connection.rollback(); }
				catch( SQLException e1 ) {}
			throw e;
		} catch (ClassNotFoundException e) {
			throw new TypeNotPresentException("org.sqlite.JDBC", e);
		}
	}
	
	private int totalDocumentCount;
	private Map<String, Integer> documentCounts;
}
