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
package quest.haramel;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * quest_data.xml declares this quest with only a quest_kill (npc 653213, Overseer Nukiti) and a
 * start_conditions/finished on quest 71102 - no giver NPC at all. No Java handler existed for it,
 * so the Haramel chain dead-ended after 71102 and Overseer Nukiti could never be tracked.
 *
 * @author QuestGenerator by Mariella
 */
public class _71103AKoboldWhoProtectsHamerun extends QuestHandler {

	private final static int questId = 71103;

	public _71103AKoboldWhoProtectsHamerun() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(653213).addOnKillEvent(questId); // Overseer Nukiti
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		QuestState prev = player.getQuestStateList().getQuestState(71102);
		if (qs == null && prev != null && prev.getStatus() == QuestStatus.COMPLETE) {
			return QuestService.startQuest(env);
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && env.getTargetId() == 653213) {
			qs.setQuestVar(1);
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1000, true);
	}
}
