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

import gg.mineads.monitor.shared.batch.BatchProcessor;
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import lombok.Getter;

public class MineAdsMonitorPlugin {

  private final AbstractMineAdsMonitorBootstrap bootstrap;
  @Getter
  private BatchProcessor batchProcessor;

  public MineAdsMonitorPlugin(AbstractMineAdsMonitorBootstrap bootstrap) {
    this.bootstrap = bootstrap;
  }

  public void onEnable() {
    // Initialize platform-specific components
    bootstrap.initializePlatform();

    // Load configuration
    bootstrap.loadConfig();

    // Initialize core services
    bootstrap.initializeCoreServices();

    // Get the batch processor after initialization
    this.batchProcessor = bootstrap.getBatchProcessor();

    // Create and register commands
    MineAdsCommandManager<?> commandManager = bootstrap.createCommandManager();
    commandManager.registerCommands();

    // Register platform-specific listeners
    if (this.batchProcessor != null) {
      bootstrap.registerListeners(this.batchProcessor);
    }
  }

  public void onDisable() {
    // Shutdown core services
    bootstrap.shutdownCoreServices();

    // Shutdown platform-specific components
    bootstrap.shutdownPlatform();
  }
}
