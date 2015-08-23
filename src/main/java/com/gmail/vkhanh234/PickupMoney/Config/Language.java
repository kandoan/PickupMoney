package com.gmail.vkhanh234.PickupMoney.Config;

import com.gmail.vkhanh234.PickupMoney.KUtils;
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
public class Language {
    private FileConfiguration config;
    private File configFile = new File("plugins/PickupMoney/language.yml");
    private final PickupMoney plugin;
    public Language(PickupMoney plugin){
        this.plugin = plugin;
        config = YamlConfiguration.loadConfiguration(configFile);
        try {
            update();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    public void update() throws IOException, InvalidConfigurationException {
        if(!configFile.exists()) {
            config.load(plugin.getResource("language.yml"));
            config.save(configFile);
        }
        else{
            FileConfiguration c = YamlConfiguration.loadConfiguration(plugin.getResource("language.yml"));
            for(String k:c.getKeys(true)){
                if(!config.contains(k)){
                    config.set(k,c.get(k));
                }
            }
            config.save(configFile);
        }
    }
    public String get(String name) {
        return KUtils.convertColor(config.getString(name));
    }
}
