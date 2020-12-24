/*
 * Eat and heal a player instantly!
 * Copyright (C) 2020 KygekTeam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package org.kygekteam.kygekeatheal;

import cn.nukkit.Player;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import me.onebone.economyapi.EconomyAPI;
import org.kygekteam.kygekeatheal.commands.EatCommand;
import org.kygekteam.kygekeatheal.commands.HealCommand;

import java.io.File;
import java.util.ArrayList;

public class EatHeal extends PluginBase {

    public static final String PREFIX = TextFormat.YELLOW + "[KygekEatHeal] " + TextFormat.RESET;
    public static final String INFO = TextFormat.GREEN.toString();
    public static final String WARNING = TextFormat.RED.toString();

    public boolean economyEnabled = false;
    public EconomyAPI economyAPI;

    @Override
    public void onEnable() {
        if (!classExists("me.onebone.economyapi.EconomyAPI")) {
            this.getLogger().notice("EconomyAPI plugin is not installed or enabled, all actions will be free");
        } else {
            this.economyEnabled = true;
            this.economyAPI = EconomyAPI.getInstance();
        }

        this.saveDefaultConfig();
        if (!this.getConfig().getString("config-version").equals("1.0")) {
            this.getLogger().notice("Your configuration file is outdated, updating the config.yml...");
            this.getLogger().notice("The old configuration file can be found at config_old.yml");
            this.renameConfig();
            this.saveDefaultConfig();
            this.reloadConfig();
        }

        ArrayList<PluginCommand<EatHeal>> commands = new ArrayList<>();
        commands.add(new EatCommand("eat", this));
        commands.add(new HealCommand("heal", this));

        this.getServer().getCommandMap().registerAll("KygekEatHeal", commands);
    }

    private double getEatValue(Player player) {
        String config = this.getConfig().getString("eat-value", "max");
        double maxFood = player.getFoodData().getMaxLevel();
        double food = maxFood - player.getFoodData().getLevel();

        return (config.equals("max") ? maxFood :
                (Double.parseDouble(config) > food ? maxFood : Double.parseDouble(config) + player.getFoodData().getLevel()));
    }

    private double getHealValue(Player player) {
        String config = this.getConfig().getString("heal-value", "max");
        double maxHealth = player.getMaxHealth();
        double health = maxHealth - player.getHealth();

        return (config.equals("max") ? maxHealth :
                (Double.parseDouble(config) > health ? maxHealth : Double.parseDouble(config) + player.getHealth()));
    }

    public String eatTransaction(Player player, boolean economyEnabled) {
        if (player.getFoodData().getLevel() == 20) return "true";
        Double price = null;

        if (this.economyEnabled && economyEnabled) {
            price = this.getConfig().getDouble("eat-price", 0);
            if (this.economyAPI.myMoney(player) < price) return "false";
            this.economyAPI.reduceMoney(player, price);
        }

        double eatValue = this.getEatValue(player);
        player.getFoodData().setLevel((int) eatValue);
        player.getFoodData().setFoodSaturationLevel(20);
        return price == null ? "0" : price.toString();
    }

    public String eatTransaction(Player player) {
        return this.eatTransaction(player, true);
    }

    public String healTransaction(Player player, boolean economyEnabled) {
        if (player.getHealth() == 20) return "true";
        Double price = null;

        if (this.economyEnabled && economyEnabled) {
            price = this.getConfig().getDouble("heal-price", 0);
            if (this.economyAPI.myMoney(player) < price) return "false";
            this.economyAPI.reduceMoney(player, price);
        }

        double healValue = this.getHealValue(player);
        player.setHealth((float) healValue);
        return price == null ? "0" : price.toString();
    }

    public String healTransaction(Player player) {
        return this.healTransaction(player, true);
    }

    private void renameConfig() {
        File oldConfig = new File(this.getDataFolder() + "/config.yml");
        File newConfig = new File(this.getDataFolder() + "/config-old.yml");

        if (newConfig.exists()) newConfig.delete();

        oldConfig.renameTo(newConfig);
    }

    private static boolean classExists(String classPath) {
        try {
            Class.forName(classPath);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}