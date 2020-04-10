package com.simonsejse.Inventorys;

import com.simonsejse.Builders.ItemBuilder;
import com.simonsejse.FileLoadSaver.ConfigFile;
import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import com.simonsejse.SubCommands.Flag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FlagReportGUI implements InvGUI {

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);
    private int report_id;
    private int current_page;
    private FileInterface configFile;

    private int slot;

    public ReportSystem getPlugin() {
        return plugin;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public void setPlugin(ReportSystem plugin) {
        this.plugin = plugin;
    }

    public void setReport_id(int report_id) {
        this.report_id = report_id;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public int getReport_id() {
        return report_id;
    }

    public int getSlot() {
        return slot;
    }

    public FlagReportGUI(int report_id, int current_page){
        this.report_id = report_id;
        this.current_page = current_page;
        this.configFile = ReportSystem.getConfigFile();
    }

    @Override
    public void onGuiClick(ItemStack item, int slot, Player whoClicked){
        if(ReportManager.doesReportExistsById(getReport_id())) {
            if (slot == 11 && item.getType().equals(Material.GREEN_TERRACOTTA)) {
                Report report = ReportManager.getSpecificReportById(report_id);
                report.setFlag(EnumFlag.OPEN);
            } else if (slot == 12 && item.getType().equals(Material.YELLOW_TERRACOTTA)) {
                Report report = ReportManager.getSpecificReportById(report_id);
                report.setFlag(EnumFlag.WORKING);
            } else if (slot == 13 && item.getType().equals(Material.PURPLE_TERRACOTTA)) {
                Report report = ReportManager.getSpecificReportById(report_id);
                report.setFlag(EnumFlag.PENDING);
            }else if (slot == 14 && item.getType().equals(Material.RED_TERRACOTTA)) {
                Report report = ReportManager.getSpecificReportById(report_id);
                report.setFlag(EnumFlag.CLOSED);
            }else if (slot == 16 && item.getType().equals(Material.RED_BED)) {
                SpecifyReportGUI specifyReportGUI = new SpecifyReportGUI(report_id, current_page);
                whoClicked.openInventory(specifyReportGUI.getInventory());
            }
        }else{
            List<String> report_not_found = (List<String>) configFile.get("Messages.reportDeletedInside");
            report_not_found.forEach(s-> whoClicked.sendMessage(loadColor(s)));
            ListGUI listGUI = new ListGUI(current_page - 1);
            whoClicked.openInventory(listGUI.getInventory());
            return;
        }
    }

    @Override
    public void setItem(Inventory inventory, int slot, Material type, String name, String... lore) {
        inventory.setItem(slot,  new ItemBuilder(type).setDisplayName(name).setLore(lore).build());
    }


    public void setItem(Inventory inv, int slot, Material type, String name, boolean isChosen, String... lore){
        ItemStack item;
        if(isChosen) item = new ItemBuilder(type).setDisplayName(name).setLore(lore).addFlags(ItemFlag.HIDE_ENCHANTS).addEnchantments(Enchantment.LURE, 5).build();
        else item = new ItemBuilder(type).setDisplayName(name).setLore(lore).build();
        inv.setItem(slot, item);
    }

    /*
    Doesn't need to check if player closes inventory, because that it doesn't add to players inventory but to our Inventory object,
    therefore it doesnt matter if player closes his inventory.
    */
    public Inventory getInventory(){
        Inventory inventory = Bukkit.createInventory(this, 9*3, "Flag");
        List<String> list = (List<String>) configFile.get("SpecifyReportGUI.goBackSpecify");
        StringBuilder goBackLore = new StringBuilder();
        for(String s : list){
            goBackLore.append(s).append("\n");
        }
        setItem(inventory, 16, Material.RED_BED, "&c&lGo back to List.", goBackLore.toString());
        new BukkitRunnable(){
            @Override
            public void run(){
                if (!ReportManager.doesReportExistsById(getReport_id())){
                    cancel();
                }
                setItem(inventory, slot++, (slot % 2 == 1 ? Material.BLUE_STAINED_GLASS_PANE : Material.LIGHT_BLUE_STAINED_GLASS_PANE), " ", " ");
                if (slot == 10) {
                    slot = 17;
                }
                if (slot > 26) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0l, 1L);
        new BukkitRunnable(){
           @Override
           public void run(){
               if(ReportManager.doesReportExistsById(getReport_id())){
                   Report report = ReportManager.getSpecificReportById(getReport_id());
                   EnumFlag flag = report.isFlag();
                   boolean[] chosenOne = new boolean[]{false, false, false, false};
                   switch(flag){
                       case OPEN:
                           chosenOne[0] = true;
                           break;
                       case WORKING:
                           chosenOne[1] = true;
                           break;
                       case PENDING:
                           chosenOne[2] = true;
                           break;
                       case CLOSED:
                           chosenOne[3] = true;
                           break;
                   }
                   setItem(inventory, 11, Material.GREEN_TERRACOTTA, "&eChange flag", chosenOne[0], "&bSelect &lOPTION&b to change flag", "&e&lClick&e to change report flag to &a&lOpen", "", "", "&aPlayer can still interact and comment on report");
                   setItem(inventory, 12, Material.YELLOW_TERRACOTTA, "&eChange flag", chosenOne[1], "&bSelect &lOPTION&b to change flag", "&e&lClick&e to change report flag to &e&lWorking", "", "", "&aPlayer can still interact and comment on report");
                   setItem(inventory, 13, Material.PURPLE_TERRACOTTA, "&eChange flag", chosenOne[2],  "&bSelect &lOPTION&b to change flag", "&e&lClick&e to change report flag to &5&lPending", "", "", "&aPlayer can still interact and comment on report");
                   setItem(inventory, 14, Material.RED_TERRACOTTA, "&eChange flag", chosenOne[3], "&bSelect &lOPTION&b to change flag", "&e&lClick&e to change report flag to &4&lClosed", "", "", "&cPlayer can no longer interact and comment on report");

                   if (inventory.getViewers().size() < 1){
                       this.cancel();
                   }
               }else{
                   cancel();
               }

           }
        }.runTaskTimer(plugin, 20L, 0L);
        return inventory;
    }

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
