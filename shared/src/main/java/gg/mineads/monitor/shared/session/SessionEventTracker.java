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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks per-session once-only events (e.g., client brand, player settings) so we only emit them once.
 */
public final class SessionEventTracker {

  private static final ConcurrentMap<UUID, Boolean> brandSent = new ConcurrentHashMap<>();
  private static final ConcurrentMap<UUID, Boolean> settingsSent = new ConcurrentHashMap<>();

  private SessionEventTracker() {}

  public static boolean markBrandSentIfFirst(UUID sessionId) {
    return brandSent.putIfAbsent(sessionId, Boolean.TRUE) == null;
  }

  public static boolean markSettingsSentIfFirst(UUID sessionId) {
    return settingsSent.putIfAbsent(sessionId, Boolean.TRUE) == null;
  }

  public static void clearBrand(UUID sessionId) {
    brandSent.remove(sessionId);
  }

  public static void clearSettings(UUID sessionId) {
    settingsSent.remove(sessionId);
  }

  public static void clearSession(UUID sessionId) {
    clearBrand(sessionId);
    clearSettings(sessionId);
  }
}
