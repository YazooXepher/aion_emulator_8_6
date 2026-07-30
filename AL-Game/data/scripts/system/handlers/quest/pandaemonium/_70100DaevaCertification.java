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
package quest.pandaemonium;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Falke_34
 */
public class _70100DaevaCertification extends QuestHandler {

	private final static int questId = 70100;
	private final static int[] onTalkNpc = {};

	public _70100DaevaCertification() {
		super(questId);
	}

	@Override
	public void register() {
		for (int npc : onTalkNpc) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(203679).addOnTalkEvent(questId);
		qe.registerQuestNpc(204182).addOnTalkEvent(questId);
		qe.registerQuestNpc(204075).addOnTalkEvent(questId);
		qe.registerQuestNpc(798800).addOnTalkEvent(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			env.setQuestId(questId);
			QuestService.startQuest(env);
		} else if (qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 120010000) {
				changeQuestStep(env, 0, 1, false);
				return true;
			}
			// The teleport statue (730268) is a plain, always-available portal (handled
			// entirely by the generic portal_dialog system) and is intentionally NOT a
			// quest NPC here - registering it as one made the client's reward-preview UI
			// hijack clicks on it whenever this quest was in REWARD status, blocking the
			// normal teleport prompt. Instead, arriving at the Convent of Marchutan by
			// any means (statue, scroll, etc.) is what advances the quest.
			if (player.getWorldId() == 120020000 && qs.getQuestVarById(0) == 3) {
				qs.setQuestVar(4);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}

		int targetId = env.getTargetId();
		DialogAction action = env.getDialog();

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 204182) {
				// Heimdall - confirmed via official capture (Session_1_templier, questId
				// 70100): QUEST_SELECT->1352, SELECT_ACTION_1353->1353, then SETPRO2(10001)
				// closes and advances the quest. The dialogIds here used to be a copy of
				// Balder's own sequence below, so Heimdall showed the wrong page entirely.
				switch (action) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 1352);
				case SELECT_ACTION_1353:
					return sendQuestDialog(env, 1353);
				case SETPRO2:
					qs.setQuestVar(2);
					updateQuestStatus(env);
					// confirmed via official capture: a flavor cutscene (movie 121) plays
					// right here, sent with objectId=0/questId=0 (not tied to the quest
					// engine's own per-quest movie system at all, which is why it was
					// never firing - we have no generic non-quest movie trigger)
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 0, 121, 0, 0));
					return closeDialogWindow(env);
				default:
					break;
				}
			} else if (targetId == 204075) {
				// Balder - confirmed via the same capture: QUEST_SELECT->1693,
				// SELECT_ACTION_1694->1694 (this is the page carrying the official
				// <CutScene id="122"/> ceremony), SELECT_ACTION_1695->1695, then
				// SETPRO3(10002) closes. This used to be copy-pasted generic
				// teleport-statue dialog (2034/2035/SET_SUCCEED) that also wrongly jumped
				// straight to REWARD status and teleported the player here - skipping the
				// real teleport-statue step below and never showing the ceremony cutscene.
				switch (action) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 1693);
				case SELECT_ACTION_1694:
					return sendQuestDialog(env, 1694);
				case SELECT_ACTION_1695:
					return sendQuestDialog(env, 1695);
				case SETPRO3:
					qs.setQuestVar(3);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				default:
					break;
				}
			}

		} else if (qs.getStatus() == QuestStatus.REWARD) {
			// confirmed via official capture + object-id resolution (SM_NPC_INFO): the
			// turn-in dialog (USE_OBJECT -> 10002 -> SELECT_QUEST_REWARD -> 5 ->
			// SELECTED_QUEST_NOREWARD -> complete) happens at Agehia (798800).
			if (targetId == 798800) {
				switch (action) {
				case USE_OBJECT:
					return sendQuestDialog(env, 10002);
				case SELECT_QUEST_REWARD:
					return sendQuestDialog(env, 5);
				case SELECTED_QUEST_REWARD1:
				case SELECTED_QUEST_REWARD2:
				case SELECTED_QUEST_NOREWARD:
					return sendQuestEndDialog(env);
				default:
					break;
				}
			}

		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}
}
