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
package gg.mineads.monitor.bungee.listener;

import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import gg.mineads.monitor.shared.event.TypeUtil;
import gg.mineads.monitor.shared.event.generated.*;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.shared.session.PlayerSessionManager;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Objects;
import java.util.UUID;

@Log
public class PlayerListener implements Listener {

  private final BatchProcessor batchProcessor;
  private final Config config;
  private final MineAdsScheduler scheduler;
  private final MineAdsMonitorPlugin plugin;

  public PlayerListener(BatchProcessor batchProcessor, Config config, MineAdsScheduler scheduler, MineAdsMonitorPlugin plugin) {
    this.batchProcessor = batchProcessor;
    this.config = config;
    this.scheduler = scheduler;
    this.plugin = plugin;
  }

  @EventHandler
  public void onPostLogin(PostLoginEvent event) {
    if (!isEventEnabled(EventType.JOIN)) {
      if (config.isDebug()) {
        log.info("[DEBUG] Player join event ignored - JOIN events disabled");
      }
      return;
    }

    ProxiedPlayer player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.createSession(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      LuckPermsData luckPermsData = LuckPermsUtil.getAllGroups(player.getUniqueId());

      if (config.isDebug()) {
        log.info("[DEBUG] Player joined: %s (%s), session: %s, groups: %s".formatted(player.getName(), player.getUniqueId(), sessionId, luckPermsData != null ? luckPermsData.getGroupsList() : null));
      }

      // Check for update notification
      if (plugin.isOutdated() && player.hasPermission("mineadsmonitor.admin")) {
        TextComponent updateMessage = new TextComponent("[MineAdsMonitor] A new version is available! Download it at: https://modrinth.com/project/mineads-monitor");
        updateMessage.setColor(ChatColor.YELLOW);
        player.sendMessage(updateMessage);
      }

      PlayerJoinData.Builder builder = PlayerJoinData.newBuilder()
        .setSessionId(sessionId.toString())
        .setUuid(player.getUniqueId().toString())
        .setUsername(player.getName())
        .setOnlineMode(player.getPendingConnection().isOnlineMode());

      if (luckPermsData != null) {
        builder.setLuckpermsData(luckPermsData);
      }

      String host = TypeUtil.getIPString(player.getSocketAddress());
      if (host != null) {
        builder.setHost(host);
      }

      int protocolVersion = player.getPendingConnection().getVersion();
      if (protocolVersion != -1) {
        builder.setProtocolVersion(protocolVersion);
      }

      String locale = Objects.toString(player.getLocale(), null);
      if (locale != null && !locale.isBlank()) {
        builder.setLocale(locale);
      }

      String clientBrand = player.getClientBrand();
      if (clientBrand != null && !clientBrand.isBlank()) {
        builder.setClientBrand(clientBrand);
      }

      String virtualHost = TypeUtil.getHostString(player.getPendingConnection().getVirtualHost());
      if (virtualHost != null && !virtualHost.isBlank()) {
        builder.setVirtualHost(virtualHost);
      }

      PlayerJoinData data = builder.build();

      MineAdsEvent protoEvent = TypeUtil.createJoinEvent(data);

      batchProcessor.addEvent(protoEvent);
    });
  }

  @EventHandler
  public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    if (!isEventEnabled(EventType.LEAVE)) {
      if (config.isDebug()) {
        log.info("[DEBUG] Player quit event ignored - LEAVE events disabled");
      }
      return;
    }

    ProxiedPlayer player = event.getPlayer();
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
  public void onChat(ChatEvent event) {
    if (!(event.getSender() instanceof ProxiedPlayer player)) {
      return;
    }

    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId == null) {
      if (config.isDebug()) {
        log.info("[DEBUG] Chat event ignored: %s - no active session".formatted(player.getName()));
      }
      return;
    }

    boolean isCommand = event.isCommand() || event.isProxyCommand();
    boolean isChatEnabled = isEventEnabled(EventType.CHAT);
    boolean isCommandEnabled = isEventEnabled(EventType.COMMAND);

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (isCommand) {
        if (isCommandEnabled) {
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
          log.info("[DEBUG] Player command event ignored - COMMAND events disabled");
        }
      } else {
        if (isChatEnabled) {
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
          log.info("[DEBUG] Player chat event ignored - CHAT events disabled");
        }
      }
    });
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
