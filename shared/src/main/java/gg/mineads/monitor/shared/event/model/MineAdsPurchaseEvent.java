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

import lombok.Data;

@Data
public class MineAdsPurchaseEvent {

  private final long time = System.currentTimeMillis();
  private final String id; // Game-specific primary ID (Steam64, Xbox XUID, UUID, etc.)
  private final String username; // Username used during login or purchase
  private final String player; // For CraftingStore, username (alphanumeric)
  private final String playerUnsafe; // Raw player name for CraftingStore
  private final String uuid; // UUID or SteamID
  private final String uuidDashed; // Dashed UUID
  private final String transaction; // Unique transaction ID
  private final String server; // Server name
  private final String price; // Purchase amount
  private final String cost; // Price paid (CraftingStore)
  private final String currency; // Currency used
  private final String date; // Date of purchase
  private final String email; // Customer's email
  private final String ip; // Customer's IP
  private final String packageId; // Package identifier
  private final String packageName; // Package name
  private final String ingamePackageName; // Formatted package name (CraftingStore)
  private final String packages; // Comma separated list of packages (CraftingStore)
  private final String packagePrice; // Price of package
  private final String packageExpiry; // Expiration period
  private final String purchaserName; // For gift givers
  private final String purchaserUuid; // For gift givers
  private final String purchaseQuantity; // Quantity of items purchased
  private final String amount; // Quantity selected (CraftingStore)
  private final String steamId; // Steam ID
  private final String transactionId; // Transaction ID (CraftingStore)
  private final String discordId; // Discord user ID
  private final String discordName; // Discord user name

}
