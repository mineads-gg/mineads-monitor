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
package gg.mineads.monitor.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import gg.mineads.monitor.shared.event.model.MineAdsEvent;
import gg.mineads.monitor.shared.event.model.TypeUtil;
import gg.mineads.monitor.shared.event.model.data.*;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.shared.session.PlayerSessionManager;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Log
public class PlayerListener {

  private final BatchProcessor batchProcessor;
  private final Config config;
  private final MineAdsScheduler scheduler;

  public PlayerListener(BatchProcessor batchProcessor, Config config, MineAdsScheduler scheduler) {
    this.batchProcessor = batchProcessor;
    this.config = config;
    this.scheduler = scheduler;
  }

  @Subscribe
  public void onPostLogin(PostLoginEvent event) {
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
        log.info("[DEBUG] Player joined: " + player.getUsername() + " (" + player.getUniqueId() + "), session: " + sessionId + ", ranks: " + ranks);
      }

      PlayerJoinData data = new PlayerJoinData(
        sessionId,
        player.getUniqueId(),
        player.getUsername(),
        Objects.toString(player.getEffectiveLocale(), null),
        TypeUtil.getHostString(player.getRemoteAddress()),
        player.getClientBrand(),
        player.getProtocolVersion().getProtocol(),
        player.isOnlineMode(),
        ranks,
        TypeUtil.getHostString(player.getVirtualHost().orElse(null))
      );
      batchProcessor.addEvent(MineAdsEvent.from(data));
    });
  }

  @Subscribe
  public void onDisconnect(DisconnectEvent event) {
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
          log.info("[DEBUG] Player quit: " + player.getUsername() + " (" + player.getUniqueId() + "), session: " + sessionId);
        }
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerLeaveData(sessionId)));
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player quit: " + player.getUsername() + " - no active session found");
      }
    });
  }

  @Subscribe
  public void onPlayerChat(PlayerChatEvent event) {
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
          log.info("[DEBUG] Player chat: " + player.getUsername() + " - " + event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : ""));
        }
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerChatData(sessionId, event.getMessage())));
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player chat ignored: " + player.getUsername() + " - no active session");
      }
    });
  }

  @Subscribe
  public void onCommandExecute(CommandExecuteEvent event) {
    if (!isEventEnabled(EventType.COMMAND)) {
      if (config.isDebug()) {
        log.info("[DEBUG] Player command event ignored - COMMAND events disabled");
      }
      return;
    }

    if (!(event.getCommandSource() instanceof Player player)) {
      return;
    }

    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (sessionId != null) {
        if (config.isDebug()) {
          log.info("[DEBUG] Player command: " + player.getUsername() + " - " + event.getCommand().substring(0, Math.min(50, event.getCommand().length())) + (event.getCommand().length() > 50 ? "..." : ""));
        }
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerCommandData(sessionId, event.getCommand())));
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player command ignored: " + player.getUsername() + " - no active session");
      }
    });
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
