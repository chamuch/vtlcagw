package com.ericsson.raso.cac.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="config")
public class Config {
	
	private ArrayList<Section>section = new ArrayList<Section>() ;  
	public Config() {}  
	public Config(ArrayList<Section> section) {  
	    super();  
	    this.section = section;  
	} 
	
	
	@XmlElement(name="section")
	public ArrayList<Section> getSection() {
	    return section;  
	} 
		
	public void setSection(ArrayList<Section> section) {  
	    this.section = section;  
	}  
	  

}
