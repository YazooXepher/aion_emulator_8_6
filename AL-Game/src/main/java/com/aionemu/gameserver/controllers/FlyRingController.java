package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.controllers.observer.FlyRingObserver;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xavier
 */
public class FlyRingController extends VisibleObjectController<FlyRing> {

	ConcurrentHashMap<Integer, FlyRingObserver> observed = new ConcurrentHashMap<Integer, FlyRingObserver>();

	@Override
	public void see(VisibleObject object) {
		Player p = (Player) object;
		FlyRingObserver observer = new FlyRingObserver(getOwner(), p);
		p.getObserveController().addObserver(observer);
		observed.put(p.getObjectId(), observer);
	}

	@Override
	public void notSee(VisibleObject object, boolean isOutOfRange) {
		Player p = (Player) object;
		FlyRingObserver observer = observed.remove(p.getObjectId());
		if (isOutOfRange) {
			observer.moved();
		}
		p.getObserveController().removeObserver(observer);
	}
}
