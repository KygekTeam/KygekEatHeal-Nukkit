/*
 * Eat and heal a player instantly!
 * Copyright (C) 2020 KygekTeam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package org.kygekteam.kygekeatheal.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import org.kygekteam.kygekeatheal.EatHeal;

public class EatCommand extends PluginCommand<EatHeal> {

    public EatCommand(String name, EatHeal owner) {
        super(name, owner);

        String desc = owner.getConfig().getString("eat-desc").isEmpty() ?
                "Eat or feed a player" : owner.getConfig().getString("eat-desc");
        this.setDescription(desc);
        this.setAliases(owner.getConfig().getStringList("eat-aliases").toArray(new String[0]));
        this.setUsage("/eat [player]");
        this.setPermission("kygekeatheal.eat");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) return true;

        EatHeal owner = this.getPlugin();
        owner.reloadConfig();

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(EatHeal.PREFIX + EatHeal.INFO + "Usage: /eat <player>");
                return true;
            }

            Object result = owner.eatTransaction((Player) sender);

            if (result.equals("true")) {
                sender.sendMessage(EatHeal.PREFIX + EatHeal.INFO + "You are already full!");
                return true;
            }
            if (result.equals("false")) {
                sender.sendMessage(EatHeal.PREFIX + EatHeal.WARNING + "You do not have enough money to eat!");
                return true;
            }

            String price = owner.economyEnabled ? " for " + owner.economyAPI.getMonetaryUnit() + result : "";
            sender.sendMessage(EatHeal.PREFIX + EatHeal.INFO + "You have eaten" + price);
        } else {
            Player player = owner.getServer().getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(EatHeal.PREFIX + EatHeal.WARNING + "Player is not online!");
                return true;
            }

            boolean isPlayer = sender instanceof Player;
            Object result = owner.eatTransaction(player, isPlayer);

            if (result.equals("true")) {
                sender.sendMessage(EatHeal.PREFIX + EatHeal.INFO + player.getName() + " is already full!");
                return true;
            }
            if (result.equals("false")) {
                sender.sendMessage(EatHeal.PREFIX + EatHeal.WARNING + "You do not have enough money to feed " + player.getName() + "!");
                return true;
            }

            String price = owner.economyEnabled && isPlayer ? " for " + owner.economyAPI.getMonetaryUnit() + result : "";
            sender.sendMessage(EatHeal.PREFIX + EatHeal.INFO + "Player " + player.getName() + " has been fed" + price);
            player.sendMessage(EatHeal.PREFIX + EatHeal.INFO + "You have been fed by " + sender.getName());
        }

        return true;
    }

}
