package pl.trollcraft.AntyLogout;

import org.bukkit.configuration.file.FileConfiguration;
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
}
