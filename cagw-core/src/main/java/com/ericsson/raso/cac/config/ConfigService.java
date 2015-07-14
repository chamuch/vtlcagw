package com.ericsson.raso.cac.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.satnar.common.LogService;

//import com.ericsson.raso.cac.core.config.RangeRouter;

public class ConfigService implements IConfig {
		
	Config conf = null;

	public ConfigService() {
		init();
	}

	public void init() {
		try {
			String filename = "config.xml";
			String finalfile = "";
			String workingDir = System.getenv("CONFIG_HOME");
			String your_os = System.getProperty("os.name").toLowerCase();
			if (your_os.indexOf("win") >= 0) {
				finalfile = workingDir + "\\" + filename;
			} else if (your_os.indexOf("nix") >= 0
					|| your_os.indexOf("nux") >= 0) {
				finalfile = workingDir + "/" + filename;
			} else {
				finalfile = workingDir + "{others}" + filename;
			}

			// Setup JAXB to unmarshal
			JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			// jaxbUnmarshaller.setValidating(false);
			conf = (Config) jaxbUnmarshaller.unmarshal(new File(finalfile));			
			
		} catch (Exception e) {
			//e.printStackTrace();
			LogService.appLog.debug("ConfigService-init:JAXBException while loading configuration",e);
			throw new RuntimeException(
					"JAXBException while loading configuration", e);
		}		
		LogService.appLog.info("ConfigService-init:Config loaded successfully!!");
	}
	
	public Properties getProperties(String sectionName){
		Section section = getSection(sectionName);
		if ( section == null)
			throw new IllegalArgumentException(String.format("%s is not configured in the config.xml", sectionName));
		
		List<Property> sectionProperties = getProperties(section);
		Properties properties = new Properties();
		for (Property property : sectionProperties) {
			properties.put(property.getKey(), property.getValue());
		}
		return properties;
	}
	
	public Section getSection(String sectionId) {
		ArrayList<Section> list = conf.getSection();
		Section section = null;
		for (Section sectionList : list) {
			if (sectionList.getId().equalsIgnoreCase(sectionId)) {
				section = sectionList;
				break;
			}
		}
		return section;
	}

	public List<Property> getProperties(Section section) {
		ArrayList<Property> propertyList = section.getProperty();
		return propertyList;
	}

	public String getValue(String sectionId, String propKey) {
		String retVal = null;
		Section section = getSection(sectionId);
		if (section != null) {
			List<Property> sectionProperties = getProperties(section);
			if (sectionProperties != null) {
				for (Property property : sectionProperties) {
					if (property.getKey().equalsIgnoreCase(propKey)) {
						return property.getValue();
					}
				}
			}
		}
		return retVal;
	}

	public Value getRoute(String sectionId, long locate) {
		// return RangeRouter.getInstance().getRoute(sectionId, locate);
		return null;
	}
}
