package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.lumiel_transform.LumielMaterialTemplate;

import java.util.concurrent.ConcurrentHashMap;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlRootElement(name="lumiel_material_templates")
public class LumielMaterialData {

    @XmlElement(name="lumiel_material_template")
    private List<LumielMaterialTemplate> materialTemplate;
    @XmlTransient
    private ConcurrentHashMap<Integer, LumielMaterialTemplate> templates = new ConcurrentHashMap<Integer, LumielMaterialTemplate>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (LumielMaterialTemplate template : materialTemplate) {
            templates.put(template.getId(), template);
        }
    }

    public int size() {
        return templates.size();
    }

    public LumielMaterialTemplate getTemplate(int lumielId, int itemId) {
        LumielMaterialTemplate lumiel = null;
        for (LumielMaterialTemplate template : templates.values()) {
            if (template.getLumielId() != lumielId || template.getItemId() != itemId) continue;
            lumiel = template;
        }
        return lumiel;
    }
}

