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

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public final class CraftingStorePurchaseData implements PurchaseProviderData {
  @SerializedName("player")
  private final String player; // Username (alphanumeric)
  @SerializedName("uuid")
  private final String uuid; // UUID (steamid64 for Steam games)
  @SerializedName("package_name")
  private final String packageName; // Package name
  @SerializedName("cost")
  private final String cost; // Price paid

  // Optional fields
  @SerializedName("uuid_dashed")
  @Nullable
  private final String uuidDashed; // Dashed UUID
  @SerializedName("packages")
  private final String packages; // Comma separated list of packages
  @SerializedName("ingame_package_name")
  @Nullable
  private final String ingamePackageName; // Formatted package name
  @SerializedName("steam_id")
  @Nullable
  private final String steamId; // Steam ID
  @SerializedName("amount")
  private final String amount; // Quantity selected
  @SerializedName("transaction_id")
  @Nullable
  private final String transactionId; // Transaction ID
  @SerializedName("discord_id")
  @Nullable
  private final String discordId; // Discord user ID
  @SerializedName("discord_name")
  @Nullable
  private final String discordName; // Discord user name
}
