package pl.trollcraft.AntyLogout;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimestampManager {

    private static TimestampManager instance = new TimestampManager();

    public static TimestampManager getInstance() {
        return instance;
    }

    private final Map<UUID, Integer> cooldowns = new HashMap<>();

    public static final int DEFAULT_COOLDOWN = 15;

    public void setCooldown(UUID player, int time){
        if(time < 1) {
            cooldowns.remove(player);
        } else {
            cooldowns.put(player, time);
        }
    }

    public int getCooldown(UUID player) {
        return cooldowns.getOrDefault(player, 0);
    }
}
