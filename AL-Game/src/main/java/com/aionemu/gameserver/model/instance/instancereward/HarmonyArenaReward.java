package com.aionemu.gameserver.model.instance.instancereward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author xTz
 */
public class HarmonyArenaReward extends PvPArenaReward {

	private ArrayList<HarmonyGroupReward> groups = new ArrayList<>();

	public HarmonyArenaReward(Integer mapId, int instanceId, WorldMapInstance instance) {
		super(mapId, instanceId, instance);
	}

	public HarmonyGroupReward getHarmonyGroupReward(Integer object) {
		for (InstancePlayerReward reward : groups) {
			HarmonyGroupReward harmonyReward = (HarmonyGroupReward) reward;
			if (harmonyReward.containPlayer(object)) {
				return harmonyReward;
			}
		}
		return null;
	}

	public ArrayList<HarmonyGroupReward> getHarmonyGroupInside() {
		ArrayList<HarmonyGroupReward> harmonyGroups = new ArrayList<>();
		for (HarmonyGroupReward group : groups) {
			for (AGPlayer agp : group.getAGPlayers()) {
				if (agp.isInInstance()) {
					harmonyGroups.add(group);
					break;
				}
			}
		}
		return harmonyGroups;
	}

	public ArrayList<Player> getPlayersInside(HarmonyGroupReward group) {
		ArrayList<Player> players = new ArrayList<>();
		for (Player playerInside : instance.getPlayersInside()) {
			if (group.containPlayer(playerInside.getObjectId())) {
				players.add(playerInside);
			}
		}
		return players;
	}

	public void addHarmonyGroup(HarmonyGroupReward reward) {
		groups.add(reward);
	}

	public ArrayList<HarmonyGroupReward> getGroups() {
		return groups;
	}

	public void sendPacket(final int type, final Integer object) {
		instance.doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(type, getTime(), getInstanceReward(), object));
			}
		});
	}

	@Override
	public int getRank(int points) {
		int rank = -1;
		for (HarmonyGroupReward reward : sortGroupPoints()) {
			if (reward.getPoints() >= points) {
				rank++;
			}
		}
		return rank;
	}

	public List<HarmonyGroupReward> sortGroupPoints() {
		List<HarmonyGroupReward> sorted = new ArrayList<>(groups);
		Collections.sort(sorted, new Comparator<HarmonyGroupReward>() {
			@Override
			public int compare(HarmonyGroupReward o1, HarmonyGroupReward o2) {
				return Integer.compare(o2.getPoints(), o1.getPoints());
			}
		});
		return sorted;
	}

	@Override
	public int getTotalPoints() {
		int total = 0;
		for (HarmonyGroupReward group : groups) {
			total += group.getPoints();
		}
		return total;
	}

	@Override
	public void clear() {
		groups.clear();
		super.clear();
	}
}