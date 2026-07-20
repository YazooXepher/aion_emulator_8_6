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
package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Synchronizes Abyss landing location score categories (quest, siege, artifact, base,
 * facility, monument, commander) to the client.
 *
 * TODO: no AbyssLandingService/LandingLocation model exists yet - this packet currently
 * takes the per-category score arrays directly. Wire it up to a real service once the
 * landing-location subsystem is implemented.
 */
public class SM_ABYSS_LANDING extends AionServerPacket {

	private final int[] questPoints;
	private final int[] siegePoints;
	private final int[] artifactPoints;
	private final int[] basePoints;
	private final int[] facilityPoints;
	private final int[] monumentPoints;
	private final int[] commanderPoints;

	public SM_ABYSS_LANDING(int[] questPoints, int[] siegePoints, int[] artifactPoints, int[] basePoints,
			int[] facilityPoints, int[] monumentPoints, int[] commanderPoints) {
		this.questPoints = questPoints;
		this.siegePoints = siegePoints;
		this.artifactPoints = artifactPoints;
		this.basePoints = basePoints;
		this.facilityPoints = facilityPoints;
		this.monumentPoints = monumentPoints;
		this.commanderPoints = commanderPoints;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		for (int i = 0; i < questPoints.length; i++) {
			writeD(questPoints[i]); // Quest Completion.
			writeD(siegePoints[i]); // Fortress Occupation.
			writeD(artifactPoints[i]); // Artifact Occupation.
			writeD(basePoints[i]); // Base Occupation.
			writeD(facilityPoints[i]); // Facility Control.
			writeD(monumentPoints[i]); // Monument Control.
			writeD(commanderPoints[i]); // Commander Defense.
		}
	}
}
