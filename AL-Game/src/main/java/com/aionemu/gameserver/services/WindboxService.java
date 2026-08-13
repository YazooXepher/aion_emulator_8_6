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
 * walking into a world "windbox" wind vortex volume (e.g. the Ishalgen/Poeta tutorial tornadoes,
 * which lift a player into the air without requiring the zone to normally allow flight). The
 * vortex visual/lift itself is rendered purely client-side from the client's own world geometry;
 * the server only needs to detect the player is inside one and grant the reward buff.
 */
public class WindboxService {

	private static final org.slf4j.Logger diagLog = org.slf4j.LoggerFactory.getLogger(WindboxService.class);
	private static final int WINDBOX_SPEED_BUFF_SKILL_ID = 22882;

	private WindboxService() {
	}

	public static void onMove(Player player) {
		List<WindboxTemplate> windboxes = DataManager.WINDBOX_DATA.getWindboxesByMapId(player.getPosition().getMapId());
		if (windboxes.isEmpty()) {
			return;
		}

		float x = player.getX();
		float y = player.getY();
		float z = player.getZ();
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

		for (WindboxTemplate windbox : windboxes) {
			if (!windbox.containsAltitude(z)) {
				continue;
			}
			if (!windbox.isActiveNow(hour)) {
				continue;
			}
			if (windbox.contains2D(x, y)) {
				diagLog.info("[DIAG Windbox] player=" + player.getName() + " entered windbox=" + windbox.getName()
					+ " x=" + x + " y=" + y + " z=" + z);
				grantSpeedBuff(player);
				return;
			}
		}
	}

	private static void grantSpeedBuff(Player player) {
		if (player.getEffectController().hasAbnormalEffect(WINDBOX_SPEED_BUFF_SKILL_ID)) {
			return;
		}
		Skill skill = SkillEngine.getInstance().getSkill(player, WINDBOX_SPEED_BUFF_SKILL_ID, 1, player);
		if (skill != null) {
			skill.useNoAnimationSkill();
		}
	}
}
