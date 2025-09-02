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
import gg.mineads.monitor.shared.event.model.data.*;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class MineAdsEvent {
  @SerializedName("event_type")
  private final EventType eventType;
  @SerializedName("time")
  private final long time = System.currentTimeMillis();

  // Event data (nullable - only one should be non-null based on eventType)
  @SerializedName("join_data")
  @Nullable
  private final PlayerJoinData joinData;
  @SerializedName("leave_data")
  @Nullable
  private final PlayerLeaveData leaveData;
  @SerializedName("chat_data")
  @Nullable
  private final PlayerChatData chatData;
  @SerializedName("command_data")
  @Nullable
  private final PlayerCommandData commandData;
  @SerializedName("purchase_data")
  @Nullable
  private final PurchaseData purchaseData;

  /**
   * Private constructor for internal use.
   */
  private MineAdsEvent(EventType eventType,
                        @Nullable PlayerJoinData joinData,
                        @Nullable PlayerLeaveData leaveData,
                        @Nullable PlayerChatData chatData,
                        @Nullable PlayerCommandData commandData,
                        @Nullable PurchaseData purchaseData) {
    this.eventType = eventType;
    this.joinData = joinData;
    this.leaveData = leaveData;
    this.chatData = chatData;
    this.commandData = commandData;
    this.purchaseData = purchaseData;
  }

  /**
   * Create a MineAdsEvent from player join data.
   *
   * @param data the player join data
   * @return a new MineAdsEvent for player join
   */
  public static MineAdsEvent from(PlayerJoinData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.JOIN, data, null, null, null, null);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from player leave data.
   *
   * @param data the player leave data
   * @return a new MineAdsEvent for player leave
   */
  public static MineAdsEvent from(PlayerLeaveData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.LEAVE, null, data, null, null, null);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from player chat data.
   *
   * @param data the player chat data
   * @return a new MineAdsEvent for player chat
   */
  public static MineAdsEvent from(PlayerChatData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.CHAT, null, null, data, null, null);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from player command data.
   *
   * @param data the player command data
   * @return a new MineAdsEvent for player command
   */
  public static MineAdsEvent from(PlayerCommandData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.COMMAND, null, null, null, data, null);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from purchase data.
   *
   * @param data the purchase data
   * @return a new MineAdsEvent for purchase
   */
  public static MineAdsEvent from(PurchaseData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.PURCHASE, null, null, null, null, data);
    event.validate();
    return event;
  }

  /**
   * Validates that only the correct data field is set based on the event type.
   *
   * @throws IllegalStateException if validation fails
   */
  private void validate() {
    int nonNullCount = 0;
    if (joinData != null) nonNullCount++;
    if (leaveData != null) nonNullCount++;
    if (chatData != null) nonNullCount++;
    if (commandData != null) nonNullCount++;
    if (purchaseData != null) nonNullCount++;

    if (nonNullCount != 1) {
      throw new IllegalStateException("Exactly one data field must be non-null, found: " + nonNullCount);
    }

    // Validate type matches data
    switch (eventType) {
      case JOIN -> {
        if (joinData == null) throw new IllegalStateException("JOIN type requires joinData to be non-null");
      }
      case LEAVE -> {
        if (leaveData == null) throw new IllegalStateException("LEAVE type requires leaveData to be non-null");
      }
      case CHAT -> {
        if (chatData == null) throw new IllegalStateException("CHAT type requires chatData to be non-null");
      }
      case COMMAND -> {
        if (commandData == null) throw new IllegalStateException("COMMAND type requires commandData to be non-null");
      }
      case PURCHASE -> {
        if (purchaseData == null) throw new IllegalStateException("PURCHASE type requires purchaseData to be non-null");
      }
    }
  }

}
