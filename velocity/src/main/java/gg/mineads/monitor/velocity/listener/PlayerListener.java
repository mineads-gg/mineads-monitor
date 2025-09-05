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
import gg.mineads.monitor.shared.event.TypeUtil;
import gg.mineads.monitor.shared.event.generated.*;
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
      List<String> groups = LuckPermsUtil.getAllGroups(player.getUniqueId());

      if (config.isDebug()) {
        log.info("[DEBUG] Player joined: %s (%s), session: %s, groups: %s".formatted(player.getUsername(), player.getUniqueId(), sessionId, groups));
      }

      PlayerJoinData.Builder builder = PlayerJoinData.newBuilder()
        .setSessionId(sessionId.toString())
        .setUuid(player.getUniqueId().toString())
        .setUsername(player.getUsername())
        .setOnlineMode(player.isOnlineMode());

      if (groups != null) {
        builder.addAllLuckpermsGroups(groups);
      }

      String host = TypeUtil.getIPString(player.getRemoteAddress());
      if (host != null) {
        builder.setHost(host);
      }

      int protocolVersion = player.getProtocolVersion().getProtocol();
      if (protocolVersion != -1) {
        builder.setProtocolVersion(protocolVersion);
      }

      String locale = Objects.toString(player.getEffectiveLocale(), "");
      if (!locale.isBlank()) {
        builder.setLocale(locale);
      }

      String clientBrand = player.getClientBrand();
      if (clientBrand != null && !clientBrand.isBlank()) {
        builder.setClientBrand(clientBrand);
      }

      String virtualHost = TypeUtil.getHostString(player.getVirtualHost().orElse(null));
      if (virtualHost != null && !virtualHost.isBlank()) {
        builder.setVirtualHost(virtualHost);
      }

      PlayerJoinData data = builder.build();

      MineAdsEvent protoEvent = TypeUtil.createJoinEvent(data);

      batchProcessor.addEvent(protoEvent);
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
          log.info("[DEBUG] Player quit: %s (%s), session: %s".formatted(player.getUsername(), player.getUniqueId(), sessionId));
        }
        PlayerLeaveData data = PlayerLeaveData.newBuilder()
          .setSessionId(sessionId.toString())
          .build();

        MineAdsEvent protoEvent = TypeUtil.createLeaveEvent(data);

        batchProcessor.addEvent(protoEvent);
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player quit: %s - no active session found".formatted(player.getUsername()));
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
          String message = event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : "");
          log.info("[DEBUG] Player chat: %s - %s".formatted(player.getUsername(), message));
        }
        PlayerChatData data = PlayerChatData.newBuilder()
          .setSessionId(sessionId.toString())
          .setMessage(event.getMessage())
          .build();

        MineAdsEvent protoEvent = TypeUtil.createChatEvent(data);

        batchProcessor.addEvent(protoEvent);
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player chat ignored: %s - no active session".formatted(player.getUsername()));
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
          String command = event.getCommand().substring(0, Math.min(50, event.getCommand().length())) + (event.getCommand().length() > 50 ? "..." : "");
          log.info("[DEBUG] Player command: %s - %s".formatted(player.getUsername(), command));
        }
        PlayerCommandData data = PlayerCommandData.newBuilder()
          .setSessionId(sessionId.toString())
          .setCommand(event.getCommand())
          .build();

        MineAdsEvent protoEvent = TypeUtil.createCommandEvent(data);

        batchProcessor.addEvent(protoEvent);
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player command ignored: %s - no active session".formatted(player.getUsername()));
      }
    });
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
