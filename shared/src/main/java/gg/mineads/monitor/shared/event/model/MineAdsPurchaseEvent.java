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

  private final MineAdsEvent event;

  // Convenience constructor for Tebex
  public MineAdsPurchaseEvent(PurchaseType type, PurchaseData data) {
    this.event = MineAdsEvent.purchase(type, data);
  }


  // Inner wrapper class for purchase data
  @Data
  public static final class PurchaseWrapper {
    private final PurchaseType type;
    private final PurchaseData data; // TebexPurchaseData or CraftingStorePurchaseData

    /**
     * Constructor with PurchaseType.
     *
     * @param type the purchase type
     * @param data the purchase data
     */
    public PurchaseWrapper(PurchaseType type, PurchaseData data) {
      this.type = type;
      this.data = data;
    }


  }

}
