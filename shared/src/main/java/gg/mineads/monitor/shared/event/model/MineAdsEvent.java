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

import java.util.UUID;

@Data
public class MineAdsEvent {
  private final EventType eventType;
  private final long time = System.currentTimeMillis();

  // Player event data (nullable - only one should be non-null based on eventType)
  @Nullable
  private final PlayerJoinData joinData;
  @Nullable
  private final PlayerLeaveData leaveData;
  @Nullable
  private final PlayerChatData chatData;
  @Nullable
  private final PlayerCommandData commandData;

  // Purchase event data (nullable)
  @Nullable
  private final MineAdsPurchaseEvent.PurchaseWrapper purchaseData;

  /**
   * Private constructor for internal use.
   */
  private MineAdsEvent(EventType eventType,
                       @Nullable PlayerJoinData joinData,
                       @Nullable PlayerLeaveData leaveData,
                       @Nullable PlayerChatData chatData,
                       @Nullable PlayerCommandData commandData,
                       @Nullable MineAdsPurchaseEvent.PurchaseWrapper purchaseData) {
    this.eventType = eventType;
    this.joinData = joinData;
    this.leaveData = leaveData;
    this.chatData = chatData;
    this.commandData = commandData;
    this.purchaseData = purchaseData;
  }

  /**
   * Create a player join event.
   *
   * @param sessionId        the player session ID
   * @param locale           the player locale
   * @param ip               the player IP address
   * @param clientBrand      the client brand
   * @param minecraftVersion the Minecraft version
   * @param onlineMode       whether online mode is enabled
   * @param luckPermsRank    the LuckPerms rank
   * @return a new MineAdsEvent for player join
   */
  public static MineAdsEvent playerJoin(UUID sessionId, String locale, String ip,
                                        String clientBrand, String minecraftVersion,
                                        boolean onlineMode, String luckPermsRank) {
    PlayerJoinData data = new PlayerJoinData(sessionId, locale, ip, clientBrand,
      minecraftVersion, onlineMode, luckPermsRank);
    MineAdsEvent event = new MineAdsEvent(EventType.JOIN, data, null, null, null, null);
    event.validate();
    return event;
  }

  /**
   * Create a player leave event.
   *
   * @param sessionId the player session ID
   * @return a new MineAdsEvent for player leave
   */
  public static MineAdsEvent playerLeave(UUID sessionId) {
    PlayerLeaveData data = new PlayerLeaveData(sessionId);
    MineAdsEvent event = new MineAdsEvent(EventType.LEAVE, null, data, null, null, null);
    event.validate();
    return event;
  }

  /**
   * Create a player chat event.
   *
   * @param sessionId the player session ID
   * @param message   the chat message
   * @return a new MineAdsEvent for player chat
   */
  public static MineAdsEvent playerChat(UUID sessionId, String message) {
    PlayerChatData data = new PlayerChatData(sessionId, message);
    MineAdsEvent event = new MineAdsEvent(EventType.CHAT, null, null, data, null, null);
    event.validate();
    return event;
  }

  /**
   * Create a player command event.
   *
   * @param sessionId the player session ID
   * @param command   the command executed
   * @return a new MineAdsEvent for player command
   */
  public static MineAdsEvent playerCommand(UUID sessionId, String command) {
    PlayerCommandData data = new PlayerCommandData(sessionId, command);
    MineAdsEvent event = new MineAdsEvent(EventType.COMMAND, null, null, null, data, null);
    event.validate();
    return event;
  }

  /**
   * Create a purchase event.
   *
   * @param type the purchase type
   * @param data the purchase data
   * @return a new MineAdsEvent for purchase
   */
  public static MineAdsEvent purchase(PurchaseType type, PurchaseData data) {
    MineAdsPurchaseEvent.PurchaseWrapper wrapper = new MineAdsPurchaseEvent.PurchaseWrapper(type, data);
    MineAdsEvent event = new MineAdsEvent(EventType.PURCHASE, null, null, null, null, wrapper);
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
