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
