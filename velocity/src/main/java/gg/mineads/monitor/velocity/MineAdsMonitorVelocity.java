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
package gg.mineads.monitor.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.mineads.monitor.shared.AbstractMineAdsMonitorBootstrap;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.event.EventCollector;
import gg.mineads.monitor.shared.scheduler.Scheduler;
import gg.mineads.monitor.velocity.command.VelocityCommandManager;
import gg.mineads.monitor.velocity.listener.PlayerListener;
import gg.mineads.monitor.velocity.scheduler.VelocityScheduler;
import lombok.Getter;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

@Getter
public class MineAdsMonitorVelocity {
  private final ProxyServer proxyServer;
  private final Logger log;
  private final Path pluginDir;
  private final PluginContainer container;
  private final Metrics.Factory metricsFactory;
  private final Bootstrap bootstrap;
  private MineAdsMonitorPlugin plugin;

  @Inject
  public MineAdsMonitorVelocity(ProxyServer proxyServer, Logger log, @DataDirectory Path pluginDir, PluginContainer container, Metrics.Factory metricsFactory) {
    this.proxyServer = proxyServer;
    this.log = log;
    this.pluginDir = pluginDir;
    this.container = container;
    this.metricsFactory = metricsFactory;
    this.bootstrap = new Bootstrap(this);
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    // Initialize LuckPerms utility if available
    gg.mineads.monitor.shared.permission.LuckPermsUtil.initialize(() -> {
      try {
        return net.luckperms.api.LuckPermsProvider.get();
      } catch (Exception | NoClassDefFoundError e) {
        return null;
      }
    });

    this.plugin = new MineAdsMonitorPlugin(bootstrap);
    this.plugin.onEnable();
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) {
    if (this.plugin != null) {
      this.plugin.onDisable();
      this.plugin = null;
    }
  }

  public static class Bootstrap extends AbstractMineAdsMonitorBootstrap {

    private final MineAdsMonitorVelocity plugin;

    public Bootstrap(MineAdsMonitorVelocity plugin) {
      this.plugin = plugin;
    }

    @Override
    public Scheduler getScheduler() {
      return new VelocityScheduler(plugin, plugin.proxyServer);
    }

    @Override
    public Path getDataFolder() {
      return plugin.pluginDir;
    }

    @Override
    public MineAdsMonitorVelocity getOwningPlugin() {
      return plugin;
    }

    @Override
    public MineAdsCommandManager<?> createCommandManager() {
      return new VelocityCommandManager(plugin);
    }

    @Override
    public void registerListeners(EventCollector eventCollector) {
      plugin.proxyServer.getEventManager().register(plugin, new PlayerListener(eventCollector));
    }

    @Override
    public void initializePlatform() {
      plugin.metricsFactory.make(plugin, 27110);
    }

    @Override
    public void shutdownPlatform() {
      // Velocity doesn't require specific shutdown operations for metrics
    }
  }
}
