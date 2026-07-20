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
 * Synchronizes the player's Abyss Favor percentage value to the client.
 *
 * TODO: no AbyssFavor field exists on PlayerCommonData yet - this packet takes the raw
 * value directly (50000 = 5%, 100000 = 10%). Wire it up once that field is added.
 */
public class SM_ABYSS_FAVOR extends AionServerPacket {

	private long abyssFavor;

	public SM_ABYSS_FAVOR(long abyssFavor) {
		this.abyssFavor = abyssFavor;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeQ(abyssFavor);
	}
}
