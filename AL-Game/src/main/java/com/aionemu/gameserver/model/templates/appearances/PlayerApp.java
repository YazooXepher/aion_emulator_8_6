package com.aionemu.gameserver.model.templates.appearances;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;

/**
 * @author CoolyT
 */
@XmlType(name = "player")
public class PlayerApp {

	@XmlAttribute(name = "name")
	public String name;

	@XmlAttribute(name = "gender")
	public String gender;

	@XmlAttribute(name = "race")
	public String race;

	@XmlAttribute(name = "level")
	public int level;

	@XmlAttribute(name = "class")
	public String playerClass;

	@XmlElement(name = "appearance")
	public PlayerAppearanceTemplate appearance = new PlayerAppearanceTemplate();

	@XmlElement(name = "items")
	public ArrayList<PlayerItem> items = new ArrayList<PlayerItem>();
}
