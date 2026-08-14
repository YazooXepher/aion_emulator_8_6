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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

/**
 * One Daeva Pass reward tier (a season level). Each tier grants a basic reward (all players) and an
 * unlock reward (premium pass), split per faction (light = Elyos, dark = Asmodian).
 * XML: battlepass/battlepass_rewards.xml
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reward")
public class DaevaPassRewardTemplate {

	@XmlAttribute(name = "id", required = true)
	protected int id;

	@XmlAttribute(name = "season_id", required = true)
	protected int seasonId;

	@XmlAttribute(name = "level", required = true)
	protected int level;

	@XmlAttribute(name = "point_reward")
	protected int pointReward;

	@XmlElementWrapper(name = "light_basic")
	@XmlElement(name = "item")
	protected List<DaevaPassItem> lightBasic;

	@XmlElementWrapper(name = "light_unlock")
	@XmlElement(name = "item")
	protected List<DaevaPassItem> lightUnlock;

	@XmlElementWrapper(name = "dark_basic")
	@XmlElement(name = "item")
	protected List<DaevaPassItem> darkBasic;

	@XmlElementWrapper(name = "dark_unlock")
	@XmlElement(name = "item")
	protected List<DaevaPassItem> darkUnlock;

	public int getId() {
		return id;
	}

	public int getSeasonId() {
		return seasonId;
	}

	public int getLevel() {
		return level;
	}

	public int getPointReward() {
		return pointReward;
	}

	public List<DaevaPassItem> getLightBasic() {
		return lightBasic == null ? new ArrayList<>() : lightBasic;
	}

	public List<DaevaPassItem> getLightUnlock() {
		return lightUnlock == null ? new ArrayList<>() : lightUnlock;
	}

	public List<DaevaPassItem> getDarkBasic() {
		return darkBasic == null ? new ArrayList<>() : darkBasic;
	}

	public List<DaevaPassItem> getDarkUnlock() {
		return darkUnlock == null ? new ArrayList<>() : darkUnlock;
	}
}
