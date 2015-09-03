package com.gmail.vkhanh234.PickupMoney.Listener;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class MainListener implements Listener {
    private final PickupMoney plugin;
    public MainListener(PickupMoney plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
        Item item = e.getItem();
        if(item.getCustomName()!=null) {
            String name = ChatColor.stripColor(item.getCustomName());
//		if(name!=null && ChatColor.stripColor(language.get("nameSyntax")).replace("{money}", "").equals(name.replaceAll(regex, ""))){
            e.setCancelled(true);
            String money = plugin.getMoney(name);
            Player p = e.getPlayer();
            if (p.hasPermission("PickupMoney.pickup")) {
                item.remove();
                plugin.giveMoney(Float.parseFloat(money), p);
                p.sendMessage(plugin.language.get("pickup").replace("{money}", money));
                if (plugin.fc.getBoolean("sound.enable")) {
                    p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(plugin.fc.getString("sound.type"))
                            , (float) plugin.fc.getDouble("sound.volumn")
                            , (float) plugin.fc.getDouble("sound.pitch"));
                }
            }
//		}
        }
    }


    @EventHandler
    public void onDeath(EntityDeathEvent e){
        if(plugin.fc.getBoolean("enableEntitiesDrop")) {
            if(e.getEntity().getKiller()!=null && e.getEntity().getKiller() instanceof Player) {
                Entity entity = e.getEntity();
                if (!plugin.checkWorld(entity.getLocation())) return;
                String name = entity.getType().toString();
                if (plugin.entities.contain(name) && plugin.entities.getEnable(name) && KUtils.getSuccess(plugin.entities.getChance(name))) {
                    if(entity instanceof Player) {
                        Player p = (Player) entity;
                        for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
                            float money = plugin.getMoneyOfPlayer((Player) entity, plugin.entities.getMoney(name));
                            if(plugin.entities.getCost(name)){
                                plugin.costMoney(money, p);
                                p.sendMessage(plugin.language.get("dropOut").replace("{money}",String.valueOf(money)));
                            }
                            plugin.spawnMoney(money, entity.getLocation());
                        }
                    }
                    else{
                        int perc = 100;
                        if(plugin.spawners.contains(entity.getUniqueId())) perc = plugin.fc.getInt("spawnerPercent");
                        for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
                            plugin.spawnMoney(KUtils.getRandom(plugin.entities.getMoney(name)) * perc / 100, entity.getLocation());
                        }
                    }
                    plugin.spawnParticle(entity.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        if(plugin.fc.getBoolean("enableBlocksDrop")) {
            Block block = e.getBlock();
            if (!plugin.checkWorld(block.getLocation())) return;
            String name = block.getType().toString();
            if (plugin.blocks.contain(name) && plugin.blocks.getEnable(name) && KUtils.getSuccess(plugin.blocks.getChance(name))) {
                for (int i = 0; i < KUtils.getRandomInt(plugin.blocks.getAmount(name)); i++) {
                    plugin.spawnMoney(KUtils.getRandom(plugin.blocks.getMoney(name)), block.getLocation());
                }
                plugin.spawnParticle(block.getLocation());
            }
        }
    }
    @EventHandler
    public void onSpawner(CreatureSpawnEvent e){
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER){
            plugin.spawners.add(e.getEntity().getUniqueId());
        }
    }
    @EventHandler
    public void onHopper(InventoryPickupItemEvent e){
        if(e.getInventory().getType().toString().equalsIgnoreCase("hopper") && e.getItem().getCustomName()!=null){
            e.setCancelled(true);
        }
    }
}
