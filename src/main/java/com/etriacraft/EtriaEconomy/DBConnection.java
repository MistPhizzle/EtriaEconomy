package main.java.com.etriacraft.EtriaEconomy;

import main.java.com.etriacraft.EtriaEconomy.SQLite.Database;
import main.java.com.etriacraft.EtriaEconomy.SQLite.SQLite;

public class DBConnection {

	public static Database sql;

	public static void init() {
		sql = new SQLite(EtriaEconomy.log, "[EtriaEconomy] Establishing SQLite Connection.", "econ.db", EtriaEconomy.plugin.getDataFolder().getAbsolutePath());
		((SQLite) sql).open();

		if (!sql.tableExists("econ_players")) {
			EtriaEconomy.log.info("Creating economy table.");
			String query = "CREATE TABLE `econ_players` ("
					+ "`id` INTEGER PRIMARY KEY,"
					+ "`uuid` TEXT(255),"
					+ "`player` TEXT(255),"
					+ "`amount` DOUBLE(255));";
			sql.modifyQuery(query);
		}
	}
}