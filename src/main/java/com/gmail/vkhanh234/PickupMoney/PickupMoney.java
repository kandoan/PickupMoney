package com.gmail.vkhanh234.PickupMoney;

import com.darkblade12.particleeffect.ParticleEffect;
import com.gmail.vkhanh234.PickupMoney.Config.Blocks;
import com.gmail.vkhanh234.PickupMoney.Config.Entities;
import com.gmail.vkhanh234.PickupMoney.Config.Language;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PickupMoney extends JavaPlugin implements Listener {
	public static FileConfiguration fc;
	public static Economy economy = null;
	public Entities entities = new Entities(this);
	public Language language = new Language(this);
	public Blocks blocks = new Blocks(this);
	String version = "1.0.0";
	{
		loadConfiguration();
		initConfig();
	}
	 @Override
	 public void onEnable() {
		 String ver = getServer().getBukkitVersion();
		 if(Integer.parseInt(ver.split("\\.")[1])<8){
			 getLogger().info("Server version is too old. Please update!");
			 getLogger().info("This plugin will be disabled.");
			 getServer().getPluginManager().disablePlugin(this);
			 return;
		 }

		 if(!getServer().getPluginManager().isPluginEnabled("Vault")){
			 getLogger().info("Vault is not installed or not enabled. ");
			 getLogger().info("This plugin will be disabled.");
			 getServer().getPluginManager().disablePlugin(this);
			 return;
		 }
		 if (!setupEconomy() ) {
			 getLogger().info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			 getServer().getPluginManager().disablePlugin(this);
			 return;
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

	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+"PickupMoney version "+version);
		sender.sendMessage(ChatColor.GREEN+"Reload - "+ ChatColor.AQUA+"/pickupmoney reload");
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e){
		Item item = e.getItem();
		String name = item.getName();
		if(language.get("nameSyntax").replace("{money}", "").equals(name.replaceAll("[0-9]+\\.[0-9][0-9]",""))){
			String money = getMoney(name);
			Player p = e.getPlayer();
			if(p.hasPermission("PickupMoney.pickup")) {
				giveMoney(Float.parseFloat(money), p);
				p.sendMessage(language.get("pickup").replace("{money}", money));
				e.setCancelled(true);
				item.remove();
			}
			if(fc.getBoolean("sound.enable")){
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(fc.getString("sound.type"))
						, (float) fc.getDouble("sound.volumn")
						, (float) fc.getDouble("sound.pitch"));
			}
		}
	}

	@EventHandler
	public void onMerge(ItemMergeEvent e){
		Item item = e.getEntity();
		if(item.getCustomName()!=null && language.get("nameSyntax").replace("{money}", "").equals(item.getCustomName().replaceAll("[0-9]+\\.[0-9][0-9]",""))){
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void onDeath(EntityDeathEvent e){
		if(fc.getBoolean("enableEntitiesDrop")) {
			Entity entity = e.getEntity();
			String name = entity.getType().toString();
			if (entities.contain(name) && entities.getEnable(name) && KUtils.getSuccess(entities.getChance(name))) {
				for (int i = 0; i < KUtils.getRandomInt(entities.getAmount(name)); i++) {
					spawnMoney(KUtils.getRandom(entities.getMoney(name)), entity.getLocation());
					if (fc.getBoolean("particle.enable")) {
						ParticleEffect.fromName(fc.getString("particle.type")).display((float) 0.5, (float) 0.5, (float) 0.5, 1, fc.getInt("particle.amount"), entity.getLocation(), 20);
					}
				}
			}
		}
	}
	@EventHandler
	public void onBreak(BlockBreakEvent e){
		if(fc.getBoolean("enableBlocksDrop")) {
			Block block = e.getBlock();
			String name = block.getType().toString();
			if (blocks.contain(name) && blocks.getEnable(name) && KUtils.getSuccess(blocks.getChance(name))) {
				for (int i = 0; i < KUtils.getRandomInt(blocks.getAmount(name)); i++) {
					spawnMoney(KUtils.getRandom(blocks.getMoney(name)), block.getLocation());
					if (fc.getBoolean("particle.enable")) {
						ParticleEffect.fromName(fc.getString("particle.type")).display((float) 0.5, (float) 0.5, (float) 0.5, 1, fc.getInt("particle.amount"), block.getLocation(), 20);
					}
				}
			}
		}
	}
	private String getMoney(String name) {
		Pattern pattern = Pattern.compile("[0-9]+\\.[0-9][0-9]");
		Matcher matcher = pattern.matcher(name);
		if(matcher.find()) return matcher.group(0);
		return "0";
	}

	private void giveMoney(float amount, Player p) {
		economy.depositPlayer(p, amount);
	}
	public void spawnMoney(float money,Location l){
			Item i = l.getWorld().dropItemNaturally(l, getItem(Float.valueOf(money).intValue()));
			i.setCustomName(language.get("nameSyntax").replace("{money}", String.valueOf(money)));
			i.setCustomNameVisible(true);
	}
	public ItemStack getItem(int money){
		if(money<fc.getInt("item.small.amount")){
			return new ItemStack(Material.getMaterial(fc.getString("item.small.type")));
		}
		else if(money<fc.getInt("item.normal.amount")){
			return new ItemStack(Material.getMaterial(fc.getString("item.normal.type")));
		}
		else{
			return new ItemStack(Material.getMaterial(fc.getString("item.big.type")));
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
}