package com.aionemu.gameserver.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.configs.administration.DeveloperConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

import java.util.concurrent.ConcurrentHashMap;

public class MapRegion {

	private static final Logger log = LoggerFactory.getLogger(MapRegion.class);
	private final int regionId;
	private final WorldMapInstance parent;
	private volatile MapRegion[] neighbours = new MapRegion[0];
	private final ConcurrentHashMap<Integer, VisibleObject> objects = new ConcurrentHashMap<>();
	private final AtomicInteger playerCount = new AtomicInteger(0);
	private final AtomicBoolean regionActive = new AtomicBoolean(false);
	private final int zoneCount;
	private ConcurrentHashMap<Integer, TreeSet<ZoneInstance>> zoneMap;

	MapRegion(int id, WorldMapInstance parent, ZoneInstance[] zones) {
		this.regionId = id;
		this.parent = parent;
		this.zoneCount = zones.length;
		createZoneMap(zones);
		addNeighbourRegion(this);
	}

	public Integer getMapId() {
		return getParent().getMapId();
	}

	public World getWorld() {
		return getParent().getWorld();
	}

	public int getRegionId() {
		return regionId;
	}

	public WorldMapInstance getParent() {
		return parent;
	}

	public ConcurrentHashMap<Integer, VisibleObject> getObjects() {
		return objects;
	}

	public Map<Integer, StaticDoor> getDoors() {
		Map<Integer, StaticDoor> doors = new HashMap<Integer, StaticDoor>();
		for (VisibleObject obj : objects.values()) {
			if (obj instanceof StaticDoor) {
				StaticDoor door = (StaticDoor) obj;
				doors.put(door.getSpawn().getStaticId(), door);
			}
		}
		return doors;
	}

	public MapRegion[] getNeighbours() {
		return neighbours;
	}

	void addNeighbourRegion(MapRegion neighbour) {
		neighbours = (MapRegion[]) ArrayUtils.add(neighbours, neighbour);
	}

	void add(VisibleObject object) {
		if (objects.put(object.getObjectId(), object) == null) {
			if (object instanceof Player) {
				checkActiveness(playerCount.incrementAndGet() > 0);
			} else if (DeveloperConfig.SPAWN_CHECK) {
				Iterator<TreeSet<ZoneInstance>> zoneIter = zoneMap.values().iterator();
				while (zoneIter.hasNext()) {
					TreeSet<ZoneInstance> zones = zoneIter.next();
					for (ZoneInstance zone : zones) {
						if (!zone.isInsideCordinate(object.getX(), object.getY(), object.getZ())) {
							continue;
						}
						if (zone.getZoneTemplate().getZoneType() != ZoneClassName.DUMMY) {
							return;
						}
					}
				}
				log.warn("Outside any zones: id=" + object + " > X:" + object.getX() + ",Y:" + object.getY() + ",Z:" + object.getZ());
			}
		}
	}

	void remove(VisibleObject object) {
		if (objects.remove(object.getObjectId()) != null) {
			if (object instanceof Player) {
				checkActiveness(playerCount.decrementAndGet() > 0);
			}
		}
	}

	final void checkActiveness(boolean active) {
		if (active && regionActive.compareAndSet(false, true)) {
			startActivation();
		} else if (!active) {
			startDeactivation();
		}
	}

	final void startActivation() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				log.debug("Activating in map {} region {}", getMapId(), regionId);
				MapRegion.this.activateObjects();
				for (MapRegion neighbor : getNeighbours()) {
					neighbor.activate();
				}
			}
		}, 1000);
	}

	final void startDeactivation() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				log.debug("Deactivating in map {} region {}", getMapId(), regionId);
				for (MapRegion neighbor : getNeighbours()) {
					if (!neighbor.isNeighboursActive()) {
						neighbor.deactivate();
					}
				}
			}
		}, 60000);
	}

	public void activate() {
		if (regionActive.compareAndSet(false, true)) {
			activateObjects();
		}
	}

	private final void activateObjects() {
		for (VisibleObject visObject : objects.values()) {
			if (visObject instanceof Creature) {
				Creature creature = (Creature) visObject;
				creature.getAi2().onGeneralEvent(AIEventType.ACTIVATE);
			}
		}
	}

	public void deactivate() {
		if (regionActive.compareAndSet(true, false)) {
			deactivateObjects();
		}
	}

	private void deactivateObjects() {
		for (VisibleObject visObject : objects.values()) {
			if (visObject instanceof Creature && !(SiegeConfig.BALAUR_AUTO_ASSAULT && visObject instanceof SiegeNpc) && !((Creature) visObject).isFlag() && !((Creature) visObject).isRaidMonster()) {
				Creature creature = (Creature) visObject;
				creature.getAi2().onGeneralEvent(AIEventType.DEACTIVATE);
			}
		}
	}

	public boolean isMapRegionActive() {
		return !WorldConfig.WORLD_ACTIVE_TRACE || regionActive.get();
	}

	boolean isNeighboursActive() {
		for (int i = 0; i < neighbours.length; i++) {
			MapRegion r = neighbours[i];
			if (r != null && r.regionActive.get() && r.playerCount.get() > 0) {
				return true;
			}
		}
		return false;
	}

	public void revalidateZones(Creature creature) {
		for (Map.Entry<Integer, TreeSet<ZoneInstance>> e : zoneMap.entrySet()) {
			boolean foundZone = false;
			int category = e.getKey();
			TreeSet<ZoneInstance> zones = e.getValue();
			for (ZoneInstance zone : zones) {
				if (!creature.isSpawned() || (category != -1 && foundZone)) {
					zone.onLeave(creature);
					continue;
				}
				boolean result = zone.revalidate(creature);
				if (!result) {
					zone.onLeave(creature);
					continue;
				}
				if (category != -1) {
					foundZone = true;
				}
				zone.onEnter(creature);
			}
		}
	}

	public List<ZoneInstance> getZones(Creature creature) {
		List<ZoneInstance> z = new ArrayList<ZoneInstance>();
		for (TreeSet<ZoneInstance> zones : zoneMap.values()) {
			for (ZoneInstance zone : zones) {
				if (zone.isInsideCreature(creature)) {
					z.add(zone);
				}
			}
		}
		return z;
	}

	public boolean onDie(Creature attacker, Creature target) {
		for (TreeSet<ZoneInstance> zones : zoneMap.values()) {
			for (ZoneInstance zone : zones) {
				if (zone.isInsideCreature(target)) {
					if (zone.onDie(attacker, target)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isInsideZone(ZoneName zoneName, float x, float y, float z) {
		for (TreeSet<ZoneInstance> zones : zoneMap.values()) {
			for (ZoneInstance zone : zones) {
				if (zone.getZoneTemplate().getName() != zoneName) {
					continue;
				}
				return zone.isInsideCordinate(x, y, z);
			}
		}
		return false;
	}

	public boolean isInsideZone(ZoneName zoneName, Creature creature) {
		for (TreeSet<ZoneInstance> zones : zoneMap.values()) {
			for (ZoneInstance zone : zones) {
				if (zone.getZoneTemplate().getName() != zoneName) {
					continue;
				}
				return zone.isInsideCreature(creature);
			}
		}
		return false;
	}

	public boolean isInsideItemUseZone(ZoneName zoneName, Creature creature) {
		for (TreeSet<ZoneInstance> zones : zoneMap.values()) {
			for (ZoneInstance zone : zones) {
				if (!zone.getZoneTemplate().getXmlName().startsWith(zoneName.toString())) {
					continue;
				}
				if (!zone.isInsideCreature(creature)) {
					continue;
				}
				return true;
			}
		}
		return false;
	}

	private void createZoneMap(ZoneInstance[] zones) {
		zoneMap = new ConcurrentHashMap<>();
		for (int i = 0; i < zones.length; i++) {
			ZoneInstance zone = zones[i];
			int category = -1;
			if (zone.getZoneTemplate().getPriority() != 0) {
				category = zone.getZoneTemplate().getZoneType().ordinal();
			}
			TreeSet<ZoneInstance> zoneCategory = zoneMap.get(category);
			if (zoneCategory == null) {
				zoneCategory = new TreeSet<ZoneInstance>();
				zoneMap.put(category, zoneCategory);
			}
			zoneCategory.add(zone);
		}
	}

	public int getZoneCount() {
		return zoneCount;
	}
}