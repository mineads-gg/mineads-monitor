package gg.mineads.monitor.bungee;

import gg.mineads.monitor.shared.MineAdsMonitorBootstrap;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public class MineAdsMonitorBungee extends Plugin implements MineAdsMonitorBootstrap {
  private BungeeAudiences adventure;

  @Override
  public void onEnable() {
    this.adventure = BungeeAudiences.create(this);
    new Metrics(this, 27109);
  }

  @Override
  public void onDisable() {
    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }
}
