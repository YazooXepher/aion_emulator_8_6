package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.HouseObjectCooldownsDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.Player;

public class MySQL5HouseObjectCooldownsDAO extends HouseObjectCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5HouseObjectCooldownsDAO.class);
	public static final String INSERT_QUERY = "INSERT INTO `house_object_cooldowns` (`player_id`, `object_id`, `cooldown`) VALUES (?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `house_object_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `object_id`, `cooldown` FROM `house_object_cooldowns` WHERE `player_id`=?";

	@Override
	public void loadHouseObjectCooldowns(Player player) {
		Connection con = null;
		PreparedStatement stmt = null;
		ConcurrentHashMap<Integer, Long> houseObjectCoolDowns = new ConcurrentHashMap<>();
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(SELECT_QUERY);
			stmt.setInt(1, player.getObjectId());
			ResultSet rset = stmt.executeQuery();
			while (rset.next()) {
				int objectId = rset.getInt("object_id");
				long reuseTime = rset.getLong("cooldown");
				if (reuseTime > System.currentTimeMillis()) {
					houseObjectCoolDowns.put(objectId, reuseTime);
				}
			}
			player.getHouseObjectCooldownList().setHouseObjectCooldowns(houseObjectCoolDowns);
			rset.close();
		} catch (SQLException e) {
			log.error("LoadHouseObjectCooldowns", e);
		} finally {
			DatabaseFactory.close(stmt, con);
		}
	}

	@Override
	public void storeHouseObjectCooldowns(Player player) {
		deleteHouseObjectCooldowns(player);
		ConcurrentHashMap<Integer, Long> houseObjectCoolDowns = player.getHouseObjectCooldownList().getHouseObjectCooldowns();
		if (houseObjectCoolDowns == null || houseObjectCoolDowns.isEmpty()) {
			return;
		}
		for (Map.Entry<Integer, Long> entry : houseObjectCoolDowns.entrySet()) {
			int objectId = entry.getKey();
			long reuseTime = entry.getValue();
			if (reuseTime < System.currentTimeMillis()) {
				continue;
			}
			Connection con = null;
			PreparedStatement stmt = null;
			try {
				con = DatabaseFactory.getConnection();
				stmt = con.prepareStatement(INSERT_QUERY);
				stmt.setInt(1, player.getObjectId());
				stmt.setInt(2, objectId);
				stmt.setLong(3, reuseTime);
				stmt.execute();
			} catch (SQLException e) {
				log.error("storeHouseObjectCooldowns", e);
			} finally {
				DatabaseFactory.close(stmt, con);
			}
		}
	}

	private void deleteHouseObjectCooldowns(Player player) {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(DELETE_QUERY);
			stmt.setInt(1, player.getObjectId());
			stmt.execute();
		} catch (SQLException e) {
			log.error("deleteHouseObjectCooldowns", e);
		} finally {
			DatabaseFactory.close(stmt, con);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}