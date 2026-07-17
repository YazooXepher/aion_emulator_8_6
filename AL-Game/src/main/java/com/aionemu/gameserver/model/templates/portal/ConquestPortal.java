package com.aionemu.gameserver.model.templates.portal;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;

import java.util.ArrayList;

/**
 * @author CoolyT
 */
@XmlType(name = "Portal")
public class ConquestPortal {

	@XmlAttribute(name = "npc_id")
	public int npcId;

	@XmlElement(name = "destination")
	public List<ConquestPortalLoc> locs = new ArrayList<>();
}
