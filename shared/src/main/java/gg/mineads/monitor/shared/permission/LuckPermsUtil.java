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
package gg.mineads.monitor.shared.permission;

import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.generated.LuckPermsData;
import lombok.extern.java.Log;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Log
public class LuckPermsUtil {
  private static Object luckPerms = null;
  private static boolean initialized = false;

  /**
   * Attempts to initialize the LuckPerms API.
   * This should be called during plugin startup.
   */
  public static void initialize() {
    if (initialized) {
      log.info("[MineAdsMonitor] LuckPerms already initialized, skipping");
      return;
    }

    try {
      luckPerms = LuckPermsProvider.get();
      log.info("[MineAdsMonitor] LuckPerms integration initialized successfully");
    } catch (Exception | NoClassDefFoundError e) {
      // LuckPerms is not available
      luckPerms = null;
      log.info("[MineAdsMonitor] LuckPerms not available, permission features will be limited");
    }

    initialized = true;
  }

  /**
   * Checks if LuckPerms is available and initialized.
   *
   * @return true if LuckPerms is available, false otherwise
   */
  public static boolean isAvailable() {
    return luckPerms != null;
  }

  /**
   * Gets all groups that a user has.
   *
   * @param uuid The UUID of the user
   * @return A LuckPermsData instance with all group names the user has, or null if not available
   */
  public static LuckPermsData getLuckPermsData(Config config, UUID uuid) {
    if (!isAvailable()) {
      if (config.isDebug()) {
        log.info("[DEBUG] LuckPerms not available when retrieving data for UUID: " + uuid);
      }
      return null;
    }

    try {
      LuckPerms luckPermsInstance = (LuckPerms) luckPerms;
      User user = luckPermsInstance.getUserManager().getUser(uuid);
      if (user == null) {
        user = luckPermsInstance.getUserManager().loadUser(uuid).get(30, TimeUnit.SECONDS);
      }

      if (user == null) {
        if (config.isDebug()) {
          log.info("[DEBUG] LuckPerms user not found for UUID: " + uuid);
        }
        return null;
      }

      return LuckPermsData.newBuilder()
        .addAllGroups(
          user.getInheritedGroups(user.getQueryOptions())
            .stream()
            .sorted(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
            .map(Group::getName)
            ::iterator
        )
        .build();
    } catch (Exception e) {
      log.warning("[DEBUG] Error retrieving LuckPerms data for UUID: " + uuid + ", error: " + e);
      return null;
    }
  }

  /**
   * Looks up a player's UUID by their username using LuckPerms.
   * This uses LuckPerms' user lookup which queries Mojang's API if needed.
   *
   * @param config The plugin config for debug logging
   * @param username The username to look up
   * @return An Optional containing the UUID if found, empty otherwise
   */
  public static Optional<UUID> lookupUuidByUsername(Config config, String username) {
    if (!isAvailable()) {
      if (config.isDebug()) {
        log.info("[DEBUG] LuckPerms not available when looking up UUID for username: " + username);
      }
      return Optional.empty();
    }

    try {
      LuckPerms luckPermsInstance = (LuckPerms) luckPerms;
      UserManager userManager = luckPermsInstance.getUserManager();

      // lookupUniqueId queries Mojang's API if the user isn't cached
      CompletableFuture<UUID> lookupFuture = userManager.lookupUniqueId(username);
      UUID uuid = lookupFuture.get(30, TimeUnit.SECONDS);

      if (uuid == null) {
        if (config.isDebug()) {
          log.info("[DEBUG] No UUID found for username: " + username);
        }
        return Optional.empty();
      }

      if (config.isDebug()) {
        log.info("[DEBUG] Found UUID " + uuid + " for username: " + username);
      }
      return Optional.of(uuid);
    } catch (Exception e) {
      log.warning("[MineAdsMonitor] Error looking up UUID for username: " + username + ", error: " + e);
      return Optional.empty();
    }
  }
}
