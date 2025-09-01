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

import de.exlll.configlib.YamlConfigurations;
import gg.mineads.monitor.shared.batch.BatchProcessor;
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.shared.update.UpdateChecker;
import lombok.Getter;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMineAdsMonitorBootstrap implements PlatformBootstrap {

  @Getter
  private BatchProcessor batchProcessor;
  private Config config;

  protected void loadConfig() {
    Path configPath = getDataFolder().resolve("config.yml");
    config = YamlConfigurations.update(configPath, Config.class);
  }

  protected void initializeCoreServices() {
    if (config.getPluginKey() == null || config.getPluginKey().isEmpty()) {
      // Log message to configure plugin key
      return;
    }

    batchProcessor = new BatchProcessor(config.getPluginKey());

    getScheduler().scheduleAsync(batchProcessor, 10, 10, TimeUnit.SECONDS); // 10 seconds

    // Initialize platform-specific services
    initializeLuckPerms();

    // Check for updates asynchronously
    UpdateChecker.checkForUpdates();
  }

  protected void shutdownCoreServices() {
    if (batchProcessor != null) {
      batchProcessor.run(); // Process any remaining events
      batchProcessor.shutdown();
    }
  }


  public abstract MineAdsScheduler getScheduler();

  public abstract Path getDataFolder();

  public abstract Object getOwningPlugin();
}
