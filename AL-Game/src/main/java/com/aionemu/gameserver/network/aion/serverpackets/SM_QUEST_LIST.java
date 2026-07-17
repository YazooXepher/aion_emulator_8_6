package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

import java.util.ArrayList;

public class SM_QUEST_LIST extends AionServerPacket {

	private ArrayList<QuestState> questState;

	public SM_QUEST_LIST(ArrayList<QuestState> questState) {
		this.questState = questState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeH(0x01); // unk
		writeH(-questState.size() & 0xFFFF);

		for (QuestState qs : questState) {
			writeD(qs.getQuestId());
			writeC(qs.getStatus().value());
			writeD(qs.getQuestVars().getQuestVars());
			writeC(qs.getCompleteCount());
		}
		// ArrayList.recycle() removed
		questState = null;
	}
}
