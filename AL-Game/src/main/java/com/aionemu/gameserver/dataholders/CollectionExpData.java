package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.collection.CollectionExpTemplate;
import com.aionemu.gameserver.model.templates.collection.CollectionType;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlRootElement(name = "collection_exp_templates")
public class CollectionExpData {

	@XmlElement(name = "collection_exp_template")
	private List<CollectionExpTemplate> collectionExpTemplates;

	@XmlTransient
    private ConcurrentHashMap<CollectionType, List<CollectionExpTemplate>> expTemplateMap = new ConcurrentHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (CollectionExpTemplate template : collectionExpTemplates) {
            if (expTemplateMap.containsKey(template.getGrade())) {
                expTemplateMap.get(template.getGrade()).add(template);
            } else {
                List<CollectionExpTemplate> exp = new ArrayList<>();
                exp.add(template);
                expTemplateMap.put(template.getGrade(), exp);
            }
        }
    }

	public CollectionExpTemplate getTemplate(int level, CollectionType grade) {
        CollectionExpTemplate template = null;

        for (CollectionExpTemplate expTemplate : expTemplateMap.get(grade)) {
			if (expTemplate.getLevel() == level) {
				template = expTemplate;
            }
        }
        return template;
    }

	public int size() {
		return expTemplateMap.size();
	}
}
