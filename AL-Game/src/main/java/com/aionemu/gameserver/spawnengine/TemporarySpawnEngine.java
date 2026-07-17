package com.aionemu.gameserver.spawnengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.TemporarySpawn;

/**
 * @author xTz
 */
public class TemporarySpawnEngine {

	private static final ConcurrentHashMap<SpawnGroup2, HashSet<Integer>> temporarySpawns = new ConcurrentHashMap<>();

	public static void spawnAll() {
		spawn(true);
	}

	public static void onHourChange() {
		despawn();
		spawn(false);
	}

	private static void despawn() {
		for (SpawnGroup2 spawn : temporarySpawns.keySet()) {
			for (SpawnTemplate template : spawn.getSpawnTemplates()) {
				if (!template.getTemporarySpawn().isInSpawnTime()) {
					List<VisibleObject> objects = template.getVisibleObjects();
					if (objects == null) {
						continue;
					}
					for (VisibleObject object : objects) {
						if (object instanceof Npc) {
							Npc npc = (Npc) object;
							npc.getController().cancelTask(TaskId.RESPAWN);
						}
						if (object.isSpawned()) {
							object.getController().onDelete();
						}
						spawn.setTemplateUse(object.getInstanceId(), template, false);
					}
					objects.clear();
				}
			}
		}
	}

	private static void spawn(boolean startCheck) {
		for (SpawnGroup2 spawn : temporarySpawns.keySet()) {
			HashSet<Integer> instances = temporarySpawns.get(spawn);
			if (spawn.hasPool()) {
				TemporarySpawn temporarySpawn = spawn.geTemporarySpawn();
				if ((temporarySpawn.canSpawn() || (startCheck && spawn.getRespawnTime() != 0 && temporarySpawn.isInSpawnTime()))) {
					for (Integer instanceId : instances) {
						spawn.resetTemplates(instanceId);
						for (int pool = 0; pool < spawn.getPool(); pool++) {
							SpawnTemplate template = spawn.getRndTemplate(instanceId);
							SpawnEngine.spawnObject(template, instanceId);
						}
					}
				}
			} else {
				for (SpawnTemplate template : spawn.getSpawnTemplates()) {
					TemporarySpawn temporarySpawn = template.getTemporarySpawn();
					if ((temporarySpawn.isInSpawnTime() || (startCheck && !template.isNoRespawn() && temporarySpawn.isInSpawnTime()))) {
						for (Integer instanceId : instances) {
							if (!spawn.isTemplateUsed(instanceId, template)) {
								SpawnEngine.spawnObject(template, instanceId);
								spawn.setTemplateUse(instanceId, template, true);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @param spawnTemplate
	 */
	public static void addSpawnGroup(SpawnGroup2 spawn, int instanceId) {
		HashSet<Integer> instances = temporarySpawns.get(spawn);
		if (instances == null) {
			instances = new HashSet<>();
			temporarySpawns.put(spawn, instances);
		}
		instances.add(instanceId);
	}
}