package com.etriacraft.EtriaEconomy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class API implements Economy {

	private EtriaEconomy plugin;

	public API (EtriaEconomy plugin) {
		this.plugin = plugin;
	}

	@Override
	public EconomyResponse bankBalance(String name) {
		return null;
	}

	@Override
	public EconomyResponse bankDeposit(String name, double amount) {
		return null;
	}

	@Override
	public EconomyResponse bankHas(String name, double amount) {
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String name, String playerName) {
		return null;
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		Methods.accounts.put(playerName, (double) 0);
		OfflinePlayer oP = Bukkit.getOfflinePlayer(playerName);
		double startingamount = plugin.getConfig().getDouble("Settings.Accounts.StartingAmount");
		if (oP == null || oP.getUniqueId() == null) {
			DBConnection.sql.modifyQuery("INSERT INTO econ_players(player, amount) VALUES ('" + playerName + "', " + startingamount + ");");
		}
		else {
			Methods.uuids.put(playerName, oP.getUniqueId().toString());
			DBConnection.sql.modifyQuery("INSERT INTO econ_players(uuid, player, amount) VALUES ('" + oP.getUniqueId().toString() + "', '" + playerName + "', " + startingamount + ");");
		}
		return true;
	}

	@Override
	public boolean createPlayerAccount(String playerName, String world) {
		return false;
	}

	@Override
	public String currencyNamePlural() {
		return plugin.getConfig().getString("Settings.Currency.PluralName");
	}

	@Override
	public String currencyNameSingular() {
		return plugin.getConfig().getString("Settings.Currency.SingularName");
	}

	@Override
	public EconomyResponse deleteBank(String name) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		String player = Methods.getAccount(playerName);
		if (amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds.");
		}

		if (!hasAccount(player)) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
		}


		double initamount = Methods.accounts.get(player);
		double newamount = initamount + amount;
		Methods.accounts.put(player, newamount);
		DBConnection.sql.modifyQuery("UPDATE econ_players SET amount = " + newamount + " WHERE player = '" + player + "';");
		return new EconomyResponse(amount, getBalance(player), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, String world, double amount) {
		return this.depositPlayer(playerName, amount);
	}

	@Override
	public String format(double amount) {
		return Methods.formatNoColor(amount);
	}

	@Override
	public int fractionalDigits() {
		return -1;
	}

	@Override
	public double getBalance(String playerName) {
		String player = Methods.getAccount(playerName);
		return Methods.accounts.get(player);
	}

	@Override
	public double getBalance(String playerName, String world) {
		return this.getBalance(playerName);
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public String getName() {
		return "EtriaEconomy";
	}

	@Override
	public boolean has(String playerName, double amount) {
		String player2 = Methods.getAccount(playerName);
		if (Methods.accounts.get(player2) >= amount) return true;
		return false;
	}

	@Override
	public boolean has(String playerName, String world, double amount) {
		return this.has(playerName, amount);
	}

	@Override
	public boolean hasAccount(String playerName) {
		for (String account: Methods.accounts.keySet()) {
			if (account.equalsIgnoreCase(playerName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasAccount(String playerName, String world) {
		return this.hasAccount(playerName);
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String name, String playerName) {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return plugin != null;
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		String player = Methods.getAccount(playerName);
		if (amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds.");
		}

		if (!hasAccount(player)) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist.");
		}

		if (!has(player, amount)) {
			return new EconomyResponse(0, getBalance(player), ResponseType.FAILURE, "Insufficient Funds.");
		} else {
			Double initamount = Methods.accounts.get(player);
			Double newamount = initamount - amount;
			Methods.accounts.put(player, newamount);
			DBConnection.sql.modifyQuery("UPDATE econ_players SET amount = " + newamount + " WHERE player = '" + player + "';");
			return new EconomyResponse(0, getBalance(player), ResponseType.SUCCESS, "");
		}

	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {
		return this.withdrawPlayer(playerName, amount);
	}

}
