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

import gg.mineads.monitor.bungee.command.BungeeCommandManager;
import gg.mineads.monitor.bungee.listener.PlayerListener;
import gg.mineads.monitor.bungee.scheduler.BungeeMineAdsScheduler;
import gg.mineads.monitor.shared.AbstractMineAdsMonitorBootstrap;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import lombok.Getter;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.nio.file.Path;

@Getter
public class MineAdsMonitorBungee extends Plugin {
  private final Bootstrap bootstrap;
  private MineAdsMonitorPlugin plugin;

  public MineAdsMonitorBungee() {
    this.bootstrap = new Bootstrap(this);
  }

  @Override
  public void onEnable() {
    this.plugin = new MineAdsMonitorPlugin(bootstrap);
    this.plugin.onEnable();
  }

  @Override
  public void onDisable() {
    if (this.plugin != null) {
      this.plugin.onDisable();
      this.plugin = null;
    }
  }

  public static class Bootstrap extends AbstractMineAdsMonitorBootstrap {

    private final MineAdsMonitorBungee plugin;
    private BungeeAudiences adventure;

    public Bootstrap(MineAdsMonitorBungee plugin) {
      this.plugin = plugin;
    }

    @Override
    public MineAdsScheduler getScheduler() {
      return new BungeeMineAdsScheduler(plugin);
    }

    @Override
    public Path getDataFolder() {
      return plugin.getDataFolder().toPath();
    }

    @Override
    public MineAdsMonitorBungee getOwningPlugin() {
      return plugin;
    }

    @Override
    public MineAdsCommandManager<?> createCommandManager(MineAdsMonitorPlugin mineAdsPlugin) {
      return new BungeeCommandManager(plugin.getBootstrap(), mineAdsPlugin);
    }

    @Override
    public void registerListeners(BatchProcessor batchProcessor, Config config) {
      plugin.getProxy().getPluginManager().registerListener(plugin, new PlayerListener(batchProcessor, config));
    }

    @Override
    public void initializePlatform() {
      this.adventure = BungeeAudiences.create(plugin);
      new Metrics(plugin, 27109);
    }

    @Override
    public void shutdownPlatform() {
      if (this.adventure != null) {
        this.adventure.close();
        this.adventure = null;
      }
    }

    @Override
    public boolean isPluginEnabled(String pluginName) {
      return plugin.getProxy().getPluginManager().getPlugin(pluginName) != null;
    }

    @Override
    public String getLuckPermsPlatformName() {
      return "LuckPerms";
    }

    @Override
    public void initializeLuckPerms() {
      if (isLuckPermsEnabled()) {
        LuckPermsUtil.initialize(() -> {
          try {
            return LuckPermsProvider.get();
          } catch (Exception | NoClassDefFoundError e) {
            return null;
          }
        });
      }
    }
  }
}
