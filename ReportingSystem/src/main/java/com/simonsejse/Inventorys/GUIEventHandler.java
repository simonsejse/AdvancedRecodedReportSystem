package com.simonsejse.Inventorys;

import com.simonsejse.FileLoadSaver.FileInterface;
import com.simonsejse.ReportManagingSystem.Comment;
import com.simonsejse.ReportManagingSystem.Report;
import com.simonsejse.ReportManagingSystem.ReportManager;
import com.simonsejse.ReportSystem;
import net.minecraft.server.v1_15_R1.PacketPlayInUpdateSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIEventHandler implements Listener {

    private ReportSystem plugin;
    private static Map<UUID, Integer> activeChatUsers;
    private static Map<UUID, InetAddress> userIp;
    private FileInterface configFile;

    public GUIEventHandler(ReportSystem plugin) {
        this.plugin = plugin;
        this.activeChatUsers = new HashMap<>();
        this.userIp = new HashMap<>();
        this.configFile = ReportSystem.getConfigFile();
    }

    public static void addUser(UUID uuid, int report_id) {
        activeChatUsers.put(uuid, report_id);
    }


    public void removePlayer(UUID uuid) {
        activeChatUsers.remove(uuid);
    }


    @EventHandler
    public void signPlace(PlayerInteractEvent i){
        if (i.getClickedBlock() != null && i.getClickedBlock().getType().equals(Material.OAK_WALL_SIGN)) {
            if (i.getClickedBlock().getLocation().add(0,0,2).getBlock().getType().equals(Material.BEDROCK)){
                i.getPlayer().performCommand("report list");
            }
        }
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
        UUID uuid = asyncPlayerChatEvent.getPlayer().getUniqueId();
        for (Map.Entry<UUID, Integer> map : activeChatUsers.entrySet()) {
            if (map.getKey().equals(asyncPlayerChatEvent.getPlayer().getUniqueId())) {
                String message = asyncPlayerChatEvent.getMessage();
                String[] split = message.split(" ");
                int num;
                try {
                    num = Integer.parseInt(split[0].trim());
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < split.length; i++) {
                        sb.append(split[i] + " ");
                    }

                    int local_id = map.getValue().intValue();
                    Report report = ReportManager.getSpecificReportById(local_id);
                    String finalMsg = sb.toString();
                    Comment[] comment = report.getComments();
                    for (int i = 0; i < comment.length; i++) {
                        if (comment[i].getId() == num) {
                            comment[i].setComment(finalMsg);
                            comment[i].setCommenter(asyncPlayerChatEvent.getPlayer().getDisplayName() + "(EDITED)");
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                            LocalDateTime now = LocalDateTime.now();
                            String date = dtf.format(now);
                            comment[i].setDate(date);

                            ((List<String>) configFile.get("Messages.Comment.editedComment")).forEach(s -> {
                                if (s.contains("{id}")) s = s.replace("{id}", String.valueOf(local_id));
                                if (s.contains("{line}")) s = s.replace("{line}", String.valueOf(num));
                                if (s.contains("{comment}")) s = s.replace("{comment}", finalMsg);
                                asyncPlayerChatEvent.getPlayer().sendMessage(loadColor(s));
                            });
                            removePlayer(uuid);
                            asyncPlayerChatEvent.setCancelled(true);
                            return;
                        }
                    }
                    ((List<String>) configFile.get("Messages.Comment.invalidLine")).forEach(s -> {
                        if (s.contains("{line}")) s = s.replace("{line}", String.valueOf(num));
                        asyncPlayerChatEvent.getPlayer().sendMessage(loadColor(s));
                    });
                    asyncPlayerChatEvent.setCancelled(true);
                } catch (NumberFormatException nfe) {
                    ((List<String>) configFile.get("Messages.ARGUMENT_NOT_A_NUMBER")).forEach(s -> {
                        if (s.contains("{number}")) s = s.replace("{number}", split[0]);
                        asyncPlayerChatEvent.getPlayer().sendMessage(loadColor(s));
                    });
                    asyncPlayerChatEvent.setCancelled(true);
                    return;
                }
            }

        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent playerQuitEvent) {
        if (activeChatUsers.containsKey(playerQuitEvent.getPlayer().getUniqueId())) {
            activeChatUsers.remove(playerQuitEvent.getPlayer().getUniqueId());
        }
    }

    private String loadColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        userIp.put(p.getUniqueId(), p.getAddress().getAddress());
        UUID uuid = p.getUniqueId();
        try (ResultSet rs = plugin.getStatement().executeQuery("SELECT * from users WHERE userUuid ='" + uuid + "'")) {
            int warningLevel = 0;
            int reportSent = 0;
            if (rs.next()) {
                warningLevel = rs.getInt("warningLevel");
                reportSent = rs.getInt("reportSent");
            }
            plugin.updateUser(p.getUniqueId().toString(), warningLevel, reportSent, p.getAddress().getAddress().toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {
            return;
        }
        InventoryHolder inventoryHolder = e.getClickedInventory().getHolder();
        if (inventoryHolder == null) {
            return;
        }//Used when AnvilGUI is open, so we don't interfere with that.

        int slot = e.getSlot();
        ItemStack item = e.getCurrentItem();
        Player whoClicked = (Player) e.getWhoClicked();
        if (inventoryHolder instanceof ListGUI) {
            ListGUI listGUI = (ListGUI) inventoryHolder;
            assert listGUI != null;
            listGUI.onGuiClick(item, slot, whoClicked);
            e.setCancelled(true);
        } else if (inventoryHolder instanceof SpecifyReportGUI) {
            SpecifyReportGUI specifyReportGUI = (SpecifyReportGUI) inventoryHolder;
            assert specifyReportGUI != null;
            specifyReportGUI.onGuiClick(item, slot, whoClicked);
            e.setCancelled(true);
        } else if (inventoryHolder instanceof AdminGUI) {
            AdminGUI adminGUI = (AdminGUI) inventoryHolder;
            assert adminGUI != null;
            adminGUI.onGuiClick(item, slot, whoClicked);
            e.setCancelled(true);
        } else if (inventoryHolder instanceof FlagReportGUI) {
            FlagReportGUI flagReportGUI = (FlagReportGUI) inventoryHolder;
            assert flagReportGUI != null;
            flagReportGUI.onGuiClick(item, slot, whoClicked);
            e.setCancelled(true);
        } else if (inventoryHolder instanceof ConfirmGUI) {
            ConfirmGUI confirmGUI = (ConfirmGUI) inventoryHolder;
            assert confirmGUI != null;
            confirmGUI.onGuiClick(item, slot, whoClicked);
            e.setCancelled(true);
        } else if (e.getInventory().getHolder() instanceof DeleteGUI) {
            DeleteGUI deleteGUI = (DeleteGUI) e.getInventory().getHolder();
            deleteGUI.onGuiClick(item, slot, whoClicked);
            e.setCancelled(true);
        } else if (e.getInventory().getHolder() instanceof UserInfoGUI) {
            UserInfoGUI userInfoGUI = (UserInfoGUI) e.getInventory().getHolder();
            if (userInfoGUI.getPlayersInventory() != null && e.getClickedInventory().equals(userInfoGUI.getPlayersInventory())) {
                if (slot < 41) return;
                e.setCancelled(true);
                //Clicking inside edit players inventory gui
                userInfoGUI.onPlayerInventoryGUI(item, slot, whoClicked);
            } else if (userInfoGUI.getRestoreInv() != null && e.getClickedInventory().equals(userInfoGUI.getRestoreInv())) {
                e.setCancelled(true);
                boolean isShiftClicking = e.getClick().isShiftClick();
                userInfoGUI.onRestoreGUIClick(item, slot, whoClicked, isShiftClicking);
            } else if (userInfoGUI.getKitRestoreMenu() != null && e.getClickedInventory().equals(userInfoGUI.getKitRestoreMenu())) {
                e.setCancelled(true);
                userInfoGUI.onKitRestoreGUIClick(item, slot, whoClicked);
            } else if (userInfoGUI.getCurrentInventory() != null && e.getClickedInventory().equals(userInfoGUI.getCurrentInventory())) {
                e.setCancelled(true);
                userInfoGUI.onCurrentInventoryGUIClick(item, slot, whoClicked);
            } else if (userInfoGUI.getDeleteRestoreGUI() != null && e.getClickedInventory().equals(userInfoGUI.getDeleteRestoreGUI())) {
                e.setCancelled(true);
                userInfoGUI.onDeleteRestoreGUIClick(slot, whoClicked);
            } else {
                userInfoGUI.onGuiClick(item, slot, whoClicked);
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onInventoryLeave(InventoryCloseEvent e) {

        if (e.getInventory().getHolder() instanceof UserInfoGUI) {
            UserInfoGUI userInfo = (UserInfoGUI) e.getInventory().getHolder();
            if (userInfo.getPlayersInventory() != null && userInfo.getPlayersInventory().equals(e.getPlayer().getOpenInventory().getTopInventory())) {
                ItemStack[] items = new ItemStack[41];
                Inventory inv = e.getInventory();
                for (int i = 0; i < 41; i++) {
                    items[i] = inv.getItem(i);
                }
                userInfo.setInventoryContent(items);
            }
        }
    }




}
