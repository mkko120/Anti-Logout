package pl.trollcraft.AntyLogout;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
    private final PVPUsersController controller = AntyLogout.getPlugin(AntyLogout.class).getPvpUsersController();
    private final ArrayList<String> pvp = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony pvp");
    private final ArrayList<String> dungeon = (ArrayList<String>) AntyLogout.getInstance().getConfig().getList("regiony dungeon");
    private final HashMap<Player, Float> xp = new HashMap<>();
    private final HashMap<Player, Integer> lvl = new HashMap<>();

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
                EventFunctions.getInstance().cooldown(atacker, victim);
            }
        }
    }
    @EventHandler
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
                if (EventFunctions.getInstance().getPlayerRegion(victim, dungeon)) {
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
                                victim.setExp(EventFunctions.getInstance().barChanger(victim));
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
    @EventHandler
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
            EventFunctions.getInstance().dropItems(player);
        }
        controller.unregister(user);
    }
    @EventHandler
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
    @EventHandler
    public void onKill (PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (!(victim.getKiller() instanceof Player)){
            return;
        }
        PVPUser killerUser = controller.find(killer.getName());
        if (EventFunctions.getInstance().getPlayerRegion(killer, pvp)) {
            PVPUser victimUser = controller.find(victim.getName());
            victimUser.addDeaths();
            killerUser.addKills();
        } else {
            killerUser.substractKills();
        }
    }
    @EventHandler
    public void onUse (PlayerInteractEvent event) {
        double cooldown = TimestampManager.getInstance().getCooldown(event.getPlayer());
        if (cooldown > 0) {
            event.setCancelled(true);
        }
    }


}
