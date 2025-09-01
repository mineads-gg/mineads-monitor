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
import org.jetbrains.annotations.Nullable;

@Data
public class TebexPurchaseData {
  private final String id; // Game-specific primary ID (Steam64, Xbox XUID, UUID, etc.)
  private final String username; // Username used during login or purchase
  private final String transaction; // Unique transaction ID
  private final String price; // Purchase amount
  private final String currency; // Currency used
  private final String packageName; // Package name

  // Optional fields
  @Nullable
  private final String server; // Server name
  @Nullable
  private final String date; // Date of purchase
  @Nullable
  private final String email; // Customer's email
  @Nullable
  private final String ip; // Customer's IP
  @Nullable
  private final String packageId; // Package identifier
  @Nullable
  private final String packagePrice; // Price of package
  @Nullable
  private final String packageExpiry; // Expiration period
  @Nullable
  private final String purchaserName; // For gift givers
  @Nullable
  private final String purchaserUuid; // For gift givers
  @Nullable
  private final String purchaseQuantity; // Quantity of items purchased
}
