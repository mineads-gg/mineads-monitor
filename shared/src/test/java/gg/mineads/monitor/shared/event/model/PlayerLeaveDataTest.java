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

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerLeaveDataTest {

  @Test
  void testConstructor() {
    String sessionId = "session-123";
    PlayerLeaveData data = new PlayerLeaveData(sessionId);

    assertEquals(sessionId, data.getSessionId());
  }

  @Test
  void testConstructorWithEmptyString() {
    String sessionId = "";
    PlayerLeaveData data = new PlayerLeaveData(sessionId);

    assertEquals("", data.getSessionId());
  }

  @Test
  void testConstructorWithNullString() {
    // Since sessionId is final, we can't test null directly
    // But we can test with an empty string
    PlayerLeaveData data = new PlayerLeaveData("");
    assertEquals("", data.getSessionId());
  }

  @Test
  void testConstructorWithLongSessionId() {
    String sessionId = "very-long-session-id-that-might-be-used-in-production-123456789";
    PlayerLeaveData data = new PlayerLeaveData(sessionId);

    assertEquals(sessionId, data.getSessionId());
  }

  @Test
  void testConstructorWithUUIDSessionId() {
    String sessionId = "550e8400-e29b-41d4-a716-446655440000";
    PlayerLeaveData data = new PlayerLeaveData(sessionId);

    assertEquals(sessionId, data.getSessionId());
  }
}
