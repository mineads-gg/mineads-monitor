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
import gg.mineads.monitor.shared.config.ConfigErrorType;
import gg.mineads.monitor.shared.event.BatchProcessor;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
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
      if (config != null && config.isDebug()) {
        log.info("[DEBUG] Starting plugin enable process");
      }

      // Initialize platform-specific components
      bootstrap.initializePlatform();

      // Load configuration
      loadConfig();

      // Validate configuration
      ConfigErrorType error = validateConfiguration();
      if (error != null) {
        logConfigurationError(error);
        return;
      }

      if (config != null && config.isDebug()) {
        log.info("[DEBUG] Configuration loaded successfully");
      }

      // Initialize core services
      initializeCoreServices();

      // Create and register commands
      MineAdsCommandManager<?> commandManager = bootstrap.createCommandManager(this);
      commandManager.registerCommands();

      // Register platform-specific listeners
      if (this.batchProcessor != null) {
        bootstrap.registerListeners(this.batchProcessor, this.config);
        if (config != null && config.isDebug()) {
          log.info("[DEBUG] Event listeners registered");
        }
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
      if (config != null && config.isDebug()) {
        log.info("[DEBUG] Starting plugin disable process");
      }

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
    if (config != null && config.isDebug()) {
      log.info("[DEBUG] Configuration file loaded from: " + configPath.toAbsolutePath());
    }

    // Export config to string, read exiting file string, if not matching, overwrite file
    try {
      String currentConfigContent = Files.readString(configPath);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      YamlConfigurations.write(outputStream, Config.class, config);
      String newConfigContent = outputStream.toString();
      if (!currentConfigContent.equals(newConfigContent)) {
        Files.writeString(configPath, newConfigContent);
        if (config.isDebug()) {
          log.info("[DEBUG] Configuration file updated with new defaults");
        }
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "[MineAdsMonitor] Failed to update configuration file", e);
    }
  }

  /**
   * Validate that required configuration is present
   */
  private ConfigErrorType validateConfiguration() {
    if (config == null || config.getPluginKey() == null || config.getPluginKey().isEmpty()) {
      return ConfigErrorType.PLUGIN_KEY_MISSING;
    }
    if (!config.getPluginKey().startsWith("pluginkey_")) {
      return ConfigErrorType.PLUGIN_KEY_INVALID_FORMAT;
    }
    return null; // No error
  }

  /**
   * Initialize core services
   */
  private void initializeCoreServices() {
    batchProcessor = new BatchProcessor(config.getPluginKey(), config);

    bootstrap.getScheduler().scheduleAsync(batchProcessor, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    if (config.isDebug()) {
      log.info("[DEBUG] Batch processor scheduled to run every 10 seconds");
    }

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
      ConfigErrorType error = validateConfig(newConfig);
      if (error != null) {
        logConfigurationError(error, true);
        return false;
      }

      // Check if plugin key changed - if so, we need to restart the batch processor
      boolean pluginKeyChanged = !config.getPluginKey().equals(newConfig.getPluginKey());

      // Update configuration
      this.config = newConfig;

      if (pluginKeyChanged && batchProcessor != null) {
        // Restart batch processor with new plugin key
        batchProcessor.shutdown();
        batchProcessor = new BatchProcessor(config.getPluginKey(), config);
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
  private ConfigErrorType validateConfig(Config config) {
    if (config == null || config.getPluginKey() == null || config.getPluginKey().isEmpty()) {
      return ConfigErrorType.PLUGIN_KEY_MISSING;
    }
    if (!config.getPluginKey().startsWith("pluginkey_")) {
      return ConfigErrorType.PLUGIN_KEY_INVALID_FORMAT;
    }
    return null; // No error
  }

  /**
   * Log configuration error messages
   */
  private void logConfigurationError(ConfigErrorType error) {
    logConfigurationError(error, false);
  }

  /**
   * Log configuration error messages
   */
  private void logConfigurationError(ConfigErrorType error, boolean isReload) {
    String prefix = isReload ? "[MineAdsMonitor] Reload failed: " : "[MineAdsMonitor] ";

    switch (error) {
      case PLUGIN_KEY_MISSING:
        log.warning(prefix + "Plugin key not configured. Please set 'pluginKey' in config.yml");
        break;
      case PLUGIN_KEY_INVALID_FORMAT:
        log.warning(prefix + "Invalid plugin key. Plugin keys must start with 'pluginkey_'. Please check that you used the correct type of key from the MineAds dashboard.");
        break;
    }
  }
}
