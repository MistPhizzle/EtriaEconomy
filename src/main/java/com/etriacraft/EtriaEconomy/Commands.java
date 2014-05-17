package com.etriacraft.EtriaEconomy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class Commands {

	EtriaEconomy plugin;

	public Commands(EtriaEconomy plugin) {
		this.plugin = plugin;
		init();
	}

	String[] helpaliases = {"help", "?", "h"};
	String[] createaliases = {"create", "new", "c"};
	String[] givealiases = {"give", "grant", "g"};
	String[] takealiases = {"take", "t"};
	String[] sendaliases = {"send", "pay"};
	String[] deletealiases = {"delete", "remove", "d"};
	String[] interestaliases = {"interest", "i"};

	public String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "EtriaEconomy" + ChatColor.GRAY + "] ";

	private void init() {
		PluginCommand money = plugin.getCommand("money");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) { // /money
					if (!plugin.getAPI().hasAccount(s.getName())) {
						s.sendMessage(prefix + " §cYou do not have an account. Try relogging to create one.");
						return true;
					}
					String message = Methods.format(plugin.getAPI().getBalance(s.getName()));
					s.sendMessage(prefix + "§aYou have §3" + message +"§a.");
					return true;
				}
				if (args.length == 1) {
					if (Arrays.asList(helpaliases).contains(args[0])) { // They are using /money help.
						s.sendMessage("§a-----§6EtriaEconomy Commands§a-----");
						s.sendMessage("§3/money [Player]§f - View balance.");
						s.sendMessage("§3/money create [Player]§f - Create an account for another player.");
						s.sendMessage("§3/money give [Player] [Amount]§f - Add money to a player's account.");
						s.sendMessage("§3/money take [Player] [Amount]§f - Take money from a player's account.");
						s.sendMessage("§3/money delete [Player]§f - Delete a player's account.");
						s.sendMessage("§3/money send [Player] [Amount] <Message>§f - Send money to a player.");
						s.sendMessage("§3/money top [#]§f - View top ranking players by wealth.");
						return true;
					}
					if (Arrays.asList(interestaliases).contains(args[0])) {
						s.sendMessage(prefix + "§cProper Usage: §3/money interest [Compound|Info]");
						return true;
					}
					if (Arrays.asList(sendaliases).contains(args[0])) {
						s.sendMessage(prefix + "§cProper Usage: §3/money send [Player] [Amount] <Message>");
						return true;
					}
					if (Arrays.asList(createaliases).contains(args[0])) {
						s.sendMessage(prefix + "§cProper Usage: §3/money create [AccountName]");
						return true;
					}
					if (Arrays.asList(givealiases).contains(args[0])) {
						s.sendMessage(prefix + "§cProper Usage: §3/money give [Player] [Amount]");
						return true;
					}
					if (Arrays.asList(takealiases).contains(args[0])) {
						s.sendMessage(prefix + "§cProper Usage: §3/money take [Player] [Amount]");
						return true;
					}
					if (Arrays.asList(deletealiases).contains(args[0])) {
						s.sendMessage(prefix + "§cProper Usage: §3/money delete [Player]");
						return true;
					}
					if (args[0].equalsIgnoreCase("top")) {
						if (!s.hasPermission("etriaeconomy.money.top")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}

						int number = 5;
						ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM econ_players ORDER BY amount DESC LIMIT " + number + ";");
						try {
							if (!rs2.next()) {
								s.sendMessage(prefix + "§cNo accounts found.");
								return true;
							}
							s.sendMessage("§a-----§6EtriaEconomy Richest§a-----");
							int i = 0;
							do {
								i++;
								s.sendMessage("§2" + i + "§a: " + rs2.getString("player") + " - " + Methods.format(rs2.getDouble("amount")));
							} while (rs2.next());
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return true;
					}
					else {
						// They are looking up money of another player.
						if (!s.hasPermission("etriaeconomy.balance.other")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}
						String player = args[0];
						if (!plugin.getAPI().hasAccount(player)) {
							s.sendMessage(prefix + "§cNo account found for §3" + player);
							return true;
						}
						s.sendMessage(prefix + "§3" + player + " §ahas " + Methods.format(plugin.getAPI().getBalance(player)));
						return true;
					}
				}
				if (args.length == 2) {
					if (Arrays.asList(interestaliases).contains(args[0])) {
						if (args[1].equalsIgnoreCase("compound")) {
							if (!s.hasPermission("etriaeconomy.money.interest.compound")) {
								s.sendMessage(prefix + "§cYou don't have permission to do that.");
								return true;
							}
							if (!plugin.getConfig().getBoolean("Settings.Interest.Enabled")) {
								s.sendMessage(prefix + "§cInterest is disabled.");
								return true;
							}
							String type = null;
							if (plugin.getConfig().getString("Settings.Interest.Type").equalsIgnoreCase("bracket")) {
								type = "bracket";
							} else {
								type = "flat";
							}
							
							if (type.equalsIgnoreCase("flat")) {
								double rate = plugin.getConfig().getDouble("Settings.Interest.Flat.Rate");
								double total = 0;
								int accounts = 0;
								for (String accountName: Methods.accounts.keySet()) {
									if (plugin.getAPI().getBalance(accountName) == 0) continue;
									if (accountName.equalsIgnoreCase(plugin.getConfig().getString("Settings.Accounts.ServerAccount"))) continue;
									double interest = plugin.getAPI().getBalance(accountName) * rate;
									plugin.getAPI().depositPlayer(accountName, interest);
									total = total + interest;
									accounts++;
								}
								if (plugin.getConfig().getBoolean("Settings.Interest.Flat.SubtractFromServerAccount")) {
									s.sendMessage(prefix + "§aSubtracted " + Methods.formatNoColor(total) + " from " + plugin.getConfig().getString("Settings.Accounts.ServerAccount"));
									plugin.getAPI().withdrawPlayer(plugin.getConfig().getString("Settings.Accounts.ServerAccount"), total);
								}
								s.sendMessage(prefix + "§aInterest compounded to " + accounts + " accounts.");
							}
							if (type.equalsIgnoreCase("bracket")) {
								double total = 0;
								int accounts = 0;
								boolean useServerAverage = plugin.getConfig().getBoolean("Settings.Interest.Bracket.Factor.UseServerAverage");
								double average = 0;
								double totalmoney = 0;
								if (useServerAverage) {
									for (String accountName: Methods.accounts.keySet()) {
										if (accountName.equalsIgnoreCase(plugin.getConfig().getString("Settings.Accounts.ServerAccount"))) continue;
										if (plugin.getAPI().getBalance(accountName) == 0) continue;
										totalmoney = totalmoney + plugin.getAPI().getBalance(accountName);
										accounts++;
									}
									average = totalmoney / accounts;
									
									for (String accountName: Methods.accounts.keySet()) {
										if (accountName.equalsIgnoreCase(plugin.getConfig().getString("Settings.Accounts.ServerAccount"))) continue;
										if (plugin.getAPI().getBalance(accountName) == 0) continue;
										if (plugin.getAPI().getBalance(accountName) > average) {
											double interest = plugin.getAPI().getBalance(accountName) * plugin.getConfig().getDouble("Settings.Interest.Bracket.High.Rate");
											total = total + interest;
											plugin.getAPI().depositPlayer(accountName, interest);
										} else {
											double interest = plugin.getAPI().getBalance(accountName) * plugin.getConfig().getDouble("Settings.Interest.Bracket.Low.Rate");
											total = total + interest;
											plugin.getAPI().depositPlayer(accountName, interest);
										}
									}
									s.sendMessage(prefix + "§aInterest compounded to " + accounts + " accounts.");
									if (plugin.getConfig().getBoolean("Settings.Interest.Bracket.SubtractFromServerAccount")) {
										plugin.getAPI().withdrawPlayer(plugin.getConfig().getString("Settings.Accounts.ServerAccount"), total);
										s.sendMessage(prefix + "§aSubtracted " + Methods.formatNoColor(total) + " from " + plugin.getConfig().getString("Settings.Accounts.ServerAccount"));
									}
									return true;
								}
								if (!useServerAverage) {
									double minimum = plugin.getConfig().getDouble("Settings.Interest.Bracket.High.Minimum");
									for (String accountName: Methods.accounts.keySet()) {
										if (accountName.equalsIgnoreCase(plugin.getConfig().getString("Settings.Accounts.ServerAccount"))) continue;
										if (plugin.getAPI().getBalance(accountName) == 0) continue;
										if (plugin.getAPI().getBalance(accountName) >= minimum) {
											double interest = plugin.getAPI().getBalance(accountName) * plugin.getConfig().getDouble("Settings.Interest.Bracket.High.Rate");
											total = total + interest;
											plugin.getAPI().depositPlayer(accountName, interest);
											accounts++;
										} else {
											double interest = plugin.getAPI().getBalance(accountName) * plugin.getConfig().getDouble("Settings.Interest.Bracket.Low.Rate");
											total = total + interest;
											plugin.getAPI().depositPlayer(accountName, interest);
											accounts++;
										}
									}
									
									s.sendMessage(prefix + "§aInterest compounded to " + accounts + " accounts");
									if (plugin.getConfig().getBoolean("Settings.Interest.Bracket.SubtractFromServerAccount")) {
										plugin.getAPI().withdrawPlayer(plugin.getConfig().getString("Settings.Accounts.ServerAccount"), total);
										s.sendMessage(prefix + "§aSubtracted " + Methods.formatNoColor(total) + " from " + plugin.getConfig().getString("Settings.Accounts.ServerAccount"));
									}
									return true;
								}
							}
						}
						if (args[1].equalsIgnoreCase("info")) {
							if (!plugin.getConfig().getBoolean("Settings.Interest.Enabled")) {
								s.sendMessage(prefix + "§cInterest is disabled.");
								return true;
							}
							if (!s.hasPermission("etriaeconomy.money.interest.info")) {
								s.sendMessage(prefix + "§cYou don't have permission to do that.");
								return true;
							}
							String type = null;
							if (plugin.getConfig().getString("Settings.Interest.Type").equalsIgnoreCase("bracket")) {
								type = "bracket";
							} else {
								type = "flat";
							}
							
							if (type.equalsIgnoreCase("flat")) {
								double rate = plugin.getConfig().getDouble("Settings.Interest.Flat.Rate");
								s.sendMessage(prefix + "§aType: §3Flat Interest Rate");
								s.sendMessage(prefix + "§aInterest Rate: §3" + rate * 100 + "%");
								s.sendMessage(prefix + "§aIf interest were compounded right now you would receive: §3" + Methods.formatNoColor(rate * plugin.getAPI().getBalance(s.getName())));
							} else {
								boolean useServerAverage = plugin.getConfig().getBoolean("Settings.Interest.Bracket.Factor.UseServerAverage");
								s.sendMessage(prefix + "§aType: §3Progressive Interest Rate");
								double average = 0;
								int accounts = 0;
								double totalmoney = 0;
								double rate = 0;
								if (useServerAverage) {
									for (String accountName: Methods.accounts.keySet()) {
										if (accountName.equalsIgnoreCase(plugin.getConfig().getString("Settings.Accounts.ServerAccount"))) continue;
										if (plugin.getAPI().getBalance(accountName) == 0) continue;
										totalmoney = totalmoney + plugin.getAPI().getBalance(accountName);
										accounts++;
									}
									average = totalmoney / accounts;
									if (plugin.getAPI().getBalance(s.getName()) <= average) {
										s.sendMessage("§aBracket: §3LOW §a(You Have Less Than Average)");
										rate = plugin.getConfig().getDouble("Settings.Interest.Bracket.Low.Rate");
										s.sendMessage(prefix + "§aInterest Rate: §3" + rate * 100 + "%");
									} else {
										s.sendMessage("§aBracket: §3HIGH §a(You Have More Than Average)");
										rate = plugin.getConfig().getDouble("Settings.Interest.Bracket.High.Rate");
										s.sendMessage(prefix + "§aInterest Rate: §3" + rate * 100 + "%");
									}
									s.sendMessage(prefix + "§aIf interest were compounded right now you would receive: §3" + Methods.formatNoColor(rate * plugin.getAPI().getBalance(s.getName())));
									return true;
								} else {
									double minimum = plugin.getConfig().getDouble("Settings.Interest.Bracket.High.Minimum");
									if (plugin.getAPI().getBalance(s.getName()) >= minimum) { // High
										s.sendMessage("§aBracket: §3High §a(You have more than " + minimum + ")");
										rate = plugin.getConfig().getDouble("Settings.Interest.Bracket.High.Rate");
										s.sendMessage(prefix + "§aInterest Rate: §3" + rate * 100 + "%");
									} else {
										s.sendMessage("§aBracket: §3Low §a(You have less than " + minimum + ")");
										rate = plugin.getConfig().getDouble("Settings.Interest.Bracket.Low.Rate");
										s.sendMessage(prefix + "§aInterest Rate: §3" + rate * 100 + "%");
									}
									s.sendMessage(prefix + "§aIf interest were compounded right now you would receive: §3" + Methods.formatNoColor(rate * plugin.getAPI().getBalance(s.getName())));
									return true;
								}
							}
						} else {
							s.sendMessage("§cProper Usage: §3/money interest [Compound|Info]");
							return true;
						}
					}
					if (Arrays.asList(deletealiases).contains(args[0])) {
						if (!s.hasPermission("etriaeconomy.money.delete")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}
						
						String account = Methods.getAccount(args[1]);
						if (!plugin.getAPI().hasAccount(account)) {
							s.sendMessage(prefix + "§cCannot find account specified.");
							return true;
						}
						
						double balance = plugin.getAPI().getBalance(account);
						if (plugin.getConfig().getBoolean("Settings.Accounts.AddBalanceToServerAccountOnDelete")) {
							plugin.getAPI().depositPlayer(plugin.getConfig().getString("Settings.Accounts.ServerAccount"), balance);
						}
						Methods.deletePlayerAccount(account);
						s.sendMessage("§cDeleted account of " + args[1]);
					}
					if (Arrays.asList(createaliases).contains(args[0])) {
						// Creating an account.
						if (!s.hasPermission("etriaeconomy.create")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}

						if (plugin.getAPI().hasAccount(args[1])) {
							s.sendMessage(prefix + "§cAn account with that name already exists.");
							return true;
						}

						plugin.getAPI().createPlayerAccount(args[1]);
						s.sendMessage(prefix + "§aYou have created an account for: §3" + args[1]);
						return true;
					}
					if (args[0].equalsIgnoreCase("top")) {
						if (!s.hasPermission("etriaeconomy.money.top")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}

						int number = Integer.parseInt(args[1]);
						ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM econ_players ORDER BY amount DESC LIMIT " + number + ";");
						try {
							if (!rs2.next()) {
								s.sendMessage(prefix + "§cNo accounts found.");
								return true;
							}
							s.sendMessage("§a-----§6EtriaEconomy Richest§a-----");
							int i = 0;
							do {
								i++;
								s.sendMessage("§2" + i + "§a: " + rs2.getString("player") + " - " + Methods.format(rs2.getDouble("amount")));
							} while (rs2.next());
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return true;
					}
				}
				
				if (args.length == 3) {
					if (Arrays.asList(givealiases).contains(args[0])) {
						if (!s.hasPermission("etriaeconomy.money.give")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}
						double amount = Double.parseDouble(args[2]);
						String player = args[1];

						if (!plugin.getAPI().hasAccount(player)) {
							s.sendMessage(prefix + "§cNo account found for §3" + player);
							return true;
						}

						if (amount < 0) {
							s.sendMessage(prefix + "§cYou may not grant negative numbers.");
							return true;
						}
						plugin.getAPI().depositPlayer(player, amount);
						s.sendMessage("§cYou have given §3" + Methods.format(amount) + " §cto §3" + player);
						return true;
					}
					if (Arrays.asList(takealiases).contains(args[0])) {
						if (!s.hasPermission("etriaeconomy.money.take")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}
						double amount = Double.parseDouble(args[2]);
						String player = args[1];

						if (!plugin.getAPI().hasAccount(player)) {
							s.sendMessage(prefix + "§cNo account found for §3" + player);
							return true;
						}

						if (amount < 0) {
							s.sendMessage(prefix + "§cYou cannot take a negative amount.");
							return true;
						}

						if (plugin.getAPI().getBalance(player) < amount) {
							s.sendMessage(prefix + "§cYou cannot take more than the player has.");
							return true;
						}

						plugin.getAPI().withdrawPlayer(player, amount);
						s.sendMessage("§cYou have taken §3" + Methods.format(amount) + " §cfrom §3" + player);
						return true;

					}
					if (Arrays.asList(sendaliases).contains(args[0])) {
						if (!s.hasPermission("etriaeconomy.money.send")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}
						double amount = Double.parseDouble(args[2]);
						String target = args[1];

						if (!plugin.getAPI().hasAccount(target)) {
							s.sendMessage(prefix + "§cNo account found for §3" + target);
							return true;
						}

						if (!plugin.getAPI().hasAccount(s.getName())) {
							s.sendMessage(prefix + "§cYou don't have an account to send money from.");
							return true;
						}
						if (amount < 0) {
							s.sendMessage(prefix + "§cYou cannot send a negative amount.");
							return true;
						}

						if (amount > plugin.getAPI().getBalance(s.getName())) {
							s.sendMessage(prefix + "§cYou cannot send more money than you have.");
							return true;
						}

						plugin.getAPI().depositPlayer(target, amount);
						plugin.getAPI().withdrawPlayer(s.getName(), amount);

						Methods.logTransaction(target, amount, "RECEIVED", s.getName(), null);
						Methods.logTransaction(s.getName(), amount, "SENT", s.getName(), null);
						s.sendMessage(prefix + "§aYou have sent §3" + Methods.format(amount) + "§a to §3" + target);
						for (Player player: Bukkit.getOnlinePlayers()) {
							if (player.getName().equalsIgnoreCase(target)) {
								player.sendMessage(prefix + "§aYou have received §3" + Methods.format(amount) + "§a from §3" + s.getName());
							}
						}
						return true;
					}
				}
				if (args.length == 4) {
					if (Arrays.asList(sendaliases).contains(args[0])) {
						if (!s.hasPermission("etriaeconomy.money.send")) {
							s.sendMessage(prefix + "§cYou don't have permission to do that.");
							return true;
						}
						double amount = Double.parseDouble(args[2]);
						String target = args[1];
						
						if (!plugin.getAPI().hasAccount(target)) {
							s.sendMessage(prefix + "§cNo account found for §3" + target);
							return true;
						}
						
						if (!plugin.getAPI().hasAccount(s.getName())) {
							s.sendMessage(prefix + "§cYou don't have an account to send money from.");
							return true;
						}
						
						if (amount > plugin.getAPI().getBalance(s.getName())) {
							s.sendMessage(prefix + "§cYou cannot send more money than you have.");
							return true;
						}
						
						plugin.getAPI().depositPlayer(target, amount);
						plugin.getAPI().withdrawPlayer(s.getName(), amount);
						
						String message = Methods.buildString(args, 3);
						Methods.logTransaction(target, amount, "RECEIVED", s.getName(), message);
						Methods.logTransaction(s.getName(), amount, "SENT", s.getName(), message);
						s.sendMessage(prefix + "§aYou have sent §3" + Methods.format(amount) + "§a to §3" + target + "§a for §3" + message);
						for (Player player: Bukkit.getOnlinePlayers()) {
							if (player.getName().equalsIgnoreCase(target)) {
								player.sendMessage(prefix + "§aYou have received §3" + Methods.format(amount) + "§a from §3" + s.getName() + "§a for §3" + message);
							}
						}
						return true;
					}
				}
				else {
					s.sendMessage(prefix + "§cYou have not entered a valid economy command. Use §6/money ?§c for more info.");
				}
				return true;
			}
		}; money.setExecutor(exe);
	}

}
