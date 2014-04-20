package com.etriacraft.EtriaEconomy;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.java.JavaPlugin;

public class EtriaEconomy extends JavaPlugin {

	public static EtriaEconomy plugin;
	protected static Logger log;
	private API api;
	
	@Override
	public void onEnable() {
		EtriaEconomy.log = this.getLogger();
		plugin = this;		
		checkConfig();
		
		DBConnection.host = getConfig().getString("Storage.MySQL.host");
		DBConnection.port = getConfig().getInt("Storage.MySQL.port");
		DBConnection.pass = getConfig().getString("Storage.MySQL.pass");
		DBConnection.db = getConfig().getString("Storage.MySQL.db");
		DBConnection.user = getConfig().getString("Storage.MySQL.user");
		
		DBConnection.init();
		new Methods(this);
		setupVault();
		api = new API(this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		Methods.loadAccounts();
		Methods.loadUUIDs();
		new Commands(this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public static void checkConfig() {
		plugin.getConfig().addDefault("Storage.engine", "sqlite");
		
		plugin.getConfig().addDefault("Storage.MySQL.host", "localhost");
		plugin.getConfig().addDefault("Storage.MySQL.port", 3306);
		plugin.getConfig().addDefault("Storage.MySQL.user", "root");
		plugin.getConfig().addDefault("Storage.MySQL.pass", "");
		plugin.getConfig().addDefault("Storage.MySQL.db", "minecraft");
		
		plugin.getConfig().addDefault("Settings.Currency.SingularName", "Coin");
		plugin.getConfig().addDefault("Settings.Currency.PluralName", "Coins");
		
		plugin.getConfig().addDefault("Settings.Accounts.StartingAmount", 0);
		
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
	}
	
	private void setupVault() {
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if (vault == null) return;
		
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		
		if (economyProvider != null) {
			getServer().getServicesManager().unregister(economyProvider.getProvider());
		}
		
		plugin.getServer().getServicesManager().register(Economy.class, new API(this), this, ServicePriority.Highest);
	}
	
	public API getAPI() {
		return api;
	}

}
