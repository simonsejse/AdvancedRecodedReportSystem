package com.simonsejse.Inventorys;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface InvGUI extends InventoryHolder {

    void onGuiClick(ItemStack item, int slot, Player whoClicked);
    void setItem(Inventory inventory, int slot, Material type, String name, String... lore);


}
