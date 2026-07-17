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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SourceLocation;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sarynth, antness
 */
@XmlRootElement(name = "siege_locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeLocationData {

	@XmlElement(name = "siege_location")
	private List<SiegeLocationTemplate> siegeLocationTemplates;
	/**
	 * Map that contains skillId - SkillTemplate key-value pair
	 */
	@XmlTransient
	private ConcurrentHashMap<Integer, ArtifactLocation> artifactLocations = new ConcurrentHashMap<Integer, ArtifactLocation>();
	@XmlTransient
	private ConcurrentHashMap<Integer, FortressLocation> fortressLocations = new ConcurrentHashMap<Integer, FortressLocation>();
	@XmlTransient
	private ConcurrentHashMap<Integer, SourceLocation> sourceLocations = new ConcurrentHashMap<Integer, SourceLocation>();
	@XmlTransient
	private ConcurrentHashMap<Integer, SiegeLocation> siegeLocations = new ConcurrentHashMap<Integer, SiegeLocation>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		artifactLocations.clear();
		fortressLocations.clear();
		sourceLocations.clear();
		siegeLocations.clear();
		for (SiegeLocationTemplate template : siegeLocationTemplates) {
			switch (template.getType()) {
				case FORTRESS:
					FortressLocation fortress = new FortressLocation(template);
					fortressLocations.put(template.getId(), fortress);
					siegeLocations.put(template.getId(), fortress);
					artifactLocations.put(template.getId(), new ArtifactLocation(template));
					break;
				case ARTIFACT:
					ArtifactLocation artifact = new ArtifactLocation(template);
					artifactLocations.put(template.getId(), artifact);
					siegeLocations.put(template.getId(), artifact);
					break;
				case SOURCE:
					SourceLocation source = new SourceLocation(template);
					sourceLocations.put(template.getId(), source);
					siegeLocations.put(template.getId(), source);
					break;
				default:
					break;
			}
		}
	}

	public int size() {
		return siegeLocations.size();
	}

	public ConcurrentHashMap<Integer, ArtifactLocation> getArtifacts() {
		return artifactLocations;
	}

	public ConcurrentHashMap<Integer, FortressLocation> getFortress() {
		return fortressLocations;
	}

	public ConcurrentHashMap<Integer, SourceLocation> getSource() {
		return sourceLocations;
	}

	public ConcurrentHashMap<Integer, SiegeLocation> getSiegeLocations() {
		return siegeLocations;
	}
}
