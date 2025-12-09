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
package gg.mineads.monitor.shared;

import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.shared.session.PlayerOnlineChecker;
import net.lenni0451.reflect.Modules;

import java.nio.file.Path;

public abstract class AbstractMineAdsMonitorBootstrap {
  static {
    // Enable modules for chronicle queue
    Modules.openEntireModule(sun.misc.Unsafe.class); // jdk.unsupported
    Modules.openEntireModule(com.sun.tools.javac.Main.class); // jdk.compiler
    Modules.openEntireModule(java.util.List.class); // java.base
  }

  private MineAdsMonitorPlugin plugin;

  public MineAdsMonitorPlugin getMineAdsPlugin() {
    return plugin;
  }

  public void onEnable() {
    this.plugin = new MineAdsMonitorPlugin(this);
    this.plugin.onEnable();
  }

  public void onDisable() {
    if (this.plugin != null) {
      this.plugin.onDisable();
      this.plugin = null;
    }
  }

  public abstract MineAdsScheduler getScheduler();

  public abstract Path getDataFolder();

  public abstract Object getOwningPlugin();

  public abstract MineAdsCommandManager<?> createCommandManager(MineAdsMonitorPlugin plugin);

  public abstract void registerListeners(MineAdsMonitorPlugin plugin);

  public abstract void initializePlatform();

  public abstract void shutdownPlatform();

  public abstract boolean isPluginEnabled(String pluginName);

  public abstract String getLuckPermsPlatformName();

  public boolean isLuckPermsEnabled() {
    return isPluginEnabled(getLuckPermsPlatformName());
  }

  public abstract void initializeLuckPerms();

  public abstract PlayerOnlineChecker getPlayerOnlineChecker();
}
