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
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;

public interface PlatformBootstrap {
  MineAdsCommandManager<?> createCommandManager(MineAdsMonitorPlugin plugin);

  void registerListeners(BatchProcessor batchProcessor, Config config);

  void initializePlatform();

  void shutdownPlatform();

  boolean isPluginEnabled(String pluginName);

  String getLuckPermsPlatformName();

  default boolean isLuckPermsEnabled() {
    return isPluginEnabled(getLuckPermsPlatformName());
  }

  void initializeLuckPerms();
}
