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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.templates.daevapass.DaevaPassSeasonTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Loads Daeva Pass (battlepass) seasons from battlepass/battlepass_seasons.xml.
 *
 * @author Claude
 */
@XmlRootElement(name = "battlepass_seasons")
@XmlAccessorType(XmlAccessType.FIELD)
public class DaevaPassSeasonData {

	private static final Logger log = LoggerFactory.getLogger(DaevaPassSeasonData.class);
	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@XmlElement(name = "season")
	private List<DaevaPassSeasonTemplate> slist;

	@XmlTransient
	private final TIntObjectHashMap<DaevaPassSeasonTemplate> seasons = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (DaevaPassSeasonTemplate s : slist) {
			seasons.put(s.getId(), s);
		}
	}

	public int size() {
		return seasons.size();
	}

	public DaevaPassSeasonTemplate getSeason(int id) {
		return seasons.get(id);
	}

	/**
	 * @return the season whose pass window (pass_start..pass_end) contains "now" and is active, or
	 *         null if there is no active season for the current date.
	 */
	public DaevaPassSeasonTemplate getActiveSeason() {
		long now = System.currentTimeMillis();
		DaevaPassSeasonTemplate best = null;
		for (DaevaPassSeasonTemplate s : seasons.valueCollection()) {
			if (!s.isActive()) {
				continue;
			}
			long start = parse(s.getPassStart());
			long end = parse(s.getPassEnd());
			if (start <= 0 || end <= 0) {
				continue;
			}
			if (now >= start && now <= end) {
				// pick the latest-starting matching season if several overlap
				if (best == null || start > parse(best.getPassStart())) {
					best = s;
				}
			}
		}
		return best;
	}

	private static long parse(String date) {
		if (date == null) {
			return -1;
		}
		try {
			synchronized (DF) {
				return DF.parse(date).getTime();
			}
		} catch (ParseException e) {
			log.warn("[DaevaPassSeasonData] bad date: " + date);
			return -1;
		}
	}
}
