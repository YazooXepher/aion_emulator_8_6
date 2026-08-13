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
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.CubeExpandService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Eloann
 */
public class CM_EXPAND_CUBE extends AionClientPacket {

	/** First row (starting cube = 3 rows / 27 slots) up to the 9th kinah-bought row, indexed by current cubeExpands. */
	private static final int[] KINAH_COST_BY_EXPAND = { 1000, 10000, 50000, 150000, 300000, 3000000, 6000000, 12000000, 24000000, 48000000 };

	/** Any of these "Cube Expansion Key" item ids (5 each) unlocks a coin-bought row. */
	private static final int[] EXPANSION_COIN_ITEM_IDS = { 186000444, 186000419, 186000440, 186000445 };
	private static final int EXPANSION_COIN_COUNT = 5;

	/** Kinah rows go up to expand 10 (117 slots); coins take it to the 15-row cap. */
	private static final int MAX_KINAH_EXPAND = KINAH_COST_BY_EXPAND.length; // 10
	private static final int MAX_EXPAND = 15;

	private int action;

	public CM_EXPAND_CUBE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		action = readC();
	}

	@Override
	protected void runImpl() {
		final Player player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		int expands = player.getCubeExpands();

		switch (action) {
			case 0: // Kinah
				if (expands >= MAX_KINAH_EXPAND) {
					PacketSendUtility.sendMessage(player, "You need a cube expansion coin to expand your cube further.");
					return;
				}
				int cost = KINAH_COST_BY_EXPAND[expands];
				if (player.getInventory().tryDecreaseKinah(cost)) {
					CubeExpandService.expand(player, true);
				} else {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_MONEY);
				}
				break;
			case 1: // Cube Expansion Coin
				if (expands >= MAX_EXPAND) {
					PacketSendUtility.sendMessage(player, "No more expansion available.");
					return;
				}
				if (consumeExpansionCoin(player)) {
					CubeExpandService.expand(player, true);
				} else {
					PacketSendUtility.sendMessage(player, "You need a cube expansion coin to expand your cube now.");
				}
				break;
		}
	}

	private static boolean consumeExpansionCoin(Player player) {
		for (int itemId : EXPANSION_COIN_ITEM_IDS) {
			if (player.getInventory().decreaseByItemId(itemId, EXPANSION_COIN_COUNT)) {
				return true;
			}
		}
		return false;
	}
}
