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
package gg.mineads.monitor.bukkit.listener;

import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import gg.mineads.monitor.shared.event.model.MineAdsEvent;
import gg.mineads.monitor.shared.event.model.TypeUtil;
import gg.mineads.monitor.shared.event.model.data.*;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.shared.session.PlayerSessionManager;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Log
public class PlayerListener implements Listener {

  private final BatchProcessor batchProcessor;
  private final Config config;
  private final MineAdsScheduler scheduler;

  public PlayerListener(BatchProcessor batchProcessor, Config config, MineAdsScheduler scheduler) {
    this.batchProcessor = batchProcessor;
    this.config = config;
    this.scheduler = scheduler;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!isEventEnabled(EventType.JOIN)) {
      if (config.isDebug()) {
        log.info("[DEBUG] Player join event ignored - JOIN events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.createSession(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      List<String> ranks = LuckPermsUtil.getAllGroups(player.getUniqueId());

      if (config.isDebug()) {
        log.info("[DEBUG] Player joined: " + player.getName() + " (" + player.getUniqueId() + "), session: " + sessionId + ", ranks: " + ranks);
      }

      PlayerJoinData data = new PlayerJoinData(
        sessionId,
        player.getUniqueId(),
        player.getName(),
        Objects.toString(player.locale(), null),
        TypeUtil.getHostString(player.getAddress()),
        player.getClientBrandName(),
        player.getProtocolVersion(),
        player.getServer().getOnlineMode(),
        ranks,
        TypeUtil.getHostString(player.getVirtualHost())
      );
      batchProcessor.addEvent(MineAdsEvent.from(data));
    });
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (!isEventEnabled(EventType.LEAVE)) {
      if (config.isDebug()) {
        log.info("[DEBUG] Player quit event ignored - LEAVE events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.removeSession(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (sessionId != null) {
        if (config.isDebug()) {
          log.info("[DEBUG] Player quit: " + player.getName() + " (" + player.getUniqueId() + "), session: " + sessionId);
        }
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerLeaveData(sessionId)));
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player quit: " + player.getName() + " - no active session found");
      }
    });
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    if (!isEventEnabled(EventType.CHAT)) {
      if (config.isDebug()) {
        log.info("[DEBUG] Player chat event ignored - CHAT events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (sessionId != null) {
        if (config.isDebug()) {
          log.info("[DEBUG] Player chat: " + player.getName() + " - " + event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : ""));
        }
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerChatData(sessionId, event.getMessage())));
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player chat ignored: " + player.getName() + " - no active session");
      }
    });
  }

  @EventHandler
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    if (!isEventEnabled(EventType.COMMAND)) {
      if (config.isDebug()) {
        log.info("[DEBUG] Player command event ignored - COMMAND events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (sessionId != null) {
        if (config.isDebug()) {
          log.info("[DEBUG] Player command: " + player.getName() + " - " + event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : ""));
        }
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerCommandData(sessionId, event.getMessage())));
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player command ignored: " + player.getName() + " - no active session");
      }
    });
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
