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

import lombok.extern.java.Log;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Log
public class PlayerSessionManager {
  public record Session(UUID sessionId, AtomicBoolean brandSent, AtomicBoolean settingsSent) {
      public boolean markBrandSentIfFirst() {
        return brandSent.compareAndSet(false, true);
      }

      public boolean markSettingsSentIfFirst() {
        return settingsSent.compareAndSet(false, true);
      }
    }

  private static final Map<UUID, Session> playerSessions = new ConcurrentHashMap<>();
  private static final Map<UUID, Session> sessionsById = new ConcurrentHashMap<>();

  /**
   * Generates a new session ID for a player when they join.
   *
   * @param playerUuid The UUID of the player
   * @return The session instance
   */
  public static Session createSession(UUID playerUuid) {
    UUID sessionId = UUID.randomUUID();
    Session session = new Session(sessionId, new AtomicBoolean(false), new AtomicBoolean(false));
    playerSessions.put(playerUuid, session);
    sessionsById.put(sessionId, session);
    return session;
  }

  /**
   * Gets the current session for a player.
   *
   * @param playerUuid The UUID of the player
   * @return The session, or null if no session exists
   */
  public static Session getSession(UUID playerUuid) {
    return playerSessions.get(playerUuid);
  }

  /**
   * Removes a player's session when they leave.
   *
   * @param playerUuid The UUID of the player
   * @return The session that was removed, or null if none existed
   */
  public static Session removeSession(UUID playerUuid) {
    Session session = playerSessions.remove(playerUuid);
    if (session != null) {
      sessionsById.remove(session.sessionId());
      return session;
    }
    return null;
  }

  /**
   * Checks if a player has an active session.
   *
   * @param playerUuid The UUID of the player
   * @return true if the player has an active session, false otherwise
   */
  public static boolean hasSession(UUID playerUuid) {
    return playerSessions.containsKey(playerUuid);
  }

  /**
   * Gets the current number of active sessions.
   *
   * @return The number of active player sessions
   */
  public static int getActiveSessionCount() {
    return playerSessions.size();
  }

  /**
   * Returns a snapshot of active player sessions.
   *
   * @return Map of player UUID -> session
   */
  public static Map<UUID, Session> getActiveSessionsSnapshot() {
    return playerSessions.entrySet().stream()
      .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
