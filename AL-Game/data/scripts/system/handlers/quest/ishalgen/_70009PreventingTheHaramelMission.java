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
package quest.ishalgen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.world.zone.ZoneName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Falke_34
 */
public class _70009PreventingTheHaramelMission extends QuestHandler {

	private final static int questId = 70009;

	public _70009PreventingTheHaramelMission() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(703488).addOnTalkEvent(questId); // Third Odella Transport Track
		qe.registerQuestNpc(806883).addOnTalkEvent(questId); // Cheska
		qe.registerQuestNpc(799524).addOnTalkEvent(questId); // Gestanerk
		qe.registerQuestNpc(806814).addOnTalkEvent(questId); // Marko
		qe.registerQuestNpc(700834).addOnTalkEvent(questId); // Odella
		qe.registerQuestNpc(653196).addOnKillEvent(questId); // Drudgelord Kakiti
		qe.registerQuestNpc(653205).addOnKillEvent(questId); // MuMu Ham the Grey
		qe.registerQuestNpc(653218).addOnKillEvent(questId); // Hamerun the Bleeder
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnEnterZone(ZoneName.get("HARAMEL_TOWER_300200000"), questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}
		int targetId = env.getTargetId();
		int var = qs.getQuestVarById(0);
		// var scheme mirrors the proven Elyos twin 60009 exactly (see quest_data.xml's
		// collecting_step="6" for the Odella drop, identical in both quests): Kakiti at var 3,
		// MuMu Ham at var 5, Hamerun at var 8. The previous version inserted an extra phantom
		// step (NPC 820012, which only exists in the Elyos/Poeta version of this chain and is
		// never spawned in the Asmodian Haramel instance) between Kakiti and MuMu Ham, which
		// shifted every var after it by one and made the MuMu Ham/Gestanerk/Hamerun steps
		// unreachable - nothing could ever satisfy the phantom step's condition.
		if (targetId == 653196 && var == 3) {
			qs.setQuestVar(4);
			updateQuestStatus(env);
			return true;
		} else if (targetId == 653205 && var == 5) {
			qs.setQuestVar(6);
			updateQuestStatus(env);
			return true;
		} else if (targetId == 653218 && var == 8) {
			qs.setQuestVar(9);
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			env.setQuestId(questId);
			QuestService.startQuest(env);
		} else if (player.getWorldId() == 300200000 && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
			int instanceId = player.getInstanceId();
			qs.setQuestVar(2);
			updateQuestStatus(env);

			List<Npc> mobs = new ArrayList<Npc>();
			mobs.add((Npc) QuestService.spawnQuestNpc(300200000, instanceId, 806883, 141.7932f, 22.274172f, 144.2455f,(byte) 0));
			mobs.add((Npc) QuestService.spawnQuestNpc(300200000, instanceId, 799995, 221.85893f, 351.66858f, 141.01141f,(byte) 0));
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

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 703488) {
				if (dialog == DialogAction.USE_OBJECT) {
					changeQuestStep(env, 0, 1, false);
					return true;
				}
			} else if (targetId == 806883) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (dialog == DialogAction.SETPRO3) {
					return defaultCloseDialog(env, 2, 3);
				}
			} else if (targetId == 799524) {
				if (dialog == DialogAction.QUEST_SELECT) {
					// var scheme matches 60009's Gestanerk case exactly: var==6 (item not yet
					// handed over) shows 3057; only after checkQuestItems below advances to
					// var==7 does the second-visit dialog chain (3398 -> ... -> SETPRO8) start.
					return sendQuestDialog(env, var == 6 ? 3057 : 3398);
				} else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 6, 7, false, 10000, 10001);
				} else if (dialog == DialogAction.SETPRO8) {
					return defaultCloseDialog(env, 7, 8);
				}
			} else if (targetId == 700834) {
				if (dialog == DialogAction.USE_OBJECT) {
					ItemService.addItem(player, 182216581, 1);
					return true;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 806814) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (zoneName == ZoneName.get("HARAMEL_TOWER_300200000")) {
			// only advance from step 4 (matches 60009's onEnterZoneEvent exactly) - re-entering
			// the tower zone later (relog, walking back in) must not reset progress that's
			// already moved past this point
			if (qs.getQuestVarById(0) == 4) {
				qs.setQuestVar(5);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}

}
