package pl.trollcraft.AntyLogout.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.trollcraft.AntyLogout.AntyLogout;
import pl.trollcraft.AntyLogout.Helper;
import pl.trollcraft.AntyLogout.PvpPoints.PVPUser;
import pl.trollcraft.AntyLogout.PvpPoints.PVPUsersController;

public class StatsCommand implements CommandExecutor {

    private PVPUsersController controller = AntyLogout.getInstance().getPvpUsersController();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        switch (args.length) {

            case 0:
                if (!(sender instanceof Player)) {
                sender.sendMessage(Helper.color("Musisz byc graczem aby uzyc tej komendy"));
                } else {
                    PVPUser user = controller.find(sender.getName());
                    getStatistics(sender, user);
                }
                break;

            case 1:
                PVPUser user = null;
                if (sender.hasPermission("antilogout.stats.others")) {
                    if (args[0] != null) {
                        user = controller.find(args[0]);
                    } else {
                        return true;
                    }
                } else {
                    user = controller.find(sender.getName());
                }
                getStatistics(sender, user);
                break;


            default:
                sender.sendMessage(Helper.color("Uzycie: &a./" + label));
                break;
        }
        return true;
    }

    private void getStatistics(CommandSender sender, PVPUser user) {
        if (user == null) {
            sender.sendMessage(Helper.color("&cBrak gracza!"));
        } else {
            sender.sendMessage(Helper.color("&aStatystyki dla gracza: &l" + user.getName()));
            sender.sendMessage(Helper.color("&2Zabojstwa: &c&l" + user.GetKills() + "&r&2, Smierci: &c&l" + user.getDeaths()));
            sender.sendMessage(Helper.color("&2KDR: &c&l" + user.getKDR() + "&r&2, Wyjscia podczas walki: &c&l" + user.getLogouts()));
            sender.sendMessage(Helper.color("Statystyki aktualizuja sie &lpo wyjsciu z serwera &rwiec zanim zglosisz blad, sprobuj relognac ;)"));
        }
    }
}
