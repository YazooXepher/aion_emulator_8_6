package com.aionemu.gameserver.model.instance.instancereward;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.instance.playerreward.JormungandPlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Falke_34
 */
public class JormungandReward extends InstanceReward<JormungandPlayerReward> {

	private MutableInt asmodiansPoints = new MutableInt(3800);
	private MutableInt elyosPoins = new MutableInt(3800);

	private MutableInt asmodiansPvpKills = new MutableInt(0);
	private MutableInt elyosPvpKills = new MutableInt(0);

	private Race race;
	protected WorldMapInstance instance;

	private int winnerPoints;
	private int looserPoints;
	private int capPoints;
	private long instanceTime;
	private int bonusTime;
	private final byte buffId;

	public JormungandReward(Integer mapId, int instanceId, WorldMapInstance instance) {
		super(mapId, instanceId);
		this.instance = instance;
		winnerPoints = 3000;
		looserPoints = 2500;
		capPoints = 30000;
		bonusTime = 12000;
		buffId = 11;
	}

	public void addPvpKillsByRace(Race race, int points) {
		MutableInt racePoints = getPvpKillsByRace(race);
		racePoints.add(points);
		if (racePoints.intValue() < 0) {
			racePoints.setValue(0);
		}
	}

	public void setInstanceStartTime() {
		this.instanceTime = System.currentTimeMillis();
	}

	public boolean hasCapPoints() {
		return Collections.max(getInstanceRewards(), Comparator.comparingInt(JormungandPlayerReward::getPoints)).getPoints() >= capPoints;
	}

	@Override
	public JormungandPlayerReward getPlayerReward(Integer object) {
		return (JormungandPlayerReward) super.getPlayerReward(object);
	}

	public MutableInt getPointsByRace(Race race) {
		return (race == Race.ELYOS) ? elyosPoins : (race == Race.ASMODIANS) ? asmodiansPoints : null;
	}

	public int getWinnerPoints() {
		return winnerPoints;
	}

	public void portToPosition(Player player) {
		float Rx = Rnd.get(-5, 5);
		float Ry = Rnd.get(-5, 5);
		Point3D AsmoStartPoint = new Point3D(758.2061f + Rx, 560.8301f + Ry, 576.874f);
		Point3D ElyosStartPoint = new Point3D(322.03433f + Rx, 490.0247f + Ry, 596.1155f);

		if (player.getRace() == Race.ASMODIANS) {
			TeleportService2.teleportTo(player, mapId, instanceId, AsmoStartPoint.getX(), AsmoStartPoint.getY(), AsmoStartPoint.getZ(), (byte) 45);
		} else {
			TeleportService2.teleportTo(player, mapId, instanceId, ElyosStartPoint.getX(), ElyosStartPoint.getY(), ElyosStartPoint.getZ(), (byte) 105);
		}
	}

	public List<JormungandPlayerReward> sortPoints() {
		List<JormungandPlayerReward> rewards = getInstanceRewards();
		Collections.sort(rewards, new Comparator<JormungandPlayerReward>() {
			@Override
			public int compare(JormungandPlayerReward o1, JormungandPlayerReward o2) {
				return Integer.compare(o2.getScorePoints(), o1.getScorePoints());
			}
		});
		return rewards;
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

	public int getCapPoints() {
		return capPoints;
	}

	public Race getWinningRace() {
		return race;
	}

	public void setWinningRace(Race race) {
		this.race = race;
	}

	public byte getBuffId() {
		return buffId;
	}

	public void regPlayerReward(Player player) {
		if (!containPlayer(player.getObjectId())) {
			addPlayerReward(new JormungandPlayerReward(player.getObjectId(), bonusTime, player.getRace()));
		}
	}

	public void sendPacket(final int type, final Integer object) {
		instance.doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(type, getTime(), getInstanceReward(), object));
			}
		});
	}

	public MutableInt getPvpKillsByRace(Race race) {
		return (race == Race.ELYOS) ? elyosPvpKills : (race == Race.ASMODIANS) ? asmodiansPvpKills : null;
	}

	public Race getWinningRaceByScore() {
		return asmodiansPoints.compareTo(elyosPoins) > 0 ? Race.ASMODIANS : Race.ELYOS;
	}

	@Override
	public void addPlayerReward(JormungandPlayerReward reward) {
		super.addPlayerReward(reward);
	}

	public int getLooserPoints() {
		return looserPoints;
	}

	public void addPointsByRace(Race race, int points) {
		MutableInt racePoints = getPointsByRace(race);
		racePoints.add(points);
		if (racePoints.intValue() < 0) {
			racePoints.setValue(0);
		}
	}

	@Override
	public void clear() {
		super.clear();
	}
}