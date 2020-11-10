package pl.trollcraft.AntyLogout;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import pl.trollcraft.AntyLogout.PvpPoints.PVPUsersController;

import java.io.File;
import java.io.IOException;

public class AntyLogout extends JavaPlugin {
    private static AntyLogout instance;

    private FileConfiguration usersConfig;

    public static AntyLogout getInstance() {
        return instance;
    }

    private PVPUsersController pvpUsersController;

    @Override
    public void onEnable() {

        pvpUsersController = new PVPUsersController();
        BukkitScheduler scheduler = getServer().getScheduler();
        instance = this;
        final FileConfiguration config = getInstance().getConfig();
        File file = new File(instance.getDataFolder(), "config.yml");
        if (!(file.exists())) {
            instance.saveDefaultConfig();
            config.options().copyDefaults(true);
        }
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    public void loadUsers(){

        File users = new File(getDataFolder(),"users.yml");
        if (!users.exists()) {
            users.getParentFile().mkdirs();
            saveResource("users.yml", false);
        }
        usersConfig = new YamlConfiguration();
        try {
            usersConfig.load(users);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public PVPUsersController getPvpUsersController() {
        return pvpUsersController;
    }

    public FileConfiguration getUsersConfig() {
        return usersConfig;
    }

    @Override
    public void onDisable() {
        try {
            getInstance().getUsersConfig().save("users.yml");
            getInstance().getConfig().save("config.yml");
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
