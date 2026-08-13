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
package com.aionemu.gameserver.services;

import java.util.Calendar;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.windbox.WindboxTemplate;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * Grants the temporary movement speed buff (skill 22882, "F6_Windbox_MoveSpeed_StatUp") to players
 * entering a world "windbox" wind vortex volume (e.g. the Ishalgen/Poeta tutorial tornadoes, which
 * lift a player without requiring the zone to normally allow flight). The vortex visual/lift is
 * rendered client-side from the client's own world geometry; the server only detects the player is
 * inside one and grants the reward buff.
 *
 * This runs on every movement packet, so the hot path exits immediately for the common case of a
 * map with no windboxes, and only touches the wall clock when a windbox actually has a time window.
 */
public class WindboxService {

	private static final int WINDBOX_SPEED_BUFF_SKILL_ID = 22882;

	private WindboxService() {
	}

	public static void onMove(Player player) {
		List<WindboxTemplate> windboxes = DataManager.WINDBOX_DATA.getWindboxesByMapId(player.getPosition().getMapId());
		if (windboxes.isEmpty()) {
			return;
		}

		// Cheapest exit first: if the buff is already running there is nothing to do, so skip the
		// geometry test entirely while the player lingers in (or has just left) a vortex.
		if (player.getEffectController().hasAbnormalEffect(WINDBOX_SPEED_BUFF_SKILL_ID)) {
			return;
		}

		float x = player.getX();
		float y = player.getY();
		float z = player.getZ();
		int hour = -1; // computed lazily, only for windboxes that actually gate on time of day

		for (WindboxTemplate windbox : windboxes) {
			if (!windbox.containsAltitude(z)) {
				continue;
			}
			if (windbox.hasTimeWindow()) {
				if (hour < 0) {
					hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				}
				if (!windbox.isActiveNow(hour)) {
					continue;
				}
			}
			if (windbox.contains2D(x, y)) {
				grantSpeedBuff(player);
				return;
			}
		}
	}

	private static void grantSpeedBuff(Player player) {
		Skill skill = SkillEngine.getInstance().getSkill(player, WINDBOX_SPEED_BUFF_SKILL_ID, 1, player);
		if (skill != null) {
			skill.useNoAnimationSkill();
		}
	}
}
