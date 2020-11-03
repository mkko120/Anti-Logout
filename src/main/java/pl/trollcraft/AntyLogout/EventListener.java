package pl.trollcraft.AntyLogout;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.HashMap;

public class EventListener implements Listener {
    private final HashMap<Player, Float> xp = new HashMap<>();
    private final HashMap<Player, Integer> lvl = new HashMap<>();
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
                ArrayList<String> strefa = (ArrayList<String>) AntyLogout
                        .getInstance()
                        .getConfig()
                        .getList("regiony");
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
                    TimestampManager.getInstance().setCooldown(atacker, TimestampManager.DEFAULT_COOLDOWN);
                    TimestampManager.getInstance().setCooldown(victim, TimestampManager.DEFAULT_COOLDOWN);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int timeLeftAtacker = TimestampManager.getInstance().getCooldown(atacker);
                            int timeLeftVictim = TimestampManager.getInstance().getCooldown(victim);
                            TimestampManager.getInstance().setCooldown(player, --timeLeftAtacker);
                            TimestampManager.getInstance().setCooldown(player, --timeLeftAtacker);
                            if(timeLeftAtacker == 0 || timeLeftVictim == 0){
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(AntyLogout.getPlugin(AntyLogout.class), 20, 20);

                }
            }
        }
    }

    public void barRefresh(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (TimestampManager.getInstance().getCooldown(player) > 0) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Helper.color("&cJestes w walce!")));
            if (xp.get(player) == null && lvl.get(player) == null) {
                xp.put(player, player.getExp());
                lvl.put(player, player.getLevel());
            }
            player.setExp(barChanger(player));
            player.setLevel(TimestampManager.getInstance().getCooldown(player));
        } else {
            if (xp.get(player) != null && lvl.get(player) != null) {
                player.setExp(xp.get(player));
                player.setLevel(lvl.get(player));
                xp.remove(player);
                lvl.remove(player);
            }
        }
    }
    public void onQuit(PlayerQuitEvent event) {
        double cooldown = TimestampManager
                .getInstance()
                .getCooldown(event.getPlayer());
        if (cooldown > 0) {
            Player player = event.getPlayer();
            player.getInventory().clear();
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(Helper.color("&cInformacja"));
            meta.getLore().set(1, "Lognales podczas walki wiec tracisz itemy!");
            paper.setItemMeta(meta);
            player.getInventory().setItemInMainHand(paper);
            dropItems(player);
        }
    }
    public void onCommand(PlayerCommandPreprocessEvent event) {
        double cooldown = TimestampManager
                .getInstance()
                .getCooldown(event.getPlayer());
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
    private float barChanger(Player player) {
        float x = TimestampManager.getInstance().getCooldown(player);
        float z = TimestampManager.DEFAULT_COOLDOWN;
        return x / z;

    }
}
