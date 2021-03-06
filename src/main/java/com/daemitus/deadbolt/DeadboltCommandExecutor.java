package com.daemitus.deadbolt;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeadboltCommandExecutor implements CommandExecutor {

    private final DeadboltPlugin plugin;

    public DeadboltCommandExecutor(final DeadboltPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            return onPlayerCommand((Player) sender, command, label, args);
        } else {
            return onConsoleCommand(sender, command, label, args);
        }
    }

    private boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
        int arg = args.length;

        if (arg == 0) {
            player.sendMessage(ChatColor.RED + "Deadbolt v" + plugin.getDescription().getVersion());
            player.sendMessage(ChatColor.RED + Deadbolt.getLanguage().cmd_help_editsign);
            if (player.hasPermission(Perm.command_reload)) {
                player.sendMessage(ChatColor.RED + Deadbolt.getLanguage().cmd_help_reload);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return reload(player);
        }
        try {
            return lineChange(player, Integer.valueOf(args[0]), args);
        } catch (NumberFormatException ex) {
        }

        Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().cmd_command_not_found);
        return true;
    }

    private boolean reload(Player player) {
        Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().cmd_reload);
        Deadbolt.getPlugin().bootStrap();
        return true;
    }

    private boolean lineChange(Player player, int lineNum, String[] args) {
        if (lineNum < 1 || lineNum > 4) {
            Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().cmd_line_num_out_of_range);
            return true;
        }
        Block block = Deadbolt.getConfig().selectedSign.get(player);
        if (block == null) {
            Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().cmd_sign_not_selected);
            return true;
        }
        if (!(block.getBlockData() instanceof WallSign)) {
            Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().cmd_sign_selected_error);
            Deadbolt.getConfig().selectedSign.remove(player);
            return true;
        }

        lineNum--;
        Sign sign = (Sign) block.getState();
        String[] lines = sign.getLines();

        String text = "";
        for (int i = 1; i < args.length; i++) {
            text += args[i] + (i + 1 < args.length ? " " : "");
        }

        text = Util.formatForSign(text);
        if (lineNum == 0) {
            if (!Util.removeColor(lines[0]).equalsIgnoreCase(Util.removeColor(text))) {
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().cmd_identifier_not_changeable);
                return true;
            }
        } else if (lineNum == 1 && Deadbolt.getLanguage().isPrivate(Util.removeColor(lines[0]))) {
            if (!Util.removeColor(lines[1]).equalsIgnoreCase(Util.removeColor(text))) {
                if (!player.hasPermission(Perm.admin_change_owner)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().cmd_owner_not_changeable);
                    return true;
                }
            }
        }
        lines[lineNum] = text;

        if (Deadbolt.getConfig().clear_sign_selection) {
            Deadbolt.getConfig().selectedSign.remove(player);
        }

        boolean isPrivate = Deadbolt.getLanguage().isPrivate(Util.removeColor(lines[0]));
        boolean isMoreUsers = Deadbolt.getLanguage().isMoreUsers(Util.removeColor(lines[0]));
        if (isPrivate) {
            for (int i = 0; i < 4; i++) {
                lines[i] = Util.formatForSign(lines[i]);
            }
        } else if (isMoreUsers) {
            for (int i = 0; i < 4; i++) {
                lines[i] = Util.formatForSign(lines[i]);
            }
        }
        sign.update(true);
        Deadbolt.getConfig().sendMessage(player, ChatColor.GOLD, Deadbolt.getLanguage().cmd_sign_updated);
        return true;
    }

    private boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
        int arg = args.length;
        if (arg == 0) {
            sender.sendMessage("[Deadbolt] " + plugin.getDescription().getVersion() + " options: reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return creload(sender);
        }

        sender.sendMessage("[Deadbolt] " + Deadbolt.getLanguage().cmd_console_command_not_found);
        return true;
    }

    private boolean creload(CommandSender sender) {
        sender.sendMessage("[Deadbolt] " + Deadbolt.getLanguage().cmd_console_reload);
        Deadbolt.getPlugin().bootStrap();
        return true;
    }
}
