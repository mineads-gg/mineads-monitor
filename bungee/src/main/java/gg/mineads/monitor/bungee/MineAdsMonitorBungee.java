/*
 * MineAdsMonitor
 * Copyright (C) 2025  MineAds
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gg.mineads.monitor.bungee;

import gg.mineads.monitor.bungee.listener.PlayerListener;
import gg.mineads.monitor.bungee.scheduler.BungeeScheduler;
import gg.mineads.monitor.shared.AbstractMineAdsMonitorBootstrap;
import gg.mineads.monitor.shared.scheduler.Scheduler;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.nio.file.Path;

public class MineAdsMonitorBungee extends Plugin {

  private final AbstractMineAdsMonitorBootstrap bootstrap;
  private BungeeAudiences adventure;

  public MineAdsMonitorBungee() {
    this.bootstrap = new Bootstrap(this);
  }

  @Override
  public void onEnable() {
    this.adventure = BungeeAudiences.create(this);
    new Metrics(this, 27109);

    this.bootstrap.onEnable();

    getProxy().getPluginManager().registerListener(this, new PlayerListener(bootstrap.getEventCollector()));
  }

  @Override
  public void onDisable() {
    this.bootstrap.onDisable();

    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }

  private static class Bootstrap extends AbstractMineAdsMonitorBootstrap {

    private final MineAdsMonitorBungee plugin;

    public Bootstrap(MineAdsMonitorBungee plugin) {
      this.plugin = plugin;
    }

    @Override
    public Scheduler getScheduler() {
      return new BungeeScheduler(plugin);
    }

    @Override
    public Path getDataFolder() {
      return plugin.getDataFolder().toPath();
    }

    @Override
    public String getPluginVersion() {
      return plugin.getDescription().getVersion();
    }
  }
}
