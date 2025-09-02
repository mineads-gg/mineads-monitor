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
import gg.mineads.monitor.shared.event.model.purchase.PurchaseProviderData;
import gg.mineads.monitor.shared.event.model.purchase.PurchaseType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data class for purchase event data containing the purchase type and data.
 */
@Data
@AllArgsConstructor
public final class PurchaseData implements EventData {
  @SerializedName("type")
  private final PurchaseType type;
  @SerializedName("data")
  private final PurchaseProviderData data; // TebexPurchaseData or CraftingStorePurchaseData
}
