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
import gg.mineads.monitor.shared.event.TypeUtil;
import gg.mineads.monitor.shared.event.generated.*;
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
      List<String> groups = LuckPermsUtil.getAllGroups(player.getUniqueId());

      if (config.isDebug()) {
        log.info("[DEBUG] Player joined: %s (%s), session: %s, groups: %s".formatted(player.getName(), player.getUniqueId(), sessionId, groups));
      }

      PlayerJoinData.Builder builder = PlayerJoinData.newBuilder()
        .setSessionId(sessionId.toString())
        .setUuid(player.getUniqueId().toString())
        .setUsername(player.getName())
        .setOnlineMode(player.getServer().getOnlineMode());

      if (groups != null) {
        builder.addAllLuckpermsGroups(groups);
      }

      String host = TypeUtil.getIPString(player.getAddress());
      if (host != null) {
        builder.setHost(host);
      }

      int protocolVersion = player.getProtocolVersion();
      if (protocolVersion != -1) {
        builder.setProtocolVersion(protocolVersion);
      }

      String locale = Objects.toString(player.locale(), null);
      if (locale != null && !locale.isBlank()) {
        builder.setLocale(locale);
      }

      String clientBrand = player.getClientBrandName();
      if (clientBrand != null && !clientBrand.isBlank()) {
        builder.setClientBrand(clientBrand);
      }

      String virtualHost = TypeUtil.getHostString(player.getVirtualHost());
      if (virtualHost != null && !virtualHost.isBlank()) {
        builder.setVirtualHost(virtualHost);
      }

      PlayerJoinData data = builder.build();

      MineAdsEvent protoEvent = TypeUtil.createJoinEvent(data);

      batchProcessor.addEvent(protoEvent);
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
          log.info("[DEBUG] Player quit: %s (%s), session: %s".formatted(player.getName(), player.getUniqueId(), sessionId));
        }
        PlayerLeaveData data = PlayerLeaveData.newBuilder()
          .setSessionId(sessionId.toString())
          .build();

        MineAdsEvent protoEvent = TypeUtil.createLeaveEvent(data);

        batchProcessor.addEvent(protoEvent);
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player quit: %s - no active session found".formatted(player.getName()));
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
          String message = event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : "");
          log.info("[DEBUG] Player chat: %s - %s".formatted(player.getName(), message));
        }
        PlayerChatData.Builder dataBuilder = PlayerChatData.newBuilder()
          .setSessionId(sessionId.toString());

        if (!config.isDisableChatContent()) {
          dataBuilder.setMessage(event.getMessage());
        }

        PlayerChatData data = dataBuilder.build();

        MineAdsEvent protoEvent = TypeUtil.createChatEvent(data);

        batchProcessor.addEvent(protoEvent);
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player chat ignored: %s - no active session".formatted(player.getName()));
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
          String command = event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : "");
          log.info("[DEBUG] Player command: %s - %s".formatted(player.getName(), command));
        }
        PlayerCommandData.Builder dataBuilder = PlayerCommandData.newBuilder()
          .setSessionId(sessionId.toString());

        if (!config.isDisableCommandContent()) {
          dataBuilder.setCommand(event.getMessage());
        }

        PlayerCommandData data = dataBuilder.build();

        MineAdsEvent protoEvent = TypeUtil.createCommandEvent(data);

        batchProcessor.addEvent(protoEvent);
      } else if (config.isDebug()) {
        log.info("[DEBUG] Player command ignored: %s - no active session".formatted(player.getName()));
      }
    });
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
