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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMineAdsMonitorBootstrap implements PlatformBootstrap {

  private static final String GITHUB_API_URL = "https://api.github.com/repos/mineads-gg/mineads-monitor/releases/latest";

  private EventCollector eventCollector;
  private BatchProcessor batchProcessor;
  private Config config;

  @Override
  public void onEnable() {
    loadConfig();

    if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
      // Log message to configure API key
      return;
    }

    eventCollector = new EventCollector();
    batchProcessor = new BatchProcessor(eventCollector, config.getApiKey());

    getScheduler().scheduleAsync(batchProcessor, 10, 10, TimeUnit.SECONDS); // 10 seconds

    checkForUpdates();
  }

  @Override
  public void onDisable() {
    if (batchProcessor != null) {
      batchProcessor.run();
    }
  }

  private void loadConfig() {
    Path configPath = getDataFolder().resolve("config.yml");
    config = YamlConfigurations.update(configPath, Config.class);
  }

  private void checkForUpdates() {
    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(GITHUB_API_URL))
      .build();

    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenAccept(body -> {
        // very basic update checker, just checking if the version is the same
        if (!body.contains(getPluginVersion())) {
          // Log message about new version
        }
      });
  }

  public abstract Scheduler getScheduler();

  public abstract Path getDataFolder();

  public abstract String getPluginVersion();

  public EventCollector getEventCollector() {
    return eventCollector;
  }
}
