package tzer0.Money2XP;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.event.spout.SpoutListener;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericScreen;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.player.SpoutPlayer;

public class Money2XPSpoutCraftListener extends SpoutListener {

    Money2XP plugin;
    boolean enabled;
    public Money2XPSpoutCraftListener(Money2XP plugin) {
        this.plugin = plugin;
        enabled = false;
    }
    public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
        enabled = true;
    }
    
    public void openXpWindow(Player pl) {
        if (!enabled) {
            pl.sendMessage(ChatColor.RED + "Spoutcraft is not enabled!");
        }
        SpoutPlayer spl =  SpoutManager.getPlayer(pl);
        if (!spl.isSpoutCraftEnabled()) {
            pl.sendMessage(ChatColor.RED + "Spout is not enabled on the client");
        }
        GenericLabel name = new GenericLabel();
        XpBuyButton but = new XpBuyButton("archery", 100, pl);
        GenericPopup scr = new GenericPopup();
        but.setVisible(true);
        but.setX(0);
        but.setY(0);
        but.setWidth(50);
        but.setHeight(50);
        scr.attachWidget(plugin, but);
        name.setText(""+plugin);
        scr.attachWidget(plugin, name);
        spl.getMainScreen().attachPopupScreen(scr);
    }
    
    class XpBuyButton extends GenericButton {
        String skill;
        int price;
        Player pl;
        public XpBuyButton (String skill, int price, Player pl) {
            this.skill = skill;
            this.price = price;
            this.pl = pl;
            String label = skill.toUpperCase();
            if (price < 0) {
                label += " is unavailable";
            } else {
                label += " - " + price;
            }
            setText(label);
        }
        
        
    }
}
