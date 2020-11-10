package pl.trollcraft.AntyLogout;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import pl.trollcraft.AntyLogout.PvpPoints.PVPUser;
import pl.trollcraft.AntyLogout.PvpPoints.PVPUsersController;

import java.util.ArrayList;
import java.util.HashMap;

public class EventListener implements Listener {
    private final HashMap<Player, Float> xp = new HashMap<>();
    private final HashMap<Player, Integer> lvl = new HashMap<>();
    private final PVPUsersController controller = AntyLogout.getPlugin(AntyLogout.class).getPvpUsersController();
    private final ArrayList<String> pvp = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony pvp");
    private final ArrayList<String> dungeon = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony dungeon");
    @EventHandler
    public void onDamagePvp(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
            if (ev.getDamager() instanceof Player && ev.getEntity() instanceof Player) {
                PVPUser user = controller.find(ev.getEntity().getName());
                PVPUser user2 = controller.find(ev.getDamager().getName());
                if (user == null){
                    user = new PVPUser(user.getName(), 0, 0);
                    controller.register(user);
                }
                if (user2 == null) {
                    user2 = new PVPUser(user2.getName(), 0, 0);
                    controller.register(user2);
                }
                Player atacker = ((Player) ev.getDamager()).getPlayer();
                Player victim = ((Player) ev.getEntity()).getPlayer();
                cooldown(atacker, victim);
            }
        }
    }
    public void onDamageDungeon(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
            if (ev.getEntity() instanceof Player) {
                PVPUser user = controller.find(ev.getEntity().getName());
                if (user == null){
                    user = new PVPUser(ev.getEntity().getName(), 0, 0);
                    controller.register(user);
                }
                Player victim = ((Player) ev.getEntity()).getPlayer();
                if (getPlayerRegion(victim, dungeon)) {
                    TimestampManager.getInstance().setCooldown(victim, TimestampManager.DEFAULT_COOLDOWN);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int timeLeftVictim = TimestampManager.getInstance().getCooldown(victim);
                            TimestampManager.getInstance().setCooldown(victim, --timeLeftVictim);
                            if(timeLeftVictim > 0){
                                if (xp.get(victim) == null && lvl.get(victim) == null) {
                                    xp.put(victim, victim.getExp());
                                    lvl.put(victim, victim.getLevel());
                                }
                                victim.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        TextComponent.fromLegacyText(Helper.color("&cJestes w walce!")));
                                victim.setExp(barChanger(victim));
                                victim.setLevel(TimestampManager.getInstance().getCooldown(victim));
                            } else {
                                if (xp.get(victim) != null && lvl.get(victim) != null) {
                                    victim.setExp(xp.get(victim));
                                    victim.setLevel(lvl.get(victim));
                                    xp.remove(victim);
                                    lvl.remove(victim);
                                }
                                this.cancel();
                                TimestampManager.getInstance().setCooldown(victim, 0);
                            }
                        }
                    }.runTaskTimer(AntyLogout.getPlugin(AntyLogout.class), 0, 20);

                }

            }
        }
    }
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PVPUser user = controller.find(player.getName());
        double cooldown = TimestampManager
                .getInstance()
                .getCooldown(event.getPlayer());
        if (cooldown > 0) {
            player.getInventory().clear();
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(Helper.color("&cInformacja"));
            meta.getLore().set(1, "Lognales podczas walki wiec tracisz itemy!");
            paper.setItemMeta(meta);
            player.getInventory().setItemInMainHand(paper);
            dropItems(player);
        }
        controller.unregister(user);
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
    public void onKill (PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (!(victim.getKiller() instanceof Player)){
            return;
        }
        PVPUser killerUser = controller.find(killer.getName());
        if (getPlayerRegion(killer, pvp)) {
            PVPUser victimUser = controller.find(victim.getName());
            victimUser.addDeaths();
            killerUser.addKills();
        } else {
            killerUser.substractKills();
        }
    }
    public void onUse (PlayerInteractEvent event) {
        double cooldown = TimestampManager.getInstance().getCooldown(event.getPlayer());
        if (cooldown > 0) {
            event.setCancelled(true);
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
    private boolean getPlayerRegion(Player player, ArrayList<String> strefa) {
        Location location = player.getLocation();
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter
                        .adapt(player.getWorld()));
        assert regionManager != null;
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
        return test;
    }
    private void cooldown(Player atacker, Player victim) {
        if (getPlayerRegion(atacker, pvp) || getPlayerRegion(victim, pvp) || getPlayerRegion(atacker, dungeon) || getPlayerRegion(victim, dungeon)) {
            TimestampManager.getInstance().setCooldown(atacker, TimestampManager.DEFAULT_COOLDOWN);
            TimestampManager.getInstance().setCooldown(victim, TimestampManager.DEFAULT_COOLDOWN);
            new BukkitRunnable() {
                @Override
                public void run() {
                    int timeLeftAtacker = TimestampManager.getInstance().getCooldown(atacker);
                    int timeLeftVictim = TimestampManager.getInstance().getCooldown(victim);
                    TimestampManager.getInstance().setCooldown(atacker, --timeLeftAtacker);
                    TimestampManager.getInstance().setCooldown(victim, --timeLeftVictim);
                    if(timeLeftAtacker > 0 || timeLeftVictim > 0){
                        if (xp.get(atacker) == null && lvl.get(atacker) == null || xp.get(victim) == null && lvl.get(victim) == null) {
                            xp.put(atacker, atacker.getExp());
                            xp.put(victim, victim.getExp());
                            lvl.put(atacker, atacker.getLevel());
                            lvl.put(victim, victim.getLevel());
                        }
                        atacker.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(Helper.color("&cJestes w walce!")));
                        victim.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(Helper.color("&cJestes w walce!")));
                        atacker.setExp(barChanger(atacker));
                        victim.setExp(barChanger(victim));
                        atacker.setLevel(TimestampManager.getInstance().getCooldown(atacker));
                        victim.setLevel(TimestampManager.getInstance().getCooldown(victim));
                    } else {
                        if (xp.get(atacker) != null && lvl.get(atacker) != null || xp.get(victim) != null && lvl.get(victim) != null) {
                            atacker.setExp(xp.get(atacker));
                            victim.setExp((xp.get(victim)));
                            atacker.setLevel(lvl.get(atacker));
                            victim.setLevel(lvl.get(victim));
                            xp.remove(atacker);
                            xp.remove(victim);
                            lvl.remove(atacker);
                            lvl.remove(victim);
                        }
                        this.cancel();
                        TimestampManager.getInstance().setCooldown(atacker, 0);
                        TimestampManager.getInstance().setCooldown(victim, 0);
                    }
                }
            }.runTaskTimer(AntyLogout.getPlugin(AntyLogout.class), 0, 20);

        }
    }
}
