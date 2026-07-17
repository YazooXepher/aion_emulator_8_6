package com.aionemu.commons.services;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.scriptmanager.ScriptManager;

/**
 * Script Service class that is designed to manage all loaded contexts
 *
 * @author SoulKeeper
 */
public class ScriptService {

	private static final Logger log = LoggerFactory.getLogger(ScriptService.class);

	private final Map<File, ScriptManager> map = new ConcurrentHashMap<>();

	public void load(String file) throws RuntimeException {
		load(new File(file));
	}

	public void load(File file) throws RuntimeException {
		if (file.isFile()) {
			loadFile(file);
		} else if (file.isDirectory()) {
			loadDir(file);
		}
	}

	private void loadFile(File file) {
		if (map.containsKey(file)) {
			throw new IllegalArgumentException("ScriptManager by file:" + file + " already loaded");
		}

		ScriptManager sm = new ScriptManager();
		try {
			sm.load(file);
		} catch (Exception e) {
			log.error("loadFile", e);
			throw new RuntimeException(e);
		}

		map.put(file, sm);
	}

	private void loadDir(File dir) {
		for (Object file : FileUtils.listFiles(dir, new String[] { "xml" }, false)) {
			loadFile((File) file);
		}
	}

	public void unload(File file) throws IllegalArgumentException {
		ScriptManager sm = map.remove(file);
		if (sm == null) {
			throw new IllegalArgumentException("ScriptManager by file " + file + " is not loaded.");
		}
		sm.shutdown();
	}

	public void reload(File file) throws IllegalArgumentException {
		ScriptManager sm = map.get(file);
		if (sm == null) {
			throw new IllegalArgumentException("ScriptManager by file " + file + " is not loaded.");
		}
		sm.reload();
	}

	public void addScriptManager(ScriptManager scriptManager, File file) {
		if (map.containsKey(file)) {
			throw new IllegalArgumentException("ScriptManager by file " + file + " is already loaded.");
		}
		map.put(file, scriptManager);
	}

	public Map<File, ScriptManager> getLoadedScriptManagers() {
		return Collections.unmodifiableMap(map);
	}

	public void shutdown() {
		for (Iterator<Entry<File, ScriptManager>> it = this.map.entrySet().iterator(); it.hasNext();) {
			try {
				it.next().getValue().shutdown();
			} catch (Exception e) {
				log.warn("An exception occured during shutdown procedure.", e);
			}
			it.remove();
		}
	}
}