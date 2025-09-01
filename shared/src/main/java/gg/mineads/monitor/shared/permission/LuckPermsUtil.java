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

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class LuckPermsUtil {
  private static LuckPerms luckPerms = null;
  private static boolean initialized = false;

  /**
   * Attempts to initialize the LuckPerms API.
   * This should be called during plugin startup.
   *
   * @param luckPermsProvider A supplier that attempts to get the LuckPerms instance
   */
  public static void initialize(Supplier<@Nullable LuckPerms> luckPermsProvider) {
    if (initialized) {
      return;
    }

    try {
      luckPerms = luckPermsProvider.get();
    } catch (Exception | NoClassDefFoundError e) {
      // LuckPerms is not available
      luckPerms = null;
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
   * Gets the primary group of a user.
   *
   * @param uuid The UUID of the user
   * @return The primary group of the user, or "Unknown" if not available
   */
  public static String getPrimaryGroup(UUID uuid) {
    if (!isAvailable()) {
      return "Unknown";
    }

    try {
      User user = luckPerms.getUserManager().getUser(uuid);
      if (user == null) {
        return "Unknown";
      }

      return user.getPrimaryGroup();
    } catch (Exception e) {
      return "Unknown";
    }
  }
}
