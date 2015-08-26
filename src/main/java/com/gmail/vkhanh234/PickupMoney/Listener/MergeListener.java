package com.gmail.vkhanh234.PickupMoney.Listener;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;

/**
 * Created by Admin on 26/8/2015.
 */
public class MergeListener implements Listener{
    private final PickupMoney plugin;
    public MergeListener(PickupMoney plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onMerge(ItemMergeEvent e){
        Item item = e.getEntity();
        if(item.getCustomName()!=null && plugin.language.get("nameSyntax").replace("{money}", "").equals(item.getCustomName().replaceAll(plugin.regex,""))){
            e.setCancelled(true);
        }
    }

}
