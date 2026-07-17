package com.aionemu.gameserver.dataholders;

import com.aionemu.gameserver.model.templates.lumiel_transform.LumielTransformTemplate;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.concurrent.ConcurrentHashMap;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlRootElement(name="lumiel_templates")
public class LumielTemplateData {

    @XmlElement(name="lumiel_template")
    private List<LumielTransformTemplate> lumielTransformTemplates;
    @XmlTransient
    private ConcurrentHashMap<Integer, LumielTransformTemplate> templates = new ConcurrentHashMap<Integer, LumielTransformTemplate>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (LumielTransformTemplate template : lumielTransformTemplates) {
            templates.put(template.getId(), template);
        }
    }

    public int size() {
        return templates.size();
    }

    public LumielTransformTemplate getTemplate(int lumielId) {
        return templates.get(lumielId);
    }

    public Map<Integer, LumielTransformTemplate> getAllTemplates() {
        return templates;
    }
}

