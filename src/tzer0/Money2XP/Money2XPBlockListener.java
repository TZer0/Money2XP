package tzer0.Money2XP;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class Money2XPBlockListener extends BlockListener {
    private final Money2XP plugin;

    public Money2XPBlockListener(final Money2XP plugin) {
        this.plugin = plugin;
    }

    public void onSignChange(SignChangeEvent event) {
        Player pl = event.getPlayer();
        boolean access = (plugin.permissions == null && pl.isOp()) 
        || (plugin.permissions != null && plugin.permissions.has(pl, "money2xp.admin"));
        boolean changed = false;
        String line = event.getLine(0);
        if (line.equalsIgnoreCase("[m2x]")) {
            event.setLine(0, ChatColor.DARK_GREEN + "[M2X]");
            plugin.updateAndCheckSign((Sign) event.getBlock().getState(), true, pl);
        }
        
        if (changed) {
            if (!access) {
                event.setLine(0, ChatColor.RED + "No access.");
            }
            ((Sign)event.getBlock().getState()).update(true);
        }
    }
}
