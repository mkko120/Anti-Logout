package pl.trollcraft.AntyLogout.model;

import java.util.UUID;

public class FightUser {

    private static FightUser instance;

    public static FightUser getInstance() {
        return instance;
    }

    private UUID uuid;

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        instance = this;
        return uuid;
    }


}
