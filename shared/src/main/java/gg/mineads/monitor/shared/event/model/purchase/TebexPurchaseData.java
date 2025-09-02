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
package gg.mineads.monitor.shared.event.model.purchase;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public final class TebexPurchaseData implements PurchaseProviderData {
  @SerializedName("id")
  private final String id; // Game-specific primary ID (Steam64, Xbox XUID, UUID, etc.)
  @SerializedName("username")
  private final String username; // Username used during login or purchase
  @SerializedName("transaction")
  private final String transaction; // Unique transaction ID
  @SerializedName("price")
  private final String price; // Purchase amount
  @SerializedName("currency")
  private final String currency; // Currency used
  @SerializedName("package_name")
  private final String packageName; // Package name

  // Optional fields
  @SerializedName("server")
  @Nullable
  private final String server; // Server name
  @SerializedName("date")
  @Nullable
  private final String date; // Date of purchase
  @SerializedName("email")
  @Nullable
  private final String email; // Customer's email
  @SerializedName("ip")
  @Nullable
  private final String ip; // Customer's IP
  @SerializedName("package_id")
  @Nullable
  private final String packageId; // Package identifier
  @SerializedName("package_price")
  @Nullable
  private final String packagePrice; // Price of package
  @SerializedName("package_expiry")
  @Nullable
  private final String packageExpiry; // Expiration period
  @SerializedName("purchaser_name")
  @Nullable
  private final String purchaserName; // For gift givers
  @SerializedName("purchaser_uuid")
  @Nullable
  private final String purchaserUuid; // For gift givers
  @SerializedName("purchase_quantity")
  @Nullable
  private final String purchaseQuantity; // Quantity of items purchased
}
