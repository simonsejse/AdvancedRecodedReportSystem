package com.simonsejse.Inventorys;

import com.simonsejse.Builders.ItemBuilder;
import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ConfirmGUI implements InvGUI {

    private int deleteReportId;
    private SpecifyReportGUI specifyReportGUI;
    private boolean isReport;
    private int commentLine;
    private FileInterface configFile;

    public void setSpecifyReportGUI(SpecifyReportGUI specifyReportGUI) {
        this.specifyReportGUI = specifyReportGUI;
    }
    public void setDeleteReportId(int deleteReportId) {
        this.deleteReportId = deleteReportId;
    }


    public int getDeleteReportId(){
        return deleteReportId;
    }

    public int getCurrent_page(){
        return specifyReportGUI.getCurrent_page();
    }

    public ConfirmGUI(int deleteReportId, SpecifyReportGUI specifyReportGUI, boolean isReport, int commentLine){
        this.deleteReportId = deleteReportId;
        this.specifyReportGUI = specifyReportGUI;
        this.isReport = isReport;
        this.commentLine = commentLine;
        this.configFile = ReportSystem.getConfigFile();
    }

    public ConfirmGUI(int deleteReportId, SpecifyReportGUI specifyReportGUI, boolean isReport){
        this.deleteReportId = deleteReportId;
        this.specifyReportGUI = specifyReportGUI;
        this.isReport = isReport;
    }

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

     @Override
     public void onGuiClick(ItemStack item, int slot, Player whoClicked){
        if(!whoClicked.hasPermission("report.delete")){
            ((List<String>) ReportSystem.getConfigFile().get("Messages.noPermissions")).stream().forEach(s -> whoClicked.sendMessage(loadColor(s)));
            return;
        }

        if (slot == 2 && !isReport){
            Report report = ReportManager.getSpecificReportById(deleteReportId);
            Comment[] comments = report.getComments();
            System.out.println(commentLine);
            if (comments[(commentLine)] != null) {
                System.out.println(commentLine);
                comments = (Comment[]) ArrayUtils.remove(comments, commentLine);
                System.out.println(commentLine);
                Arrays.stream(comments).filter(comment -> comment.getId() > commentLine).forEach(comment -> comment.setId(comment.getId() - 1));
                report.setComments(comments);

                specifyReportGUI.deleteUpdateGUI(whoClicked);
            }else{
                ((List<String>) configFile.get("Messages.Comment.invalidLine")).forEach(s -> whoClicked.sendMessage(loadColor(s)));
                return;
            }
        }else if (slot == 2 && item.getType().equals(Material.SKELETON_SKULL) && isReport){
            if (ReportManager.doesReportExistsById(getDeleteReportId())){
                List<Report> reports = ReportManager.getReportList();
                reports.remove(getDeleteReportId());
             /*
            @param remove a report and then filter all the reports above this report, then place reports one lower.
            They automatically has a lower index, so we don't need to do anything about that, since we remove a report,
            they'll automatically have that lower index, now WE ONLY NEED TO SET THEIR ID'S inside the report TO THE RIGHT THING.
             */
                reports.stream().filter(report -> report.getId() > getDeleteReportId()).forEach(report -> report.setId(report.getId() - 1));


            /* delete one, because when initializing listGUI it adds one to current_page.
            Meaning in our SpecifyReportGUI when we call the ConfirmGUI we might have added the current_page 10, so when we then
            in this code try to go back to page 10, it will add one more in the ListGUI.getInventory method, therefore we have to minus 1
            to get the same page, just like we have to minus 2 to get the page before.
             */

                ListGUI listGUI = new ListGUI(getCurrent_page() - 1);
                whoClicked.openInventory(listGUI.getInventory());
            }else{
                ((List<String>) configFile.get("Messages.reportDeletedInside")).stream().forEach(s -> whoClicked.sendMessage(loadColor(s)));
                return;
            }


        }else if (slot == 6 && item.getType().equals(Material.SUNFLOWER)){
            SpecifyReportGUI specifyReportGUI = new SpecifyReportGUI(deleteReportId, getCurrent_page());
            whoClicked.openInventory(specifyReportGUI.getInventory());
        }
     }
     @Override
     public void setItem(Inventory inventory, int slot, Material type, String name, String... lore){
        inventory.setItem(slot, new ItemBuilder(type).setDisplayName(name).setLore(lore).build());
     }

    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, 9*1, ChatColor.RED+"Are you sure?");
        setItem(inventory, 2, Material.SKELETON_SKULL, "&4&lYes, I am sure!", "");
        setItem(inventory, 6, Material.SUNFLOWER, "&4Nooo! I regret, don't delete!", "");
        return inventory;
    }
}
