package com.aionemu.gameserver.model.templates.stats;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This class is only a container for Stats. Created on: 04.08.2009 14:59:10
 *
 * @author Aquanox, Dr2co
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "stats_template")
public abstract class StatsTemplate {

	@XmlAttribute(name = "maxHp")
	private int maxHp;
	@XmlAttribute(name = "maxMp")
	private int maxMp;
	@XmlAttribute(name = "evasion")
	private int evasion;
	@XmlAttribute(name = "block")
	private int block;
	@XmlAttribute(name = "parry")
	private int parry;
	@XmlAttribute(name = "main_hand_attack")
	private int mainHandAttack;
	@XmlAttribute(name = "main_hand_accuracy")
	private int mainHandAccuracy;
	@XmlAttribute(name = "main_hand_crit_rate")
	private int mainHandCritRate;
	@XmlAttribute(name = "magic_accuracy")
	private int magicAccuracy;
	@XmlAttribute(name = "crit_spell")
	private int critSpell;
	@XmlAttribute(name = "strike_resist")
	private int strikeResist;
	@XmlAttribute(name = "spell_resist")
	private int spellResist;
	@XmlElement
	protected CreatureSpeeds speeds;

	/* ======================================= */
	public int getMaxHp() {
		return maxHp;
	}

	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}

	public int getMaxMp() {
		return maxMp;
	}

	public void setMaxMp(int maxMp) {
		this.maxMp = maxMp;
	}

	/* ======================================= */
	public float getWalkSpeed() {
		return speeds == null ? 0 : speeds.getWalkSpeed();
	}

	public float getRunSpeed() {
		return speeds == null ? 0 : speeds.getRunSpeed();
	}

	public float getGroupWalkSpeed() {
		return getWalkSpeed();
	}

	public float getRunSpeedFight() {
		return getRunSpeed();
	}

	public float getGroupRunSpeedFight() {
		return getRunSpeed();
	}

	public float getFlySpeed() {
		return speeds == null ? 0 : speeds.getFlySpeed();
	}

	public void setWalkSpeed(CreatureSpeeds walkSpeed) {
		this.speeds = walkSpeed;
	}

	public void setRunSpeed(CreatureSpeeds runSpeed) {
		this.speeds = runSpeed;
	}

	/* ======================================= */
	public int getEvasion() {
		return evasion;
	}

	public void setEvasion(int evasion) {
		this.evasion = evasion;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public int getParry() {
		return parry;
	}

	public void setParry(int parry) {
		this.parry = parry;
	}

	public int getStrikeResist() {
		return strikeResist;
	}

	public void setStrikeResist(int resist) {
		this.strikeResist = resist;
	}

	public int getSpellResist() {
		return spellResist;
	}

	public void setSpellResist(int resist) {
		this.spellResist = resist;
	}

	/* ======================================= */
	public int getMainHandAttack() {
		return mainHandAttack;
	}

	public int getMainHandAccuracy() {
		return mainHandAccuracy;
	}

	public int getMainHandCritRate() {
		return mainHandCritRate;
	}

	/* ======================================= */
	public int getMagicAccuracy() {
		return magicAccuracy;
	}

	public int getMCritical() {
		return critSpell;
	}
}
