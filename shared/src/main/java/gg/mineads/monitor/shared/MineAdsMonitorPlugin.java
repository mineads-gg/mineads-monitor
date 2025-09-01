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
import gg.mineads.monitor.shared.event.EventCollector;
import gg.mineads.monitor.shared.scheduler.Scheduler;
import lombok.Getter;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MineAdsMonitorPlugin {

  private final AbstractMineAdsMonitorBootstrap bootstrap;
  @Getter
  private EventCollector eventCollector;
  private BatchProcessor batchProcessor;
  private Config config;

  public MineAdsMonitorPlugin(AbstractMineAdsMonitorBootstrap bootstrap) {
    this.bootstrap = bootstrap;
  }

  public void onEnable() {
    loadConfig();

    if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
      // Log message to configure API key
      return;
    }

    eventCollector = new EventCollector();
    batchProcessor = new BatchProcessor(eventCollector, config.getApiKey());

    bootstrap.getScheduler().scheduleAsync(batchProcessor, 10, 10, TimeUnit.SECONDS); // 10 seconds
  }

  public void onDisable() {
    if (batchProcessor != null) {
      batchProcessor.run();
    }
  }

  private void loadConfig() {
    Path configPath = bootstrap.getDataFolder().resolve("config.yml");
    config = YamlConfigurations.update(configPath, Config.class);
  }
}
