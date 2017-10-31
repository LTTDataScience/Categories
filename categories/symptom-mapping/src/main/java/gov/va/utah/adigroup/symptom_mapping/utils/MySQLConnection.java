package gov.va.utah.adigroup.symptom_mapping.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;
import com.mysql.jdbc.Driver;

public class MySQLConnection {
	Connection conn = null;

	public MySQLConnection() throws Exception {
		// Please change the following databse connection
		String     host = "localhost:3306";
		String database = "umls";
		String url = "jdbc:mysql://" + host + "/" + database;
        try
        {
        	Class.forName("com.mysql.jdbc.Driver"); 
            conn = DriverManager.getConnection(url,"ltran","admin2017!");
            System.out.println("Connection established to " + url + "...");
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("Connection couldn't be established to " + url);
            throw (e);
        }
	}
	
    /**
     * This method executes an update statement
     * @param con database connection
     * @param sqlStatement SQL DDL or DML statement to execute
     */
    public void executeUpdate(String sqlStatement) throws Exception
    {
        try
        {
            Statement s = conn.createStatement();
            s.execute(sqlStatement);
            s.close();
        }
        catch (SQLException e)
        {
            System.out.println("Error executing sql statement");
            throw (e);
        }
        
    }
    
    public Statement createStatement() {
    	try{
    		return conn.createStatement();
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		return null;    		
    	}
    }
   
    /**
     * This method executes a select statement and displays the result
     * @param con database connection
     * @param sqlStatement SQL SELECT statement to execute
     */
    public ResultSet executeQuery(Statement s, String sqlStatement) throws Exception
    {
        try
        {
            //s = conn.createStatement();

            return s.executeQuery(sqlStatement);
        }
        catch (SQLException e)
        {
            System.out.println("Error executing sql statement");
            throw (e);
        }
    }
    
    
    
    public void close() throws Exception{
    	conn.close();
    }

}

