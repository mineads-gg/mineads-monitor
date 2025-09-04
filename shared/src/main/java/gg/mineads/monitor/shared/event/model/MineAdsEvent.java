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
  @SerializedName("transaction_data")
  @Nullable
  private final TransactionData transactionData;

  /**
   * Private constructor for internal use.
   */
  private MineAdsEvent(EventType eventType,
                       @Nullable PlayerJoinData joinData,
                       @Nullable PlayerLeaveData leaveData,
                       @Nullable PlayerChatData chatData,
                       @Nullable PlayerCommandData commandData,
                       @Nullable TransactionData transactionData) {
    this.eventType = eventType;
    this.joinData = joinData;
    this.leaveData = leaveData;
    this.chatData = chatData;
    this.commandData = commandData;
    this.transactionData = transactionData;
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
   * Create a MineAdsEvent from transaction data for initial purchase.
   *
   * @param data the transaction data
   * @return a new MineAdsEvent for initial purchase
   */
  public static MineAdsEvent initial(TransactionData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.INITIAL, null, null, null, null, data);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from transaction data for expiry.
   *
   * @param data the transaction data
   * @return a new MineAdsEvent for expiry
   */
  public static MineAdsEvent expiry(TransactionData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.EXPIRY, null, null, null, null, data);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from transaction data for renewal.
   *
   * @param data the transaction data
   * @return a new MineAdsEvent for renewal
   */
  public static MineAdsEvent renewal(TransactionData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.RENEWAL, null, null, null, null, data);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from transaction data for chargeback.
   *
   * @param data the transaction data
   * @return a new MineAdsEvent for chargeback
   */
  public static MineAdsEvent chargeback(TransactionData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.CHARGEBACK, null, null, null, null, data);
    event.validate();
    return event;
  }

  /**
   * Create a MineAdsEvent from transaction data for refund.
   *
   * @param data the transaction data
   * @return a new MineAdsEvent for refund
   */
  public static MineAdsEvent refund(TransactionData data) {
    MineAdsEvent event = new MineAdsEvent(EventType.REFUND, null, null, null, null, data);
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
    if (transactionData != null) nonNullCount++;

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
      case INITIAL, EXPIRY, RENEWAL, CHARGEBACK, REFUND -> {
        if (transactionData == null) throw new IllegalStateException(eventType + " type requires transactionData to be non-null");
      }
    }
  }

}
