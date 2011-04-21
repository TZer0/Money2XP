package tzer0.Money2XP;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
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
    HashMap<String, TrainingArea> activity;

    public Money2XPPlayerListener () {        
        activity = new HashMap<String, TrainingArea>();
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
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new PositionChecker(), 0L, 100L);
    }
    public void onPlayerJoin(PlayerJoinEvent event) {
        activity.put(event.getPlayer().getName(), null);
    }
    public void onPlayerQuit(PlayerQuitEvent event) {
        activity.remove(event.getPlayer().getName());
    }
    /** 
     * Checks if the command is valid and wether the player attempting to do the command has permissions to do so
     * 
     * @see org.bukkit.event.player.PlayerListener#onPlayerCommandPreprocess(org.bukkit.event.player.PlayerCommandPreprocessEvent)
     */
    public void onPlayerMove(PlayerMoveEvent event) {

    }

    public class PositionChecker implements Runnable {
        public void run() {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (activity.containsKey(player.getName())) {
                    activity.put(player.getName(), null);
                }
                TrainingArea area = activity.get(player.getName());
                activity.put(player.getName(), null);
                for (TrainingArea test : plugin.areas) {
                    player.sendMessage("testing!");
                    //player.sendMessage(""+test);
                    if (test.isInArea(player.getLocation())) {
                        if (!(area == test)) {
                            if (test.skills.size() != 0) {
                                player.sendMessage(ChatColor.GREEN + "You feel the presense of a training ground.");
                                player.sendMessage(ChatColor.GREEN + "You may train the following skills here:");
                                for (Integer i : test.skills) {
                                    player.sendMessage(ChatColor.GREEN + plugin.names[i]);
                                }
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "You feel the presense of an unused training ground");
                            }

                            activity.put(player.getName(), test);
                        }
                    }
                }
            }
        }
    }
}
