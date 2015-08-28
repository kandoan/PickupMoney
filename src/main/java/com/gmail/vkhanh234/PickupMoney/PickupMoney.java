package com.gmail.vkhanh234.PickupMoney;

import com.darkblade12.particleeffect.ParticleEffect;
import com.gmail.vkhanh234.PickupMoney.Config.Blocks;
import com.gmail.vkhanh234.PickupMoney.Config.Entities;
import com.gmail.vkhanh234.PickupMoney.Config.Language;
import com.gmail.vkhanh234.PickupMoney.Listener.MergeListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
				 sendConsole(ChatColor.GREEN + "Newest version: " + ChatColor.RED + vers);
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
		 try {
			 Class.forName("org.bukkit.event.entity.ItemMergeEvent");
			 getServer().getPluginManager().registerEvents(new MergeListener(this), this);
		 } catch( ClassNotFoundException e ) {
			 preVer = true;
			 sendConsole(ChatColor.RED+"WARNING: Old Spigot version! Please update your Spigot for safe!");
		 }
		 getServer().getPluginManager().registerEvents(this, this);
	 }

	@Override
	public void onDisable() {
	        // TODO Insert logic to be performed when the plugin is disabled
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("PickupMoney.command")){
			sender.sendMessage(language.get("noPermission"));
		}
			if (args.length >= 1) {
				if (args[0].equals("reload")) {
					reloadConfig();
					initConfig();
					sender.sendMessage(language.get("reload"));
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
		sender.sendMessage(ChatColor.GREEN+"Reload - "+ ChatColor.AQUA+"/pickupmoney reload");
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e){
		Item item = e.getItem();
		String name = item.getCustomName();
		if(name!=null && language.get("nameSyntax").replace("{money}", "").equals(name.replaceAll(regex, ""))){
			e.setCancelled(true);
			String money = getMoney(name);
			Player p = e.getPlayer();
			if(p.hasPermission("PickupMoney.pickup")) {
				item.remove();
				giveMoney(Float.parseFloat(money), p);
				p.sendMessage(language.get("pickup").replace("{money}", money));
				if(fc.getBoolean("sound.enable")){
					p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(fc.getString("sound.type"))
							, (float) fc.getDouble("sound.volumn")
							, (float) fc.getDouble("sound.pitch"));
				}
			}
		}
	}


	@EventHandler
	public void onDeath(EntityDeathEvent e){
		if(fc.getBoolean("enableEntitiesDrop")) {
			if(e.getEntity().getKiller()!=null && e.getEntity().getKiller() instanceof Player) {
				Entity entity = e.getEntity();
				if (!checkWorld(entity.getLocation())) return;
				String name = entity.getType().toString();
				if (entities.contain(name) && entities.getEnable(name) && KUtils.getSuccess(entities.getChance(name))) {
					if(entity instanceof Player) {
						Player p = (Player) entity;
						for (int i = 0; i < KUtils.getRandomInt(entities.getAmount(name)); i++) {
							float money = getMoneyOfPlayer((Player) entity, entities.getMoney(name));
							if(entities.getCost(name)){
								costMoney(money,p);
								p.sendMessage(language.get("dropOut").replace("{money}",String.valueOf(money)));
							}
							spawnMoney(money,entity.getLocation());
						}
					}
					else{
						for (int i = 0; i < KUtils.getRandomInt(entities.getAmount(name)); i++) {
							spawnMoney(KUtils.getRandom(entities.getMoney(name)), entity.getLocation());
						}
					}
					spawnParticle(entity.getLocation());
				}
			}
		}
	}

	private boolean checkWorld(Location location) {
		if(fc.getList("disableWorld").contains(location.getWorld().getName())) return false;
		return true;
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e){
		if(fc.getBoolean("enableBlocksDrop")) {
			Block block = e.getBlock();
			if (!checkWorld(block.getLocation())) return;
			String name = block.getType().toString();
			if (blocks.contain(name) && blocks.getEnable(name) && KUtils.getSuccess(blocks.getChance(name))) {
				for (int i = 0; i < KUtils.getRandomInt(blocks.getAmount(name)); i++) {
					spawnMoney(KUtils.getRandom(blocks.getMoney(name)), block.getLocation());
				}
				spawnParticle(block.getLocation());
			}
		}
	}
	private float getMoneyOfPlayer(Player p, String val){
		if (val.contains("%")){
			String s = val.replace("%","");
			int percent = KUtils.getRandomInt(s);
			return Double.valueOf(economy.getBalance(p)).floatValue()*percent/100;
		}
		else return KUtils.getRandom(val);
	}
	private String getMoney(String name) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(name);
		if(matcher.find()) return matcher.group(0);
		return "0";
	}

	private void giveMoney(float amount, Player p) {
		economy.depositPlayer(p, amount);
	}
	private boolean costMoney(float amount, Player p){
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
	public ItemStack getItem(int money){
		if(money<fc.getInt("item.small.amount")){
			return new ItemStack(Material.getMaterial(fc.getString("item.small.type")),1);
		}
		else if(money<fc.getInt("item.normal.amount")){
			return new ItemStack(Material.getMaterial(fc.getString("item.normal.type")),1);
		}
		else{
			return new ItemStack(Material.getMaterial(fc.getString("item.big.type")),1);
		}
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