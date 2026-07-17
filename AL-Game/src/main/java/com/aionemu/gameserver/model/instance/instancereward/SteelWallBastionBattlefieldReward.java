package com.aionemu.gameserver.model.instance.instancereward;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.SteelWallBastionBattlefieldPlayerReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Eloann
 */
public class SteelWallBastionBattlefieldReward extends InstanceReward<SteelWallBastionBattlefieldPlayerReward> {

	private MutableInt asmodiansPoints = new MutableInt(0);
	private MutableInt elyosPoins = new MutableInt(0);
	private MutableInt asmodiansPvpKills = new MutableInt(0);
	private MutableInt elyosPvpKills = new MutableInt(0);
	private Race race;
	protected WorldMapInstance instance;
	private int capPoints;
	private int winnerPoints;
	private int looserPoints;
	private int bonusTime;
	private long instanceTime;
	private final byte buffId;

	public SteelWallBastionBattlefieldReward(Integer mapId, int instanceId, WorldMapInstance instance) {
		super(mapId, instanceId);
		this.instance = instance;
		asmodiansPoints = new MutableInt(3800);
		elyosPoins = new MutableInt(3800);
		asmodiansPvpKills = new MutableInt(0);
		elyosPvpKills = new MutableInt(0);
		winnerPoints = 3000;
		looserPoints = 2500;
		capPoints = 30000;
		bonusTime = 12000;
		buffId = 12;
	}

	@Override
	public void clear() {
		super.clear();
	}

	public void regPlayerReward(Player player) {
		if (!containPlayer(player.getObjectId())) {
			addPlayerReward(new SteelWallBastionBattlefieldPlayerReward(player.getObjectId(), bonusTime, player.getRace()));
		}
	}

	@Override
	public void addPlayerReward(SteelWallBastionBattlefieldPlayerReward reward) {
		super.addPlayerReward(reward);
	}

	@Override
	public SteelWallBastionBattlefieldPlayerReward getPlayerReward(Integer object) {
		return (SteelWallBastionBattlefieldPlayerReward) super.getPlayerReward(object);
	}

	public List<SteelWallBastionBattlefieldPlayerReward> sortPoints() {
		List<SteelWallBastionBattlefieldPlayerReward> rewards = getInstanceRewards();
		Collections.sort(rewards, new Comparator<SteelWallBastionBattlefieldPlayerReward>() {
			@Override
			public int compare(SteelWallBastionBattlefieldPlayerReward o1, SteelWallBastionBattlefieldPlayerReward o2) {
				return Integer.compare(o2.getScorePoints(), o1.getScorePoints());
			}
		});
		return rewards;
	}

	public void portToPosition(Player player) {
		float Rx = Rnd.get(-5, 5);
		float Ry = Rnd.get(-5, 5);
		Point3D ElyosStartPoint = new Point3D(570.468f + Rx, 166.897f + Ry, 432.28986f);
		Point3D AsmoStartPoint = new Point3D(400.741f + Rx, 166.713f + Ry, 432.290f);
		if (player.getRace() == Race.ASMODIANS) {
			TeleportService2.teleportTo(player, mapId, instanceId, AsmoStartPoint.getX(), AsmoStartPoint.getY(), AsmoStartPoint.getZ(), (byte) 45);
		} else {
			TeleportService2.teleportTo(player, mapId, instanceId, ElyosStartPoint.getX(), ElyosStartPoint.getY(), ElyosStartPoint.getZ(), (byte) 105);
		}
	}

	public MutableInt getPointsByRace(Race race) {
		return (race == Race.ELYOS) ? elyosPoins : (race == Race.ASMODIANS) ? asmodiansPoints : null;
	}

	public void addPointsByRace(Race race, int points) {
		MutableInt racePoints = getPointsByRace(race);
		racePoints.add(points);
		if (racePoints.intValue() < 0) {
			racePoints.setValue(0);
		}
	}

	public MutableInt getPvpKillsByRace(Race race) {
		return (race == Race.ELYOS) ? elyosPvpKills : (race == Race.ASMODIANS) ? asmodiansPvpKills : null;
	}

	public void addPvpKillsByRace(Race race, int points) {
		MutableInt racePoints = getPvpKillsByRace(race);
		racePoints.add(points);
		if (racePoints.intValue() < 0) {
			racePoints.setValue(0);
		}
	}

	public void setWinningRace(Race race) {
		this.race = race;
	}

	public Race getWinningRace() {
		return race;
	}

	public Race getWinningRaceByScore() {
		return asmodiansPoints.compareTo(elyosPoins) > 0 ? Race.ASMODIANS : Race.ELYOS;
	}

	public void setInstanceStartTime() {
		this.instanceTime = System.currentTimeMillis();
	}

	public int getTime() {
		long result = System.currentTimeMillis() - instanceTime;
		if (result < 45000) {
			return (int) (45000 - result);
		}
		if (result < 1800000) {
			return (int) (1800000 - (result - 20000));
		}
		return 0;
	}

	public int getWinnerPoints() {
		return winnerPoints;
	}

	public int getCapPoints() {
		return capPoints;
	}

	public int getLooserPoints() {
		return looserPoints;
	}

	public byte getBuffId() {
		return buffId;
	}

	public void sendPacket(final int type, final Integer object) {
		instance.doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(type, getTime(), getInstanceReward(), object));
			}
		});
	}
}