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
