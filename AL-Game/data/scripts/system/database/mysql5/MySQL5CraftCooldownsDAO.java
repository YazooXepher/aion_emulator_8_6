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
import com.aionemu.gameserver.dao.CraftCooldownsDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.Player;

public class MySQL5CraftCooldownsDAO extends CraftCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5CraftCooldownsDAO.class);
	public static final String INSERT_QUERY = "INSERT INTO `craft_cooldowns` (`player_id`, `recipe_id`, `cooldown`) VALUES (?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `craft_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `recipe_id`, `cooldown` FROM `craft_cooldowns` WHERE `player_id`=?";

	@Override
	public void loadCraftCooldowns(Player player) {
		Connection con = null;
		PreparedStatement stmt = null;
		// Récupération de la map interne de la CraftCooldownList
		ConcurrentHashMap<Integer, Long> craftCoolDowns = new ConcurrentHashMap<>();
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(SELECT_QUERY);
			stmt.setInt(1, player.getObjectId());
			ResultSet rset = stmt.executeQuery();
			while (rset.next()) {
				int recipeId = rset.getInt("recipe_id");
				long reuseTime = rset.getLong("cooldown");
				if (reuseTime > System.currentTimeMillis()) {
					craftCoolDowns.put(recipeId, reuseTime);
				}
			}
			// On remplace la map existante par celle chargée
			player.getCraftCooldownList().setCraftCoolDowns(craftCoolDowns);
			rset.close();
		} catch (SQLException e) {
			log.error("LoadCraftCooldowns", e);
		} finally {
			DatabaseFactory.close(stmt, con);
		}
	}

	@Override
	public void storeCraftCooldowns(Player player) {
		deleteCraftCooldowns(player);
		ConcurrentHashMap<Integer, Long> craftCoolDowns = player.getCraftCooldownList().getCraftCoolDowns();
		if (craftCoolDowns == null || craftCoolDowns.isEmpty()) {
			return;
		}
		for (Map.Entry<Integer, Long> entry : craftCoolDowns.entrySet()) {
			int recipeId = entry.getKey();
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
				stmt.setInt(2, recipeId);
				stmt.setLong(3, reuseTime);
				stmt.execute();
			} catch (SQLException e) {
				log.error("storeCraftCooldowns", e);
			} finally {
				DatabaseFactory.close(stmt, con);
			}
		}
	}

	private void deleteCraftCooldowns(Player player) {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(DELETE_QUERY);
			stmt.setInt(1, player.getObjectId());
			stmt.execute();
		} catch (SQLException e) {
			log.error("deleteCraftCooldowns", e);
		} finally {
			DatabaseFactory.close(stmt, con);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}