package pl.trollcraft.AntyLogout.PvpPoints;

public class PVPUser {
     private String name;
     private int kills;
     private int deaths;
     private int logouts;

     public PVPUser(String name, int kills, int deaths, int logouts) {
          this.name = name;
          this.kills = kills;
          this.deaths = deaths;
          this.logouts = logouts;
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

     public void substractKills() {
        --this.kills;
    }

     public int getDeaths() {
        return this.deaths;
    }

     public void addDeaths() {
        ++this.deaths;
    }

     public int getLogouts() {
         return this.logouts;
     }

     public void addLogouts() {
         ++this.logouts;
     }

     public double getKDR() {
         if (this.deaths == 0){
             return this.kills;
         } else {
          return (double)this.kills / (double)this.deaths;
         }
     }
}



