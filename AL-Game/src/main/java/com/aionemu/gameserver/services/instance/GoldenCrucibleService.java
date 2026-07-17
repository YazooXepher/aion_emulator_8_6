package com.aionemu.gameserver.services.instance;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

import java.util.ArrayList;

/**
 * @author Falke_34
 */
public class GoldenCrucibleService {

	/*
	 * Used to logs information.
	 */
	private static final Logger log = LoggerFactory.getLogger(GoldenCrucibleService.class);

	/**
	 * Entry Level: 66-75
	 */
	public static final byte minlevel = 66, maxlevel = 76;

	// Determines whether users can still register for running instance?
	private boolean registerAvailable;

	// Determines whether the given player is already registered for instance?
	private ArrayList<Integer> playersWithCooldown = new ArrayList<Integer>();

	// MaskId
	public static final int maskId = 125;

	public static final int InstanceMapId = 302320000;

	/**
	 * instantiate class
	 */
	private static class SingletonHolder {

		protected static final GoldenCrucibleService instance = new GoldenCrucibleService();
	}

	public static GoldenCrucibleService getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Schedules the Golden Crucible Instance to be launched at the specified Cron-Time.
	 */
	public void start() {
		String[] times = AutoGroupConfig.GOLDENCRUCIBLE_TIMES.split("\\|");
		for (String cron : times) {
			CronService.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					startRegistration();
				}
			}, cron);
			log.info("Scheduled Golden Crucible based on cron expression: " + cron + " Duration: " + AutoGroupConfig.GOLDENCRUCIBLE_TIMER + " in minutes");
		}
	}

	private void startRegistration() {
		registerAvailable = true;
		ScheduleUnregistration();
		Iterator<Player> iter = World.getInstance().getPlayersIterator();
		while (iter.hasNext()) {
			Player player = iter.next();
			if (player.getLevel() > minlevel && player.getLevel() < maxlevel) {
				if (!isInInstance(player)) {
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon));
					// You can enter the Golden Crucible.
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDTM_Lobby01);
				}
			}
		}
	}

	private void ScheduleUnregistration() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				registerAvailable = false;
				playersWithCooldown.clear();
				AutoGroupService.getInstance().unRegisterInstance(maskId);
				Iterator<Player> iter = World.getInstance().getPlayersIterator();
				while (iter.hasNext()) {
					Player player = iter.next();
					if (player.getLevel() > minlevel) {
						PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, true));
					}
				}
			}
		}, AutoGroupConfig.GOLDENCRUCIBLE_TIMER * 60 * 1000);
	}

	public byte getMinLevel() {
		return minlevel;
	}

	public byte getMaxLevel() {
		return maxlevel;
	}

	public boolean isGoldenCrucibleAvailable() {
		return registerAvailable;
	}

	public void addCoolDown(Player player) {
		playersWithCooldown.add(player.getObjectId());
	}

	public boolean hasCoolDown(Player player) {
		return playersWithCooldown.contains(player.getObjectId());
	}

	public void showWindow(Player player, byte instanceMaskId) {
		if (!playersWithCooldown.contains(player.getObjectId())) {
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId));
		}
	}

	private boolean isInInstance(Player player) {
		return player.isInInstance();
	}

	public boolean canPlayerJoin(Player player) {
		return registerAvailable && player.getLevel() > minlevel && player.getLevel() < maxlevel && !hasCoolDown(player) && !isInInstance(player);
	}
}
