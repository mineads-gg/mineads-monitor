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
package gg.mineads.monitor.shared.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public final class CraftingStorePurchaseData implements PurchaseData {
  private final String player; // Username (alphanumeric)
  private final String uuid; // UUID (steamid64 for Steam games)
  private final String packageName; // Package name
  private final String cost; // Price paid

  // Optional fields
  @Nullable
  private final String uuidDashed; // Dashed UUID
  @Nullable
  private final String packages; // Comma separated list of packages
  @Nullable
  private final String ingamePackageName; // Formatted package name
  @Nullable
  private final String steamId; // Steam ID
  @Nullable
  private final String amount; // Quantity selected
  @Nullable
  private final String transactionId; // Transaction ID
  @Nullable
  private final String discordId; // Discord user ID
  @Nullable
  private final String discordName; // Discord user name
}
