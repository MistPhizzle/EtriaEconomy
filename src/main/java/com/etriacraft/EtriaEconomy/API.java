package com.etriacraft.EtriaEconomy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class API implements Economy {

	EtriaEconomy plugin;

	private final String name = "EtriaEconomy";

	public API (EtriaEconomy plugin) {
		this.plugin = plugin;
	}

	@Override
	public EconomyResponse bankBalance(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return null;
	}

	@Override
	public boolean createPlayerAccount(String player) {
		Methods.accounts.put(player, (double) 0);
		OfflinePlayer oP = Bukkit.getOfflinePlayer(player);
		if (oP == null || oP.getUniqueId() == null) {
			DBConnection.sql.modifyQuery("INSERT INTO econ_players(player, amount) VALUES ('" + player + "', " + 0 + ");");
		}
		else {
			Methods.uuids.put(player, oP.getUniqueId().toString());
			DBConnection.sql.modifyQuery("INSERT INTO econ_players(uuid, player, amount) VALUES ('" + oP.getUniqueId().toString() + "', '" + player + "', " + 0 + ");");
		}
		return true;
	}

	@Override
	public boolean createPlayerAccount(String player, String world) {
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
	public EconomyResponse deleteBank(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String acc, double amount) {
		String player = Methods.getAccount(acc);
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
	public EconomyResponse depositPlayer(String player, String world, double amount) {
		return this.depositPlayer(player, amount);
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
	public double getBalance(String acc) {
		String player = Methods.getAccount(acc);
		return Methods.accounts.get(player);
	}

	@Override
	public double getBalance(String acc, String world) {
		return this.getBalance(acc);
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean has(String player, double amount) {
		String player2 = Methods.getAccount(player);
		if (Methods.accounts.get(player2) >= amount) return true;
		return false;
	}

	@Override
	public boolean has(String player, String world, double amount) {
		return this.has(player, amount);
	}

	@Override
	public boolean hasAccount(String player) {
		for (String account: Methods.accounts.keySet()) {
			if (account.equalsIgnoreCase(player)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasAccount(String player, String world) {
		return this.hasAccount(player);
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return plugin != null;
	}

	@Override
	public EconomyResponse withdrawPlayer(String acc, double amount) {
		String player = Methods.getAccount(acc);
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
	public EconomyResponse withdrawPlayer(String acc, String world, double amount) {
		return null;
	}

}
