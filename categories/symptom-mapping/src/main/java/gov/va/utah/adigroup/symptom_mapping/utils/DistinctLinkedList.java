package gov.va.utah.adigroup.symptom_mapping.utils;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class DistinctLinkedList {
	ArrayList storage;
	
	public DistinctLinkedList(){
		storage = new ArrayList();
	}
	
	public boolean addWord(String word){
		for( int i = 0; i < storage.size(); i++){
			if( ((String) storage.get(i)).toString().compareTo(word) == 0){
				return false;
			}
			else if ( ((String) storage.get(i)).toString().compareTo(word) > 0){
				storage.add(i,word);
				return true;		
			}
		}
		storage.add(word);
		return true;
	}

	public void clear(){
		storage.clear();
	}
	public String getValue(int index){
		return storage.get(index).toString();
	}
	
	public int getSize(){
		return storage.size();
	}
	
	public void printList(String filename){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for(int i = 0; i < storage.size(); i++){
				bw.write((String) storage.get(i).toString() + "\n");
			}
			bw.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void printList(){
		for(int i = 0; i < storage.size(); i++){
			System.out.print((String) storage.get(i).toString() + "\n");
		}
	}
	public static void main(String [] args){
		DistinctLinkedList dll = new DistinctLinkedList();
		dll.addWord("C7687");
		dll.addWord("C7782");
		dll.addWord("C7796");
		dll.addWord("C7687");
		dll.addWord("C7786");
		dll.addWord("C7786");
		dll.addWord("C5678");
		dll.addWord("C7982");
		dll.addWord("C7786");
		dll.printList();
	}
}

