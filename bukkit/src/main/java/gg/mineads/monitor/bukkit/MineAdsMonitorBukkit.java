package gg.mineads.monitor.bukkit;

import com.tcoded.folialib.FoliaLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import gg.mineads.monitor.shared.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class MineAdsMonitorBukkit extends JavaPlugin implements MineAdsMonitorBootstrap {
  private FoliaLib foliaLib;
  private BukkitAudiences adventure;

  @Override
  public void onEnable() {
    this.foliaLib = new FoliaLib(this);
    this.adventure = BukkitAudiences.create(this);
    new Metrics(this, 27108);
  }

  @Override
  public void onDisable() {
    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }
}
