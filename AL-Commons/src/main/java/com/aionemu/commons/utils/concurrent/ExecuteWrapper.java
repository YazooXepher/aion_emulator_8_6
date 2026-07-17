package com.aionemu.commons.utils.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;

/**
 * @author NB4L1
 */
public class ExecuteWrapper implements Executor {

	private static final Logger log = LoggerFactory.getLogger(ExecuteWrapper.class);

	@Override
	public void execute(Runnable runnable) {
		execute(runnable, Long.MAX_VALUE);
	}

	public static void execute(Runnable runnable, long maximumRuntimeInMillisecWithoutWarning) {
		long begin = System.nanoTime();

		try {
			runnable.run();
		} catch (Throwable t) {
			log.warn("Exception in a Runnable execution:", t);
		} finally {

			long runtimeInNanosec = System.nanoTime() - begin;
			Class<? extends Runnable> clazz = runnable.getClass();

			if (CommonsConfig.RUNNABLESTATS_ENABLE) {
				RunnableStatsManager.handleStats(clazz, runtimeInNanosec);
			}

			long runtimeInMillisec = TimeUnit.NANOSECONDS.toMillis(runtimeInNanosec);
			if (runtimeInMillisec > maximumRuntimeInMillisecWithoutWarning) {
				StringBuilder sb = new StringBuilder();
				sb.append(clazz);
				sb.append(" - execution time: ");
				sb.append(runtimeInMillisec);
				sb.append("msec");
				log.warn(sb.toString());
			}
		}
	}
}