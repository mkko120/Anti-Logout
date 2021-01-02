package pl.trollcraft.AntyLogout;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import pl.trollcraft.AntyLogout.PvpPoints.PVPUser;
import pl.trollcraft.AntyLogout.PvpPoints.PVPUsersController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public class EventListener implements Listener {
    private HashMap<Player, Integer> antylogout = AntyLogout.getInstance().antylogout;
    private HashMap<Player, Integer> logouts = new HashMap<>();
    private final PVPUsersController controller = AntyLogout.getPlugin(AntyLogout.class).getPvpUsersController();
    private final ArrayList<String> pvp = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony pvp");
    private final ArrayList<String> dungeon = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony dungeon");
    public static final String swiat = AntyLogout.getInstance().getConfig().getString("swiat");

    @EventHandler
    public void onDamagePvp(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
            if (ev.getEntity() instanceof Player) {
                Entity atacker = ev.getDamager();
                Player victim = ((Player) ev.getEntity()).getPlayer();
                String player1 = atacker.getName();
                String player2 = victim.getName();
                PVPUser user = controller.find(player1);
                PVPUser user2 = controller.find(player2);
                if (user == null){
                    user = new PVPUser(player1, 0, 0, 0);
                    controller.register(user);
                }
                if (user2 == null) {
                    user2 = new PVPUser(player2, 0, 0, 0);
                    controller.register(user2);
                }
                if (ev.getDamager() instanceof Player) {
                    EventFunctions.getInstance().cooldown((Player) atacker);
                    EventFunctions.getInstance().cooldown(victim);
                } else if (ev.getDamager() instanceof Arrow) {
                    Player arrowDamager = (Player) ((Arrow) ev.getDamager()).getShooter();
                    EventFunctions.getInstance().cooldown(arrowDamager);
                    EventFunctions.getInstance().cooldown(victim);
                } else {
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onDamageDungeon(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
            Player player = null;
            if (ev.getEntity() instanceof Player && !(ev.getDamager() instanceof Player)) {
                player = ((Player) ev.getEntity()).getPlayer();
            } else if (!(ev.getEntity() instanceof Player) && ev.getDamager() instanceof Player) {
                player = ((Player) ev.getDamager()).getPlayer();
            }

            if (player == null) {
                throw (new NullPointerException("Error when initalizing player: Player is not initalized correctly!!"));
            }
            String username = player.getName();
            PVPUser user = controller.find(username);
            if (user == null){
                user = new PVPUser(username, 0, 0, 0);
                controller.register(user);
            }
            assert dungeon != null;
            assert player != null;
            if (EventFunctions.getInstance().getPlayerRegion(player, dungeon, swiat)) {
                if ((!player.hasPermission("antilogout.override")) && player.getGameMode() == GameMode.SURVIVAL) {
                    if (!player.hasPermission("antilogout.override")) {
                        EventFunctions.getInstance().cooldown(player);
                    }
                };
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PVPUser user = controller.find(player.getName());
        double cooldown = 0;
        if (antylogout.containsKey(player)) {
            cooldown = antylogout.get(player);
        }
        if (cooldown > 0) {
            user.addLogouts();
            Entity damage = event.getPlayer().getLastDamageCause().getEntity();
            if (damage == null) {
                return;
            } else {
                EventFunctions.getInstance().dropItems(player);
                player.getInventory().clear();
                if (damage instanceof Player) {
                    antylogout.put((Player)damage, -1);
                }
                Bukkit.broadcastMessage(Helper.color("&6[&4Anti&cLogout&6] &6Gracz &l" + player.getName() + " &r&6wylogowal sie podczas walki!"));
            }
        }

    }

    @EventHandler
    public void onJoin (PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (controller.find(player.getName()) == null) {
           PVPUser user = new PVPUser(player.getName(), 0, 0, 0);
           controller.register(user);
        }
        PVPUser logoutsPvp = controller.find(player.getName());
        if (logoutsPvp != null && logouts.containsKey(event.getPlayer())) {
            if (logouts.get(player) < logoutsPvp.getLogouts()) {
                Player rageplayer = event.getPlayer();
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName(Helper.color("&cInformacja"));
                meta.getLore().set(0, "Lognales podczas walki wiec tracisz itemy!");
                paper.setItemMeta(meta);
                rageplayer.getInventory().addItem(paper);
                logouts.put(player, logoutsPvp.getLogouts());
            }
        } else {
            PVPUser user = new PVPUser(player.getName(), 0, 0, 0);
            controller.register(user);
            logouts.put(event.getPlayer(), controller.find(player.getName()).getLogouts());
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        double cooldown = antylogout.getOrDefault(event.getPlayer(), 0);
        if (cooldown > 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Helper.color("&6[&4Anti&cLogout&6] &cJestes w walce! Walcz, a nie uzywasz komend!\n&6[&4Anti&cLogout&6] &cDo końca walki pozostało: " + cooldown + "s."));
        } else if (cooldown == 0) {
            return;
        }
    }

    @EventHandler
    public void onKill (PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (!(victim.getKiller() instanceof Player)){
            return;
        }
        event.setDroppedExp(0);
        PVPUser killerUser = controller.find(killer.getName());
        assert pvp != null;
        if (EventFunctions.getInstance().getPlayerRegion(killer, pvp, swiat)) {
            PVPUser victimUser = controller.find(victim.getName());
            victimUser.addDeaths();
            killerUser.addKills();
            ItemStack air = new ItemStack(Material.AIR);
            if (killer.getInventory().getItemInMainHand() == air) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "aach add 1 mcgyver " + killer.getName());
            }
            if (killerUser.GetKills() == 1000) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "aach give ");
            }
        } else {
            killerUser.substractKills();
        }
        antylogout.remove(victim);
    }
}
