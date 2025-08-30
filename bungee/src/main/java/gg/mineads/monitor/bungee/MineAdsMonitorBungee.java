package gg.mineads.monitor.bungee;

import gg.mineads.monitor.shared.MineAdsMonitorPlatform;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;

public class MineAdsMonitorBungee extends Plugin implements MineAdsMonitorPlatform {
  private BungeeAudiences adventure;

  @Override
  public void onEnable() {
    this.adventure = BungeeAudiences.create(this);
  }

  @Override
  public void onDisable() {
    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }
}
