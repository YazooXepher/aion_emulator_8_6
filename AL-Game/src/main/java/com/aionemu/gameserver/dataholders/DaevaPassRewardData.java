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
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.daevapass.DaevaPassRewardTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Loads Daeva Pass (battlepass) reward tiers from battlepass/battlepass_rewards.xml.
 *
 * @author Claude
 */
@XmlRootElement(name = "battlepass_rewards")
@XmlAccessorType(XmlAccessType.FIELD)
public class DaevaPassRewardData {

	@XmlElement(name = "reward")
	private List<DaevaPassRewardTemplate> rlist;

	/** seasonId -> (level -> reward tier) */
	@XmlTransient
	private final TIntObjectHashMap<TIntObjectHashMap<DaevaPassRewardTemplate>> bySeason = new TIntObjectHashMap<>();

	@XmlTransient
	private int total = 0;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (DaevaPassRewardTemplate r : rlist) {
			TIntObjectHashMap<DaevaPassRewardTemplate> levels = bySeason.get(r.getSeasonId());
			if (levels == null) {
				levels = new TIntObjectHashMap<>();
				bySeason.put(r.getSeasonId(), levels);
			}
			levels.put(r.getLevel(), r);
			total++;
		}
	}

	public int size() {
		return total;
	}

	/** All reward tiers for a season, ordered by level. */
	public List<DaevaPassRewardTemplate> getSeasonRewards(int seasonId) {
		List<DaevaPassRewardTemplate> out = new ArrayList<>();
		TIntObjectHashMap<DaevaPassRewardTemplate> levels = bySeason.get(seasonId);
		if (levels != null) {
			out.addAll(levels.valueCollection());
			out.sort((a, b) -> Integer.compare(a.getLevel(), b.getLevel()));
		}
		return out;
	}

	public DaevaPassRewardTemplate getReward(int seasonId, int level) {
		TIntObjectHashMap<DaevaPassRewardTemplate> levels = bySeason.get(seasonId);
		return levels == null ? null : levels.get(level);
	}
}
