package com.simonsejse.Inventorys;

import com.simonsejse.Builders.ItemBuilder;
import com.simonsejse.ChatManage;
import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportSystem;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class UserInfoGUI implements InvGUI {

    ReportSystem plugin = ReportSystem.getPlugin(ReportSystem.class);

    private Player guiOpener;
    private String user, dangerous;
    private int current_page;
    private FileInterface configFile;
    private DecimalFormat df;

    private int warningLevel, reportSent;
    private int report_id;
    private int clickId = 0;
    private UUID uuid;
    private double health, level, saturation, exhaustion;
    private GameMode gamemode;
    private boolean isFlying, online, sneaking, onGround, sleeping, sprinting, swimming, whitelisted, isOp, fromSpecifyGUI, emptyIsZero;

    private Inventory playersInventory, restoreInv, kitRestoreMenu, currentInventory, deleteRestoreGUI;
    private float flyspeed, walkspeed;
    private double[] location = new double[3];
    private double facingX,facingY,facingZ, distance;
    private String[] effects;
    private String ip;


    private int getPage(){
        return current_page;
    }

    private String getUser(){
        return user;
    }

    public void setUser(String user){
        this.user = user;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(uuid);
    }

    public Inventory getRestoreInv(){
        return restoreInv;
    }

    public Inventory getKitRestoreMenu(){
        return kitRestoreMenu;
    }

    //Preview current inventory inside the getKitRestoreMenu()
    public Inventory getCurrentInventory(){
        return currentInventory;
    }

    public Inventory getDeleteRestoreGUI(){
        return deleteRestoreGUI;
    }

    private void update(){
        if (getOfflinePlayer().isOnline()){
            Player target = getPlayer();
            health = target.getHealth();
            level = target.getLevel();
            saturation = target.getFoodLevel();
            exhaustion = target.getExhaustion();
            isFlying = target.isFlying();
            online = target.isOnline();
            sneaking = target.isSneaking();
            sleeping = target.isSleeping();
            onGround = target.isOnGround();
            sprinting = target.isSprinting();
            gamemode = target.getGameMode();
            swimming = target.isSwimming();
            whitelisted = target.isWhitelisted();
            isOp = target.isOp();
            walkspeed = target.getWalkSpeed();
            flyspeed = target.getFlySpeed();
            facingX = target.getFacing().getDirection().getX();
            facingY = target.getFacing().getDirection().getY();
            facingZ = target.getFacing().getDirection().getZ();
            location[0] = target.getLocation().getX();
            location[1] = target.getLocation().getY();
            location[2] = target.getLocation().getZ();
            distance = target.getLocation().distance(guiOpener.getLocation());
            effects = new String[]{};
            for(PotionEffect potionEffect : target.getActivePotionEffects()){
                addPotion(potionEffect);
            }

            try(ResultSet rs = plugin.getStatement().executeQuery("SELECT * FROM users WHERE userUuid='"+uuid+"'")){
                if (rs.next()){
                    ip = rs.getString("userIp");
                    warningLevel = rs.getInt("warningLevel");
                    reportSent = rs.getInt("reportSent");
                    if (warningLevel >= 30){
                        dangerous = "&4&lBAN";
                    }else if (warningLevel >= 20){
                        dangerous = "&4&lVERY HIGH";
                    }else if (warningLevel >= 10){
                        dangerous = "&c&lHIGH";
                    }else if (warningLevel >= 5){
                        dangerous = "&e&lMEDIUM";
                    }else{
                        dangerous = "&a&lLOW";
                    }
                }

            }catch(SQLException sql){
                Bukkit.getLogger().log(Level.SEVERE, null, UserInfoGUI.class);
            }

        }else{
            OfflinePlayer op = getOfflinePlayer();
            online = op.isOnline();
            isOp = op.isOp();
        }
    }

    public double getHealth(){
        return health;
    }
    public double getLevel(){
        return level;
    }
    public double getSaturation(){
        return saturation;
    }
    public double getExhaustion(){
        return exhaustion;
    }
    public boolean isFlying(){
        return isFlying;
    }
    public boolean isOnline(){
        return online;
    }
    public boolean isOnGround(){
        return onGround;
    }
    public boolean isSneaking(){
        return sneaking;
    }
    public boolean isSprinting(){
        return sprinting;
    }
    public boolean isSleeping(){
        return sleeping;
    }
    public boolean isOp(){
        return isOp;
    }
    public UUID getUuid(){
        return uuid;
    }
    public double getFacingX(){
        return facingX*10;
    }
    public double getFacingY(){
        return facingY*10;
    }
    public double getFacingZ(){
        return facingZ*10;
    }
    public float getFlySpeed(){
        return flyspeed*10;
    }
    public float getWalkSpeed(){
        return walkspeed*10;
    }
    public boolean isSwimming(){
        return swimming;
    }
    public boolean isWhitelisted(){
        return whitelisted;
    }
    public String getIp(){
        return ip;
    }
    public GameMode getGameMode(){
        return gamemode;
    }

    private int getCurrent_Page() {
        return current_page;
    }

    private int getWarningLevel(){
        return warningLevel;
    }

    private int getReportSent(){
        return reportSent;
    }

    private int getReport_Id() {
        return report_id;
    }

    public Inventory getPlayersInventory(){
        return playersInventory;
    }

    private String getDangerous(){
        return dangerous;
    }

    public UserInfoGUI(){

    }

    public UserInfoGUI(UUID uuid, boolean fromSpecifyGUI, int report_id, int current_page){
        this.uuid = uuid;
        this.fromSpecifyGUI = fromSpecifyGUI;
        this.report_id = report_id;
        this.current_page = current_page;
        init();
    }

    public UserInfoGUI(UUID uuid, boolean fromSpecifyGUI){ //use on command /report view simonsejse
        this.fromSpecifyGUI = fromSpecifyGUI;
        this.uuid = uuid;
        init();
    }

    public void init(){
        user = getOfflinePlayer().getName();
        configFile = ReportSystem.getConfigFile();
        df = new DecimalFormat("0.00");
    }


    public Inventory getInventory(){
        Inventory inventory = Bukkit.createInventory(this, 9*6, "UserInfo on "+user);
        current_page += 1;
        IntStream.range(0, 9*6).filter(n -> n>=0 && n < 9 || n == 9 || n == 17 || n == 18 || n == 26||n==27||n==35||n==36||n==44||n>44 && n<53).forEach(n -> setItem(inventory, n, Material.GRAY_STAINED_GLASS_PANE, " ",""));
        StringBuilder sb = new StringBuilder();
        ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).forEach(s-> sb.append(s));
        setItem(inventory, 53, Material.RED_BED, "&c&lGo back", sb.toString());

        new BukkitRunnable(){
            @Override
            public void run(){
                if (guiOpener == null){
                    guiOpener = (Player) inventory.getViewers().get(0);
                }
                if (inventory.getViewers().size() < 1){
                    cancel();
                }
                update();
                setPlayerHeadGui(inventory, 20, getPlayerHead(), "&eData");
                setItem(inventory, 23, Material.POTION, "&dPotion Effects", "", "&7Effects &e» "+(getPotionEffects().length() == 0 ? "&cNo effects applied." : "\n"+getPotionEffects()), "");
                setItem(inventory, 30, Material.CHEST, "&6Open "+user+ "'s inventory", "", "&7Click to open &c"+user+"'s&7 inventory", "&7And be prompt with a GUI", "&7containing "+user+"'s inventory items.",(isOnline() == true ? "&a"+user+"&7 is &8(&aonline&8)" : "&c"+user+"&7 is &8(&coffline&8)"), "&7only possible if player is online.");
                setItem(inventory, 28, Material.BOOK, "&9Info", "", "&7Warning level: &6"+getWarningLevel(), "&7Danger: &6"+getDangerous(),"","&7Reports sent: &6"+getReportSent());

            }
        }.runTaskTimer(ReportSystem.getPlugin(ReportSystem.class), 0l, 1l);
        IntStream.range(9,9*5).filter(tal -> inventory.getItem(tal) == null).forEach(tal -> setItem(inventory, tal, Material.WHITE_STAINED_GLASS_PANE, "&7【&c&l✪&7】", ""));
        return inventory;
    }

    public void setPlayerHeadGui(Inventory inventory, int slot, ItemStack item, String name){
        inventory.setItem(slot, new ItemBuilder().setItem(item).setDisplayName(name).setLore("", "&eReported: "+(isOnline() ? "&a"+user+" &7(&aonline&7)" : "&c"+user+" &7(&coffline&7)"), "&7Gamemode &e» &6"+getGameMode().name().toLowerCase()+"&8,"+" &7On ground &e» &c"+(isOnGround() ? "&ayes" : "&cno"), "&7Health &e» &c"+getHealth() +"&8/&c20&8, &7Level &e» &6"+getLevel(),"&7Saturation &e» &c"+getSaturation()+"&8/&c20"+"&8, &7Exhaustion &e» &6"+(df.format(getExhaustion())), "&7Is Flying &e» "+(isFlying() ? "&ayes" : "&cno")+"&8, &7Is Sneaking &e» "+(isSneaking() ? "&ayes" : "&cno")+"&8, &7Is Sleeping &e» "+(isSleeping() ? "&ayes" : "&cno")+"&8, &7Is Sprinting &e» "+(isSprinting() ? "&ayes" : "&cno"), "&7Is Whitelisted &e» "+(isWhitelisted() ? "&ayes" : "&cno")+"&8, &7Is Swimming &e» "+(isSwimming() ? "&ayes" : "&cno"),"&7Walk Speed &e» &6"+getWalkSpeed() +"&8/&c10&8, &7Fly Speed &e» &6"+getFlySpeed()+"&8/&c10&8","&7Facing X &e» &c"+getFacingX() +", &7Facing Y &e» &6"+getFacingY()+", &7Facing Z &e» &e"+getFacingZ(), "&e&lLOC &6X &e» &7"+df.format(location[0])+"&8, "+"&e&lLOC &6Y &e» &7"+df.format(location[1])+"&8, &e&lLOC &6Z &e» &7"+df.format(location[2])," &8| &fDistance From "+user+" &e» &a&l"+df.format(getDistance()), "&7Ip &e» &e"+getIp()+"&8, &7Is OP &e» &6"+(isOp() ? "&ayes" : "&cno"), "&7UUID &e» &8"+getUuid(), "").build());
    }




    public double getDistance(){
        return distance;
    }

    public void addPotion(PotionEffect potionEffect){
        effects = (String[]) ArrayUtils.add(effects, "&8&l | &8[&7Minutes. &6» &c"+(df.format((double) potionEffect.getDuration()/1200))+"&8]&7:&8[&7Lvl. &8» &6"+potionEffect.getAmplifier()+"&8]&7:&8[&7Name &6» &e"+potionEffect.getType().getName().toLowerCase()+"&8]"+"\n");
    }

    public String getPotionEffects(){
        StringBuilder sb = new StringBuilder();
        Arrays.stream(effects).forEach(s -> sb.append(s));
        return sb.toString();
    }

    public OfflinePlayer getOfflinePlayer(){
        return Bukkit.getOfflinePlayer(uuid);
    }

    private ItemStack getPlayerHead() {
        ItemStack itemSkull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta metaSkull = (SkullMeta) itemSkull.getItemMeta();
        metaSkull.setOwningPlayer(getOfflinePlayer());
        itemSkull.setItemMeta(metaSkull);
        return itemSkull;
    }

    public void onPlayerInventoryGUI(ItemStack item, int slot, Player whoClicked){
        if (slot == 49) {
            if (getOfflinePlayer().isOnline()){
                whoClicked.openInventory(getInventory());
                //Remove one from current_page, since we run getInventory() and it adds one to current_page
                current_page-=1;
            }else{
                whoClicked.closeInventory();
                ((List<String>) configFile.get("Messages.playerNotOnline")).forEach(s -> whoClicked.sendMessage(loadColor(s)));
            }
        }else if (slot == 47){
            emptyInventory(whoClicked);
        }else if (slot ==46){
            whoClicked.openInventory(openSpecificRestore(whoClicked));
        }
    }

    private void emptyInventory(Player whoClicked){
        String deleter = whoClicked.getName();
        ItemStack[] inventoryContents = new ItemStack[41];
        ItemStack air = new ItemStack(Material.AIR);
        int amountItems = 0;
        for(int i = 0;i<41;i++){
            if (playersInventory.getItem(i) != null){
                amountItems+=playersInventory.getItem(i).getAmount();
                inventoryContents[i] = playersInventory.getItem(i);
                playersInventory.setItem(i, air);
            }
        }

        if (amountItems < 1){
            if (!emptyIsZero){
                emptyIsZero = true;
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        emptyIsZero = false;
                        cancel();
                        return;
                    }
                }.runTaskLater(plugin, 80l);
            }else{
                whoClicked.sendMessage(ChatManage.loadColor("&8&l | &cStaff &8: &fStop spamming (EMPTY ITEMS)"));
            }
        }else {
            saveInventoryToRestore(inventoryContents, amountItems, deleter);
        }
    }

    private void saveInventoryToRestore(ItemStack[] inventoryContents, int total, String deleter){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String date = dtf.format(LocalDateTime.now());


    }

    private String loadColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public void onGuiClick(ItemStack item, int slot, Player whoClicked){
        if (slot == 53){
            if(fromSpecifyGUI){
                SpecifyReportGUI specifyReportGUI = new SpecifyReportGUI(report_id, current_page - 1);
                whoClicked.openInventory(specifyReportGUI.getInventory());
                return;
            }
            whoClicked.closeInventory();
        }else if (slot == 30){
            if (getOfflinePlayer().isOnline()){
                whoClicked.openInventory(getInventorySubMenu(whoClicked));
            }else{
                 whoClicked.closeInventory();
                ((List<String>) configFile.get("Messages.playerNotOnline")).forEach(s -> whoClicked.sendMessage(s));
            }
        }
    }
    /*
       @param THIS MENU IS WHEN YOU CLICK THE RESTORE
     */
    public void onRestoreGUIClick(ItemStack item, int slot, Player whoClicked, boolean isShift){
        if (slot == 49){
            whoClicked.openInventory(getInventorySubMenu(whoClicked));
        }else{
            clickId = slot;
            if (isShift){
                //Delete
                whoClicked.openInventory(deleteRestoreDataGUI(whoClicked));
                return;
            }
            whoClicked.openInventory(showKitGUI());
        }
    }

    public void onDeleteRestoreGUIClick(int slot, Player whoClicked){
       if (slot == 2){
            //List<RestoreInventory> restoreInventory = ReportManager.getRestoreInventorys().get(getUuid());
            //restoreInventory.remove(clickId);
        }
        whoClicked.openInventory(openSpecificRestore(whoClicked));
    }

    public Inventory deleteRestoreDataGUI(Player whoClicked){
        deleteRestoreGUI = Bukkit.createInventory(this, 9, "Are you sure?");
        setItem(deleteRestoreGUI, 2, Material.GREEN_STAINED_GLASS_PANE, "&a&lCONFIRM", "", "&fClick &7to confirm a delete of the restoring data!","","&cBeware, the inventory will be &4forever &7lost!");
        StringBuilder lore = new StringBuilder();
        ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).forEach(s -> lore.append(s));
        setItem(deleteRestoreGUI, 6, Material.RED_STAINED_GLASS_PANE, "&c&lDECLINE", "");
        return deleteRestoreGUI;
    }
     /*
           @param THIS MENU IS WHEN YOU CLICK A RESTORE ITEM INSIDE RESTORE MENU
        */
    public void onKitRestoreGUIClick(ItemStack item, int slot, Player whoClicked){
        if (slot == 49){
            whoClicked.openInventory(openSpecificRestore(whoClicked));
        }else if (slot == 47){

        }else if (slot == 51){

        }else if (slot == 46){
            whoClicked.openInventory(openCurrentInventoryGUI(whoClicked));
        }
    }

    /*
         @param THIS MENU IS WHEN YOU CLICK CURRENT INVENTORY
      */
    public void onCurrentInventoryGUIClick(ItemStack item, int slot, Player whoClicked){
        if (slot == 49){
            whoClicked.openInventory(showKitGUI());
        }
    }


    public Inventory openCurrentInventoryGUI(Player whoClicked){
        currentInventory = Bukkit.createInventory(this, 9*6, "Previwing current inventory:");
        if (getOfflinePlayer().isOnline()){
            currentInventory.setContents(getPlayer().getInventory().getContents());
        }
        StringBuilder lore = new StringBuilder();
        ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).forEach(s -> lore.append(s));
        setItem(currentInventory, 49, Material.RED_BED, "&c&lGo back to previous GUI.", lore.toString());
        return currentInventory;
    }

    public void setItem(Inventory inv, int slot, ItemStack item, String name, String... lore){
        inv.setItem(slot, new ItemBuilder().setItem(item).setDisplayName(name).setLore(lore).build());
    }

    public Inventory getInventorySubMenu(Player whoClicked){
        playersInventory = Bukkit.createInventory(this, 9*6, "Viewing "+user+" inventory");
        playersInventory.setContents(getPlayer().getOpenInventory().getBottomInventory().getContents());
        for(int i = 41;i<54;i++){
            if (i!=49 ||i!=47||i!=46) setItem(playersInventory, i, Material.GRAY_STAINED_GLASS_PANE, "", "");
        }
        StringBuilder lore = new StringBuilder();
        ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).forEach(s -> lore.append(s));
        setItem(playersInventory, 46, Material.PAPER, "&6&lRESTORE INVENTORY", "&8&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&7Click this to restore &8" + getName(), "&6inventory!", "&7&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        setItem(playersInventory,49, Material.RED_BED, "&c&lGo back to previous GUI.", lore.toString());
        new BukkitRunnable(){
            @Override
            public void run() {
                if (playersInventory.getViewers().size() < 1) {
                    cancel();
                }
                if (!getOfflinePlayer().isOnline()) {
                    whoClicked.closeInventory();
                    whoClicked.sendMessage(ChatManage.getMsg("Messages.PlayerLeft").replace("{player}", getOfflinePlayer().getName()));
                    cancel();
                }
                int amountItems = 0;
                for (int i = 0; i < 41; i++) {
                    if (playersInventory.getItem(i) != null) amountItems += playersInventory.getItem(i).getAmount();
                }
                if (emptyIsZero) {
                    StringBuilder lore = new StringBuilder();
                    ((List<String>) configFile.get("UserInfoGUI.noItemsLore")).forEach(s -> lore.append(s));
                    setItem(playersInventory, 47, Material.BARRIER, "&c&lCannot EMPTY inventory!", lore.toString());
                } else {
                    setItem(playersInventory, 47, Material.BUCKET, "&f&lEMPTY INVENTORY", "&8&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&7Click this to empty &8" + getName(), "&6inventory!", "&6" + amountItems + "&7 items will be &cremoved.", "&7&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                }
            }
        }.runTaskTimer(plugin, 0l, 0l);
        return playersInventory;
    }



    public Inventory openSpecificRestore(Player whoClicked){
        restoreInv = Bukkit.createInventory(this, 9*6, loadColor("&cRestore players Inventory"));
        //if (ReportManager.getRestoreInventorys().get(getUuid()) != null){
        //    List<RestoreInventory> list = ReportManager.getRestoreInventorys().get(getUuid());
        //    for(int i = 0;i<list.size();i++){
        //        setItem(restoreInv, i, Material.ANVIL, "&e&lID&7: &f"+(i+1), "&7Amount of items deleted &8» &6"+list.get(i).getTotal(),"", "&7Inventory was deleted at &8»","&8&l | &6Date &8| &c"+list.get(i).getDate(),"", "&7Deleted by &8»", "&8&l | &6Player &9"+list.get(i).getDeleter()+" &8|","","&7Lost Inventory &8»", "&8&l | &6Player &f"+list.get(i).getAttacker()+" &8|", "","&fClick &7to see the &6inventorys contents&7!", "&7and decide whether &6"+getName(), "&7should retrive his stuff or not.","&7(&fShift-click&7) to &cdelete&7 the &6restoration data&7!");
        //     }
        // }
        StringBuilder lore = new StringBuilder();
        ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).forEach(s -> lore.append(s));
        setItem(restoreInv, 49, Material.RED_BED, "&c&lGo back to previous GUI.", lore.toString());

        return getRestoreInv();
    }

    public Inventory showKitGUI(){
        kitRestoreMenu = Bukkit.createInventory(this, 9*6, String.valueOf(clickId));
        // List<RestoreInventory> restoreInventories = ReportManager.getRestoreInventorys().get(getUuid());
        int i = 0;
        // for(ItemStack item : restoreInventories.get(clickId).getInventoryContents()){
        //     if (item != null) setItem(kitRestoreMenu, i, item, "", "");
        //    i+=1;
        // }
        StringBuilder lore = new StringBuilder();
        ((List<String>) configFile.get("SpecifyReportGUI.goBackSpecify")).forEach(s -> lore.append(s));

        setItem(kitRestoreMenu, 46, Material.CHEST, "&9"+getName()+"'s &fcurrent inventory", "", "&fClick here &7to show &6"+getName()+"'s&7 current inventory!");
        setItem(kitRestoreMenu, 47, Material.GREEN_STAINED_GLASS_PANE, "&a&lCONFIRM", "", "&fClick &7to confirm a restoration of", "&6"+getName()+"&7 inventory!","","&cBeware, the current inventory cannot be saved!");
        setItem(kitRestoreMenu, 49, Material.RED_BED, "&c&lGo back to previous GUI.", lore.toString());
        setItem(kitRestoreMenu, 51, Material.RED_STAINED_GLASS_PANE, "&c&lDECLINE", "lore.toString()");
        IntStream.range(45,54).filter(num -> num != 46 && num != 47 && num != 49 && num != 51).forEach(num -> setItem(kitRestoreMenu, num, Material.BLACK_STAINED_GLASS_PANE, "&7【&c&l✪&7】", ""));
        return kitRestoreMenu;
    }


    private String getName(){
        return getOfflinePlayer().getName();
    }

    public void setInventoryContent(ItemStack[] content){
        if (getOfflinePlayer().isOnline()){
            getPlayer().getInventory().setContents(content);
        }
    }

    @Override
    public void setItem(Inventory inventory, int slot, Material type, String name, String... lore){
        inventory.setItem(slot, new ItemBuilder(type).setDisplayName(name).setLore(lore).addFlags(ItemFlag.HIDE_ATTRIBUTES).addFlags(ItemFlag.HIDE_POTION_EFFECTS).build());
    }

    public void sideMenu(){
         /* int[] itemSlot = new int[]{ 0 };
        int[] amountOfReports = new int[] { 0 };
        ReportManager.getReportList().stream().filter(report -> report.getDefenseUsername().equalsIgnoreCase(user)).filter(report -> report.getId() >= (current_page * 52)-52).filter(report -> report.getId() < (current_page*52)).forEach(report -> {
            setItem(inventory, 53, Material.SUNFLOWER, "&e&lGo to next page", "&cClick this to go to next reports of "+user);

            amountOfReports[0]+=1;
            StringBuilder comment = new StringBuilder();
            for(Comment c : report.getComments()){
                ((List<String>) configFile.get("Messages.ViewGUI.comments")).forEach(s -> {
                    if (s.contains("{id}")) s = s.replace("{id}", String.valueOf(c.getId()));
                    if (s.contains("{date}")) s = s.replace("{date}", c.getDate());
                    if (s.contains("{commenter}")) s = s.replace("{commenter}", c.getCommenter());
                    if (s.contains("{comment}")) s = s.replace("{comment}", c.getComment());
                    comment.append(s).append("\n");
                });
            }
            StringBuilder sb = new StringBuilder();
            String flag = getFlag(report.isFlag());
            ((List<String>) configFile.get("Messages.ViewGUI.lore")).forEach(s -> {
                if (s.contains("{id}")) s = s.replace("{id}", String.valueOf(report.getId()));
                if (s.contains("{defenseUsername}")) s = s.replace("{defenseUsername}", report.getDefenseUsername());
                if (s.contains("{attackerUsername}")) s = s.replace("{attackerUsername}", report.getAttackerUsername());
                if (s.contains("{date}")) s = s.replace("{date}", report.getDate());
                if (s.contains("{reason}")) s = s.replace("{reason}", report.getReason());
                if (s.contains("{flag}")) s = s.replace("{flag}", flag);
                if (s.contains("{comLength}")) s = s.replace("{comLength}", String.valueOf(report.getComments().length));
                if (s.contains("{comments}")) s = s.replace("{comments}", comment.toString());
                sb.append(s).append("\n");

            });
            setItem(inventory, itemSlot[0]++, Material.PAPER, "&fReport ID: &7"+report.getId(), sb.toString());
        });
         setItem(inventory, 52, Material.LEATHER_CHESTPLATE, "&e&lGo back to previous", "&cClick this to go back to previous page!");

        */
    }


}
