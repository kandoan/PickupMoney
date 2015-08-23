package com.gmail.vkhanh234.PickupMoney;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Admin on 24/7/2015.
 */
public class KUtils {
    public static String addSpace(String s){
        return s.replace("-"," ");
    }
    public static float getRandom(String level) {
        if(level.contains("-")){
            String[] spl = level.split("-");
            return round(randomNumber(Integer.parseInt(spl[0]), Integer.parseInt(spl[1])),2);
        }
        else return Integer.parseInt(level);
    }
    public static int getRandomInt(String level) {
        if(level.contains("-")){
            String[] spl = level.split("-");
            return getRandomInt(Integer.parseInt(spl[0]), Integer.parseInt(spl[1]));
        }
        else return Integer.parseInt(level);
    }
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static float randomNumber(int min,int max){
        Random random = new Random();
        float number = random.nextFloat()* (max - min) + min;
        return random.nextFloat()* (max - min) + min;
    }
    public static int getRandomInt(int min, int max){
        Random random = new Random();
        return random.nextInt((max - min)+1) + min;
    }
    public static boolean getSuccess(int percent){
        int i = getRandomInt(1,100);
        if (i<=percent) return true;
        return false;
    }
    public static boolean hasPermmision(Player p,String perm){
        if(p.hasPermission(perm)) return true;
        if(p.isOp()) return true;
        return false;
    }
    public static String backColor(String name) {
        return name.replace("§","&");
    }
    public static String convertColor(String name){
        return name.replace("&","§");
    }
}
