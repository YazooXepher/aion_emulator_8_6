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

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.windbox.WindboxMap;
import com.aionemu.gameserver.model.templates.windbox.WindboxTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "windboxes")
public class WindboxData {

	@XmlElement(name = "windbox_map")
	private List<WindboxMap> maps;
	private TIntObjectHashMap<List<WindboxTemplate>> windboxesByMap;
	private int size;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		windboxesByMap = new TIntObjectHashMap<>();
		for (WindboxMap map : maps) {
			windboxesByMap.put(map.getMapId(), map.getWindboxes());
			size += map.getWindboxes().size();
		}
		maps = null;
	}

	public List<WindboxTemplate> getWindboxesByMapId(int mapId) {
		List<WindboxTemplate> list = windboxesByMap.get(mapId);
		return list == null ? Collections.emptyList() : list;
	}

	public int size() {
		return size;
	}
}
