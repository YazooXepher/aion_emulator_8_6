package com.aionemu.gameserver.dataholders;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.portal.ConquestPortal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CoolyT
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "conquest_portals")
public class ConquestPortalData {

	@XmlElement(name = "portal")
	public List<ConquestPortal> portals = new ArrayList<>();

	public int size() {
		return portals.size();
	}

	public ConquestPortal getPortalbyNpcId(int id) {
		for (ConquestPortal portal : portals) {
			if (portal.npcId == id)
				return portal;

		}
		return null;
	}

	public List<ConquestPortal> getPortals() {
		return portals;
	}
}
