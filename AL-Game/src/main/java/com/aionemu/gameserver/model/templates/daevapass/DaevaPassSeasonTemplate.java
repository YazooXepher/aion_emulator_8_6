/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.model.templates.daevapass;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * One Daeva Pass (battlepass) season. Mirrors the AtreianPassport template pattern.
 * XML: battlepass/battlepass_seasons.xml
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "season")
public class DaevaPassSeasonTemplate {

	@XmlAttribute(name = "id", required = true)
	protected int id;

	@XmlAttribute(name = "name")
	protected String name;

	@XmlAttribute(name = "type")
	protected String type;

	@XmlAttribute(name = "active")
	protected int active;

	@XmlAttribute(name = "max_lv")
	protected int maxLv;

	@XmlAttribute(name = "unlock_lv")
	protected int unlockLv;

	@XmlAttribute(name = "unlock_quna")
	protected int unlockQuna;

	@XmlAttribute(name = "exp_quna")
	protected int expQuna;

	@XmlAttribute(name = "pass_start")
	protected String passStart;

	@XmlAttribute(name = "mission_end")
	protected String missionEnd;

	@XmlAttribute(name = "reward_time")
	protected String rewardTime;

	@XmlAttribute(name = "pass_end")
	protected String passEnd;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isActive() {
		return active == 1;
	}

	public int getMaxLv() {
		return maxLv;
	}

	public int getUnlockLv() {
		return unlockLv;
	}

	public int getUnlockQuna() {
		return unlockQuna;
	}

	public int getExpQuna() {
		return expQuna;
	}

	public String getPassStart() {
		return passStart;
	}

	public String getMissionEnd() {
		return missionEnd;
	}

	public String getRewardTime() {
		return rewardTime;
	}

	public String getPassEnd() {
		return passEnd;
	}
}
