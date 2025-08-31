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
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.mineads.monitor.shared.MineAdsMonitorBootstrap;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

public class MineAdsMonitorVelocity implements MineAdsMonitorBootstrap {
  private final ProxyServer proxyServer;
  private final Logger log;
  private final Path pluginDir;
  private final PluginContainer container;
  private final Metrics.Factory metricsFactory;

  @Inject
  public MineAdsMonitorVelocity(ProxyServer proxyServer, Logger log, @DataDirectory Path pluginDir, PluginContainer container, Metrics.Factory metricsFactory) {
    this.proxyServer = proxyServer;
    this.log = log;
    this.pluginDir = pluginDir;
    this.container = container;
    this.metricsFactory = metricsFactory;
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    metricsFactory.make(this, 27110);
  }
}
