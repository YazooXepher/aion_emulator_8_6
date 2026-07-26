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
 * @author LokiReborn
 */
public class SM_WINDSTREAM_ANNOUNCE extends AionServerPacket {

	private int bidirectional;
	private int mapId;
	private int streamId;
	private int state;

	// ride-state variant: periodic position sync while gliding along an active windstream.
	// Real 8.6 captures show this same opcode (0xA4) also carrying a bigger payload during an
	// active ride (current + target position), on top of the original 13-byte "announce" shape
	// used when entering a zone. Field-for-field meaning beyond the position floats is unconfirmed.
	private int objectId;
	private float curX, curY, curZ;
	private float targetX, targetY, targetZ;
	private boolean rideVariant;

	public SM_WINDSTREAM_ANNOUNCE(int bidirectional, int mapId, int streamId, int state) {
		this.bidirectional = bidirectional;
		this.mapId = mapId;
		this.streamId = streamId;
		this.state = state;
	}

	public SM_WINDSTREAM_ANNOUNCE(int objectId, float x, float y, float z) {
		this.objectId = objectId;
		this.curX = x;
		this.curY = y;
		this.curZ = z;
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
		this.rideVariant = true;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (rideVariant) {
			writeD(objectId);
			writeF(curX);
			writeF(curY);
			writeF(curZ);
			writeF(targetX);
			writeF(targetY);
			writeF(targetZ);
		}
		else {
			writeD(bidirectional);
			writeD(mapId);
			writeD(streamId);
			writeC(state);
		}
	}
}
