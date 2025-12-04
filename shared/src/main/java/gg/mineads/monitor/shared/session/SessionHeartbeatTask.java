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

import java.util.Map;
import java.util.UUID;

@Log
@RequiredArgsConstructor
public class SessionHeartbeatTask implements Runnable {

  private final MineAdsMonitorPlugin plugin;

  @Override
  public void run() {
    if (plugin.hasConfigIssues()) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Skipping heartbeat emit due to configuration issues");
      }
      return;
    }

    Map<UUID, UUID> activeSessions = PlayerSessionManager.getActiveSessionsSnapshot();
    if (activeSessions.isEmpty()) {
      return;
    }

    if (plugin.getConfig().isDebug()) {
      log.info("[DEBUG] Emitting heartbeats for active sessions: " + activeSessions.size());
    }

    for (UUID sessionId : activeSessions.values()) {
      PlayerHeartbeatData heartbeat = PlayerHeartbeatData.newBuilder()
        .setSessionId(sessionId.toString())
        .build();
      MineAdsEvent event = TypeUtil.createHeartbeatEvent(heartbeat);
      plugin.getBatchProcessor().addEvent(event);
    }
  }
}
