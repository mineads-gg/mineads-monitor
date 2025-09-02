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
import gg.mineads.monitor.shared.event.model.purchase.CraftingStorePurchaseData;
import gg.mineads.monitor.shared.event.model.purchase.PurchaseType;
import gg.mineads.monitor.shared.event.model.purchase.TebexPurchaseData;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

/**
 * Data class for purchase event data containing the purchase type and data.
 */
@Data
public final class PurchaseData implements EventData {
  @SerializedName("type")
  private final PurchaseType type;

  // Purchase data fields (nullable - only one should be non-null based on type)
  @SerializedName("tebex_data")
  @Nullable
  private final TebexPurchaseData tebexData;

  @SerializedName("crafting_store_data")
  @Nullable
  private final CraftingStorePurchaseData craftingStoreData;

  /**
   * Private constructor - use factory methods instead.
   */
  private PurchaseData(PurchaseType type, @Nullable TebexPurchaseData tebexData, @Nullable CraftingStorePurchaseData craftingStoreData) {
    this.type = type;
    this.tebexData = tebexData;
    this.craftingStoreData = craftingStoreData;
  }

  /**
   * Create a PurchaseData for Tebex purchases.
   *
   * @param data the Tebex purchase data
   * @return a new PurchaseData instance
   */
  public static PurchaseData tebex(TebexPurchaseData data) {
    return new PurchaseData(PurchaseType.TEBEX, data, null);
  }

  /**
   * Create a PurchaseData for CraftingStore purchases.
   *
   * @param data the CraftingStore purchase data
   * @return a new PurchaseData instance
   */
  public static PurchaseData craftingStore(CraftingStorePurchaseData data) {
    return new PurchaseData(PurchaseType.CRAFTING_STORE, null, data);
  }
}
