package pl.trollcraft.AntyLogout;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AntyLogout extends JavaPlugin {
    private static AntyLogout instance;

    public static AntyLogout getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        final FileConfiguration config = getInstance().getConfig();
        File file = new File(instance.getDataFolder(), "config.yml");
        if (!(file.exists())) {
            instance.saveDefaultConfig();
            config.options().copyDefaults(true);
        }
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin)) {
            return null; //throws a NullPointerException, telling the Admin that WG is not loaded.
        }
        return (WorldGuardPlugin)plugin;
    }
}
