package pl.trollcraft.AntyLogout;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TimestampManager {

    private static TimestampManager instance = new TimestampManager();

    public static TimestampManager getInstance() {
        return instance;
    }

    private final Map<Player, Integer> cooldowns = new HashMap<>();

    public static final int DEFAULT_COOLDOWN = AntyLogout.getInstance().getConfig().getInt("czas");

    public void setCooldown(Player player, int time){
        if(time < 1) {
            cooldowns.remove(player);
        } else {
            cooldowns.put(player, time);
        }
    }

    public int getCooldown(Player player) {
        return cooldowns.getOrDefault(player, 0);
    }
}
