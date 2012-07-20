package tzer0.Money2XP;

import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.ScreenListener;

import tzer0.Money2XP.Money2XPSpoutCraftListener.XpBuyButton;

public class Money2XPButtonListener extends ScreenListener {
    Money2XP plugin;
    public Money2XPButtonListener(Money2XP plugin) {
        this.plugin = plugin;
    }
    public void onButtonClick(ButtonClickEvent event) {
        if (event.getButton() instanceof XpBuyButton) {
            XpBuyButton but = (XpBuyButton) event.getButton();
            plugin.modValue(but.skill, value, event.getPlayer());
        }
    }
}
