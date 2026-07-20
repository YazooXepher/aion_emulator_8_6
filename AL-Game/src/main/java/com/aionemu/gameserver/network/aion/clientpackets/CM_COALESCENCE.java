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

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * Requests an equipment coalescence (fusion).
 *
 * TODO: no coalescence service exists yet - wire this up once that subsystem
 * (see SM_COALESCENCE_RESULT / SM_COALESCENCE_STARTUP) is implemented.
 */
public class CM_COALESCENCE extends AionClientPacket {

	@SuppressWarnings("unused")
	private int mainItemObjId;
	@SuppressWarnings("unused")
	private List<Integer> materialItemObjIds;

	public CM_COALESCENCE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		materialItemObjIds = new ArrayList<>();
		mainItemObjId = readD();
		int materialCount = readH();
		for (int i = 0; i < materialCount; i++) {
			materialItemObjIds.add(readD());
		}
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
