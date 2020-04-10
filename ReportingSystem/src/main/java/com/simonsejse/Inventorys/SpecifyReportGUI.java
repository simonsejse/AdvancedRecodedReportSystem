package com.simonsejse.Inventorys;

import com.simonsejse.AnvilGUI;
import com.simonsejse.Builders.ItemBuilder;
import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class SpecifyReportGUI implements InvGUI {

    private ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);

    private int report_id;
    private int current_page;
    private FileInterface configFile;

    private Inventory deleteGUIInventory; //Has to be field variable, to access inside updateGUI method:
    private Material decorationMaterialOne;
    private Material decorationMaterialTwo;
    private int maximumComments;

    private int id;
    private String attacker;
    private String defense;
    private String reason;
    private String date;
    private String flag;


    //declare once
    private Material generalItem;
    private Material anvilItem;
    private Material editItem;
    private Material deleteCommentItem;
    private Material flagItem;
    private Material deleteItem;
    private Material goBackItem;
    //row 2
    private Material teleportItem;


    public FileInterface getConfigFile() {
        return configFile;
    }

    public SpecifyReportGUI(){
        this.configFile = ReportSystem.getConfigFile();
        try{
            this.decorationMaterialOne = Material.valueOf(configFile.get("SpecifyReportGUI.Item.decorationsItemOne").toString());
            this.decorationMaterialTwo = Material.valueOf(configFile.get("SpecifyReportGUI.Item.decorationsItemTwo").toString());
        }catch(Exception e){
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "One of the materials chosen in the SpecifyReportGUI.Item is wrong!");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Disabling plugin! Change in the config.yml!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        try{
            maximumComments = Integer.parseInt(configFile.get("Messages.Comment.maximumAmountOfComments").toString());
        }catch(NumberFormatException nfe){
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Messages.Comment.maximumAmountOfComments is not a number! Change in config.yml!");
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Plugin has been disabled!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
    }

    public void setConfigFile(FileInterface configFile) {
        this.configFile = configFile;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public void setReport_id(int report_id) {
        this.report_id = report_id;
    }

    public int getReport_id() {
        return report_id;
    }
    public int getCurrent_page() {
        return current_page;
    }

    public SpecifyReportGUI(int report_id, int current_page){
        this();
        this.report_id = report_id;
        this.current_page = current_page; //So we can access l8 and return to current page.
    }

    public Inventory getInventory(){
        Inventory inventory = Bukkit.createInventory(this, 9*4, ""+report_id);
        Material t = decorationMaterialOne;
        for(int i = 0, z = 27;i < 9 && z < 36; i++, z++){
            setItem(inventory, i, t, " ", "");
            setItem(inventory, z, t, " ", "");
            t = t == decorationMaterialOne ? decorationMaterialTwo : decorationMaterialOne;
        }

        new BukkitRunnable(){
            @Override
            public void run(){
                if(inventory.getViewers().size() < 1){
                    this.cancel();
                }
                if (!ReportManager.doesReportExistsById(report_id)){
                    Player p = Bukkit.getPlayer(inventory.getViewers().get(0).getName());
                    ListGUI listGUI = new ListGUI(getCurrent_page() - 1);
                    p.openInventory(listGUI.getInventory());
                    ((List<String>) configFile.get("Messages.reportDeletedInside")).forEach(s -> p.sendMessage(loadColor(s)));
                    this.cancel();
                    return;
                }
                Report report = ReportManager.getSpecificReportById(report_id);
                //Checks if report is null if it is, it prints to console.
                setPlayerHeadGui(inventory, 19, getPlayerHead(getPlayer(report.getDefenseUsername())), report.getDefenseUsername());
                setPlayerHeadGui(inventory, 20, getPlayerHead(getPlayer(report.getAttackerUsername())), report.getAttackerUsername());

                id = report.getId();
                attacker = report.getAttackerUsername();
                defense = report.getDefenseUsername();
                reason = report.getReason();
                date = report.getDate();
                flag = getFlag(report.isFlag());

                StringBuilder sb = new StringBuilder();
                ((List<String>) configFile.get("SpecifyReportGUI.GeneralInformation")).forEach(string -> {
                    if (string.contains("{id}")) string = string.replace("{id}", "" + id);
                    if (string.contains("{reported}")) string = string.replace("{reported}", "" + defense);
                    if (string.contains("{attacker}")) string = string.replace("{attacker}", "" + attacker);
                    if (string.contains("{date}")) string = string.replace("{date}", "" + date);
                    if (string.contains("{reason}")) string = string.replace("{reason}", "" + reason);
                    if (string.contains("{flag}")) string = string.replace("{flag}", flag);
                    sb.append(string).append("\n");
                });

                try{
                    generalItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.GeneralInformation").toString().toUpperCase());
                    anvilItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.anvilComment").toString().toUpperCase());
                    editItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.editComment").toString().toUpperCase());
                    deleteCommentItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.deleteComment").toString().toUpperCase());
                    deleteItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.deleteReportGUI").toString().toUpperCase());
                    flagItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.flagSpecifyGUI").toString().toUpperCase());
                    goBackItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.goBackSpecify").toString().toUpperCase());
                    teleportItem = Material.valueOf(configFile.get("SpecifyReportGUI.Item.teleportItem").toString().toUpperCase());
                }catch(Exception e){
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Your SpecifyReportGUI.Item setting inside the config.yml is wrong.");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Check the items and make sure they're written right.");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Disabling plugin!");
                    Bukkit.getPluginManager().disablePlugin(ReportSystem.getPlugin(ReportSystem.class));
                    return;
                }

                setItem(inventory, 9, generalItem, "&3&lINFORMATION &f»", sb.toString());

                sb.delete(0, sb.length());
                ((List<String>) configFile.get("SpecifyReportGUI.anvilComment")).stream().forEach(s -> sb.append(s).append("\n"));
                setItem(inventory, 10, anvilItem, "&c&lReports &f» " + id, sb.toString());

                sb.delete(0, sb.length());
                ((List<String>) configFile.get("SpecifyReportGUI.editComment")).stream().forEach(s -> sb.append(s).append("\n"));
                setItem(inventory, 11, editItem, "&c&lReports &f» Edit comment", sb.toString());

                sb.delete(0, sb.length());
                ((List<String>) configFile.get("SpecifyReportGUI.deleteComment")).forEach(s -> sb.append(s).append("\n"));
                setItem(inventory, 12, deleteCommentItem, "&c&lReports &f» Delete comment", sb.toString());

                sb.delete(0, sb.length());
                ((List<String>) configFile.get("SpecifyReportGUI.flagSpecifyGUI")).stream().forEach(s -> sb.append(s).append("\n"));
                setItem(inventory, 13, flagItem, "&7&lFlag &f»", sb.toString());

                sb.delete(0, sb.length());
                ((List<String>) configFile.get("SpecifyReportGUI.deleteReportGUI")).stream().forEach(s -> {
                    if (s.contains("{report_id}"))  s = s.replace("{report_id}", "" + report_id);
                    sb.append(s).append("\n");
                });
                setItem(inventory, 16, deleteItem, "&4&lDelete &f» &a&l" + report_id, sb.toString());

                sb.delete(0, sb.length());
                ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).stream().forEach(s-> sb.append(s).append("\n"));
                setItem(inventory, 17, goBackItem, "&c&lGo back to List.", sb.toString());

                sb.delete(0, sb.length());
                ((List<String>) configFile.get("SpecifyReportGUI.teleportGUI")).forEach(s -> {
                    if (s.contains("{x}")) s = s.replace("{x}", String.valueOf(report.getLocation()[0]));
                    if (s.contains("{y}")) s = s.replace("{y}", String.valueOf(report.getLocation()[1]));
                    if (s.contains("{z}")) s = s.replace("{z}", String.valueOf(report.getLocation()[2]));
                    sb.append(s).append("\n");
                });
                setItem(inventory, 21, teleportItem, "&c&LTELEPORT TO LOCATION", sb.toString());

            }
        }.runTaskTimer(plugin, 0l,0l);

        for(int i = 0;i<inventory.getSize();i++){
            if (inventory.getItem(i) == null){
                setItem(inventory, i, Material.BLACK_STAINED_GLASS_PANE, " ", "");
            }
        }
        return inventory;
    }

    public OfflinePlayer getPlayer(String name){
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public void onGuiClick(ItemStack item, int slot, Player whoClicked) {
        if(item != null){
            if (slot == 10) {
                if (!whoClicked.hasPermission("report.comment")) {
                    ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(s -> whoClicked.sendMessage(loadColor(s)));
                    return;
                }
                Report report = ReportManager.getSpecificReportById(report_id);
                if (report.getComments().length < maximumComments) {
                    AnvilGUI GUI = new AnvilGUI(whoClicked, e -> {
                        if (e.getSlot() == AnvilGUI.AnvilSlot.OUTPUT && e.hasText()) {
                            e.setWillClose(true);
                            if (report.isFlag().name().equals("CLOSED")) {
                                List<String> closedThread = (List<String>) configFile.get("Messages.Comment.closed_thread");
                                for (String s : closedThread) {
                                    whoClicked.sendMessage(loadColor(s));
                                }
                                return;
                            }
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                            LocalDateTime now = LocalDateTime.now();
                            String date = dtf.format(now);

                            report.addComment(e.getText(), date, whoClicked.getDisplayName());
                            ((List<String>) configFile.get("Messages.Comment.commented_successfully")).stream().forEach(s -> {
                                if (s.contains("{id}")) s = s.replace("{id}", "" + report_id);
                                whoClicked.sendMessage(loadColor(s));
                            });
                        }
                    });
                    ItemStack i = new ItemStack(Material.PAPER);
                    GUI.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, i);
                    GUI.setSlotName(AnvilGUI.AnvilSlot.INPUT_LEFT, "Choose a comment");
                    GUI.setTitle("Comment on report:");
                    GUI.open();
                }else{
                    ((List<String>) configFile.get("Messages.Comment.maximumCommentsReached")).forEach(s-> whoClicked.sendMessage(loadColor(s)));
                    return;
                }
            } else if (slot == 11) {
                if (!whoClicked.hasPermission("report.comment.edit")) {
                    ((List<String>) configFile.get("Messages.noPermissions")).forEach(s -> whoClicked.sendMessage(s));
                    return;
                }
                if (ReportManager.getSpecificReportById(report_id).getComments().length > 0) {
                    ((List<String>) configFile.get("Messages.Comment.editCommentChat")).forEach(s -> whoClicked.sendMessage(loadColor(s)));
                    GUIEventHandler.addUser(whoClicked.getUniqueId(), report_id);
                } else {
                    ((List<String>) configFile.get("Messages.Comment.noComments")).forEach(s -> whoClicked.sendMessage(loadColor(s)));
                }
                return;
            } else if (slot == 12) {
                if (!whoClicked.hasPermission("report.comment.delete")) {
                    ((List<String>) configFile.get("Messages.noPermissions")).forEach(s -> whoClicked.sendMessage(s));
                    return;
                }
                deleteUpdateGUI(whoClicked);
            } else if (slot == 13) {
                if (!whoClicked.hasPermission("report.flag")) {
                    ((List<String>) configFile.get("Messages.noPermissions")).stream().forEach(s -> whoClicked.sendMessage(loadColor(s)));
                    return;
                }
                openFlagUpdateGUI(whoClicked);
            } else if (slot == 17) {
                goBackListGUI(whoClicked);
            } else if (slot == 16) {
                goToConfirmGUI(whoClicked);
            }else if (slot == 21){
                if (ReportManager.doesReportExistsById(report_id)){
                    Report report = ReportManager.getSpecificReportById(report_id);
                    double[] loc = report.getLocation();
                    Location location = new Location(Bukkit.getWorld("World"), loc[0], loc[1], loc[2]);
                    whoClicked.teleport(location);

                    ((List<String>) configFile.get("Messages.SuccessfullyTeleported")).forEach(s -> {
                        if (s.contains("{x}")) s = s.replace("{x}", String.valueOf(loc[0]));
                        if (s.contains("{y}")) s = s.replace("{y}", String.valueOf(loc[1]));
                        if (s.contains("{z}")) s = s.replace("{z}", String.valueOf(loc[2]));
                        whoClicked.sendMessage(loadColor(s));
                    });
                    return;
                }else{
                    ((List<String>) configFile.get("Messages.reportDeletedInside")).forEach(s-> whoClicked.sendMessage(loadColor(s)));
                }
            }else if (slot == 19 || slot == 20){
                goToUserInfo(whoClicked, slot);
            }
        }
    }

    public void openFlagUpdateGUI(Player whoClicked){
        FlagReportGUI flagReportGUI = new FlagReportGUI(report_id, current_page);
        whoClicked.openInventory(flagReportGUI.getInventory());
    }

    public void goBackListGUI(Player whoClicked){
        ListGUI listGUI = new ListGUI(current_page - 1);
        whoClicked.openInventory(listGUI.getInventory());
    }

    public void goToConfirmGUI(Player whoClicked){
        ConfirmGUI confirmGUI = new ConfirmGUI(report_id, this, true);
        whoClicked.openInventory(confirmGUI.getInventory());
    }

    public void goToUserInfo(Player whoClicked, int slot){
        UserInfoGUI userInfoGUI = new UserInfoGUI(slot == 19 ? getUuidOfPlayer(defense) : getUuidOfPlayer(attacker), true, report_id, current_page);
        whoClicked.openInventory(userInfoGUI.getInventory());
    }

    public void deleteUpdateGUI(Player whoClicked){
        DeleteGUI deleteGUI = new DeleteGUI(report_id, this);
        deleteGUIInventory = deleteGUI.getInventory();
        whoClicked.openInventory(deleteGUIInventory);
        new BukkitRunnable(){
            @Override
            public void run() {
                if (!whoClicked.getOpenInventory().getTopInventory().equals(deleteGUIInventory)){
                    cancel();
                    return;
                }
                deleteGUIInventory = deleteGUI.getInventory();
                whoClicked.openInventory(deleteGUIInventory);
            }
        }.runTaskTimer(plugin, 00l,20l);
    }

    private ItemStack getPlayerHead(OfflinePlayer player){
        ItemStack itemSkull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta metaSkull = (SkullMeta) itemSkull.getItemMeta();
        metaSkull.setOwningPlayer(player);
        itemSkull.setItemMeta(metaSkull);
        return itemSkull;


    }

    private String getFlag(EnumFlag enums){
        switch(enums){
            case OPEN:
                return "&a&lOpen";
            case CLOSED:
                return "&c&lClosed";
            case PENDING:
                return "&5&lPENDING";
            case WORKING:
                return "&e&lWORKING";
        }
        return null;
    }

    public boolean isPlayerOnline(String name){
        return getPlayer(name).isOnline();
    }

    public UUID getUuidOfPlayer(String name){
        return getPlayer(name).getUniqueId();
    }

    public SpecifyReportGUI getInstance(){
        return this;
    }
    @Override
    public void setItem(Inventory inventory, int slot, Material type, String name, String... lore){
       int amount = 1;
        if (type == deleteCommentItem) amount = ReportManager.getSpecificReportById(report_id).getComments().length > 1 ? ReportManager.getSpecificReportById(report_id).getComments().length : 1;
        if (type == editItem) amount = ReportManager.getSpecificReportById(report_id).getComments().length > 1 ? ReportManager.getSpecificReportById(report_id).getComments().length : 1;

        inventory.setItem(slot, new ItemBuilder(type).setDisplayName(name).setLore(lore).setAmount(amount).build());
    }

    public void setPlayerHeadGui(Inventory inventory, int slot, ItemStack item, String name){
        inventory.setItem(slot, new ItemBuilder().setItem(item).setDisplayName("&eGo to "+(isPlayerOnline(name) ? "&a" : "&c")+name + " &7info&7!").setLore("&eClick to go to &7userinformation &eabout &a&nplayer", "&8&m----------------------------------","&ePlayer "+(isPlayerOnline(name) ? "&a&n"+name+"&e is &aonline" : "&c&n"+name+"&e is &4offline")).build());
    }

    public String loadColor(String string){return ChatColor.translateAlternateColorCodes('&', string);}

}
