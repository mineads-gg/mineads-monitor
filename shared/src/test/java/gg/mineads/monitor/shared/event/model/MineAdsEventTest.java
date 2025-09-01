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

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MineAdsEventTest {

  @Test
  void testFactoryMethodPlayerJoin() {
    UUID sessionId = UUID.randomUUID();
    String locale = "en_US";
    String ip = "127.0.0.1";
    String clientBrand = "vanilla";
    String minecraftVersion = "1.20.1";
    boolean onlineMode = true;
    String luckPermsRank = "vip";

    MineAdsEvent event = MineAdsEvent.playerJoin(sessionId, locale, ip, clientBrand, minecraftVersion, onlineMode, luckPermsRank);

    assertEquals(EventType.JOIN, event.getEventType());
    assertNotNull(event.getJoinData());
    assertEquals(sessionId, event.getJoinData().getSessionId());
    assertEquals(locale, event.getJoinData().getLocale());
    assertEquals(ip, event.getJoinData().getIp());
    assertEquals(clientBrand, event.getJoinData().getClientBrand());
    assertEquals(minecraftVersion, event.getJoinData().getMinecraftVersion());
    assertEquals(onlineMode, event.getJoinData().isOnlineMode());
    assertEquals(luckPermsRank, event.getJoinData().getLuckPermsRank());
    assertTrue(event.getTime() > 0);
    assertTrue(event.getTime() <= System.currentTimeMillis());
  }

  @Test
  void testTimeIsSetToCurrentTime() {
    long beforeCreation = System.currentTimeMillis();
    MineAdsEvent event = MineAdsEvent.playerJoin(UUID.randomUUID(), null, null, null, null, true, null);
    long afterCreation = System.currentTimeMillis();

    assertTrue(event.getTime() >= beforeCreation);
    assertTrue(event.getTime() <= afterCreation);
  }

  @Test
  void testDifferentEventTypes() {
    UUID sessionId = UUID.randomUUID();

    // Test all factory methods work correctly
    MineAdsEvent joinEvent = MineAdsEvent.playerJoin(sessionId, null, null, null, null, true, null);
    MineAdsEvent leaveEvent = MineAdsEvent.playerLeave(sessionId);
    MineAdsEvent chatEvent = MineAdsEvent.playerChat(sessionId, "hello world");
    MineAdsEvent commandEvent = MineAdsEvent.playerCommand(sessionId, "/help");

    TebexPurchaseData purchaseData = new TebexPurchaseData("id", "user", "txn", "10.00", "USD", "VIP", null, null, null, null, null, null, null, null, null, null);
    MineAdsEvent purchaseEvent = MineAdsEvent.purchase(PurchaseType.TEBEX, purchaseData);

    assertEquals(EventType.JOIN, joinEvent.getEventType());
    assertEquals(EventType.LEAVE, leaveEvent.getEventType());
    assertEquals(EventType.CHAT, chatEvent.getEventType());
    assertEquals(EventType.COMMAND, commandEvent.getEventType());
    assertEquals(EventType.PURCHASE, purchaseEvent.getEventType());

    // Test that the correct data field is populated
    assertNotNull(joinEvent.getJoinData());
    assertNotNull(leaveEvent.getLeaveData());
    assertNotNull(chatEvent.getChatData());
    assertNotNull(commandEvent.getCommandData());
    assertNotNull(purchaseEvent.getPurchaseData());

    // Test that other data fields are null
    assertNull(joinEvent.getLeaveData());
    assertNull(joinEvent.getChatData());
    assertNull(joinEvent.getCommandData());
    assertNull(joinEvent.getPurchaseData());
  }

  @Test
  void testFactoryMethodPlayerLeave() {
    UUID sessionId = UUID.randomUUID();
    MineAdsEvent event = MineAdsEvent.playerLeave(sessionId);

    assertEquals(EventType.LEAVE, event.getEventType());
    assertNotNull(event.getLeaveData());
    assertEquals(sessionId, event.getLeaveData().getSessionId());
  }

  @Test
  void testFactoryMethodPlayerChat() {
    UUID sessionId = UUID.randomUUID();
    String message = "Hello world!";
    MineAdsEvent event = MineAdsEvent.playerChat(sessionId, message);

    assertEquals(EventType.CHAT, event.getEventType());
    assertNotNull(event.getChatData());
    assertEquals(sessionId, event.getChatData().getSessionId());
    assertEquals(message, event.getChatData().getMessage());
  }


}
