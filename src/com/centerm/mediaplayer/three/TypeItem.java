package com.centerm.mediaplayer.three;

import java.util.ArrayList;

public class TypeItem {
	
	private String id; //数据库中唯一标识

	private String name;

	private ArrayList<String[]> array;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String[]> getArray() {
		return array;
	}

	public void setArray(ArrayList<String[]> array) {
		this.array = array;
	}
}
