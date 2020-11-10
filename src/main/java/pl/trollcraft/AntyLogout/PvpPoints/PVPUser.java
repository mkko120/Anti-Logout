package pl.trollcraft.AntyLogout.PvpPoints;

public class PVPUser {
     private String name;
     private int kills;
     private int deaths;

     public PVPUser(String name, int kills, int deaths) {
          this.name = name;
          this.kills = kills;
          this.deaths = deaths;
     }

     public String getName() {
        return this.name;
    }

     public int GetKills() {
        return this.kills;
    }

     public void addKills() {
        ++this.kills;
    }

     public int getDeaths() {
        return this.deaths;
    }

     public void addDeaths() {
        ++this.deaths;
    }

     public void substractKills() {
         --this.kills;
     }

     public double getKDR() {
         return this.deaths == 0 ?
                 (double)this.kills : (double)this.kills / (double)this.deaths;
     }
}



