package com.etriacraft.EtriaEconomy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.bukkit.ChatColor;

public class Methods {

	static EtriaEconomy plugin;
	
	public Methods(EtriaEconomy plugin) {
		Methods.plugin = plugin;
	}
	
	public static HashMap<String, String> uuids = new HashMap<String, String>(); // {UUID / Last Known Player Name Using UUID}
	public static HashMap<String, Double> accounts = new HashMap<String, Double>(); // {Account Name / Amount}
	
	public static void loadUUIDs() {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM econ_players");
		int i = 0;
		try {
			if (!rs2.next()) {
				return;
			}
			do {
				uuids.put(rs2.getString("uuid"), rs2.getString("player"));
				i++;
			} while (rs2.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		EtriaEconomy.log.info("Loaded " + i + " uuids");
	}
	
	public static void loadAccounts() {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM econ_players");
		int i = 0;
		try {
			if (!rs2.next()) {
				return;
			}
			do {
				accounts.put(rs2.getString("player"), rs2.getDouble("amount"));
				i++;
			} while (rs2.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		EtriaEconomy.log.info("Loaded " + i + " accounts.");
	}
	
	public static String formatNoColor(double amount) {
		return ChatColor.stripColor(format(amount));
	}
	
	public static String getAccount(String player) {
		for (String acc: accounts.keySet()) {
			if (acc.equalsIgnoreCase(player)) {
				return acc;
			}
		}
		return null;
	}
	
	private static String formatValue(double value) {
		boolean isWholeNumber = value == Math.round(value);
		
		DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		
		formatSymbols.setDecimalSeparator('.');
		String pattern = isWholeNumber ? "###,###,###" : "###,##0.00";
		
		
		DecimalFormat df = new DecimalFormat(pattern, formatSymbols);
		return df.format(value);
	}
	
	public static String format(double amount) {
		amount = getMoneyRounded(amount);
		String suffix = " ";
		
		if (amount == 1.0) {
			suffix += plugin.getConfig().getString("Settings.Currency.SingularName");
		} else {
			suffix += plugin.getConfig().getString("Settings.Currency.PluralName");
		}
		
		if (suffix.equalsIgnoreCase(" ")) {
			suffix = "";
		}
		
		return formatValue(amount) + suffix;
	}
	
	public static double getMoneyRounded(double amount) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		String formattedAmount = twoDForm.format(amount);
		formattedAmount = formattedAmount.replace(",", ".");
		
		return Double.valueOf(formattedAmount);
	}
	
	public static boolean deletePlayerAccount(String playerName) {
		Methods.accounts.remove(playerName);
		DBConnection.sql.modifyQuery("DELETE FROM econ_players WHERE player = '" + playerName + "'");
		return true;
	}
	
	public static void logTransaction(String accountName, double amount, String type, String other, String message) {
		if (!plugin.getConfig().getBoolean("Settings.LogTransactions")) return;
		message = message.replaceAll("'", "");
		DBConnection.sql.modifyQuery("INSERT INTO econ_transactions (date, player, amount, type, other, message) VALUES ('" + getCurrentDate() + "', '" + accountName + "', " + amount + ", '" + type + "', '" + other + "', '" + message + "')");
	}
	
	public static String buildString(String[] args, int begin) {
		StringBuilder mess = new StringBuilder();
		for (int i = begin; i < args.length; i++) {
			if (i > begin) {
				mess.append(" ");
			}
			mess.append(args[i]);
		}
		return mess.toString().trim();
	}
	
	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
