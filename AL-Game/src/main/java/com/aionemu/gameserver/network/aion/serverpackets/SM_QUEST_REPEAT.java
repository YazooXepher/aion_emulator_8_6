package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

import java.util.ArrayList;

/**
 * @author Ever' new 4.5 packet
 * @author FrozenKiller
 */
public class SM_QUEST_REPEAT extends AionServerPacket {

	private ArrayList<Integer> questList;

	public SM_QUEST_REPEAT(ArrayList<Integer> questList) {
		this.questList = questList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeH(questList.size());
		for (Integer questId : questList) {
			writeD(questId);
		}
		questList.clear();
	}
}
