package gov.va.utah.adigroup.symptom_mapping.utils;

import java.util.ArrayList;

public class PushandPop <T>{
	ArrayList<T> list;
	
	public PushandPop(){
		list = new ArrayList<T>();
	}
	
	public void push(T data){
		list.add(data);
	}
	
	public void pop(){
		list.remove(list.size()-1);
	}
	
	public T top(){
		return list.get(list.size()-1);
	}
	public void print(){
		for(int i =0; i < list.size(); i++){
			System.out.print(list.get(i).toString());
		}
	}
}

