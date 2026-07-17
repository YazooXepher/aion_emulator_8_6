package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.collection.CollectionTemplate;

import java.util.concurrent.ConcurrentHashMap;

@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "collection_templates")
public class CollectionData {

	@XmlElement(name = "collection_template")
	private List<CollectionTemplate> collectionTemplates;
	@XmlTransient
	private ConcurrentHashMap<Integer, CollectionTemplate> templates = new ConcurrentHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (CollectionTemplate template : collectionTemplates) {
			templates.put(template.getId(), template);
		}
	}

	public int size() {
		return templates.size();
	}

	public CollectionTemplate getTemplate(int lumielId) {
		return templates.get(lumielId);
	}
}
