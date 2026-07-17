package com.aionemu.gameserver.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Iterator;

import com.aionemu.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author lord_rex and MrPoke based on l2j-free engines.
 */
public abstract class AbstractFIFOPeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

	protected static final Logger log = LoggerFactory.getLogger(AbstractFIFOPeriodicTaskManager.class);
	private final HashSet<T> queue = new HashSet<>();
	private final HashSet<T> activeTasks = new HashSet<>();

	public AbstractFIFOPeriodicTaskManager(int period) {
		super(period);
	}

	public final void add(T t) {
		writeLock();
		try {
			queue.add(t);
		} finally {
			writeUnlock();
		}
	}

	@Override
	public final void run() {
		writeLock();
		try {
			activeTasks.addAll(queue);
			queue.clear();
		} finally {
			writeUnlock();
		}

		for (Iterator<T> it = activeTasks.iterator(); it.hasNext(); ) {
			T task = it.next();
			it.remove();
			final long begin = System.nanoTime();
			try {
				callTask(task);
			} catch (RuntimeException e) {
				log.warn("", e);
			} finally {
				RunnableStatsManager.handleStats(task.getClass(), getCalledMethodName(), System.nanoTime() - begin);
			}
		}
	}

	protected abstract void callTask(T task);

	protected abstract String getCalledMethodName();
}