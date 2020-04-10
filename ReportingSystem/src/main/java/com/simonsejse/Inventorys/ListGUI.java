package com.simonsejse.Inventorys;

import com.simonsejse.Builders.ItemBuilder;
import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ListGUI implements InvGUI {

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);

    private int current_page;
    private FileInterface configFile;
    private Material slotItem;
    /*
    Block initialization
     */
    public ListGUI(){
        this.configFile = ReportSystem.getConfigFile();
        try{
            slotItem = Material.valueOf(configFile.get("ListGUI.Item").toString());
        }catch(Exception e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ListGUI.Item isn't a valid item! Choose another one in config.yml");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Choose another one inside your config.yml!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Disabling plugin!");
            Bukkit.getPluginManager().disablePlugin(ReportSystem.getPlugin(ReportSystem.class));
            return;
        }

    }
    public int getPage(){return current_page;}

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public FileInterface getConfigFile() {
        return configFile;
    }

    public Material getSlotItem() {
        return slotItem;
    }

    public void setSlotItem(Material slotItem){
        this.slotItem = slotItem;
    }

    public ListGUI(int current_page) {
        this();
        this.current_page = current_page;
    }

    public Inventory getInventory(){
        current_page+=1;
        Inventory inventory = Bukkit.createInventory(this, 9*6, "Page "+current_page);

        new BukkitRunnable(){
            @Override
            public void run(){
                if (inventory.getViewers().size() < 1){
                    cancel();
                }

                int pageIndex = getPage() - 1;
                int maximum = ReportManager.getReportCount();

                if(ReportManager.getReportCount() - 1 > getPage() * 52 ){
                    setItem(inventory, 53, Material.SUNFLOWER, "&c&lNext &9»&f page &e"+(current_page + 1));
                }else{
                    setItem(inventory, 53, Material.BARRIER, "&c&lNo more&9»&f pages&e!");
                }
                if(current_page > 1) setItem(inventory, 52, Material.BLAZE_ROD, " &fLast &9« &c&lpage &e"+(current_page - 1));
                for(int slot = 0, reportCount = getPage() > 1 ? pageIndex * 52 : -1; (current_page <= 1 ? slot < 53 : slot < 52); slot++){
                    reportCount+=1;
                    Report report = ReportManager.getSpecificReportById(reportCount);
            /*
            @param makes sure report isn't null before using methods.
             */
                    if (maximum <= reportCount){
                        return;
                    }
                    assert report != null;
                    Comment[] comment = report.getComments();
                    StringBuilder comments = new StringBuilder();
                    List<String> guiComment = (List<String>) configFile.get("ListGUI.reportComment");
                    //Loop through ALL comments
                    for (int j = 0; j < report.getComments().length; j++) {
                        //For each comment loop through the guiComment list to store EACH individual comment as a comment with their own style.
                        for(String s : guiComment){
                            if (s.contains("{id}")) s = s.replace("{id}", ""+comment[j].getId());
                            if (s.contains("{date}")) s = s.replace("{date}", comment[j].getDate());
                            if (s.contains("{commenter}")) s = s.replace("{commenter}", comment[j].getCommenter());
                            if (s.contains("{comment}")) s = s.replace("{comment}", comment[j].getComment());

                            comments.append(s).append("\n");
                        }
                    }
                    List<String> guiInfo = (List<String>) configFile.get("ListGUI.reportInfo");
                    StringBuilder lore = new StringBuilder();
                    for(String s : guiInfo) {
                        if (s.contains("{defenseUsername}")) s = s.replace("{defenseUsername}", report.getDefenseUsername());
                        if (s.contains("{attackerUsername}")) s = s.replace("{attackerUsername}", report.getAttackerUsername());
                        if (s.contains("{date}")) s = s.replace("{date}", report.getDate());
                        if (s.contains("{reason}")) s = s.replace("{reason}", report.getReason());
                        if (s.contains("{flag}")) s = s.replace("{flag}", getFlag(report.isFlag()));
                        if (s.contains("{comLength}")) s = s.replace("{comLength}", "" + report.getComments().length);
                        if (s.contains("{comments}")) s = s.replace("{comments}", comments.toString());
                        lore.append(s).append("\n");
                    }
                    setItem(inventory, slot, slotItem, "&c&lReports &f> &e&lID: "+report.getId(), lore.toString());
                }
            }
        }.runTaskTimer(plugin, 0l,0l);
        return inventory;
    }

    private String getFlag(EnumFlag enumFlag){
        switch(enumFlag){
            case CLOSED:
                return "&c&lCLOSED";
            case PENDING:
                return "&5&lPENDING";
            case WORKING:
                return "&e&lWORKING";
            case OPEN:
                return "&a&lOPEN";

        }
        return null;
    }

    @Override
    public void onGuiClick(ItemStack item, int slot, Player whoClicked){
        if(item.getType().equals(Material.BARRIER)){return;}
        if (slot == 53){
            ListGUI listGUI = new ListGUI(getPage()); //uses constructor with the report count
            whoClicked.openInventory(listGUI.getInventory());
        }else if (slot == 52 && getPage() > 1) {
            ListGUI listGUI = new ListGUI(getPage()-2);
            whoClicked.openInventory(listGUI.getInventory());
        }else{
            String[] findId = item.getItemMeta().getDisplayName().split(":");

            int id = Integer.parseInt(findId[1].trim()); //No reason for try/catch because I know it's a number, it's the report items.

            SpecifyReportGUI specifyReportGUI = new SpecifyReportGUI(id, getPage());
            whoClicked.openInventory(specifyReportGUI.getInventory());
        }
    }

    @Override
    public void setItem(Inventory inventory, int slot, Material type, String name, String... lore) {
        inventory.setItem(slot, new ItemBuilder(type).setDisplayName(name).setLore(lore).build());

    }

}
