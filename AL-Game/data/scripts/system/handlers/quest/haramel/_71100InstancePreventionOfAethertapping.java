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

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author QuestGenerator by Mariella
 */
public class _71100InstancePreventionOfAethertapping extends QuestHandler {

	private final static int questId = 71100;
	private final static int[] mobs = { 700950 };

	public _71100InstancePreventionOfAethertapping() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(700953).addOnTalkEvent(questId); // Reprocessed Odella

		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId); // Aether Cart
		}
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1000, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null) {
			return false;
		}

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700953: {
					switch (dialog) {
						case USE_OBJECT: {
							if (qs.getQuestVarById(0) == 1) {
								qs.setQuestVar(2);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
							}
							return false;
						}
						default:
							break;
					}
					break;
				}
				default:
					break;
			}
		}

		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		// quest_data.xml declares <quest_kill npc_ids="700950" seq="0"/> (destroy the Aether
		// Cart) - the previous USE_OBJECT dialog case for this NPC didn't match the official
		// objective and left the real kill handler commented out, so the cart could never
		// actually be "destroyed" to progress the quest
		if (qs != null && qs.getStatus() == QuestStatus.START && env.getTargetId() == 700950) {
			if (qs.getQuestVarById(0) == 0) {
				qs.setQuestVar(1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
