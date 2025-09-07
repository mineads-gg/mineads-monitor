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
package gg.mineads.monitor.shared.event;

import gg.mineads.monitor.shared.event.generated.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypeUtil {
  private TypeUtil() {}

  public static String getHostString(SocketAddress address) {
    if (address instanceof InetSocketAddress inetSocketAddress) {
      return inetSocketAddress.getHostString();
    } else {
      return null;
    }
  }

  public static String getIPString(SocketAddress address) {
    if (address instanceof InetSocketAddress inetSocketAddress) {
      var inetAddress = inetSocketAddress.getAddress();
      if (inetAddress == null) {
        return null;
      } else {
        return inetAddress.getHostAddress();
      }
    } else {
      return null;
    }
  }

  public static MineAdsEvent createJoinEvent(PlayerJoinData data) {
    return MineAdsEvent.newBuilder()
      .setEventType(EventType.JOIN)
      .setTime(System.currentTimeMillis())
      .setJoinData(data)
      .build();
  }

  public static MineAdsEvent createLeaveEvent(PlayerLeaveData data) {
    return MineAdsEvent.newBuilder()
      .setEventType(EventType.LEAVE)
      .setTime(System.currentTimeMillis())
      .setLeaveData(data)
      .build();
  }

  public static MineAdsEvent createChatEvent(PlayerChatData data) {
    return MineAdsEvent.newBuilder()
      .setEventType(EventType.CHAT)
      .setTime(System.currentTimeMillis())
      .setChatData(data)
      .build();
  }

  public static PlayerCommandData.Builder createCommandDataBuilder(String sessionId, String fullCommand, boolean slashPrefixed, int defaultMaxArgs, Map<String, Integer> commandArgLimits) {
    PlayerCommandData.Builder builder = PlayerCommandData.newBuilder()
      .setSessionId(sessionId);

    if (fullCommand == null || fullCommand.isBlank()) {
      return builder.setIsTruncated(false); // Empty command, no truncation
    }

    // Remove leading slash if present
    String command = slashPrefixed ? fullCommand.substring(1) : fullCommand;

    // Split into arguments
    String[] parts = command.split("\\s+");
    if (parts.length == 0) {
      return builder.setIsTruncated(false);
    }

    String commandName = parts[0];
    int maxArgs = commandArgLimits.getOrDefault(commandName, defaultMaxArgs);

    List<String> arguments = new ArrayList<>();

    // If maxArgs == 0, disable command content (empty list, not truncated)
    boolean isTruncated = false;

    if (maxArgs > 0) {
      // Add command name as first argument
      arguments.add(commandName);
      // Add additional arguments up to maxArgs - 1 (since command name is already 1)
      for (int i = 1; i < parts.length && arguments.size() < maxArgs; i++) {
        arguments.add(parts[i]);
      }
      if (parts.length > maxArgs) {
        isTruncated = true;
      }
    }

    return builder
      .addAllArguments(arguments)
      .setIsTruncated(isTruncated);
  }

  public static MineAdsEvent createCommandEvent(PlayerCommandData data) {
    return MineAdsEvent.newBuilder()
      .setEventType(EventType.COMMAND)
      .setTime(System.currentTimeMillis())
      .setCommandData(data)
      .build();
  }

  public static MineAdsEvent createTransactionEvent(TransactionData data, EventType eventType) {
    return MineAdsEvent.newBuilder()
      .setEventType(eventType)
      .setTime(System.currentTimeMillis())
      .setTransactionData(data)
      .build();
  }
}
