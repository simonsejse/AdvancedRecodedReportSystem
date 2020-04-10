package com.simonsejse.SubCommandClasses;

import org.bukkit.entity.Player;

public abstract class CommentArgs {

    public abstract String getName();
    public abstract void perform(Player p, String... args);

}
