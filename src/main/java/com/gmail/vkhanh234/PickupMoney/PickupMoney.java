package com.gmail.vkhanh234.PickupMoney;

import com.darkblade12.particleeffect.ParticleEffect;
import com.gmail.vkhanh234.PickupMoney.Config.Blocks;
import com.gmail.vkhanh234.PickupMoney.Config.Entities;
import com.gmail.vkhanh234.PickupMoney.Config.Language;
import com.gmail.vkhanh234.PickupMoney.Listener.MainListener;
import com.gmail.vkhanh234.PickupMoney.Listener.MythicMobsListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PickupMoney extends JavaPlugin implements Listener {
	public static FileConfiguration fc;
	public static Economy economy = null;
	public Entities entities = new Entities(this);
	public Language language = new Language(this);
	public Blocks blocks = new Blocks(this);
	String version = getDescription().getVersion();
	ConsoleCommandSender console = getServer().getConsoleSender();
	private String prefix = "[PickupMoney] ";
	private boolean preVer = false;
	public List<UUID> spawners = new ArrayList<>();
	public String regex="[0-9]+\\.[0-9]+";

	{
		loadConfiguration();
		initConfig();
	}
	 @Override
	 public void onEnable() {
		 if (fc.getBoolean("notiUpdate")) {
			 sendConsole(ChatColor.GREEN + "Current version: " + ChatColor.AQUA + version);
			 String vers = getNewestVersion();
			 if (vers != null) {
				 sendConsole(ChatColor.GREEN + "Latest version: " + ChatColor.RED + vers);
				 if (!vers.equals(version)) {
					 sendConsole(ChatColor.RED + "There is a new version on Spigot!");
					 sendConsole(ChatColor.RED + "https://www.spigotmc.org/resources/11334/");
				 }
			 }
		 }
		 if(!getServer().getPluginManager().isPluginEnabled("Vault")){
			 sendConsole("Vault is not installed or not enabled. ");
			 sendConsole("This plugin will be disabled.");
			 getServer().getPluginManager().disablePlugin(this);
			 return;
		 }
		 String[] bukkver = getServer().getBukkitVersion().split("\\.");
		 if(Integer.parseInt(bukkver[1].substring(0,1))<8){
			 sendConsole("Server version is too old. Please update!");
			 sendConsole("This plugin will be disabled.");
			 getServer().getPluginManager().disablePlugin(this);
			 return;
		 }
		 if (!setupEconomy() ) {
			 getLogger().info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			 getServer().getPluginManager().disablePlugin(this);
			 return;
		 }
		 getServer().getPluginManager().registerEvents(new MainListener(this), this);
		 try{
			 Class.forName("net.elseland.xikage.MythicMobs.API.Bukkit.Events.MythicMobDeathEvent");
			 getServer().getPluginManager().registerEvents(new MythicMobsListener(this), this);
		 } catch (ClassNotFoundException e) {
		 }
	 }

	@Override
	public void onDisable() {
	        // TODO Insert logic to be performed when the plugin is disabled
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("PickupMoney.command")){
			sender.sendMessage(language.get("noPermission"));
			return true;
		}
			if (args.length >= 1) {
				try {
					if (args[0].equals("reload") && sender.hasPermission("PickupMoney.admincmd")) {
						reloadConfig();
						initConfig();
						sender.sendMessage(language.get("reload"));
					}
					else if (args[0].equals("drop") && sender instanceof Player && args.length == 2) {
						Player p = (Player) sender;
						float money = KUtils.getRandom(args[1]);
						if(money<fc.getInt("minimumCmdDrop")){
							p.sendMessage(language.get("miniumCmdDrop").replace("{money}",String.valueOf(fc.getInt("minimumCmdDrop"))));
							return true;
						}
						Set<Material> set = null;
						Block b = p.getTargetBlock(set, 6);
						if (costMoney(money, p)) {
							spawnMoney(money, b.getLocation());
						} else {
							p.sendMessage(language.get("noMoney"));
						}
					} else showHelp(sender);
				}
				catch (Exception e){
					showHelp(sender);
				}
			}
		else{
				showHelp(sender);
			}
		return true;
	}
	public void sendConsole(String s){
		console.sendMessage(prefix+s);
	}
	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+"PickupMoney version "+version);
		if(sender.hasPermission("PickupMoney.admincmd")) sender.sendMessage(ChatColor.GREEN+"Reload - "+ ChatColor.AQUA+"/pickupmoney reload");
		sender.sendMessage(ChatColor.GREEN+"Drop Money - "+ ChatColor.AQUA+"/pickupmoney drop <amount>");
	}


	public float getMoneyOfPlayer(Player p, String val){
		if (val.contains("%")){
			String s = val.replace("%","");
			int percent = KUtils.getRandomInt(s);
			return Double.valueOf(economy.getBalance(p)).floatValue()*percent/100;
		}
		else return KUtils.getRandom(val);
	}
	public String getMoney(String name) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(name);
		if(matcher.find()) return matcher.group(0);
		return "0";
	}

	public void giveMoney(float amount, Player p) {
		economy.depositPlayer(p, amount);
	}
	public boolean costMoney(float amount, Player p){
		if(economy.getBalance(p)>=amount){
			economy.withdrawPlayer(p,amount);
			return true;
		}
		return false;
	}
	public void spawnMoney(float money,Location l){
		Item item = l.getWorld().dropItemNaturally(l, getItem(Float.valueOf(money).intValue()));
		String m = String.valueOf(money);
		if (!m.contains(".")) m=m+".0";
		item.setCustomName(language.get("nameSyntax").replace("{money}", m));
		item.setCustomNameVisible(true);
	}
	public void spawnParticle(Location l){
		if (fc.getBoolean("particle.enable")) {
			ParticleEffect.fromName(fc.getString("particle.type")).display((float) 0.5, (float) 0.5, (float) 0.5, 1, fc.getInt("particle.amount"), l, 20);
		}
	}
	public boolean checkWorld(Location location) {
		if(fc.getList("disableWorld").contains(location.getWorld().getName())) return false;
		return true;
	}
	public ItemStack getItem(int money){
		ItemStack item;
		if(money<fc.getInt("item.small.amount")){
			item = new ItemStack(Material.getMaterial(fc.getString("item.small.type")),1);
		}
		else if(money<fc.getInt("item.normal.amount")){
			item =  new ItemStack(Material.getMaterial(fc.getString("item.normal.type")),1);
		}
		else{
			item =  new ItemStack(Material.getMaterial(fc.getString("item.big.type")),1);
		}
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add(String.valueOf(KUtils.getRandomInt(1,100000000)));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	private void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		getConfig().options().copyDefaults(false);
	}
	private void initConfig(){
		fc = getConfig();
		language = new Language(this);
		entities = new Entities(this);
	}
	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}
	public static String getMessage(String type) {
		return KUtils.convertColor(fc.getString("Message."+type));
	}
	private String getNewestVersion() {
		try {
			URL url = new URL("https://dl.dropboxusercontent.com/s/a890l19kn0fv32l/PickupMoney.txt");
			URLConnection con = url.openConnection();
			con.setConnectTimeout(2000);
			con.setReadTimeout(1000);
			InputStream in = con.getInputStream();
			return getStringFromInputStream(in);
		}
		catch(IOException ex) {
			sendConsole(ChatColor.RED+"Failed to check for update!");
		}
		return null;

	}
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
}