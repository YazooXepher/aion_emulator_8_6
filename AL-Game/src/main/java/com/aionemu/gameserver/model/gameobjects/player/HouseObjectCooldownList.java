package com.aionemu.gameserver.model.gameobjects.player;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rolandas
 */
public class HouseObjectCooldownList {

	private ConcurrentHashMap<Integer, Long> houseObjectCooldowns;

	HouseObjectCooldownList(Player owner) {
	}

	public boolean isCanUseObject(int objectId) {
		if (houseObjectCooldowns == null || !houseObjectCooldowns.containsKey(objectId)) {
			return true;
		}

		Long coolDown = houseObjectCooldowns.get(objectId);
		if (coolDown == null) {
			return true;
		}

		if (coolDown < System.currentTimeMillis()) {
			houseObjectCooldowns.remove(objectId);
			return true;
		}

		return false;
	}

	public long getHouseObjectCooldown(int objectId) {
		if (houseObjectCooldowns == null || !houseObjectCooldowns.containsKey(objectId)) {
			return 0;
		}

		return houseObjectCooldowns.get(objectId);
	}

	public ConcurrentHashMap<Integer, Long> getHouseObjectCooldowns() {
		return houseObjectCooldowns;
	}

	public void setHouseObjectCooldowns(ConcurrentHashMap<Integer, Long> houseObjectCooldowns) {
		this.houseObjectCooldowns = houseObjectCooldowns;
	}

	public void addHouseObjectCooldown(int objectId, int delay) {
		if (houseObjectCooldowns == null) {
			houseObjectCooldowns = new ConcurrentHashMap<>();
		}

		long nextUseTime = System.currentTimeMillis() + (delay * 1000);
		houseObjectCooldowns.put(objectId, nextUseTime);
	}

	public int getReuseDelay(int objectId) {
		if (isCanUseObject(objectId)) {
			return 0;
		}
		long cd = getHouseObjectCooldown(objectId);
		int delay = (int) ((cd - System.currentTimeMillis()) / 1000);
		return delay;
	}
}
