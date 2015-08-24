package com.gmail.vkhanh234.PickupMoney.Config;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Admin on 22/8/2015.
 */
public class Blocks {
    private FileConfiguration config;
    private File configFile = new File("plugins/PickupMoney/blocks.yml");
    HashMap<String,BlockDat> map = new HashMap<>();
    private final PickupMoney plugin;
    public Blocks(PickupMoney plugin){
        this.plugin = plugin;
        config = YamlConfiguration.loadConfiguration(configFile);
        try {
            update();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        load();
    }
    public void update() throws IOException, InvalidConfigurationException {
        if(!configFile.exists()) {
            config.load(plugin.getResource("blocks.yml"));
            config.save(configFile);
        }
        else{
            FileConfiguration c = YamlConfiguration.loadConfiguration(plugin.getResource("blocks.yml"));
            for(String k:c.getKeys(true)){
                if(!config.contains(k)){
                    config.set(k,c.get(k));
                }
            }
            config.save(configFile);
        }
    }
    public void load(){
        for(String k:config.getKeys(false)){
            BlockDat e = new BlockDat();
            e.enable = config.getBoolean(k+".enable");
            e.chance = config.getInt(k + ".chance");
            e.money = config.getString(k + ".money");
            e.amount = config.getString(k+".amount");
            map.put(k,e);
        }
    }
    public boolean contain(String name){
        if(map.containsKey(name)) return true;
        return false;
    }
    public boolean getEnable(String name){
        return map.get(name).enable;
    }
    public int getChance(String name){
        return map.get(name).chance;
    }
    public String getMoney(String name){
        return map.get(name).money;
    }
    public String getAmount(String name){
        return map.get(name).amount;
    }
    class BlockDat{
        boolean enable;
        int chance;
        String money,amount;
    }
}
