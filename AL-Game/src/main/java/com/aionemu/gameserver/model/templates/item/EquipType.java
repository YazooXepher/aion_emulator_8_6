package com.aionemu.gameserver.model.templates.item;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "equipType")
@XmlEnum
public enum EquipType {

	ARMOR,
	WEAPON,
	STIGMA,
	ESTIMA,
    ACCESSORY,
    GRIND,
	NONE;

	public String value() {
		return name();
	}

	public static EquipType fromValue(String v) {
		return valueOf(v);
	}
}
