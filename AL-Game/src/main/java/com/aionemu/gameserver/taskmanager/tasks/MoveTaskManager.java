package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.taskmanager.FIFOSimpleExecutableQueue;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ATracer
 * @reworked Rolandas, parallelized by using Fork/Join framework
 */
public class MoveTaskManager extends AbstractPeriodicTaskManager {

	private final ConcurrentHashMap<Integer, Creature> movingCreatures = new ConcurrentHashMap<Integer, Creature>();
	private final TargetReachedManager targetReachedManager = new TargetReachedManager();
	private final TargetTooFarManager targetTooFarManager = new TargetTooFarManager();

	private MoveTaskManager() {
		super(100);
	}

	public void addCreature(Creature creature) {
		movingCreatures.put(creature.getObjectId(), creature);
	}

	public void removeCreature(Creature creature) {
		movingCreatures.remove(creature.getObjectId());
	}

	@Override
	public void run() {
		final ArrayList<Creature> arrivedCreatures = new ArrayList<>();
		final ArrayList<Creature> followingCreatures = new ArrayList<>();

		for (Creature creature : movingCreatures.values()) {
			creature.getMoveController().moveToDestination();
			if (creature.getAi2().poll(AIQuestion.DESTINATION_REACHED)) {
				movingCreatures.remove(creature.getObjectId());
				arrivedCreatures.add(creature);
			}
			else {
				followingCreatures.add(creature);
			}
		}
		targetReachedManager.executeAll(arrivedCreatures);
		targetTooFarManager.executeAll(followingCreatures);
	}

	public static MoveTaskManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private final class TargetReachedManager extends FIFOSimpleExecutableQueue<Creature> {

		@Override
		protected void removeAndExecuteFirst() {
			final Creature creature = removeFirst();
			try {
				creature.getAi2().onGeneralEvent(AIEventType.MOVE_ARRIVED);
				ZoneUpdateService.getInstance().add(creature);
			}
			catch (RuntimeException e) {
				log.warn("", e);
			}
		}
	}

	private final class TargetTooFarManager extends FIFOSimpleExecutableQueue<Creature> {

		@Override
		protected void removeAndExecuteFirst() {
			final Creature creature = removeFirst();
			try {
				creature.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
			}
			catch (RuntimeException e) {
				log.warn("", e);
			}
		}
	}

	private static final class SingletonHolder {

		private static final MoveTaskManager INSTANCE = new MoveTaskManager();
	}
}