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
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import lombok.Getter;
import lombok.extern.java.Log;

import java.nio.file.Path;
import java.util.logging.Level;

@Log
public class MineAdsMonitorPlugin {

  private final AbstractMineAdsMonitorBootstrap bootstrap;
  @Getter
  private BatchProcessor batchProcessor;
  @Getter
  private Config config;
  private boolean initialized = false;

  public MineAdsMonitorPlugin(AbstractMineAdsMonitorBootstrap bootstrap) {
    this.bootstrap = bootstrap;
  }

  public void onEnable() {
    try {
      // Initialize platform-specific components
      bootstrap.initializePlatform();

      // Load configuration
      loadConfig();

      // Validate configuration
      if (!isConfigurationValid()) {
        log.warning("[MineAdsMonitor] Plugin key not configured. Please set 'pluginKey' in config.yml");
        return;
      }

      // Initialize core services
      initializeCoreServices();

      // Create and register commands
      MineAdsCommandManager<?> commandManager = bootstrap.createCommandManager(this);
      commandManager.registerCommands();

      // Register platform-specific listeners
      if (this.batchProcessor != null) {
        bootstrap.registerListeners(this.batchProcessor, this.config);
      }

      initialized = true;
      log.info("[MineAdsMonitor] Plugin enabled successfully");

    } catch (Exception e) {
      log.log(Level.SEVERE, "[MineAdsMonitor] Failed to enable plugin", e);
      onDisable(); // Cleanup on failure
    }
  }

  public void onDisable() {
    try {
      // Shutdown core services
      shutdownCoreServices();

      // Shutdown platform-specific components
      bootstrap.shutdownPlatform();

      initialized = false;
      log.info("[MineAdsMonitor] Plugin disabled successfully");

    } catch (Exception e) {
      log.log(Level.SEVERE, "[MineAdsMonitor] Error during plugin shutdown", e);
    }
  }

  /**
   * Load configuration from file
   */
  private void loadConfig() {
    Path configPath = bootstrap.getDataFolder().resolve("config.yml");
    config = YamlConfigurations.update(configPath, Config.class);
  }

  /**
   * Validate that required configuration is present
   */
  private boolean isConfigurationValid() {
    return config != null && config.getPluginKey() != null && !config.getPluginKey().isEmpty();
  }

  /**
   * Initialize core services
   */
  private void initializeCoreServices() {
    batchProcessor = new BatchProcessor(config.getPluginKey());

    bootstrap.getScheduler().scheduleAsync(batchProcessor, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    // Initialize platform-specific services
    bootstrap.initializeLuckPerms();

    // Check for updates asynchronously
    gg.mineads.monitor.shared.update.UpdateChecker.checkForUpdates();
  }

  /**
   * Shutdown core services
   */
  private void shutdownCoreServices() {
    if (batchProcessor != null) {
      batchProcessor.run(); // Process any remaining events
      batchProcessor.shutdown();
    }
  }

  /**
   * Check if the plugin is properly initialized
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Reload configuration from file
   *
   * @return true if reload was successful, false otherwise
   */
  public boolean reloadConfig() {
    try {
      // Load new configuration
      Path configPath = bootstrap.getDataFolder().resolve("config.yml");
      Config newConfig = YamlConfigurations.update(configPath, Config.class);

      // Validate the new configuration
      if (!isValidConfig(newConfig)) {
        log.warning("[MineAdsMonitor] Reload failed: Invalid configuration. Plugin key is required.");
        return false;
      }

      // Check if plugin key changed - if so, we need to restart the batch processor
      boolean pluginKeyChanged = !config.getPluginKey().equals(newConfig.getPluginKey());

      // Update configuration
      this.config = newConfig;

      if (pluginKeyChanged && batchProcessor != null) {
        // Restart batch processor with new plugin key
        batchProcessor.shutdown();
        batchProcessor = new BatchProcessor(config.getPluginKey());
        bootstrap.getScheduler().scheduleAsync(batchProcessor, 10, 10, java.util.concurrent.TimeUnit.SECONDS);
        log.info("[MineAdsMonitor] Batch processor restarted with new plugin key");
      }

      log.info("[MineAdsMonitor] Configuration reloaded successfully");
      return true;

    } catch (Exception e) {
      log.log(Level.SEVERE, "[MineAdsMonitor] Failed to reload configuration", e);
      return false;
    }
  }

  /**
   * Validate that required configuration is present
   */
  private boolean isValidConfig(Config config) {
    return config != null && config.getPluginKey() != null && !config.getPluginKey().isEmpty();
  }
}
