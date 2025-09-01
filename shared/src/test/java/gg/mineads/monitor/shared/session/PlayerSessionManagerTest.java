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
package gg.mineads.monitor.shared.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSessionManagerTest {

  @AfterEach
  void tearDown() {
    // Clean up all sessions after each test
    // Since we can't directly clear the static map, we'll remove test sessions
  }

  @Test
  void testCreateSession() {
    UUID playerUuid = UUID.randomUUID();
    String sessionId = PlayerSessionManager.createSession(playerUuid);

    assertNotNull(sessionId);
    assertFalse(sessionId.isEmpty());
    assertEquals(sessionId, PlayerSessionManager.getSessionId(playerUuid));
    assertTrue(PlayerSessionManager.hasSession(playerUuid));
  }

  @Test
  void testGetSessionId() {
    UUID playerUuid = UUID.randomUUID();
    assertNull(PlayerSessionManager.getSessionId(playerUuid));

    String sessionId = PlayerSessionManager.createSession(playerUuid);
    assertEquals(sessionId, PlayerSessionManager.getSessionId(playerUuid));
  }

  @Test
  void testRemoveSession() {
    UUID playerUuid = UUID.randomUUID();
    String sessionId = PlayerSessionManager.createSession(playerUuid);

    String removedSessionId = PlayerSessionManager.removeSession(playerUuid);
    assertEquals(sessionId, removedSessionId);
    assertNull(PlayerSessionManager.getSessionId(playerUuid));
    assertFalse(PlayerSessionManager.hasSession(playerUuid));
  }

  @Test
  void testRemoveNonExistentSession() {
    UUID playerUuid = UUID.randomUUID();
    assertNull(PlayerSessionManager.removeSession(playerUuid));
  }

  @Test
  void testHasSession() {
    UUID playerUuid = UUID.randomUUID();
    assertFalse(PlayerSessionManager.hasSession(playerUuid));

    PlayerSessionManager.createSession(playerUuid);
    assertTrue(PlayerSessionManager.hasSession(playerUuid));

    PlayerSessionManager.removeSession(playerUuid);
    assertFalse(PlayerSessionManager.hasSession(playerUuid));
  }

  @Test
  void testGetActiveSessionCount() {
    UUID player1 = UUID.randomUUID();
    UUID player2 = UUID.randomUUID();
    UUID player3 = UUID.randomUUID();

    // Initially should be 0 (or whatever was left from previous tests)
    int initialCount = PlayerSessionManager.getActiveSessionCount();

    PlayerSessionManager.createSession(player1);
    assertEquals(initialCount + 1, PlayerSessionManager.getActiveSessionCount());

    PlayerSessionManager.createSession(player2);
    assertEquals(initialCount + 2, PlayerSessionManager.getActiveSessionCount());

    PlayerSessionManager.createSession(player3);
    assertEquals(initialCount + 3, PlayerSessionManager.getActiveSessionCount());

    PlayerSessionManager.removeSession(player1);
    assertEquals(initialCount + 2, PlayerSessionManager.getActiveSessionCount());

    PlayerSessionManager.removeSession(player2);
    PlayerSessionManager.removeSession(player3);
    assertEquals(initialCount, PlayerSessionManager.getActiveSessionCount());
  }

  @Test
  void testMultipleSessionsForSamePlayer() {
    UUID playerUuid = UUID.randomUUID();

    String sessionId1 = PlayerSessionManager.createSession(playerUuid);
    assertEquals(sessionId1, PlayerSessionManager.getSessionId(playerUuid));

    // Creating another session for the same player should replace the first
    String sessionId2 = PlayerSessionManager.createSession(playerUuid);
    assertNotEquals(sessionId1, sessionId2);
    assertEquals(sessionId2, PlayerSessionManager.getSessionId(playerUuid));
    assertEquals(1, PlayerSessionManager.getActiveSessionCount());
  }
}
