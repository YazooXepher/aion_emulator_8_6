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
 * Notifies nearby clients that a player should display the VIP icon.
 */
public class SM_NOTIFY_VIP_ICON extends AionServerPacket {

	private final int objectId;

	public SM_NOTIFY_VIP_ICON(Player player) {
		this(player.getObjectId());
	}

	public SM_NOTIFY_VIP_ICON(int objectId) {
		this.objectId = objectId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeH(0);
	}
}
