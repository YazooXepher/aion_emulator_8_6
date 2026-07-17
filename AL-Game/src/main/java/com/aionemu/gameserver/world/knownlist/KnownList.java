package com.aionemu.gameserver.world.knownlist;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.MapRegion;

import java.util.concurrent.ConcurrentHashMap;

public class KnownList {

	private static final Logger log = LoggerFactory.getLogger(KnownList.class);
	protected final VisibleObject owner;
	protected final ConcurrentHashMap<Integer, VisibleObject> knownObjects = new ConcurrentHashMap<>();
	protected volatile ConcurrentHashMap<Integer, Player> knownPlayers;
	protected final ConcurrentHashMap<Integer, VisibleObject> visualObjects = new ConcurrentHashMap<>();
	protected volatile ConcurrentHashMap<Integer, Player> visualPlayers;
	private ReentrantLock lock = new ReentrantLock();

	public KnownList(VisibleObject owner) {
		this.owner = owner;
	}

	public void doUpdate() {
		lock.lock();
		try {
			forgetObjects();
			findVisibleObjects();
		} finally {
			lock.unlock();
		}
	}

	public void clear() {
		for (VisibleObject object : knownObjects.values()) {
			object.getKnownList().del(owner, false);
		}
		knownObjects.clear();
		if (knownPlayers != null) {
			knownPlayers.clear();
		}
		visualObjects.clear();
		if (visualPlayers != null) {
			visualPlayers.clear();
		}
	}

	public boolean knowns(AionObject object) {
		return knownObjects.containsKey(object.getObjectId());
	}

	protected boolean add(VisibleObject object) {
		if (!isAwareOf(object)) {
			return false;
		}

		if (knownObjects.put(object.getObjectId(), object) == null) {
			if (object instanceof Player) {
				checkKnownPlayersInitialized();
				knownPlayers.put(object.getObjectId(), (Player) object);
			}
			addVisualObject(object);
			return true;
		}
		return false;
	}

	public void addVisualObject(VisibleObject object) {
		if (object instanceof Creature) {
			if (SecurityConfig.INVIS && object instanceof Player) {
				if (!owner.canSee((Player) object)) {
					return;
				}
			}
			if (visualObjects.put(object.getObjectId(), object) == null) {
				if (object instanceof Player) {
					checkVisiblePlayersInitialized();
					visualPlayers.put(object.getObjectId(), (Player) object);
				}
				owner.getController().see(object);
			}
		} else if (visualObjects.put(object.getObjectId(), object) == null) {
			owner.getController().see(object);
		}
	}

	private void del(VisibleObject object, boolean isOutOfRange) {
		if (knownObjects.remove(object.getObjectId()) != null) {
			if (knownPlayers != null) {
				knownPlayers.remove(object.getObjectId());
			}
			delVisualObject(object, isOutOfRange);
		}
	}

	public void delVisualObject(VisibleObject object, boolean isOutOfRange) {
		if (visualObjects.remove(object.getObjectId()) != null) {
			if (visualPlayers != null) {
				visualPlayers.remove(object.getObjectId());
			}
			owner.getController().notSee(object, isOutOfRange);
		}
	}

	private void forgetObjects() {
		for (VisibleObject object : knownObjects.values()) {
			if (!checkObjectInRange(object) && !object.getKnownList().checkReversedObjectInRange(owner)) {
				del(object, true);
				object.getKnownList().del(owner, true);
			}
		}
	}

	protected void findVisibleObjects() {
		if (owner == null || !owner.isSpawned()) {
			return;
		}

		MapRegion[] regions = owner.getActiveRegion().getNeighbours();
		for (int i = 0; i < regions.length; i++) {
			MapRegion r = regions[i];
			ConcurrentHashMap<Integer, VisibleObject> objects = r.getObjects();
			for (VisibleObject newObject : objects.values()) {
				if (newObject == owner || newObject == null) {
					continue;
				}
				if (!isAwareOf(newObject)) {
					continue;
				}
				if (knownObjects.containsKey(newObject.getObjectId())) {
					continue;
				}
				if (!checkObjectInRange(newObject) && !newObject.getKnownList().checkReversedObjectInRange(owner)) {
					continue;
				}
				if (add(newObject)) {
					newObject.getKnownList().add(owner);
				}
			}
		}
	}

	protected boolean isAwareOf(VisibleObject newObject) {
		return true;
	}

	protected boolean checkObjectInRange(VisibleObject newObject) {
		if (Math.abs(owner.getZ() - newObject.getZ()) > owner.getMaxZVisibleDistance()) {
			return false;
		}
		return MathUtil.isInRange(owner, newObject, owner.getVisibilityDistance());
	}

	protected boolean checkReversedObjectInRange(VisibleObject newObject) {
		return false;
	}

	public void doOnAllNpcs(Visitor<Npc> visitor) {
		doOnAllNpcs(visitor, Integer.MAX_VALUE);
	}

	public int doOnAllNpcs(Visitor<Npc> visitor, int iterationLimit) {
		int counter = 0;
		try {
			for (VisibleObject newObject : knownObjects.values()) {
				if (newObject instanceof Npc) {
					if ((++counter) == iterationLimit) {
						break;
					}
					visitor.visit((Npc) newObject);
				}
			}
		} catch (Exception ex) {
		}
		return counter;
	}

	public void doOnAllNpcsWithOwner(VisitorWithOwner<Npc, VisibleObject> visitor) {
		doOnAllNpcsWithOwner(visitor, Integer.MAX_VALUE);
	}

	public int doOnAllNpcsWithOwner(VisitorWithOwner<Npc, VisibleObject> visitor, int iterationLimit) {
		int counter = 0;
		try {
			for (VisibleObject newObject : knownObjects.values()) {
				if (newObject instanceof Npc) {
					if ((++counter) == iterationLimit) {
						break;
					}
					visitor.visit((Npc) newObject, owner);
				}
			}
		} catch (Exception ex) {
		}
		return counter;
	}

	public void doOnAllPlayers(Visitor<Player> visitor) {
		if (knownPlayers == null) {
			return;
		}
		try {
			for (Player player : knownPlayers.values()) {
				if (player != null) {
					visitor.visit(player);
				}
			}
		} catch (Exception ex) {
		}
	}

	public void doOnAllObjects(Visitor<VisibleObject> visitor) {
		try {
			for (VisibleObject newObject : knownObjects.values()) {
				if (newObject != null) {
					visitor.visit(newObject);
				}
			}
		} catch (Exception ex) {
		}
	}

	public Map<Integer, VisibleObject> getKnownObjects() {
		return knownObjects;
	}

	public Map<Integer, VisibleObject> getVisibleObjects() {
		return visualObjects;
	}

	public Map<Integer, Player> getKnownPlayers() {
		return knownPlayers != null ? knownPlayers : Collections.<Integer, Player>emptyMap();
	}

	public Map<Integer, Player> getVisiblePlayers() {
		return visualPlayers != null ? visualPlayers : Collections.<Integer, Player>emptyMap();
	}

	final void checkKnownPlayersInitialized() {
		if (knownPlayers == null) {
			synchronized (this) {
				if (knownPlayers == null) {
					knownPlayers = new ConcurrentHashMap<>();
				}
			}
		}
	}

	final void checkVisiblePlayersInitialized() {
		if (visualPlayers == null) {
			synchronized (this) {
				if (visualPlayers == null) {
					visualPlayers = new ConcurrentHashMap<>();
				}
			}
		}
	}

	public VisibleObject getObject(int targetObjectId) {
		return this.knownObjects.get(targetObjectId);
	}
}