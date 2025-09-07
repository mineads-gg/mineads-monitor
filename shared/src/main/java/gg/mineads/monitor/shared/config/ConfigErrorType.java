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
package gg.mineads.monitor.shared.config;

/**
 * Enum representing different types of configuration errors
 */
public enum ConfigErrorType {
  /**
   * Plugin key is missing or empty
   */
  PLUGIN_KEY_MISSING,

  /**
   * Plugin key does not have the correct format (must start with "pluginkey_")
   */
  PLUGIN_KEY_INVALID_FORMAT,

  /**
   * Server id contains invalid characters (only lowercase letters and dashes allowed)
   */
  SERVER_ID_INVALID_FORMAT
}
