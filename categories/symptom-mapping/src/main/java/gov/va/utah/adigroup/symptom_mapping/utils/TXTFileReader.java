package gov.va.utah.adigroup.symptom_mapping.utils;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TXTFileReader {
	BufferedReader br;
	String currentReadingLine;
	
	public TXTFileReader(String filename){		
		try{
			br = new BufferedReader(new FileReader(filename));
		}
		catch(FileNotFoundException ex){		
			ex.printStackTrace();
		}	
		
		try{
			br = new BufferedReader(new FileReader(filename));
		}
		catch(FileNotFoundException ex){		
			ex.printStackTrace();
		}		
	}
	
	public void close() throws Exception{
		br.close();
	}
	
	public boolean readMoreLine(){
		try{
			currentReadingLine = br.readLine();
			if( currentReadingLine == null)
				return false;
			return true;
		}catch(IOException ex){
			ex.printStackTrace();			
		}
		return false;
	}
	
	public String getLine(){
		return currentReadingLine;
	}
	
}

