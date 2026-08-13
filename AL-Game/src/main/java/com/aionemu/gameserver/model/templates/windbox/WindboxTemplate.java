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
package com.aionemu.gameserver.model.templates.windbox;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * A "windbox" is a world-placed wind vortex volume (2D polygon footprint + altitude band) that
 * flying players can ride to receive a temporary movement speed buff. Purely a server-side gameplay
 * check: the vortex visual itself is rendered client-side from the client's own world geometry.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Windbox")
public class WindboxTemplate {

	@XmlAttribute(required = true)
	protected int id;
	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected float force;
	@XmlAttribute
	protected float maxupspeed;
	@XmlAttribute
	protected float upheight;
	@XmlAttribute(name = "start_time")
	protected int startTime;
	@XmlAttribute(name = "end_time")
	protected int endTime;
	@XmlAttribute(name = "life_time")
	protected int lifeTime;
	@XmlAttribute(name = "always_enabled")
	protected boolean alwaysEnabled;
	@XmlAttribute(required = true)
	protected float bottom;
	@XmlAttribute(required = true)
	protected float top;
	@XmlElement(name = "point")
	protected List<WindboxPoint> points;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public float getForce() {
		return force;
	}

	public float getMaxUpSpeed() {
		return maxupspeed;
	}

	public float getUpHeight() {
		return upheight;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public int getLifeTime() {
		return lifeTime;
	}

	public boolean isAlwaysEnabled() {
		return alwaysEnabled;
	}

	public float getBottom() {
		return bottom;
	}

	public float getTop() {
		return top;
	}

	public List<WindboxPoint> getPoints() {
		return points;
	}

	/**
	 * Whether this windbox gates on the time of day at all. Used to avoid touching the wall clock
	 * on the movement hot path for the common always-active case.
	 */
	public boolean hasTimeWindow() {
		return !alwaysEnabled && lifeTime <= 0 && startTime != endTime;
	}

	/**
	 * Whether this windbox is currently active. Windboxes with a real start/end hour window are
	 * only active during that server-time-of-day range; everything else (always_enabled, or
	 * life_time-cycled boxes with no time window) is treated as always active.
	 */
	public boolean isActiveNow(int currentHour) {
		if (alwaysEnabled || lifeTime > 0) {
			return true;
		}
		if (startTime == endTime) {
			return true;
		}
		if (startTime < endTime) {
			return currentHour >= startTime && currentHour < endTime;
		}
		return currentHour >= startTime || currentHour < endTime;
	}

	/**
	 * 2D point-in-polygon test (ray casting) against this windbox's footprint.
	 */
	public boolean contains2D(float x, float y) {
		boolean inside = false;
		int n = points.size();
		for (int i = 0, j = n - 1; i < n; j = i++) {
			WindboxPoint pi = points.get(i);
			WindboxPoint pj = points.get(j);
			if ((pi.getY() > y) != (pj.getY() > y)
				&& x < (pj.getX() - pi.getX()) * (y - pi.getY()) / (pj.getY() - pi.getY()) + pi.getX()) {
				inside = !inside;
			}
		}
		return inside;
	}

	public boolean containsAltitude(float z) {
		return z >= bottom && z <= top;
	}
}
