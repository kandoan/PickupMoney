package com.gmail.vkhanh234.PickupMoney.Listener;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import net.elseland.xikage.MythicMobs.API.Bukkit.Events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by Admin on 3/9/2015.
 */
public class MythicMobsListener implements Listener {
    private final PickupMoney plugin;

    public MythicMobsListener(PickupMoney plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobsDeath(MythicMobDeathEvent e){
        if(plugin.fc.getBoolean("enableEntitiesDrop")) {
            if(e.getKiller()!=null && e.getKiller() instanceof Player) {
                Entity entity = e.getEntity();
                if (!plugin.checkWorld(entity.getLocation())) return;
                String name = e.getMobType().getInternalName();
                if (plugin.entities.contain(name) && plugin.entities.getEnable(name) && KUtils.getSuccess(plugin.entities.getChance(name))) {
                    for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
                        plugin.spawnMoney(KUtils.getRandom(plugin.entities.getMoney(name)), entity.getLocation());
                    }
                    plugin.spawnParticle(entity.getLocation());
                }
            }
        }
    }
}
