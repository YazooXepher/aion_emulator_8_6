package com.aionemu.gameserver.model.templates.item;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "weapon_type")
@XmlEnum
public enum WeaponType {

	DAGGER_1H(new int[] { 66, 45 }, 1),
	MACE_1H(new int[] { 39, 46 }, 1),
	SWORD_1H(new int[] { 37, 44 }, 1),
	TOOLHOE_1H(new int[] {}, 1),
	GUN_1H(new int[] { 112, 117 }, 1),
	BOOK_2H(new int[] { 100, 107 }, 2),
	ORB_2H(new int[] { 111 }, 2), // 65 right skill, why 64 ? u_u
	POLEARM_2H(new int[] { 52 }, 2),
	STAFF_2H(new int[] { 89 }, 2),
	SWORD_2H(new int[] { 51 }, 2),
	TOOLPICK_2H(new int[] {}, 2),
	TOOLROD_2H(new int[] {}, 2),
	BOW(new int[] { 53 }, 2),
	CANNON_2H(new int[] { 113 }, 2),
	HARP_2H(new int[] { 114, 124 }, 2),
	GUN_2H(new int[] { 113 }, 2),
	SPRAY_2H(new int[] { 473 }, 2),
	KEYBLADE_2H(new int[] { 115 }, 2),
	KEYHAMMER_2H(new int[] {}, 2);

	private int[] requiredSkill;
	private int slots;

	private WeaponType(int[] requiredSkills, int slots) {
		this.requiredSkill = requiredSkills;
		this.slots = slots;
	}

	public int[] getRequiredSkills() {
		return requiredSkill;
	}

	public int getRequiredSlots() {
		return slots;
	}

	/**
	 * @return int
	 */
	public int getMask() {
		return 1 << this.ordinal();
	}
}
