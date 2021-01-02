package pl.trollcraft.AntyLogout;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import pl.trollcraft.AntyLogout.Commands.ReloadCommand;
import pl.trollcraft.AntyLogout.Commands.StatsCommand;
import pl.trollcraft.AntyLogout.PvpPoints.PVPUsersController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class AntyLogout extends JavaPlugin {
    private static AntyLogout instance;

    private YamlConfiguration usersConfig;

    public static AntyLogout getInstance() {
        return instance;
    }

    private PVPUsersController pvpUsersController;
    public HashMap<Player, Integer> antylogout = new HashMap<>();

    @Override
    public void onEnable() {

        pvpUsersController = new PVPUsersController();
        instance = this;
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getCommand("logoutreload").setExecutor(new ReloadCommand());
        getCommand("logoutstatistics").setExecutor(new StatsCommand());
        loadUsers();
        loadConfig();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : EventFunctions.getInstance().antylogout.keySet()) {

                    if (player.isOnline()) {
                        int var = antylogout.get(player);
                        if (var != 0) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    TextComponent.fromLegacyText(Helper
                                            .color("&6[&4Anti&cLogout&6] &cJestes w walce! || Pozostalo " + var + "s")));
                            antylogout.replace(player, var-1);
                        } else {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    TextComponent.fromLegacyText(Helper
                                            .color("&6[&4Anti&cLogout&6] &aNie jestes juz w walce!")));
                        }
                    }
                }
            }
        }.runTaskTimer(AntyLogout.getPlugin(AntyLogout.class), 20, 20);

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
    public void loadConfig() {
        FileConfiguration config = getInstance().getConfig();
        File file = new File(instance.getDataFolder(), "config.yml");
        if (!(file.exists())) {
            instance.saveDefaultConfig();
            config.options().copyDefaults(true);
        }
    }

    public PVPUsersController getPvpUsersController() {
        return pvpUsersController;
    }

    public YamlConfiguration getUsersConfig() {
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
