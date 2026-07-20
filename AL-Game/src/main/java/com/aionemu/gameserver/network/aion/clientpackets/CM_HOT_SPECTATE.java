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
package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * Hot spectate related client packet.
 *
 * TODO: purpose/business logic not yet identified - currently only reads and logs its
 * fields.
 */
public class CM_HOT_SPECTATE extends AionClientPacket {

	@SuppressWarnings("unused")
	private int unkC1;
	@SuppressWarnings("unused")
	private int unkC2;
	@SuppressWarnings("unused")
	private int unkD;

	public CM_HOT_SPECTATE(int opcode, AionConnection.State state, AionConnection.State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		unkC1 = readC();
		unkC2 = readC();
		unkD = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		// TODO
	}
}
