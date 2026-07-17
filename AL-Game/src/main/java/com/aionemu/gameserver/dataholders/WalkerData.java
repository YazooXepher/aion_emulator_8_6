package com.aionemu.gameserver.dataholders;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.world.World;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KKnD, Rolandas
 */
@XmlRootElement(name = "npc_walker")
@XmlAccessorType(XmlAccessType.FIELD)
public class WalkerData {

	private static final Logger log = LoggerFactory.getLogger(WalkerData.class);
	@XmlElement(name = "walker_template")
	private List<WalkerTemplate> walkerlist;
	@XmlTransient
	private ConcurrentHashMap<String, WalkerTemplate> walkerlistData = new ConcurrentHashMap<String, WalkerTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (WalkerTemplate route : walkerlist) {
			if (walkerlistData.containsKey(route.getRouteId())) {
				log.warn("Duplicate route ID: " + route.getRouteId());
				continue;
			}
			walkerlistData.put(route.getRouteId(), route);
		}
		walkerlist.clear();
		walkerlist = null;
	}

	public int size() {
		return walkerlistData.size();
	}

	@Override
	public WalkerData clone() {
		WalkerData wd = new WalkerData();
		wd.setWalkerlistData(getWalkerlistData());
		return wd;
	}

	public WalkerTemplate getWalkerTemplate(String routeId) {
		if (routeId == null) {
			return null;
		}
		return walkerlistData.get(routeId);
	}

	public void AddTemplate(WalkerTemplate newTemplate) {
		if (walkerlist == null) {
			walkerlist = new ArrayList<WalkerTemplate>();
		}
		walkerlist.add(newTemplate);
	}

	public void saveData(String routeId) {
		Schema schema = null;
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		try {
			schema = sf.newSchema(new File("./data/static_data/npc_walker/npc_walker.xsd"));
		}
		catch (SAXException e1) {
			log.error("Error while saving data: " + e1.getMessage(), e1.getCause());
			return;
		}

		File xml = new File("./data/static_data/npc_walker/generated_npc_walker_" + routeId + ".xml");
		JAXBContext jc;
		Marshaller marshaller;
		try {
			jc = JAXBContext.newInstance(WalkerData.class);
			marshaller = jc.createMarshaller();
			marshaller.setSchema(schema);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, xml);
		}
		catch (JAXBException e) {
			log.error("Error while saving data: " + e.getMessage(), e.getCause());
			return;
		}
		finally {
			if (walkerlist != null) {
				walkerlist.clear();
				walkerlist = null;
			}
		}
	}

	public Collection<WalkerTemplate> getTemplates() {
		return walkerlistData.values();
	}

	private ConcurrentHashMap<String, WalkerTemplate> getWalkerlistData() {
		return walkerlistData;
	}

	private void setWalkerlistData(ConcurrentHashMap<String, WalkerTemplate> walkerlistData) {
		this.walkerlistData = walkerlistData;
	}

	public void replaceTemplate(WalkerTemplate template) {
		if (walkerlistData.containsKey(template.getRouteId()))
			walkerlistData.remove(template.getRouteId());

		walkerlistData.put(template.getRouteId(), template);
	}

	public void writeXml(int worldId) {
		Schema schema = null;
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		try {
			schema = sf.newSchema(new File("./data/static_data/npc_walker/npc_walker.xsd"));
		}
		catch (SAXException e1) {
			log.error("Error while saving data: " + e1.getMessage(), e1.getCause());
			return;
		}

		File xml = new File("./data/static_data/npc_walker/walker_" + worldId + "_" + World.getInstance().getWorldMap(worldId).getName() + ".xml");
		JAXBContext jc;
		Marshaller marshaller;
		try {
			jc = JAXBContext.newInstance(WalkerData.class);
			marshaller = jc.createMarshaller();
			marshaller.setSchema(schema);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, xml);
		}
		catch (JAXBException e) {
			log.error("Error while saving data: " + e.getMessage(), e.getCause());
			return;
		}
	}
}
