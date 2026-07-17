package com.aionemu.gameserver.model.templates.item;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 * @reworked Kill3r
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Stigma")
public class Stigma {

	@XmlElement(name = "require_skill")
	protected List<RequireSkill> requireSkill;
	@XmlAttribute
	protected List<String> skill;
	@XmlAttribute
	protected int kinah;

	/**
	 * @return list
	 */
	public List<StigmaSkill> getSkills() {
		List<StigmaSkill> list = new ArrayList<StigmaSkill>();
		for (String st : skill) {
			String[] array = st.split(":");
			list.add(new StigmaSkill(Integer.parseInt(array[0]), Integer.parseInt(array[1])));
		}

		return list;
	}

	public List<Integer> getSkillIdOnly() {
		List<Integer> ids = new ArrayList<Integer>();
		List<String> skill = this.skill;
		if (skill.size() != 1) { // Dual Skills like Exhausting Wave
			String[] tempArray = new String[0];
			for (String parts : skill) { // loops each of the 1:534 and 1:4342
				tempArray = parts.split(":");
				ids.add(Integer.parseInt(tempArray[1]));
			}
			return ids;
		}

		// Single 1 Skill
		for (String st : this.skill) {
			String[] array = st.split(":");
			ids.add(Integer.parseInt(array[1]));
		}
		return ids;
	}

	/**
	 * @return the kinah //4.8
	 */
	public int getKinah() {
		return kinah;
	}

	public List<RequireSkill> getRequireSkill() {
		if (requireSkill == null) {
			requireSkill = new ArrayList<RequireSkill>();
		}
		return this.requireSkill;
	}

	public static class StigmaSkill {

		private int skillId;
		private int skillLvl;

		public StigmaSkill(int skillLvl, int skillId) {
			this.skillId = skillId;
			this.skillLvl = skillLvl;
		}

		public int getSkillLvl() {
			return this.skillLvl;
		}

		public int getSkillId() {
			return this.skillId;
		}
	}
}
