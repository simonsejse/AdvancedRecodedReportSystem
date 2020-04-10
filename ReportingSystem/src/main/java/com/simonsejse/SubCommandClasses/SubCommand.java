package com.simonsejse.SubCommandClasses;

import org.bukkit.entity.Player;

public abstract class SubCommand {

    public abstract String getName();
    public abstract String getSyntax();
    public abstract void perform(Player p, String... args);



}
