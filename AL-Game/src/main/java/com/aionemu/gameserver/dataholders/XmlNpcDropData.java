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

package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.npcdrops.XmlCommonDropGroupRef;
import com.aionemu.gameserver.model.npcdrops.XmlDrop;
import com.aionemu.gameserver.model.npcdrops.XmlDropGroup;
import com.aionemu.gameserver.model.npcdrops.XmlNpcDrops;

/**
 * @author Falke_34
 */
@XmlRootElement(name = "npc_drops")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlNpcDropData {

	static Logger log = LoggerFactory.getLogger(XmlNpcDropData.class);
	@XmlElement(name = "npc_drop")
	private List<XmlNpcDrops> nds;
	@XmlElement(name = "group")
	private List<XmlDropGroup> commonGroupDefs;
	private HashMap<Integer, ArrayList<DropGroup>> drops;

	private static List<Drop> toDrops(XmlDropGroup dg, float chanceMultiplier) {
		List<Drop> dr = new ArrayList<Drop>();
		for (XmlDrop xd : dg.getDrop()) {
			float chance = xd.getChance() * chanceMultiplier;
			dr.add(new Drop(xd.getItemId(), xd.getMinAmount(), xd.getMaxAmount(), chance, xd.isNoReduction(), xd.isEachMember()));
		}
		return dr;
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		HashMap<String, XmlDropGroup> commonGroupsByName = new HashMap<String, XmlDropGroup>();
		if (this.commonGroupDefs != null) {
			for (XmlDropGroup g : this.commonGroupDefs) {
				commonGroupsByName.put(g.getGroupName(), g);
			}
		}
		this.drops = new HashMap<Integer, ArrayList<DropGroup>>();
		for (XmlNpcDrops nd : this.nds) {
			List<DropGroup> newDg = new ArrayList<DropGroup>();
			for (XmlDropGroup dg : nd.getDropGroup()) {
				DropGroup datDg = new DropGroup(toDrops(dg, 1f), dg.getRace(), dg.isUseCategory(), dg.getGroupName());
				newDg.add(datDg);
			}
			for (XmlCommonDropGroupRef ref : nd.getCommonDropGroup()) {
				XmlDropGroup commonGroup = commonGroupsByName.get(ref.getName());
				if (commonGroup == null) {
					log.warn("Unknown common_drop_group '" + ref.getName() + "' referenced by npc " + nd.getNpcId());
					continue;
				}
				float chanceMultiplier = ref.getCommonDropAdjustment() / 100f;
				DropGroup datDg = new DropGroup(toDrops(commonGroup, chanceMultiplier), commonGroup.getRace(), commonGroup.isUseCategory(), ref.getName());
				newDg.add(datDg);
			}
			if (this.drops.containsKey(Integer.valueOf(nd.getNpcId()))) {
				log.warn("Drop NPC duplicate List ID: " + nd.getNpcId());
			}
			else {
				this.drops.put(nd.getNpcId(), new ArrayList<DropGroup>());
			}
			this.drops.get(nd.getNpcId()).addAll(newDg);
		}
	}

	public int size() {
		return this.nds.size();
	}

	public HashMap<Integer, ArrayList<DropGroup>> getDrops() {
		return this.drops;
	}

	public void clear() {
		this.drops.clear();
		this.drops = null;
	}
}
