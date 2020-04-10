package com.simonsejse.Inventorys;

import com.simonsejse.Builders.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AdminGUI implements InvGUI {

    @Override
    public void onGuiClick(ItemStack item, int slot, Player whoClicked){

    }

    @Override
    public void setItem(Inventory inventory, int slot, Material type, String name, String... lore){
        inventory.setItem(slot, new ItemBuilder(type).setDisplayName(name).setLore(lore).build());
    }

    public Inventory getInventory(){
        Inventory inventory = Bukkit.createInventory(this, 9*6, "");
        setItem(inventory, 3, Material.TNT, "d", "");
        return inventory;

    }

}
