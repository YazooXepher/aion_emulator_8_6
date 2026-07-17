package com.aionemu.gameserver.dataholders;

import java.util.List;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.dynamicportal.DynamicPortalLocation;
import com.aionemu.gameserver.model.templates.dynamicportal.DynamicPortalTemplate;

/**
 * @author Falke_34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dynamic_rift")
public class DynamicPortalData {

	@XmlElement(name = "dynamic_location")
	private List<DynamicPortalTemplate> dynamicPortalTemplates;
	
	@XmlTransient
	private ConcurrentHashMap<Integer, DynamicPortalLocation> dynamicPortal = new ConcurrentHashMap<Integer, DynamicPortalLocation>();
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (DynamicPortalTemplate template : dynamicPortalTemplates) {
			dynamicPortal.put(template.getId(), new DynamicPortalLocation(template));
		}
	}
	
	public int size() {
		return dynamicPortal.size();
	}
	
	public ConcurrentHashMap<Integer, DynamicPortalLocation> getDynamicPortalLocations() {
		return dynamicPortal;
	}
}
