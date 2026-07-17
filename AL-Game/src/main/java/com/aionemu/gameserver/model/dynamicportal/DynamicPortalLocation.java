package com.aionemu.gameserver.model.dynamicportal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.dynamicportal.DynamicPortalTemplate;
import com.aionemu.gameserver.services.dynamicportal.DynamicPortal;

/**
 * @author Falke_34
 */
public class DynamicPortalLocation {

	protected int id;
	protected boolean isActive;
	protected DynamicPortalTemplate template;
	protected DynamicPortal<DynamicPortalLocation> activeDynamicPortal;
	protected ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<Integer, Player>();
	private final List<VisibleObject> spawned = new ArrayList<VisibleObject>();
	
	public DynamicPortalLocation() {
	}
	
	public DynamicPortalLocation(DynamicPortalTemplate template) {
		this.template = template;
		this.id = template.getId();
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActiveDynamicPortal(DynamicPortal<DynamicPortalLocation> dynamicPortal) {
		isActive = dynamicPortal != null;
		this.activeDynamicPortal = dynamicPortal;
	}
	
	public DynamicPortal<DynamicPortalLocation> getActiveDynamicPortal() {
		return activeDynamicPortal;
	}
	
	public final DynamicPortalTemplate getTemplate() {
		return template;
	}
	
	public int getId() {
		return id;
	}
	
	public List<VisibleObject> getSpawned() {
		return spawned;
	}
	
	public ConcurrentHashMap<Integer, Player> getPlayers() {
		return players;
	}
}
