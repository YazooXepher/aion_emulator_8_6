package com.aionemu.gameserver.dataholders;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.appearances.PlayerApp;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CoolyT
 */
@XmlRootElement(name = "players")
public class PlayersAppearanceData {

	@XmlElement(name = "player")
	public static ArrayList<PlayerApp> players = new ArrayList<PlayerApp>();
	public static ConcurrentHashMap<String, PlayerApp> playerApps = new ConcurrentHashMap<String, PlayerApp>();

	public void addPlayer(PlayerApp player) {
		if (players.contains(player))
			players.add(player);
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PlayerApp ap : players) {
			playerApps.put(ap.name.toLowerCase(), ap);
		}
	}

	public static ArrayList<PlayerApp> getApp() {
		return players;
	}

	public PlayerApp getAppearanceByName(String name) {
		name = name.toLowerCase();
		PlayerApp app = new PlayerApp();
		if (playerApps.containsKey(name))
			return playerApps.get(name);
		return app;
	}

	public int size() {
		return players.size();
	}
}
