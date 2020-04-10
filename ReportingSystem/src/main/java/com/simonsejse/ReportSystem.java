package com.simonsejse;

import com.simonsejse.Commands.ReportCmd;
import com.simonsejse.FileLoadSaver.ConfigFile;
import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.Inventorys.GUIEventHandler;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.EnumFlag;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.SubCommands.Flag;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ReportSystem extends JavaPlugin {

            //█▀▀█ █▀▀ █▀▀█ █▀▀█ █▀▀█ ▀▀█▀▀ 　 　 　 　
            //█▄▄▀ █▀▀ █░░█ █░░█ █▄▄▀ ░░█░░ 　 　 　 　
            //▀░▀▀ ▀▀▀ █▀▀▀ ▀▀▀▀ ▀░▀▀ ░░▀░░ 　

    private Comment[] comment;

    private static FileInterface configFile;
    /*
       @param Creating one instance of the ReportManager class, that contains static methods.
    */
    private ReportManager rm;

    private Connection connection;
    private Statement statement;
    private Statement secondStatement;
    private MySQLManaging mySQLManaging;

    private String host, username, password, database;
    private int port;

    private static final String CREATE_TABLE_REPORT_SQL = "CREATE TABLE IF NOT EXISTS reports ("
            +"reportId int(11) NOT NULL ,"
            +"defenseUsername varchar(100) NOT NULL,"
            +"attackerUsername varchar(100)  NOT NULL,"
            +"dateTime varchar(100)  NOT NULL,"
            +"flag VARCHAR(10) NOT NULL,"
            +"reason varchar(255) NOT NULL,"
            +"x double(11,0) NOT NULL ,"
            +"y double(11,0) NOT NULL ,"
            +"z double(11,0) NOT NULL ,"
            +"PRIMARY KEY (reportId))";

    private static final String CREATE_TABLE_COMMENTS_SQL = "CREATE TABLE IF NOT EXISTS comments ("+
            "reportId int(11) NOT NULL ," +
            "commentId int(50) NOT NULL ," +
            "comment varchar(100) NOT NULL ," +
            "commenter varchar(100) NOT NULL ," +
            "dateTime varchar(100) NOT NULL" +
            ")";


    private static final String CREATE_TABLE_USER_SQL = "CREATE TABLE IF NOT EXISTS users ("+
            "userUuid varchar(200) NOT NULL ," +
            "warningLevel int(11) NOT NULL, " +
            "reportSent int(11) NOT NULL, " +
            "userIp varchar(30) NOT NULL, " +
            "PRIMARY KEY (userUuid))";

    private static final String CREATE_TABLE_INVENTORY_SQL = "CREATE TABLE IF NOT EXISTS inventory ("+
            "inventoryId int(11) NOT NULL ,"
            +"defenseUsername varchar(100) NOT NULL,"
            +"attackerUsername varchar(100)  NOT NULL,"
            +"dateTime varchar(100)  NOT NULL,"
            +"total int(11) NOT NULL"
            +")";

    private static final String CREATE_ITEMS_INVENTORY_SQL = "CREATE TABLE IF NOT EXISTS inventoryItems ("+
            "inventoryId int(11) NOT NULL ," +
            "Material varchar(200) NOT NULL ," +
            "name varchar(255) NOT NULL, " +
            "lore varchar(255) NOT NULL)";



    //only ran if (!f.exists()) - where F is a File to config.yml
    public void setupYmlMessages() {
        List<String> NO_ARGS = Arrays.asList("", "&4&l>> &4Error&c: &4&l<<", "    &7&l-    &eUsage: &7/&8reports &7<&8view/comment/list/flag&7>&4!", "", "");
        List<String> ONE_ARGUMENT = Arrays.asList("", "&4&l>> &4Error&c: &4&l<<", "    &7&l-    &eUsage: &7/&8reports &7<&8view>&7> &7<&4&l&nID&7>&4!", "", "");
        List<String> NOT_A_NUMBER = Arrays.asList("", "&4&l>> &4Error&c: &4&l<<", "    &7&l-    &e&l{number}&e is not a number.", "", "");
        List<String> reportWasDeleted = Arrays.asList("", "","&4&l>> &4Error&c:&4&l <<", "&c&lReports &f> &f[&cStaff&f]: &eThe report was deleted by someone else while you were inside!", "&c&lReports &f> &f[&cStaff&f]: &eTherefore you were automatically brought back to the ListGUI!");
        List<String> report_not_found = Arrays.asList("&c&lReports &f> &f[&cStaff&f]: &c&lID&c, &4couldn't be found!");

        List<String> noPermission = Arrays.asList("", "","&4&l>> &4Error&c:&4&l <<"," &8&l  -  &c&lReports &f> &cYou dont have the required permission to run command.", "","");


        //errors end
        List<String> playerLeft = Arrays.asList("&c&lReports &f> &f[&c{player}&f]: &7left the server, therefore you were thrown out the menu. ;)");

        ////report viewer and reporting someone messages
        List<String> missingReason = Arrays.asList("", " "," ","&4&l>> &4Error&c:&4&l <<"," &8&l  -  &eUsage: &7/&8reports &7<&8name&7> <&4&nreason&7>"," "," ","");
        List<String> reportYourself = Arrays.asList("","", "&4&l>> &4Error&c: &4&l<<", "    &7&l-    &e&lCan't report yourself.", "", "");
        List<String> report_viewer = Arrays.asList("","&4&l&m=============[&c&lReport ✦ Viewer&4&l&m]=============", "&f> &7Viewing record with ID &c{id}&7:", "&4&m-------------------------", "&7Reported player: &c{reportedPlayer}", "&7Player who reported: &c{player} ", "&7Date (YYYY-MM-DD) (EST): &c{date} ", "&7Reason: &c{reason}", "&7flag: {flag}", "&7Found &c{amountcomments}&7 comments:", "&4&m-------------------------", "{comments}", "&4&l&m=============[&c&lReport Viewer&4&l&m]=============");
        List<String> reported_someone_successful = Arrays.asList("&c&lReports &f> &e&lID: &e{id}", "&c&lReports &f> &4Note: You have reported &c{reportedPlayer}&4 for &7{reason}&4.", "&c&lReports &f> &7Thank you. Your report has been filed."," &8&l- &7Your name: &8(&c {player}&8 )"," &8&l- &7Player you reported: &8(&c {reportedPlayer}&8 )"," &8&l- &7Date (YYYY-MM-DD) (EST): &c{date} "," &8&l- &7Reason:"," &7> &c{reason} "," &7&l** Staff should view your report within 48 hours!");
        List<String> playerNeverPlayed = Arrays.asList("&c&lReports &f> &f[&c{player}&f]: has never played on the server before? Why report him. ;)");
        List<String> NotOnline = Arrays.asList("&c&lReports &f> &f[&c{player}&f]: is not online :)");



        List<String> wait = Arrays.asList("&8&l| &e&lWAIT &8&l| &eChecking the database, please wait...");

        //comment config messages
        List<String> NO_ARGUMENT_COMMENT = Arrays.asList("","","","&4&l>> &4Error&c:&4&l <<", " &8&l  -  &eUsage: &7/&8reports &7<&8comment&7> <&4&l&nID&7> <&8comment&7> ", "", "","");
        List<String> commented_successfully = Arrays.asList("&c&lReports &f> &eYou have succesfully commented on &lTICKET &eID: &l{id}&7!");
        List<String> one_argument_given = Arrays.asList("", "", "", "&4&l>> &4Error&c:&4&l <<", " &8&l  -  &eUsage: &7/&8reports &7<&8&ncomment&7> <&8ID&7> <&4&l&ncomment&7>&4", "", "", "");
        List<String> closed_thread = Arrays.asList("", "&c&lReports &f> &f[&cStaff ✦&f]:&e You can't comment on a closed thread!");

        List<String> editedComment = Arrays.asList("", "", "&c&lReports &f> &eYou have succesfully edited line {line} on &lTICKET &eID: &l{id}&7!", "&c&lReports &f> &f[&cStaff&f]&8: &fNew comment »", " &8&l- &f{comment}", "", "");
        List<String> commentDoesntExist = Arrays.asList("", "&4&l>> &4Error&c: &4&l<<", "    &7&l-    &eThe comment you're trying to edit doesn't exist.","    &7&l-    &eAdd by using &l/report comment <id> <comment>&e or try another line!", "", "");
        List<String> noComments = Arrays.asList("", "&4&l>> &4Error&c: &4&l<<", "    &7&l-    &eThere's no comments on this report.","    &7&l-    &eAdd by using &l/report comment <id> <comment>", "", "");
        List<String> invalidLine = Arrays.asList("", "&c&lReports &f> &f[&cStaff ✦&f]:&e The comment doesn't exist", "&c&lReports &f> &f[&cStaff ✦&f]:&e The chosen line: {line} is invalid: ");
        List<String> editCommentChat = Arrays.asList("", "&c&lReports &f> &f[&cStaff ✦&f]:&e To edit a comment type the following in chat:", "&8  » &f<line> <new comment>", "&8  » &c 1 I'm a new comment", "&4WRITE &c&ncancel&4 if you regret!");

        List<String> editArgumentMissing = Arrays.asList("", "", "", "&4&l>> &4Error&c:&4&l <<", " &8&l  -  &eUsage: &7/&8reports &7<&8comment&7> <&8edit&7> <&8id&7> <&8line&7> <&4&l&ncomment&7>&4", "", "", "");

        List<String> maximumCommentsMessage = Arrays.asList("", "&c&lReports &f> &f[&cStaff ✦&f]:", " &cThe amount of maximum comments on the report has been reached!");

        //delete config messages
        List<String> deleteOneArgument = Arrays.asList("", "", "&4&l>> &4Error&c:&4&l <<", " &8&l  -  &eUsage: &7/&8reports &7<&8delete&7> <&4&l&nid&7>&4", "", "");
        List<String> deletedSuccess = Arrays.asList("", "&c&lReports &f> &f[&cStaff ✦&f]:&e {id} has been &4&l&nERASED&e.");


        //DeleteGUI
        List<String> deleteGUI = Arrays.asList("","&6✦ &e&lID&c:&6{id}&6 ✦", "&6Comment&8: &e{comment}", "&6Who commented&8: &e{commenter}", "&6Date commented: &e{date}","","&4Click to remove comment!", "");

        //view reportsGUI
        List<String> viewReportsGUI = Arrays.asList("&4✦&c&m---------&8[&4Report View&8]&c&m---------&4✦","","&4✦ &c&lID&8: &4{id}&4✦", "&7« &c&lReported player &7» &e{defenseUsername}",  "&7« &c&lPlayer who reported &7» &e{attackerUsername}", "&7« &c&lDate &7» &e{date}", "&7« &c&lReason &7» &e{reason}", "&7« &c&lFlag &7» &e{flag}",  "&7« &c&lComments &7» &e{comLength}", "{comments}");
        List<String> viewReportComment = Arrays.asList(" &8» &4{id}&7. [ID:&c{id}&7] &7[&c{date}&7]&c{commenter}&7: {comment}");

        //flag config messages
        List<String> flagOpen = Arrays.asList("&c&lReports &f> &f[&cStaff&f]: &eFlag has been set to &6&nOpen&e!");
        List<String> flagWorking = Arrays.asList("&c&lReports &f> &f[&cStaff&f]: &eFlag has been set to &e&nWorking&e!");
        List<String> flagPending = Arrays.asList("&c&lReports &f> &f[&cStaff&f]: &eFlag has been set to &5&nPending&e!");
        List<String> flagClosed = Arrays.asList("&c&lReports &f> &f[&cStaff&f]: &eFlag has been set to &4&nClosed&e!");


        List<String> flagNoArguments = Arrays.asList("", " "," ","&4&l>> &4Error&c:&4&l <<"," &8&l  -  &eUsage: &7/&8reports &7<&8flag> <&4&nID&7> <&8Open/Closed&7>&4! "," "," ","");
        List<String> flagOneArgument = Arrays.asList("", "" ," ","&4&l>> &4Error&c:&4&l <<"," &8&l  -  &eUsage: &7/&8reports &7<&8flag&7> <&8ID&7> <&4&nOpen/Closed&7>&4! "," "," ","" );
        List<String> flagWrongUserInput = Arrays.asList("&c&lReports &f> &f[&cStaff ✦&f]: &eYou may only use &6Open&e&l&7/&e&lWorking&7/&5&lPending&7/&6Closed&e!");
        List<String> delayWait = Arrays.asList("", " "," ","&4&l>> &4Error&c:&4&l <<", "&c&lReports &f> &f[&cUser&f]: &eYou can use the command in &c{sec}&e seconds."," "," ","");

        //SpecifyReportGUI
        List<String> generalInformation = Arrays.asList("&bGeneral information about the report.", "", "&b&lID &8» &e{id}", "&bRepored player &8» &9{reported}", "&bPlayer who reported&f &8» &9{attacker}", "&bReason&f &8» &f{reason}", "&bDate&f &8» &9{date}","&bFlag&f &8» {flag}");
        List<String> anvilComment = Arrays.asList(" &8- &cWrite a short comment on the report.");
        List<String> editCommentGUI = Arrays.asList("", "&8✦ &7Click here to edit comment!", "&cYou can also use command:","   &8&l» &e/report comment edit <id> <line> <comment>", "");
        List<String> deleteCommentGUI = Arrays.asList("", "&8✦ &7Click this to delete a comment within the report!", "&cYou can also use command:","   &8&l» &e/report comment delete <id> <line>", "");


        List<String> flagSpecifyGUI = Arrays.asList(" &8- &cClick this to change flag on report.", " &8 &eWhether you want the report to be open or closed.");
        List<String> deleteReportGUI = Arrays.asList("&eClick this item to remove report &a{report_id}", "&4&lBeware &c» ", "&eOnce clicked, it can't be undone.", "&cAnd the report will be gone.");
        List<String> goBackSpecify = Arrays.asList("", "&eClick this to go to the previous GUI.", "");
        List<String> teleportGUI = Arrays.asList("", "&7When clicking this item you will teleport to the &nexact&e location", "&7the report was made on.","", "&e&lCoordinates: &cx: &f{x}, &cy: &f{y}, &cz: &f{z}", "");
        List<String> teleportSuccessfully = Arrays.asList("", "&eYou have been teleported to the &e&lCoordinates: &cx: &f{x}, &cy: &f{y}, &cz: &f{z}", "");


        //ListGUI
        List<String> listGUIInfo = Arrays.asList("&7« &c&lReported player &7» &e{defenseUsername}",  "&7« &c&lPlayer who reported &7» &e{attackerUsername}", "&7« &c&lDate &7» &e{date}", "&7« &c&lReason &7» &e{reason}", "&7« &c&lFlag &7» &e{flag}",  "&7« &c&lComments &7» &e{comLength}", "{comments}");
        List<String> listGUIComment = Arrays.asList(" &8» &4{id}&7. [ID:&c{id}&7] &7[&c{date}&7]&c{commenter}&7: {comment}");
         //helpcmd
        List<String> helpCmd = Arrays.asList("&8&l&m=========&4 »&c&lReport&4« &8&l&m=========", "&8 » &7/&6report &7<&ename&7> &7<&ereason&7> &8-&6 Reports player","&8 » &7/&6report &7<&eflag&7> &7<&eClosed&8/&eOpen&7> &8- &6Turns on/off comments", "&8 » &7/&6report &7<&ereload&7> &8- &6Reloads config.yml file", "&8 » &7/&6report &7<&eview&7> &7<&eid&7> &8- &6View a report", "&8 » &7/&6report &7<&ereset&7> &8- &6Broke the config.yml? No worries.!", "&8 » &7/&6report &7<&ecomment&7> &7<&eid&7> &7<&ecomment&7> &8- &6Comment on a report!","&8 » &7/&6report &7<&ecomment&7> &7<&eedit&7> &7<&eid&7> &7<&eline&7> &7<&ecomment&7> &8- &6Edit a comment!", "&8 » &7/&6report &7<&ecomment&7> &7<&edelete&7> &7<&eid&7> &8- &6Delete a comment!", "&8 » &7/&6report &7<&edelete&7> &7<&eid&7> &8- &6Delete a report with id!", "&8 » &7/&6report &7<&ehelp&7> &8- &6This command!", "&8 » &7/&6report &7<&elist&7> &8- &6Opens GUI", "&8&l&m=========&4 »&c&lReport&4« &8&l&m=========");
        //,"&8 » &7/&6report &7<&eadmin&7> &8- &6Opens Admin GUI - &4&lBeta" 

        //UserINFO
        List<String> noItemsLore = Arrays.asList("&c&lReports &f> &f[&cStaff&f]: &eYou can't empty inventory with 0 items.");


        //Database setting
        configFile.set("Settings.MySQL.host", "localhost");
        configFile.set("Settings.MySQL.username", "root");
        configFile.set("Settings.MySQL.password", "");
        configFile.set("Settings.MySQL.port", 3306);
        configFile.set("Settings.MySQL.database", "report");
        configFile.set("Settings.reportedPlayerHasToBeOnline", true);

        //Messages errors
        configFile.set("Messages.Wait", wait);
        configFile.set("Messages.PlayerLeft", playerLeft);

        configFile.set("Messages.NO_ARGUMENTS", NO_ARGS);
        configFile.set("Messages.ONE_ARGUMENT", ONE_ARGUMENT);
        configFile.set("Messages.ARGUMENT_NOT_A_NUMBER", NOT_A_NUMBER);
        configFile.set("Messages.Report_not_found", report_not_found);
        configFile.set("Messages.noPermissions", noPermission);
        configFile.set("Messages.reportDeletedInside", reportWasDeleted);

        //report viewer and reporting someone messages
        configFile.set("Messages.cantReportYourself", reportYourself);
        configFile.set("Messages.missingReason", missingReason);
        configFile.set("Messages.ReportViewer", report_viewer);
        configFile.set("Messages.Reported_Someone_Successful", reported_someone_successful);
        configFile.set("Messages.playerNeverPlayed", playerNeverPlayed);
        configFile.set("Messages.playerNotOnline", NotOnline);
        configFile.set("Messages.delayTime", 30);
        configFile.set("Messages.Delay", delayWait);

        //comment config messages
        configFile.set("Messages.Comment.maximumAmountOfComments", 10);
        configFile.set("Messages.Comment.maximumCommentsReached", maximumCommentsMessage);
        configFile.set("Messages.Comment.NO_ARGUMENT_COMMENT", NO_ARGUMENT_COMMENT);
        configFile.set("Messages.Comment.ONE_ARGUMENT_GIVEN", one_argument_given);
        configFile.set("Messages.Comment.commented_successfully", commented_successfully);
        configFile.set("Messages.Comment.closed_thread", closed_thread);
        configFile.set("Messages.Comment.editedComment", editedComment);
        configFile.set("Messages.Comment.commentDoesntExist", commentDoesntExist);
        configFile.set("Messages.Comment.noComments", noComments);
        configFile.set("Messages.Comment.invalidLine", invalidLine);
        configFile.set("Messages.Comment.editCommentChat", editCommentChat);
        configFile.set("Messages.Comment.Edit.lastArgument", editArgumentMissing);
        //delete config messages
        configFile.set("Messages.delete.oneArgument", deleteOneArgument);
        configFile.set("Messages.delete.deletedSuccess", deletedSuccess);

        //Delete gui
        configFile.set("Messages.DeleteGUI.lore", deleteGUI);

        //Report view GUI
        configFile.set("Messages.ViewGUI.lore", viewReportsGUI);
        configFile.set("Messages.ViewGUI.comments", viewReportComment);

        //flag config messages
        configFile.set("Messages.flag.open", flagOpen);
        configFile.set("Messages.flag.closed", flagClosed);
        configFile.set("Messages.flag.pending", flagPending);
        configFile.set("Messages.flag.working", flagWorking);
        configFile.set("Messages.flag.no_arguments", flagNoArguments);
        configFile.set("Messages.flag.one_argument", flagOneArgument);
        configFile.set("Messages.flag.onlyCloseOrOpen", flagWrongUserInput);

        configFile.set("Messages.SuccessfullyTeleported", teleportSuccessfully);

        //SpecifyReportGUI
        configFile.set("SpecifyReportGUI.Item.decorationsItemOne", "BLUE_STAINED_GLASS_PANE");
        configFile.set("SpecifyReportGUI.Item.decorationsItemTwo", "LIGHT_BLUE_STAINED_GLASS_PANE");
        configFile.set("SpecifyReportGUI.Item.GeneralInformation", "BOOK");
        configFile.set("SpecifyReportGUI.Item.anvilComment", "ANVIL");
        configFile.set("SpecifyReportGUI.Item.editComment", "PAPER");
        configFile.set("SpecifyReportGUI.Item.deleteComment", "CHEST");
        configFile.set("SpecifyReportGUI.Item.flagSpecifyGUI", "LEVER");
        configFile.set("SpecifyReportGUI.Item.deleteReportGUI", "RED_STAINED_GLASS_PANE");
        configFile.set("SpecifyReportGUI.Item.goBackSpecify", "RED_BED");
        configFile.set("SpecifyReportGUI.Item.teleportItem", "ENDER_PEARL");

        configFile.set("SpecifyReportGUI.GeneralInformation", generalInformation);
        configFile.set("SpecifyReportGUI.anvilComment", anvilComment);
        configFile.set("SpecifyReportGUI.editComment", editCommentGUI);
        configFile.set("SpecifyReportGUI.deleteComment", deleteCommentGUI);
        configFile.set("SpecifyReportGUI.flagSpecifyGUI", flagSpecifyGUI);
        configFile.set("SpecifyReportGUI.deleteReportGUI", deleteReportGUI);
        configFile.set("SpecifyReportGUI.goBackSpecify", goBackSpecify);
        configFile.set("SpecifyReportGUI.teleportGUI", teleportGUI);

        //UserINFO gui
        configFile.set("UserInfoGUI.noItemsLore", noItemsLore);

        //ListGUI
        configFile.set("ListGUI.reportInfo", listGUIInfo);
        configFile.set("ListGUI.reportComment", listGUIComment);
        configFile.set("ListGUI.Item", "PAPER");

        //helpcmd
        configFile.set("Messages.HelpCmd.message", helpCmd);



    }

    public static FileInterface getConfigFile(){
        return configFile;
    }

    public Statement getStatement(){
        return statement;
    }
    public Statement getSecondStatement(){
        return secondStatement;
    }

    @Override
    public void onEnable() {
        /*
        CONFIG FILE INITIALIZATION
         */
        configFile = new ConfigFile("config.yml");
        configFile.create();
        //CONFIG FILE END //

        //connect to database
        host = configFile.get("Settings.MySQL.host").toString();
        username = configFile.get("Settings.MySQL.username").toString();
        password = configFile.get("Settings.MySQL.password").toString();
        port = (int) configFile.get("Settings.MySQL.port");
        database = configFile.get("Settings.MySQL.database").toString();


        //Done connecting to database
        try{
            openConnection();
            statement = connection.createStatement();
            secondStatement = connection.createStatement();
            String[] tables = new String[]{"reports", "comments", "users", "inventory", "inventoryItems"};
            for(int i = 0;i<tables.length;i++){
                try(ResultSet rs = connection.getMetaData().getTables(null, null, tables[i], null)){
                    if (!rs.next()){
                        createTable(tables[i]);
                    }
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }catch(SQLException | ClassNotFoundException e){
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "MySQL cannot be connected to the database.");
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Disabling plugin Reports.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        mySQLManaging = new MySQLManaging();
        rm = new ReportManager(mySQLManaging);

        try{
            loadReports();
        }catch(SQLException e){
            e.printStackTrace();
        }
        //END OF LOAD REPORTS//


        /*
        registers the eventhandler
         */
        getServer().getPluginManager().registerEvents(new GUIEventHandler(this), this);

        /*
        reportCmd initialization.
         */
        ReportCmd reportCmd = new ReportCmd(mySQLManaging);
        getCommand("report").setExecutor(reportCmd);

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    openConnection();
                    statement = connection.createStatement();
                    secondStatement = connection.createStatement();
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        r.runTaskAsynchronously(this);

    }

    public void createTable(String table){
        try{
            if (table.equals("reports")){
                statement.executeUpdate(CREATE_TABLE_REPORT_SQL);
            }else if (table.equals("comments")){
                statement.executeUpdate(CREATE_TABLE_COMMENTS_SQL);
            }else if (table.equals("users")){
                statement.executeUpdate(CREATE_TABLE_USER_SQL);
            }else if (table.equals("inventory")){
                statement.executeUpdate(CREATE_TABLE_INVENTORY_SQL);
            }else if (table.equals("inventoryItems")){
                statement.executeUpdate(CREATE_ITEMS_INVENTORY_SQL);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public Connection getConnection(){
        return connection;
    }

    public void openConnection() throws SQLException, ClassNotFoundException{
        if (connection != null && !connection.isClosed()){
            return;
        }
        synchronized(this){
            if (connection != null && !connection.isClosed()){
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);

        }
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                if (connection.isClosed() && connection == null) {
                    return;
                } else {
                    //Checking if the tables exists, if false then it creates some.
                    String[] reports = new String[]{"reports", "comments"};
                    for (int i = 0; i < reports.length; i++) {
                        try (ResultSet rs = connection.getMetaData().getTables(null, null, reports[i], null)) {
                            if (!rs.next()) {
                                if (reports[i].equals("reports")) createTable("reports");
                                if (reports[i].equals("comments")) createTable("comments");
                            }
                        }
                    }
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateUser(String uuid, int warningLevel, int reportSent, String address){
        String query = "INSERT INTO users(userUuid, warningLevel, reportSent, userIp) VALUES(?,?,?,?) on DUPLICATE KEY UPDATE userUuid = ?, warningLevel = ?, reportSent = ?, userIp = ?;";
        try{
            PreparedStatement preparedStatement = getConnection().prepareStatement(query);
            preparedStatement.setString(1, uuid);
            preparedStatement.setInt(2, warningLevel);
            preparedStatement.setInt(3, reportSent);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, uuid);
            preparedStatement.setInt(6, warningLevel);
            preparedStatement.setInt(7, reportSent);
            preparedStatement.setString(8, address);

            preparedStatement.execute();
        }catch (SQLException e) {
            e.printStackTrace();
        }


    }


    public void loadReports() throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM reports;")) {
            while (resultSet.next()) {
                comment = new Comment[]{}; //Keep here, cause every time new report its going to reset.
                int id = resultSet.getInt("reportId");
                try (ResultSet resultSetComment = secondStatement.executeQuery("SELECT * FROM comments WHERE reportId = " + id + ";")) {
                    while (resultSetComment.next()) {
                        comment = (Comment[]) ArrayUtils.add(comment, new Comment(resultSetComment.getString("comment"), resultSetComment.getString("dateTime"), resultSetComment.getInt("commentId"), resultSetComment.getString("commenter"))); //Henter de forskellige columns fra resultSetComment
                    }
                    EnumFlag enumFlag = Enum.valueOf(EnumFlag.class, resultSet.getString("flag"));
                    ReportManager.addReport(resultSet.getInt("reportId"), resultSet.getString("defenseUsername"), resultSet.getString("attackerUsername"), resultSet.getString("dateTime"), enumFlag, resultSet.getString("reason"), comment, new double[]{resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z")});
                }
            }
        }
    }
}
