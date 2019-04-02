package com.zkteco.biometric;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseHandler {

	/**
     * Connect to fingerprint database
     *
     */
    public static void connectDatabase() {
 
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/CrestMedical/db/fingerprints.db")) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                
                Statement createtable = conn.createStatement();
                String query = "CREATE TABLE IF NOT EXISTS fingerprints ("
                		+ "id INTEGER PRIMARY KEY,"
                		+ "fingerprint BLOB NOT NULL,"
                		+ "fid INTEGER NOT NULL,"
                		+ "case_id TEXT NOT NULL)";
                createtable.execute(query);
                
                
                
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void insert(byte[] fingerprint, int fid, String case_id) {
    	
    	String query = "INSERT INTO fingerprints (fingerprint, fid, case_id) VALUES (?, ?, ?)";
    	try(Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/CrestMedical/db/fingerprints.db");
    			PreparedStatement pstmt = conn.prepareStatement(query)){
    		pstmt.setBytes(1, fingerprint);
    		pstmt.setString(3, case_id);
    		pstmt.setInt(2, fid);
    		pstmt.executeUpdate();
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	
    }
    
    public static ArrayList<ArrayList<?>> getAll() {
    	ArrayList<ArrayList<?>> data = new ArrayList<>();
    	ArrayList<byte[]> fprints = new ArrayList<>();
    	ArrayList<Integer> fid = new ArrayList<>();
    	String query = "SELECT fingerprint, fid FROM fingerprints";
    	try(Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/CrestMedical/db/fingerprints.db");
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(query)){
    		while(rs.next()) {
    			fprints.add(rs.getBytes("fingerprint"));
    			fid.add(rs.getInt("fid"));
    			System.out.println("Retrieved patient iFid " + rs.getInt("fid"));
    		}
    		data.add(fprints);
    		data.add(fid);
    	}catch(SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return data;
    }
    
    public static int getSize() {
    	int size = -1;
    	String query = "SELECT id FROM fingerprints";
    	try(Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/CrestMedical/db/fingerprints.db");
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(query)){
    			size = 0;
    			while(rs.next()) {
    				size++;
    			}
    	}catch(SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return size;
    }
    
    public static ArrayList<String> getID(int id) {
    	ArrayList<String> case_array = new ArrayList<>();
    	String query = "SELECT case_id FROM fingerprints WHERE fid = ?";
    	try(Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/CrestMedical/db/fingerprints.db");
    			PreparedStatement pstmt = conn.prepareStatement(query)){
    			pstmt.setInt(1, id);
    			ResultSet rs = pstmt.executeQuery();
    		while(rs.next()) {
    			case_array.add(rs.getString("case_id"));
    			System.out.println("Retrieved patient case ID " + rs.getString("case_id"));
    		}
    	}catch(SQLException e) {
    		System.out.println(e.getMessage());
    	}
    	return case_array;
    }
    
}
