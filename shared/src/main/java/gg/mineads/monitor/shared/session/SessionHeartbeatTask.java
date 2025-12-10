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

import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.event.TypeUtil;
import gg.mineads.monitor.shared.event.generated.PlayerHeartbeatData;
import gg.mineads.monitor.shared.event.generated.MineAdsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log
@RequiredArgsConstructor
public class SessionHeartbeatTask implements Runnable {

  private final MineAdsMonitorPlugin plugin;
  private final PlayerOnlineChecker onlineChecker;

  @Override
  public void run() {
    if (plugin.hasConfigIssues()) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Skipping heartbeat emit due to configuration issues");
      }
      return;
    }

    Map<UUID, PlayerSessionManager.Session> activeSessions = PlayerSessionManager.getActiveSessionsSnapshot();
    List<String> activeSessionIds = new ArrayList<>(activeSessions.size());
    for (Map.Entry<UUID, PlayerSessionManager.Session> entry : activeSessions.entrySet()) {
      UUID playerUuid = entry.getKey();
      UUID sessionId = entry.getValue().sessionId();

      if (!onlineChecker.isOnline(playerUuid)) {
        PlayerSessionManager.removeSession(playerUuid);
        if (plugin.getConfig().isDebug()) {
          log.info("[DEBUG] Removing inactive session for player " + playerUuid + " (session " + sessionId + ")");
        }
        continue;
      }

      activeSessionIds.add(sessionId.toString());
    }

    if (plugin.getConfig().isDebug()) {
      log.info("[DEBUG] Emitting heartbeat snapshot for active sessions: " + activeSessionIds.size());
    }

    PlayerHeartbeatData heartbeat = PlayerHeartbeatData.newBuilder()
      .addAllSessionIds(activeSessionIds)
      .build();
    MineAdsEvent event = TypeUtil.createHeartbeatEvent(heartbeat);
    plugin.getBatchProcessor().addEvent(event);
  }
}
