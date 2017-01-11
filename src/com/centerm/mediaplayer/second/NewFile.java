package com.centerm.mediaplayer.second;

import java.io.Serializable;

public class NewFile implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;

	private int mode;

	private int interval;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public NewFile(){

	}

	public NewFile(String name, int mode, int interval){
		super();
		this.name = name;
		this.mode = mode;
		this.interval = interval;
	}

}
