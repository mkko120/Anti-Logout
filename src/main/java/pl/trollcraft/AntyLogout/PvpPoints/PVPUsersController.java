package pl.trollcraft.AntyLogout.PvpPoints;

import pl.trollcraft.AntyLogout.AntyLogout;

import java.util.ArrayList;
import java.util.List;


public class PVPUsersController {

    private List<PVPUser> instances;

    public PVPUsersController () {

        instances = new ArrayList<>();

    }

    public void register(PVPUser user) {

        instances.add(user);
        AntyLogout.getInstance().getUsersConfig().set("dane graczy", instances);

    }

    public void unregister(PVPUser user) {

        instances.remove(user);

    }

    public PVPUser find(String playerName) {

        for (PVPUser user : instances) {

            if (user.getName().equals(playerName)) {
                return user;
            }

        }

        return null;
    }

    public void save() {
        AntyLogout.getInstance().getUsersConfig().set("dane graczy", instances);
    }

}
