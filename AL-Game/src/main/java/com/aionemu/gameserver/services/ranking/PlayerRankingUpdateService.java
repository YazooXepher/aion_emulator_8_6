package com.aionemu.gameserver.services.ranking;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerRankingDAO;
import com.aionemu.gameserver.model.ranking.PlayerRankingEnum;
import com.aionemu.gameserver.model.ranking.PlayerRankingResult;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RANK_LIST;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerRankingUpdateService {

	private static final Logger log = LoggerFactory.getLogger(PlayerRankingUpdateService.class);
	private int lastUpdate;
	private final ConcurrentHashMap<Integer, List<SM_RANK_LIST>> players = new ConcurrentHashMap<Integer, List<SM_RANK_LIST>>();

	public void onStart() {
		renewPlayerRanking(PlayerRankingEnum.ARENA_OF_DISCIPLINE.getId());
		renewPlayerRanking(PlayerRankingEnum.ARENA_OF_COOPERATION.getId());
		log.info("[PlayerRankingUpdateService] Player Ranking Loaded");
	}

	private void renewPlayerRanking(int tableId) {
		List<SM_RANK_LIST> newlyCalculated;
		newlyCalculated = loadRankPacket(tableId);
		players.remove(tableId);
		players.put(tableId, newlyCalculated);
		log.info("[PlayerRankingUpdateService] Player Ranking Updated");
	}

	private List<SM_RANK_LIST> loadRankPacket(int tableid) {
		ArrayList<PlayerRankingResult> list = DAOManager.getDAO(PlayerRankingDAO.class).getCompetitionRankingPlayers(tableid);
//		int page = 1;
		List<SM_RANK_LIST> playerPackets = new ArrayList<SM_RANK_LIST>();
		for (int i = 0; i < list.size(); i += 94) {
			if (list.size() > i + 94) {
				playerPackets.add(new SM_RANK_LIST(tableid, 0, list.subList(i, i + 94), lastUpdate));
				playerPackets.add(new SM_RANK_LIST(tableid, 1, list.subList(i, i + 94), lastUpdate));
			} else {
				playerPackets.add(new SM_RANK_LIST(tableid, 0, list.subList(i, list.size()), lastUpdate));
				playerPackets.add(new SM_RANK_LIST(tableid, 1, list.subList(i, list.size()), lastUpdate));
			}
//			page++;
		}
		return playerPackets;
	}

	public List<SM_RANK_LIST> getPlayers(int tableId) {
		return players.get(tableId);
	}

	public static final PlayerRankingUpdateService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		protected static final PlayerRankingUpdateService INSTANCE = new PlayerRankingUpdateService();
	}
}