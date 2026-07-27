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
public class _71102InstanceSmugglerMoofrenerksStatement extends QuestHandler {

	private final static int questId = 71102;

	public _71102InstanceSmugglerMoofrenerksStatement() {
		super(questId);
	}

	@Override
	public void register() {
		// quest is literally titled "Moofrenerk's Statement" and the client's own dialogue
		// script (quest_q61102.html, the Elyos twin) has Moofrenerk offer it, then send the
		// player to Gestanerk for the actual report-in - matching 71101's own two-NPC pattern
		// (start with one Shugo, finish with another). This handler had both ends wired to
		// Gestanerk (799524) only, so Moofrenerk (799523) - who the player actually talks to
		// first - had nothing registered and every QUEST_SELECT click on him fell through.
		qe.registerQuestNpc(799523).addOnQuestStart(questId); // Moofrenerk
		qe.registerQuestNpc(799523).addOnTalkEvent(questId); // Moofrenerk
		qe.registerQuestNpc(799524).addOnTalkEvent(questId); // Gestanerk
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 71101, false);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE ) {
	  		if (targetId == 799523) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE: {
						return sendQuestStartDialog(env);
					}
					case QUEST_REFUSE_SIMPLE: {
						return closeDialogWindow(env);
					}
					default:
						break;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799524: {
					switch (dialog) {
						case QUEST_SELECT: {
							// matches the proven Elyos twin 61102: this quest has no item-check
							// leg, it goes straight from the report-in dialog to the reward
							// prompt. The old value (1011, copied from 71101's unrelated
							// second-visit page) doesn't exist in this quest's own
							// Quest_Q71102.html and produced a client-side "load fail".
							return sendQuestDialog(env, 10002);
						}
						case SELECT_QUEST_REWARD: {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 5);
						}
						default:
							break;
					}
					break;
				}
				default:
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799524) {
				return sendQuestEndDialog(env);
			}
		}

		return false;
	}
}
