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
import gg.mineads.monitor.bukkit.command.BukkitCommandManager;
import gg.mineads.monitor.bukkit.listener.PlayerListener;
import gg.mineads.monitor.bukkit.scheduler.BukkitMineAdsScheduler;
import gg.mineads.monitor.shared.AbstractMineAdsMonitorBootstrap;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

@Getter
public class MineAdsMonitorBukkit extends JavaPlugin {
  private final Bootstrap bootstrap;
  private MineAdsMonitorPlugin plugin;

  public MineAdsMonitorBukkit() {
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

  @Getter
  public static class Bootstrap extends AbstractMineAdsMonitorBootstrap {
    private final MineAdsMonitorBukkit plugin;
    private FoliaLib foliaLib;
    private BukkitAudiences adventure;

    public Bootstrap(MineAdsMonitorBukkit plugin) {
      this.plugin = plugin;
    }

    @Override
    public MineAdsScheduler getScheduler() {
      return new BukkitMineAdsScheduler(foliaLib);
    }

    @Override
    public Path getDataFolder() {
      return plugin.getDataFolder().toPath();
    }

    @Override
    public MineAdsMonitorBukkit getOwningPlugin() {
      return plugin;
    }

    @Override
    public MineAdsCommandManager<?> createCommandManager(MineAdsMonitorPlugin mineAdsPlugin) {
      return new BukkitCommandManager(plugin.getBootstrap(), mineAdsPlugin);
    }

    @Override
    public void registerListeners(BatchProcessor batchProcessor, Config config) {
      plugin.getServer().getPluginManager().registerEvents(new PlayerListener(batchProcessor, config), plugin);
    }

    @Override
    public void initializePlatform() {
      this.foliaLib = new FoliaLib(plugin);
      this.adventure = BukkitAudiences.create(plugin);
      new Metrics(plugin, 27108);
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
      return plugin.getServer().getPluginManager().isPluginEnabled(pluginName);
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
            RegisteredServiceProvider<LuckPerms> provider =
              plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            return provider != null ? provider.getProvider() : null;
          } catch (Exception | NoClassDefFoundError e) {
            return null;
          }
        });
      }
    }
  }
}
