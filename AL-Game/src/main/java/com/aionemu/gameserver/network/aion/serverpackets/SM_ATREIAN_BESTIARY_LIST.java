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
 * Synchronizes the player's full Atreian Bestiary list.
 *
 * TODO: no Player-side bestiary model exists yet - this packet takes the entry data as
 * parallel arrays directly. Wire it up to a real player bestiary once that subsystem
 * (DAO + static data + service) is implemented.
 */
public class SM_ATREIAN_BESTIARY_LIST extends AionServerPacket {

	private final int[] ids;
	private final int[] killCounts;
	private final int[] rewardLevels;
	private final int[] levels;

	public SM_ATREIAN_BESTIARY_LIST(int[] ids, int[] killCounts, int[] rewardLevels, int[] levels) {
		this.ids = ids;
		this.killCounts = killCounts;
		this.rewardLevels = rewardLevels;
		this.levels = levels;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(ids.length);
		for (int i = 0; i < ids.length; i++) {
			writeD(ids[i]); // id
			writeD(killCounts[i]); // current kill
			writeC(rewardLevels[i]); // claim reward level
			writeC(levels[i]); // current level
		}
	}
}
