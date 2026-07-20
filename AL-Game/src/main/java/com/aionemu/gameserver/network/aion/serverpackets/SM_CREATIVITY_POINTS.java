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

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Creativity Points (CP) sync packet: total points, level cap and slot allocations.
 *
 * TODO: no Creativity Panel subsystem exists yet (no PlayerCP model/DAO/service). This
 * packet takes the slot/point allocation as parallel arrays directly. Wire it up to a
 * real player CP model once that subsystem is implemented.
 */
public class SM_CREATIVITY_POINTS extends AionServerPacket {

	private int totalPoint;
	private int size;
	private boolean onLogin;
	private int[] slots = new int[0];
	private int[] points = new int[0];

	public SM_CREATIVITY_POINTS(int totalPoint, int size) {
		this.totalPoint = totalPoint;
		this.size = size;
	}

	public SM_CREATIVITY_POINTS(int totalPoint, int size, boolean onLogin, int[] slots, int[] points) {
		this.totalPoint = totalPoint;
		this.size = size;
		this.onLogin = onLogin;
		this.slots = slots;
		this.points = points;
	}

	private int cpByLevel(int level) {
		switch (level) {
			case 66:
				return 1 + 1;
			case 67:
				return 10 + 10;
			case 68:
				return 19 + 15;
			case 69:
				return 30 + 20;
			case 70:
				return 42 + 25;
			case 71:
				return 55 + 30;
			case 72:
				return 70 + 35;
			case 73:
				return 87 + 40;
			case 74:
				return 107 + 45;
			case 75:
				return 130 + 155;
		}
		return 285;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player activePlayer = con.getActivePlayer();

		writeD(totalPoint); // Creativity Points Total
		writeD(cpByLevel(activePlayer.getLevel()));
		writeH(size);
		if (onLogin) {
			for (int i = 0; i < slots.length; i++) {
				writeD(slots[i]);
				writeH(points[i]);
			}
		}
	}
}
