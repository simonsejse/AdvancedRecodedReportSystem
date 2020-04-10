package com.simonsejse.Inventorys;

import com.simonsejse.Builders.ItemBuilder;
import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Comment;
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

import java.sql.SQLException;
import java.util.List;

public class DeleteGUI implements InvGUI {

    private Report report;
    private FileInterface configFile;
    private Material commentMaterial;
    private Material goBackMaterial;
    private SpecifyReportGUI specifyReportGUI;
    private int report_id;
    private boolean closed;

    public boolean isClosed(){
        return closed;
    }

    public void setClosed(boolean closed){
        this.closed = closed;
    }

    public DeleteGUI(){
        this.configFile = ReportSystem.getConfigFile();
        closed = false;
        commentMaterial = Material.PAPER;
        goBackMaterial = Material.RED_BED;
    }

    SpecifyReportGUI getSpecifyReportGUI(){
        return specifyReportGUI;
    }

    public DeleteGUI(int report_id, SpecifyReportGUI specifyReportGUI) { //specifyreportgui instead of current_page, better because specifyreportgui holds currentpage
        this();
        this.report_id = report_id;
        this.specifyReportGUI = specifyReportGUI;
        if (ReportManager.doesReportExistsById(report_id)) {
            this.report = ReportManager.getSpecificReportById(report_id);
        }
    }

    public Inventory getInventory(){
        Inventory inventory = Bukkit.createInventory(getInstance(), getSizeOfInventory(), "Delete a comment");
        /*
        @param
        What happens is that we create an inventory instance.
        Then we create a BukkitRunnable that then continues to run as long as the viewing size is not below 1
        Therefore it will use the Inventory object, to setItem continuesly, therefore updating the inventory, that we return
        at the end. And because we return it, we can still update the exact object, inside our loop still.
         */
        int size = getSizeOfInventory() - 1;

        Comment[] comments = report.getComments();
        for(int id = 0, slot = 0; id < comments.length && slot < size; id++, slot++){
            StringBuilder stringBuilder = new StringBuilder();
            int finalId = id;

            ((List<String>) configFile.get("Messages.DeleteGUI.lore")).forEach(s -> {
                if (s.contains("{id}")) s = s.replace("{id}", String.valueOf(comments[finalId].getId()));
                if (s.contains("{comment}")) s = s.replace("{comment}", comments[finalId].getComment());
                if (s.contains("{commenter}")) s = s.replace("{commenter}", comments[finalId].getCommenter());
                if (s.contains("{date}")) s = s.replace("{date}", comments[finalId].getDate());
                stringBuilder.append(s).append("\n");
            });
            String query = "DELETE FROM comments WHERE reportId = '"+report_id+"' AND commentId = '"+comments[finalId].getId()+"'";
            try {
                ReportSystem.getPlugin(ReportSystem.class).getStatement().executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            setItem(inventory, slot, commentMaterial, "&e&lID: &6&l"+report.getComments()[id].getId(), stringBuilder.toString());
        }
        StringBuilder sb = new StringBuilder();
        ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).forEach(s -> sb.append(s).append("\n"));
        setItem(inventory, size, goBackMaterial, "&c&lGo back to List.", sb.toString());
        return inventory;
    }

    private int getSizeOfInventory(){
        int size;
        if (report.getComments().length >= 9*5){
            size = 9*6;
        }else if (report.getComments().length >= 9*4){
            size = 9*5;
        }else if (report.getComments().length >= 9*3){
            size = 9*4;
        }else if (report.getComments().length >= 9*2){
            size = 9*3;
        }else if (report.getComments().length >= 9){
            size = 9*2;
        }else{
            size = 9;
        }
        return size;
    }

    public DeleteGUI getInstance(){
        return this;
    }

    @Override
    public void onGuiClick(ItemStack item, int slot, Player p){
        if (item.getType().equals(commentMaterial)){
            String[] splitId = item.getItemMeta().getDisplayName().trim().split(":");
            String s = ChatColor.stripColor(splitId[1].trim());
            int commentLine = Integer.parseInt(s);
            commentLine-=1;
            ConfirmGUI confirmGUI = new ConfirmGUI(report.getId(), specifyReportGUI, false, commentLine);
            p.openInventory(confirmGUI.getInventory());
        }else if (item.getType().equals(goBackMaterial)) {
            SpecifyReportGUI specifyReportGUI = new SpecifyReportGUI(report.getId(), getCurrentPage());
            p.openInventory(specifyReportGUI.getInventory());
        }
    }

    private int getCurrentPage(){
        return specifyReportGUI.getCurrent_page();
    }


    @Override
    public void setItem(Inventory inventory, int slot, Material type, String name, String... lore){
        inventory.setItem(slot, new ItemBuilder(type).setDisplayName(name).setLore(lore).build());
    }


}
