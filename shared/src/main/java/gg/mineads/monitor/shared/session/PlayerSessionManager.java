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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSessionManager {

  private static final Map<UUID, String> playerSessions = new ConcurrentHashMap<>();

  /**
   * Generates a new session ID for a player when they join.
   *
   * @param playerUuid The UUID of the player
   * @return The generated session ID
   */
  public static String createSession(UUID playerUuid) {
    String sessionId = UUID.randomUUID().toString();
    playerSessions.put(playerUuid, sessionId);
    return sessionId;
  }

  /**
   * Gets the current session ID for a player.
   *
   * @param playerUuid The UUID of the player
   * @return The session ID, or null if no session exists
   */
  public static String getSessionId(UUID playerUuid) {
    return playerSessions.get(playerUuid);
  }

  /**
   * Removes a player's session when they leave.
   *
   * @param playerUuid The UUID of the player
   * @return The session ID that was removed, or null if none existed
   */
  public static String removeSession(UUID playerUuid) {
    return playerSessions.remove(playerUuid);
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
}
