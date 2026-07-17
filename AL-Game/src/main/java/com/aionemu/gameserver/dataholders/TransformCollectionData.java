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

import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.transform_book.TransformCollectionTemplate;

import java.util.concurrent.ConcurrentHashMap;

@XmlRootElement(name = "transform_collection_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransformCollectionData {

	@XmlElement(name = "transform_collection_template")
	private List<TransformCollectionTemplate> tlist;
	@XmlTransient
	private Map<Integer, TransformCollectionTemplate> transCollectionData = new ConcurrentHashMap<Integer, TransformCollectionTemplate>();

	void afterUnmarshal(Unmarshaller paramUnmarshaller, Object paramObject) {
		for (TransformCollectionTemplate book : tlist) {
			transCollectionData.put(book.getId(), book);
		}
	}

	public int size() {
		return transCollectionData.size();
	}

	public Map<Integer, TransformCollectionTemplate> getAllCollection() {
		return transCollectionData;
	}

	public TransformCollectionTemplate getTransformCollectionById(int id) {
		return transCollectionData.get(id);
	}
}
