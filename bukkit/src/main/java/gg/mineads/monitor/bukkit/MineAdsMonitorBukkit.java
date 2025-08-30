package gg.mineads.monitor.bukkit;

import com.tcoded.folialib.FoliaLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import gg.mineads.monitor.shared.*;
import org.bukkit.plugin.java.JavaPlugin;

public class MineAdsMonitorBukkit extends JavaPlugin implements MineAdsMonitorPlatform {
  private FoliaLib foliaLib;
  private BukkitAudiences adventure;

  @Override
  public void onEnable() {
    this.foliaLib = new FoliaLib(this);
    this.adventure = BukkitAudiences.create(this);
  }

  @Override
  public void onDisable() {
    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }
}
