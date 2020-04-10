package com.simonsejse.Builders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(){

    }

    public ItemBuilder setItem(ItemStack item){
        this.item = item;
        meta = item.getItemMeta();
        return this;
    }

    public void setMeta(ItemMeta meta){
        this.meta = meta;
    }

    public ItemBuilder addEnchantments(Enchantment enchant, int level){
        item.addUnsafeEnchantment(enchant, level);
        return this;
    }

    public ItemBuilder addFlags(ItemFlag itemFlag){
        meta.addItemFlags(itemFlag);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setAmount(int amount){
        if (amount > 64) amount = 64;
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder(Material TYPE){
        item = new ItemStack(TYPE);
        meta = item.getItemMeta();
    }

    public ItemBuilder setDisplayName(String name){
        meta.setDisplayName(c(name));
        item.setItemMeta(meta);
        return this;
    }

    public String c(String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public ItemBuilder setLore(String... lores){
        for(int i = 0;i<lores.length;i++){
            lores[i] = ChatColor.translateAlternateColorCodes('&', lores[i]);
        }

        meta.setLore(Arrays.asList(lores).stream().flatMap((s) -> Stream.of( s.split( "\\r?\\n" ) )).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return this;
    }

    public ItemMeta getMeta(){
        return meta;
    }
    public ItemStack build(){return item;}



}
