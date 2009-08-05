package org.cleartk.tfidf;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.cleartk.classifier.feature.Counts;

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
				
		return Math.log(totalDocumentCount / documentCount);
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
