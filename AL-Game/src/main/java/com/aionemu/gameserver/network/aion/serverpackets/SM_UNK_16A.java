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

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.events.EventService;

/**
 * @author FrozenKiller
 */

public class SM_UNK_16A extends AionServerPacket {

		static final byte[] TEMPLATE = hex2Byte("0100000000000000000000000000015E0101050F0A01010A010200140000000000040100000000000101000000803F010500"
		+ "00000164000000010D0000803F00000000002439560B0100010000010050C300000000000000000000000000000000000000"
		+ "00000000000000B80B0000000900000090D0030000000000CDCCCC3DCDCCCC3D0000003FCDCCCC3D32000000000064000000"
		+ "030006090C");

	@Override
	protected void writeImpl(AionConnection con) { //155
		byte[] data = TEMPLATE.clone();
		data[27] = (byte) GSConfig.CHARACTER_REENTRY_TIME;
		data[28] = (byte) EventsConfig.ENABLE_DECOR; //100 = Kirchblüten TODO ....
		data[29] = (byte) EventService.getInstance().getEventType().getId(); // 18 Summer Splash V1 / 20 Summer Splash V2
		writeB(data);
	}

	private static byte[] hex2Byte(String str) {
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
}
