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
import gg.mineads.monitor.shared.update.UpdateChecker;
import gg.mineads.monitor.shared.session.SessionHeartbeatTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
public class MineAdsMonitorPlugin {

  private final AbstractMineAdsMonitorBootstrap bootstrap;
  @Getter
  private final BatchProcessor batchProcessor;
  @Getter
  private Config config;
  @Getter
  private boolean initialized = false;
  @Setter
  @Getter
  private boolean outdated = false;

  public MineAdsMonitorPlugin(AbstractMineAdsMonitorBootstrap bootstrap) {
    this.bootstrap = bootstrap;
    this.batchProcessor = new BatchProcessor(this, bootstrap.getScheduler(), bootstrap.getDataFolder());
  }

  public void onEnable() {
    try {
      // Load configuration
      loadConfig();

      if (config != null && config.isDebug()) {
        log.info("[DEBUG] Starting plugin enable process");
      }

      // Initialize platform-specific components
      bootstrap.initializePlatform();

      // Create and register commands (always register commands, even with config issues)
      MineAdsCommandManager<?> commandManager = bootstrap.createCommandManager(this);
      commandManager.registerCommands();

      // Initialize core services (only if config is valid)
      initializeCoreServices();

      // Validate configuration
      ConfigErrorType error = validateConfiguration();
      if (error != null) {
        logConfigurationError(error);
        log.info("[MineAdsMonitor] Plugin partially enabled - commands available but core services disabled due to config issues");
      } else if (config != null && config.isDebug()) {
        log.info("[DEBUG] Configuration loaded successfully");
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
        if (config != null && config.isDebug()) {
          log.info("[DEBUG] Configuration file updated with new defaults");
        }
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "[MineAdsMonitor] Failed to update configuration file", e);
    }
  }

  public boolean hasConfigIssues() {
    return validateConfiguration() != null;
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
    if (!isValidServerId(config.getServerId())) {
      return ConfigErrorType.SERVER_ID_INVALID_FORMAT;
    }
    return null; // No error
  }

  private boolean isValidServerId(String serverId) {
    if (serverId == null || serverId.isEmpty()) {
      return false;
    }
    // Only allow lowercase letters, numbers, and dashes
    return serverId.matches("^[a-z0-9-]+$");
  }

  /**
   * Initialize core services
   */
  private void initializeCoreServices() {
    bootstrap.getScheduler().scheduleAsync(batchProcessor, 10, 10, TimeUnit.SECONDS);

    if (config != null && config.isDebug()) {
      log.info("[DEBUG] Batch processor scheduled to run every 10 seconds");
    }

    // Emit heartbeats for active sessions to bound sessions on crashes
    bootstrap.getScheduler().scheduleAsync(
      new SessionHeartbeatTask(this, bootstrap.getPlayerOnlineChecker()),
      10,
      60,
      TimeUnit.SECONDS
    );

    // Initialize platform-specific services
    bootstrap.initializeLuckPerms();

    // Check for updates asynchronously
    UpdateChecker.checkForUpdates(this);

    // Register platform-specific listeners
    bootstrap.registerListeners(this);
    if (config != null && config.isDebug()) {
      log.info("[DEBUG] Event listeners registered");
    }
  }

  /**
   * Shutdown core services
   */
  private void shutdownCoreServices() {
    batchProcessor.shutdown(); // Flush and close durable queue
  }

  /**
   * Reload configuration from file
   *
   * @return true if reload was successful, false otherwise
   */
  public boolean reloadConfig() {
    try {
      loadConfig();

      // Validate the new configuration
      ConfigErrorType error = validateConfig(config);
      if (error != null) {
        logConfigurationError(error, true);
        return false;
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
    if (!isValidServerId(config.getServerId())) {
      return ConfigErrorType.SERVER_ID_INVALID_FORMAT;
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
      case PLUGIN_KEY_MISSING -> log.warning(prefix + "Plugin key not configured. Please set 'pluginKey' in config.yml");
      case PLUGIN_KEY_INVALID_FORMAT -> log.warning(prefix + "Invalid plugin key. Plugin keys must start with 'pluginkey_'. Please check that you used the correct type of key from the MineAds dashboard.");
      case SERVER_ID_INVALID_FORMAT -> log.warning(prefix + "Invalid server id format. Server ids can only contain lowercase letters, numbers, and dashes (e.g., 'survival-main', 'creative-01').");
    }
  }
}
