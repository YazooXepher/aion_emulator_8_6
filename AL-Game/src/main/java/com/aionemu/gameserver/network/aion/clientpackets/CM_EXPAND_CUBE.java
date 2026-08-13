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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger diagLog = LoggerFactory.getLogger(CM_EXPAND_CUBE.class);

	int action;

	public CM_EXPAND_CUBE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		action = readC();
	}

	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();

		switch (action) {
			case 0: // Kinah
				// starting inventory = 27 slots (3 rows)
				if (activePlayer.getCubeExpands() < 10) { // max 9 rows open for kinah 117 slots
					int kinahCost;
					switch (activePlayer.getCubeExpands()) {
						case 0 -> kinahCost = 1000; // 27 slots to 36
						case 1 -> kinahCost = 10000; // 36 slots to 45
						case 2 -> kinahCost = 50000; // 45 slots to 54
						case 3 -> kinahCost = 150000; // 54 slots to 63
						case 4 -> kinahCost = 300000; // 63 slots to 72
						case 5 -> kinahCost = 3000000; // 72 slots to 81
						case 6 -> kinahCost = 6000000; // 81 slots to 90
						case 7 -> kinahCost = 12000000; // 90 slots to 99
						case 8 -> kinahCost = 24000000; // 99 slots to 108
						case 9 -> kinahCost = 48000000; // 108 slots to 117
						default -> kinahCost = -1;
					}
					diagLog.info("[DIAG CM_EXPAND_CUBE] player=" + activePlayer.getName() + " cubeExpands=" + activePlayer.getCubeExpands()
						+ " kinahCost=" + kinahCost + " currentKinah=" + activePlayer.getInventory().getKinah());
					if (kinahCost < 0) {
						PacketSendUtility.sendMessage(activePlayer, "You need cube expansion coin to expand your cube now");
					}
					else if (activePlayer.getInventory().tryDecreaseKinah(kinahCost)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else {
						PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_MONEY);
					}
				}
				else {
					diagLog.info("[DIAG CM_EXPAND_CUBE] player=" + activePlayer.getName() + " cubeExpands=" + activePlayer.getCubeExpands()
						+ " action=0 (kinah) received but cubeExpands >= 10, needs cube expansion coin instead");
					PacketSendUtility.sendMessage(activePlayer, "You need cube expansion coin to expand your cube now");
				}
				break;
			case 1: // Cube Expansion Coin
				diagLog.info("[DIAG CM_EXPAND_CUBE] player=" + activePlayer.getName() + " cubeExpands=" + activePlayer.getCubeExpands()
					+ " action=1 (coin) received currentKinah=" + activePlayer.getInventory().getKinah());
				if (activePlayer.getCubeExpands() < 11) { // if less than 10 rows open / 117 slots
					if (activePlayer.getInventory().decreaseByItemId(186000444, 5)) { // 117 slots to 126 [EU Always 5-Keys Displayed ]
						CubeExpandService.expand(activePlayer, true); // row goes to 11
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000419, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000440, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000445, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else {
						PacketSendUtility.sendMessage(activePlayer, "You need cube expansion coin to expand your cube now");
					}
				}
				else if (activePlayer.getCubeExpands() == 11) { // 11 rows open
					if (activePlayer.getInventory().decreaseByItemId(186000444, 5)) { // 126 slots to 135
						CubeExpandService.expand(activePlayer, true); // row goes to 12
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000419, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000440, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000445, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else {
						PacketSendUtility.sendMessage(activePlayer, "You need cube expansion coin to expand your cube now");
					}
				}
				else if (activePlayer.getCubeExpands() == 12) { // 12 rows open
					if (activePlayer.getInventory().decreaseByItemId(186000444, 5)) { // 135 slots to 144
						CubeExpandService.expand(activePlayer, true); // row goes to 13
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000419, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000440, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000445, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else {
						PacketSendUtility.sendMessage(activePlayer, "You need cube expansion coin to expand your cube now");
					}
				}
				else if (activePlayer.getCubeExpands() == 13) { // 13 rows open
					if (activePlayer.getInventory().decreaseByItemId(186000444, 5)) { // 144 slots to 153
						CubeExpandService.expand(activePlayer, true); // row goes to 14
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000419, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000440, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000445, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else {
						PacketSendUtility.sendMessage(activePlayer, "You need cube expansion coin to expand your cube now");
					}
				}
				else if (activePlayer.getCubeExpands() == 14) { // 14 rows open
					if (activePlayer.getInventory().decreaseByItemId(186000444, 5)) { // 153 slots to 162
						CubeExpandService.expand(activePlayer, true); // row goes to 15 (max)
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000419, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000440, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else if (activePlayer.getInventory().decreaseByItemId(186000445, 5)) {
						CubeExpandService.expand(activePlayer, true);
					}
					else {
						PacketSendUtility.sendMessage(activePlayer, "You need cube expansion coin to expand your cube now");
					}
				}
				else {
					PacketSendUtility.sendMessage(activePlayer, "No more expansion available");
				}
				break;
		}
	}
}
