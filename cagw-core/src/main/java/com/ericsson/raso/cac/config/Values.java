package com.ericsson.raso.cac.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="values")
public class Values {

	private ArrayList<Value> value;

	@XmlElement(name="value")
	public ArrayList<Value> getValue() {
		return value;
	}
	
	public void setValue(ArrayList<Value> value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Values [value=" + value + "]";
	}
	
	
}
