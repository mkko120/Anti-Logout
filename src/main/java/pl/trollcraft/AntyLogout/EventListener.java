package pl.trollcraft.AntyLogout;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class EventListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
            if (ev.getDamager() instanceof Player && ev.getEntity() instanceof Player) {
                Player player = ((Player) ev.getEntity()).getPlayer();
                Location location = player.getLocation();
                RegionManager regionManager = WorldGuard.getInstance()
                        .getPlatform()
                        .getRegionContainer()
                        .get(BukkitAdapter.adapt(player.getWorld()));
                assert regionManager != null;
                ArrayList<String> strefa = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony");
                ArrayList<ProtectedRegion> regiony = new ArrayList<>();
                for(String region_name : strefa){
                    regiony.add(regionManager.getRegion(region_name));
                }
                boolean test = false;
                for (ProtectedRegion region : regiony) {
                    if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                        test = true;
                    }
                }
                if (test) {
                    Player atacker = ((Player) ev.getDamager()).getPlayer();
                    Player victim = ((Player) ev.getEntity()).getPlayer();
                    TimestampManager.getInstance().setCooldown(atacker.getUniqueId(), AntyLogout.getInstance().getConfig().getInt("czas"));
                    TimestampManager.getInstance().setCooldown(victim.getUniqueId(), AntyLogout.getInstance().getConfig().getInt("czas"));
                }
            }
        }
    }
    public void onQuit(PlayerQuitEvent event) {
        double cooldown = TimestampManager.getInstance().getCooldown(event.getPlayer().getUniqueId());
        if (cooldown > 0) {
            Player player = event.getPlayer();
            player.getInventory().clear();
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.getLore().set(1, "Lognales podczas walki wiec tracisz itemy!");
            meta.setDisplayName(Helper.color("&cInformacja"));
            paper.setItemMeta(meta);
            player.getInventory().setItem(36, paper);
            dropItems(player);
        }
    }
    public void onCommand(PlayerCommandPreprocessEvent event) {
        double cooldown = TimestampManager.getInstance().getCooldown(event.getPlayer().getUniqueId());
        if (cooldown > 0) {
            int cooldownZostalo = (int) cooldown / 1000;
            event.setCancelled(true);
            event.getPlayer().sendMessage(Helper.color("&6[&4Anti&cLogout&6] &cJestes w walce! Walcz, a nie uzywasz komend! \n Do końca walki pozostało: " + cooldownZostalo));
        }
    }
    private void dropItems(Player player) {
        ItemStack[] stack = player.getInventory().getContents();
        ItemStack[] armorstack = player.getInventory().getArmorContents();
        for (ItemStack itemStack : armorstack) {
            if (itemStack == null) {
                itemStack = new ItemStack(Material.AIR);
            }
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
        for (ItemStack itemStack : stack) {
            if (itemStack == null) {
                itemStack = new ItemStack(Material.AIR);
            }
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }
}
