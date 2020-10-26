package pl.trollcraft.AntyLogout;

import org.bukkit.ChatColor;

public class Helper {

    public static String color(String toColor) {
        return toColor.replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR));
    }
}

