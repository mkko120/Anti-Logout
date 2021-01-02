package pl.trollcraft.AntyLogout;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;

public class EventFunctions {

    private static EventFunctions instance = new EventFunctions();
    public final HashMap<Player, Integer> antylogout = AntyLogout.getInstance().antylogout;
    private final ArrayList<String> pvp = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony pvp");
    private final ArrayList<String> dungeon = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony dungeon");

    public static EventFunctions getInstance() {
        return instance;
    }

    public void dropItems(Player player) {
        ItemStack[] stack = player.getInventory().getContents();
        ItemStack[] armorstack = player.getInventory().getArmorContents();
        for (ItemStack itemStack : armorstack) {
            if (!(itemStack == null)) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            }
        }
        for (ItemStack itemStack : stack) {
            if (!(itemStack == null)) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            }
        }
    }

    public boolean getPlayerRegion(Player player, ArrayList<String> strefa, String swiat) {
        Location location = player.getLocation();
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(Bukkit.getWorld(swiat)));
        assert regionManager != null;
        ArrayList<ProtectedRegion> regiony = new ArrayList<>();
        for (String region_name : strefa) {
            regiony.add(regionManager.getRegion(region_name));
        }

        boolean test = false;
        for (ProtectedRegion region : regiony) {
            if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                test = true;
            } else {
                return false;
            }
        }
        return test;
    }

    public void cooldown(Player player) {
        assert pvp != null;
        assert dungeon != null;
        if (getPlayerRegion(player, pvp, EventListener.swiat) || getPlayerRegion(player, dungeon, EventListener.swiat)) {
            if (!player.hasPermission("antilogout.override") || player.getGameMode() == GameMode.SURVIVAL) {
                if (!antylogout.containsKey(player)) {
                    antylogout.put(player, AntyLogout.getInstance().getConfig().getInt("cooldown"));
                } else {
                    antylogout.replace(player, AntyLogout.getInstance().getConfig().getInt("cooldown"));
                }
            }
        }
    }
}

