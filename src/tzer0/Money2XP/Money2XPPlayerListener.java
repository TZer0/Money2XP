package tzer0.Money2XP;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.config.Configuration;

import tzer0.Money2XP.Money2XP.TrainingArea;

import com.nijiko.permissions.PermissionHandler;

// TODO: Auto-generated Javadoc
/**
 * The is a listener interface for receiving PlayerCommandPreprocessEvent events.
 * 
 */
public class Money2XPPlayerListener extends PlayerListener  {
    Configuration conf;
    Money2XP plugin;
    public PermissionHandler permissions;
    HashMap<Player, TrainingArea> activity;

    public Money2XPPlayerListener () {        
        activity = new HashMap<Player, TrainingArea>();
    }
    /**
     * Sets the pointers so that they can be referenced later in the code
     *
     * @param config Plugin-config
     * @param plugin Money2XP
     * @param permissions Permissions-handler (if available)
     */
    public void setPointers(Configuration config, Money2XP plugin, PermissionHandler permissions) {
        conf = config;
        this.plugin = plugin;
        this.permissions = permissions;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new PositionChecker(), 0L, 30L);
    }
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() instanceof Chest) {
            event.getPlayer().sendMessage("wee");
        }
    }
    public void onPlayerJoin(PlayerJoinEvent event) {
        activity.remove(event.getPlayer());
    }
    public void onPlayerQuit(PlayerQuitEvent event) {
        activity.remove(event.getPlayer().getName());
    }
    /**
     * Checks where players are located and updates their status accordingly (and what skills they may train) 
     *
     *
     */
    public class PositionChecker implements Runnable {
        public void run() {
            if (!plugin.trainingzones) {
                return;
            }
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                boolean inarea = false;
                if (!activity.containsKey(player)) {
                    activity.put(player, null);
                }
                TrainingArea area = activity.get(player);
                for (TrainingArea test : plugin.areas) {
                    if (test.isInArea(player.getLocation())) {
                        inarea = true;
                        if (!(area == test)) {
                            if (test.skills.size() != 0) {
                                player.sendMessage(ChatColor.GREEN + "You feel the presence of a training ground.");
                                player.sendMessage(ChatColor.GREEN + "You may train the following skills here:");
                                for (Integer i : test.skills) {
                                    player.sendMessage(ChatColor.GREEN + plugin.names[i]);
                                }
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "You feel the presense of an unused training ground");
                            }
                            activity.put(player, test);
                            break;
                        }
                    }
                }
                if (area != null && !inarea) {
                    activity.put(player, null);
                    player.sendMessage(ChatColor.YELLOW + "You have left a training ground.");
                }
            }
        }
    }
}
