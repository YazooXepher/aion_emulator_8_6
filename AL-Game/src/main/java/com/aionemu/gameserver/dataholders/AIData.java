package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.ai.Ai;
import com.aionemu.gameserver.model.templates.ai.AITemplate;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xTz
 */
@XmlRootElement(name = "ai_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class AIData {

	@XmlElement(name = "ai", type = Ai.class)
	private List<Ai> templates;
	private ConcurrentHashMap<Integer, AITemplate> aiTemplate = new ConcurrentHashMap<Integer, AITemplate>();

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		aiTemplate.clear();
		for (Ai template : templates) {
			aiTemplate.put(template.getNpcId(), new AITemplate(template));
		}
	}

	public int size() {
		return aiTemplate.size();
	}

	public ConcurrentHashMap<Integer, AITemplate> getAiTemplate() {
		return aiTemplate;
	}
}
