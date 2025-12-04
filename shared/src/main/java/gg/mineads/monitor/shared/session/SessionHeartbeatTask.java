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
