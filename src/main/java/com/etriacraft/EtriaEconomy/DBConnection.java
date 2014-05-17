package com.etriacraft.EtriaEconomy;

import com.etriacraft.EtriaEconomy.sqls.Database;
import com.etriacraft.EtriaEconomy.sqls.MySQL;
import com.etriacraft.EtriaEconomy.sqls.SQLite;

public class DBConnection {

	public static Database sql;

	public static String host;
	public static int port;
	public static String db;
	public static String user;
	public static String pass;
	
	public static void init() {
		if (EtriaEconomy.plugin.getConfig().getString("Storage.engine").equalsIgnoreCase("mysql")) {
			sql = new MySQL(EtriaEconomy.log, "[EtriaEconomy] Establishing MySQL Connection...", host, port, user, pass, db);
			((MySQL) sql).open();
			EtriaEconomy.log.info("[EtriaEconomy] Database connection established.");
			
			if (!sql.tableExists("econ_players")) {
				EtriaEconomy.log.info("Creating economy table.");
				String query = "CREATE TABLE `econ_players` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`uuid` varchar(255),"
						+ "`player` varchar(255),"
						+ "`amount` double,"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
			
			if (!sql.tableExists("econ_transactions")) {
				EtriaEconomy.log.info("Creating economy transaction table.");
				String query = "CREATE TABLE `econ_transactions` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`date` varchar(255),"
						+ "`player` varchar(255),"
						+ "`amount` double,"
						+ "`type` varchar(255),"
						+ "`other` varchar(255),"
						+ "`message` varchar(255),"
						+ " PRIMARY KEY(id));";
				sql.modifyQuery(query);
			}
		} else {
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
			
			if (!sql.tableExists("econ_transactions")) {
				EtriaEconomy.log.info("Creating economy transactions table.");
				String query = "CREATE TABLE `econ_transactions` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`date` TEXT(255),"
						+ "`player` TEXT(255),"
						+ "`amount` DOUBLE(255),"
						+ "`type` TEXT(255),"
						+ "`other` TEXT(255),"
						+ "`message` TEXT(255));";
				sql.modifyQuery(query);
			}
		}
	}
}