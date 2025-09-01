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

import static org.junit.jupiter.api.Assertions.*;

class PlayerJoinDataTest {

  @Test
  void testConstructorWithAllFields() {
    String sessionId = "session-123";
    String locale = "en_US";
    String ip = "192.168.1.1";
    String clientBrand = "vanilla";
    String minecraftVersion = "1.20.1";
    boolean onlineMode = true;
    String luckPermsRank = "vip";

    PlayerJoinData data = new PlayerJoinData(sessionId, locale, ip, clientBrand, minecraftVersion, onlineMode, luckPermsRank);

    assertEquals(sessionId, data.getSessionId());
    assertEquals(locale, data.getLocale());
    assertEquals(ip, data.getIp());
    assertEquals(clientBrand, data.getClientBrand());
    assertEquals(minecraftVersion, data.getMinecraftVersion());
    assertEquals(onlineMode, data.isOnlineMode());
    assertEquals(luckPermsRank, data.getLuckPermsRank());
  }

  @Test
  void testConstructorWithNullFields() {
    String sessionId = "session-123";

    PlayerJoinData data = new PlayerJoinData(sessionId, null, null, null, null, false, null);

    assertEquals(sessionId, data.getSessionId());
    assertNull(data.getLocale());
    assertNull(data.getIp());
    assertNull(data.getClientBrand());
    assertNull(data.getMinecraftVersion());
    assertFalse(data.isOnlineMode());
    assertNull(data.getLuckPermsRank());
  }

  @Test
  void testConstructorWithMixedNullAndNonNullFields() {
    String sessionId = "session-456";
    String locale = "de_DE";
    String ip = null;
    String clientBrand = "fabric";
    String minecraftVersion = null;
    boolean onlineMode = true;
    String luckPermsRank = "admin";

    PlayerJoinData data = new PlayerJoinData(sessionId, locale, ip, clientBrand, minecraftVersion, onlineMode, luckPermsRank);

    assertEquals(sessionId, data.getSessionId());
    assertEquals(locale, data.getLocale());
    assertNull(data.getIp());
    assertEquals(clientBrand, data.getClientBrand());
    assertNull(data.getMinecraftVersion());
    assertTrue(data.isOnlineMode());
    assertEquals(luckPermsRank, data.getLuckPermsRank());
  }

  @Test
  void testOnlineModeTrue() {
    PlayerJoinData data = new PlayerJoinData("session-123", null, null, null, null, true, null);
    assertTrue(data.isOnlineMode());
  }

  @Test
  void testOnlineModeFalse() {
    PlayerJoinData data = new PlayerJoinData("session-123", null, null, null, null, false, null);
    assertFalse(data.isOnlineMode());
  }

  @Test
  void testEmptyStrings() {
    PlayerJoinData data = new PlayerJoinData("", "", "", "", "", false, "");

    assertEquals("", data.getSessionId());
    assertEquals("", data.getLocale());
    assertEquals("", data.getIp());
    assertEquals("", data.getClientBrand());
    assertEquals("", data.getMinecraftVersion());
    assertEquals("", data.getLuckPermsRank());
  }
}
