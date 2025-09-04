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
package gg.mineads.monitor.shared.event.model.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Unified data class for transaction events with essential fields only.
 */
@Data
@AllArgsConstructor
public final class TransactionData implements EventData {
  @SerializedName("transaction_id")
  private final String transactionId;

  @SerializedName("username")
  private final String username;

  @SerializedName("uuid")
  private final String uuid;

  @SerializedName("package_name")
  private final String packageName;

  @SerializedName("price")
  private final String price;

  @SerializedName("currency")
  private final String currency;
}
