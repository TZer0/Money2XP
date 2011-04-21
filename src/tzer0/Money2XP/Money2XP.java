package tzer0.Money2XP;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.gmail.nossr50.mcMMO;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import cosine.boseconomy.BOSEconomy;

/**
 * Plugin for bukkit allowing players to purchase mcMMO-experience points using iConomy/BOSEconomy-money.
 * 
 * @author TZer0
 */
public class Money2XP extends JavaPlugin {
    private final Money2XPPlayerListener listener = new Money2XPPlayerListener();

    String []names = {"acrobatics", "archery", "axes", "excavation", "herbalism", "mining", "repair", "swords", "unarmed", "woodcutting"};
    PluginDescriptionFile pdfFile;
    public PermissionHandler permissions;
    public LinkedHashSet<String> skillnames = new LinkedHashSet<String>();
    public LinkedHashSet<TrainingArea> areas;
    HashMap<Location, TrainingArea> map;
    HashMap<Player, AreaSelect> as;
    Configuration conf;
    public mcMMO mcmmo;
    public boolean trainingzones;
    @SuppressWarnings("unused")
    private final String name = "Money2XP";

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onDisable()
     */
    public void onDisable() {
        System.out.println(pdfFile.getName() + " disabled.");
        getServer().getScheduler().cancelTasks(this);
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    public void onEnable() {
        areas = new LinkedHashSet<TrainingArea>();
        as = new HashMap<Player, AreaSelect>();
        map = new HashMap<Location, TrainingArea>();
        pdfFile = this.getDescription();
        if (skillnames.size() == 0) {
            for (String name : names) {
                skillnames.add(name);
            }
        }
        setupPermissions();
        conf = getConfiguration();
        listener.setPointers(conf, this, permissions);
        trainingzones = conf.getBoolean("zones", false);
        PluginManager tmp = getServer().getPluginManager();
        tmp.registerEvent(Event.Type.PLAYER_JOIN, listener, Priority.Normal, this);
        tmp.registerEvent(Event.Type.PLAYER_QUIT, listener, Priority.Normal, this);
        tmp.registerEvent(Event.Type.PLAYER_MOVE, listener, Priority.Normal, this);

        System.out.println(pdfFile.getName() + " version "
                + pdfFile.getVersion() + " is enabled!");
        mcmmo = (mcMMO) tmp.getPlugin("mcMMO");
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        int l = args.length;
        sender.sendMessage(""+l);
        boolean help = false;
        if (player != null) {
            if (commandLabel.equalsIgnoreCase("m2x")) {
                // user-accessible commands
                if (!(permissions == null || (permissions != null && permissions.has(player, "money2xp.user")))) {
                    player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                    return true;
                }
                if (l >= 1 && args[0].equalsIgnoreCase("list")) {
                    // lists available skills
                    showSkills((CommandSender)player);
                } else if (l == 3 && args[0].equalsIgnoreCase("check")) {
                    xpMod(args[1], args[2], player, true);
                } else if (l == 2) {
                    // checks if the skill exists and whether the player has enough money to purchase the required
                    // amount of xp.
                    xpMod(args[0], args[1], player, false);
                } else {
                    help = true;
                }
            } else if (commandLabel.equalsIgnoreCase("m2xset")) {
                if (!((permissions == null && player.isOp()) || (permissions != null && permissions.has(player, "money2xp.admin")))) {
                    player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                    return true;
                }
                if (l == 2) {
                    if (!modValue(args[0], args[1], player)) {
                        help = true;
                    }
                } else {
                    help = true;
                }
            } else if (commandLabel.equalsIgnoreCase("m2xtrain")) {
                if (!((permissions == null && player.isOp()) || (permissions != null && permissions.has(player, "money2xp.admin")))) {
                    player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                    return true;
                }
                if (l >= 1 && args[0].equalsIgnoreCase("zones")) {
                    String status = "";
                    if (l == 2) {
                        trainingzones = toInt(args[1]) != 0;
                    } else {
                        trainingzones = !trainingzones;
                    }
                    if (trainingzones) {
                        status = "on";
                    } else {
                        status = "off";
                    }
                    player.sendMessage(ChatColor.GREEN + "Training zones have been turned " + status);
                } else if (l >= 1 && args[0].equalsIgnoreCase("create")) {
                    AreaSelect area = as.get(player);
                    if (area == null) {
                        player.sendMessage(ChatColor.RED + "No area selected.");
                        return true;
                    } else if (area.b1 == null) {
                        player.sendMessage(ChatColor.RED + "First node is missing");
                        return true;
                    } else if (area.b2 == null) {
                        player.sendMessage(ChatColor.RED + "Second node is missing");
                        return true;
                    }
                    if (l == 2) {
                        if (area.b1.getWorld().equals(area.b2.getWorld())) {
                            TrainingArea ta = new TrainingArea(area, player, args[1], true);
                            areas.add(ta);
                            as.remove(player);
                        } else {
                            player.sendMessage(ChatColor.RED + "Nodes are in different worlds.");
                        }
                    }
                } else if (l >= 1 && args[0].equalsIgnoreCase("node")) {
                    AreaSelect area = as.get(player);
                    if (area == null) {
                        area = new AreaSelect();
                        as.put(player, area);
                    }
                    if (l == 2) {
                        int i = toInt(args[1]);
                        if (i == 1) {
                            area.b1 = player.getLocation().getBlock();
                            player.sendMessage("Node 1 set");
                        } else if (i == 2) {
                            area.b2 = player.getLocation().getBlock();
                            player.sendMessage("Node 2 set");
                        } else {
                            player.sendMessage("No such node.");
                        }
                    }
                } else if (l >= 1 && args[0].equalsIgnoreCase("Select")) {
                    
                }

            }
        } else {
            sender.sendMessage("Only in-game players have access to this command");
        }
        if (help) {
            player.sendMessage(ChatColor.GREEN + "Money2XP by TZer0 (TZer0.jan@gmail.com)");
            player.sendMessage(ChatColor.YELLOW + "Commands:");
            player.sendMessage(ChatColor.YELLOW + "/m2x list - list skills and prices");
            player.sendMessage(ChatColor.YELLOW + "/m2x [skillname] [amount]");
            player.sendMessage(ChatColor.YELLOW + "/m2x check [skillname] [amount]");
            if ((permissions == null && player.isOp()) || permissions.has(player, "money2xp.admin")) {
                player.sendMessage(ChatColor.YELLOW + "Admin commands:");
                player.sendMessage(ChatColor.YELLOW + "/m2xset [skillname] [price_per_xp] - sets xp-cost for a skill");
                player.sendMessage(ChatColor.YELLOW + "/m2xtrain for more information about training zones");
            }
        }
        return true;
    }

    /**
     * Modifies a skill if test is false, if test is true, displays cost of modification
     * 
     * @param skill The skill to be modified
     * @param xpstring How much xp should be added
     * @param player Player receving the xp and taking the costs
     * @param test If this is a test or not.
     */

    public void xpMod(String skill, String xpstring, Player player, boolean test) {
        Plugin BOS = getServer().getPluginManager().getPlugin("BOSEconomy");
        skill = skill.toLowerCase();
        if (checkInt(xpstring)) {
            if (!skillnames.contains(skill)) {
                player.sendMessage(ChatColor.RED+"This skill does not exist!");
                return;
            }
            int xp = Integer.parseInt(xpstring);
            int xpcost = conf.getInt(skill.toLowerCase(), conf.getInt("default", 100));

            if (xpcost <= 0) {
                player.sendMessage(String.format(ChatColor.RED + "Training %s using money has been disabled.", skill));
                return;
            } else if (Integer.MAX_VALUE/xpcost < xp || xp <= 0) {
                // Prevents overflows and underflows and adds a disapproving comment.
                player.sendMessage(ChatColor.RED+String.format("Nice try."));
                return;
            }
            int bal = 0;
            if (getServer().getPluginManager().isPluginEnabled("iConomy")) {
                Account acc = iConomy.getBank().getAccount(player.getName());
                bal = (int)acc.getBalance();
            } else if (BOS != null) {
                bal = ((BOSEconomy) BOS).getPlayerMoney(player.getName());
            } else {
                player.sendMessage(ChatColor.RED + "No economy system disabled, cancelled.");
                return;
            }

            if (!test && bal < xp*xpcost) {
                player.sendMessage(ChatColor.RED+String.format("You cannot afford %d %s xp (@%d) since ", 
                        xp, skill, xpcost));
                player.sendMessage(ChatColor.RED+String.format("your current balance is %d, and this costs %d!", 
                        bal, xpcost*xp));
            } else if (test) {
                player.sendMessage(ChatColor.YELLOW+String.format("%d %s-xp (@%d) would cost you %d,", 
                        xp, skill, xpcost, xp*xpcost));
                player.sendMessage(ChatColor.YELLOW+String.format("leaving you with %d money.",
                        ((int) bal)-xp*xpcost));
            } else {
                if (getServer().getPluginManager().isPluginEnabled("iConomy")) {
                    iConomy.getBank().getAccount(player.getName()).subtract(xp*xpcost);
                } else {
                    ((BOSEconomy) BOS).addPlayerMoney(player.getName(), -xp*xpcost, true);
                }
                bal -= xp*xpcost;
                player.sendMessage(ChatColor.GREEN+String.format("You received %d %s-xp(@%d) for %d money!", 
                        xp, skill, xpcost, xp*xpcost));
                player.sendMessage(ChatColor.GREEN+String.format("You have %d money left", 
                        (int) bal));
                mcmmo.addXp(player, skill, xp);
            }
        } else {
            player.sendMessage(ChatColor.RED + "Value must be a number.");
        }
    }
    /**
     * Modifies a settings-value.
     *
     * @param key The key to modify or delete
     * @param value The value which will be set
     * @param sender Whoever is sending doing this command 
     * @return true, Whether it worked or not.
     */
    public boolean modValue(String key, String value, CommandSender sender) {
        if (checkInt(value)) {
            if (!(key.equals("default") || skillnames.contains(key))) {
                sender.sendMessage(ChatColor.RED + "This skill does not exist.");
                return true;
            }
            int i = Integer.parseInt(value);
            if (i == 0) {
                getConfiguration().removeProperty(key);
                value = String.format("default(%d)",
                        getConfiguration().getInt("default", 100));
            } else {
                if (i == -1) {
                    value = "unavailable";
                }
                getConfiguration().setProperty(key.toLowerCase(), i);
            }
            getConfiguration().save();
            sender.sendMessage(ChatColor.GREEN
                    + String.format("Price per xp for %s has been set to %s",
                            key, value));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
     */
    private void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

        if (this.permissions == null) {
            if (test != null) {
                this.permissions = ((Permissions) test).getHandler();
            } else {
                System.out.println(ChatColor.YELLOW
                        + "Permissons not detected - defaulting to OP!");
            }
        }
    }
    /**
     * Displays a page containing price-information about xp
     * 
     * @param page The page to display
     * @param player
     */
    public void showSkills(CommandSender sender) {
        int def = getConfiguration().getInt("default", 100);
        int value = 0;
        sender.sendMessage(ChatColor.WHITE + "Name - Cost");
        for (String name : skillnames) {
            value = getConfiguration().getInt(name, def);
            if (value > 0) {
                sender.sendMessage(ChatColor.GREEN + String.format("%s - %d", name, value));
            } else {
                sender.sendMessage(ChatColor.RED + String.format("%s is not available", name));
            }
        }
    }
    public int toInt(String in) {
        int out = 0;
        if (checkInt(in)) {
            out = Integer.parseInt(in);
        }
        return out;
    }
    /**
     * Check if the string is valid as an int (accepts signs).
     *
     * @param in The string to be checked
     * @return boolean Success
     */
    public boolean checkInt(String in) {
        char chars[] = in.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!(Character.isDigit(chars[i]) || (i == 0 && chars[i] == '-'))) {
                return false;
            }
        }
        return true;
    }
    public int getSkillIndex(String skill) {
        for (int i = 0; i < names.length; i++) {
            if (skill.equalsIgnoreCase(names[i])) {
                return i;
            }
        }
        return -1;
    }
    
    class AreaSelect {
        Block b1;
        Block b2;
        public AreaSelect() {
            b1 = null;
            b2 = null;
        }
    }
    class TrainingArea {
        LinkedList<Integer> skills;
        int xf, yf, zf, xt, yt, zt;
        String areaname;
        public TrainingArea(AreaSelect area, Player player, String areaname, boolean skipcheck) {
            this.areaname = areaname;
            setBounds(area);
            player.sendMessage(ChatColor.GREEN + String.format("Area %s has been created.", areaname));
            skills = new LinkedList<Integer>();
        }
        public void resize(AreaSelect area, Player player) {
            setBounds(area);
            player.sendMessage(ChatColor.GREEN + "Resized/moved.");
        }
        public void modSkill(String skill, boolean add, Player player) {
            int i = getSkillIndex(skill);
            if (i != -1) {
                if (add) {
                    skills.add(i);
                    player.sendMessage(ChatColor.GREEN + String.format("%s has been added to %s", skill.toLowerCase(), areaname));
                } else {
                    if (skills.contains(i)) {
                        skills.remove(i);
                        player.sendMessage(ChatColor.GREEN + "Skill has been removed.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Skill does not exist in area.");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "No such skill.");
            }
        }
        public void setBounds(AreaSelect area) {
            xf = Math.min(area.b1.getX(), area.b2.getX())-1;
            xt = Math.max(area.b1.getX(), area.b2.getX())+1;
            yf = Math.min(area.b1.getY(), area.b2.getY())-1;
            yt = Math.max(area.b1.getY(), area.b2.getY())+1;
            zf = Math.min(area.b1.getZ(), area.b2.getZ())-1;
            zt = Math.max(area.b1.getZ(), area.b2.getZ())+1;
            conf.setProperty(String.format("areas.%s.xf", areaname), xf);
            conf.setProperty(String.format("areas.%s.xt", areaname), xt);
            conf.setProperty(String.format("areas.%s.yf", areaname), yf);
            conf.setProperty(String.format("areas.%s.yt", areaname), zt);
            conf.setProperty(String.format("areas.%s.zf", areaname), zf);
            conf.setProperty(String.format("areas.%s.zt", areaname), zt);
            conf.save();
        }
        public boolean isInArea(Location loc) {
            if (xf < loc.getX() && loc.getX() < xt && yf < loc.getY() && loc.getY() < yt && zf < loc.getZ() && loc.getZ() < zt) {
                return true;
            } else {
                return false;
            }
        }
    }
}