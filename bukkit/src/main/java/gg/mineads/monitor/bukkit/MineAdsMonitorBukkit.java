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
package gg.mineads.monitor.bukkit;

import com.tcoded.folialib.FoliaLib;
import gg.mineads.monitor.bukkit.listener.PlayerListener;
import gg.mineads.monitor.bukkit.scheduler.BukkitScheduler;
import gg.mineads.monitor.shared.AbstractMineAdsMonitorBootstrap;
import gg.mineads.monitor.shared.scheduler.Scheduler;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class MineAdsMonitorBukkit extends JavaPlugin {

  @Getter
  private final Bootstrap bootstrap;
  @Getter
  private FoliaLib foliaLib;
  private BukkitAudiences adventure;

  public MineAdsMonitorBukkit() {
    this.bootstrap = new Bootstrap(this);
  }

  @Override
  public void onEnable() {
    this.foliaLib = new FoliaLib(this);
    this.adventure = BukkitAudiences.create(this);
    new Metrics(this, 27108);

    this.bootstrap.onEnable();

    getServer().getPluginManager().registerEvents(new PlayerListener(bootstrap.getEventCollector()), this);
  }

  @Override
  public void onDisable() {
    this.bootstrap.onDisable();

    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }

  public static class Bootstrap extends AbstractMineAdsMonitorBootstrap {

    private final MineAdsMonitorBukkit plugin;

    public Bootstrap(MineAdsMonitorBukkit plugin) {
      this.plugin = plugin;
    }

    @Override
    public Scheduler getScheduler() {
      return new BukkitScheduler(plugin.getFoliaLib());
    }

    @Override
    public Path getDataFolder() {
      return plugin.getDataFolder().toPath();
    }

    @Override
    public MineAdsMonitorBukkit getOwningPlugin() {
      return plugin;
    }
  }
}
