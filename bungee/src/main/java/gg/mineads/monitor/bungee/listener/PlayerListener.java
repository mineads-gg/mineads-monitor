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
import gg.mineads.monitor.shared.event.TypeUtil;
import gg.mineads.monitor.shared.event.generated.*;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.shared.session.PlayerSessionManager;
import gg.mineads.monitor.shared.skin.SkinData;
import gg.mineads.monitor.shared.skin.property.SkinProperty;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.SettingsChangedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.connection.LoginResult;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Log
public class PlayerListener implements Listener {

  private final MineAdsScheduler scheduler;
  private final MineAdsMonitorPlugin plugin;

  public PlayerListener(MineAdsScheduler scheduler, MineAdsMonitorPlugin plugin) {
    this.scheduler = scheduler;
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPostLogin(PostLoginEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.JOIN_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player join event ignored - JOIN events disabled");
      }
      return;
    }

    ProxiedPlayer player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.createSession(player.getUniqueId());
    SkinData skinData = extractSkinData(player.getPendingConnection().getLoginProfile());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      LuckPermsData luckPermsData = LuckPermsUtil.getLuckPermsData(plugin.getConfig(), player.getUniqueId());

      if (plugin.getConfig().isDebug()) {
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

      String virtualHost = TypeUtil.getHostString(player.getPendingConnection().getVirtualHost());
      if (virtualHost != null && !virtualHost.isBlank()) {
        builder.setVirtualHost(virtualHost);
      }

      if (skinData != null) {
        if (skinData.skinTextureHash() != null && !skinData.skinTextureHash().isBlank()) {
          builder.setSkinTextureHash(skinData.skinTextureHash());
        }
        if (skinData.capeTextureHash() != null && !skinData.capeTextureHash().isBlank()) {
          builder.setCapeTextureHash(skinData.capeTextureHash());
        }
        if (skinData.skinVariant() != null) {
          builder.setSkinType(skinData.skinVariant().name());
        }
      }

      PlayerJoinData data = builder.build();

      MineAdsEvent protoEvent = TypeUtil.createJoinEvent(data);

      plugin.getBatchProcessor().addEvent(protoEvent);
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.LEAVE_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player quit event ignored - LEAVE events disabled");
      }
      return;
    }

    ProxiedPlayer player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.removeSession(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (sessionId != null) {
        if (plugin.getConfig().isDebug()) {
          log.info("[DEBUG] Player quit: %s (%s), session: %s".formatted(player.getName(), player.getUniqueId(), sessionId));
        }
        PlayerLeaveData data = PlayerLeaveData.newBuilder()
          .setSessionId(sessionId.toString())
          .build();

        MineAdsEvent protoEvent = TypeUtil.createLeaveEvent(data);

        plugin.getBatchProcessor().addEvent(protoEvent);
      } else if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player quit: %s - no active session found".formatted(player.getName()));
      }
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSettingsChanged(SettingsChangedEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.SETTINGS_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player settings event ignored - PLAYER_SETTINGS events disabled");
      }
      return;
    }

    ProxiedPlayer player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());

    scheduler.runAsync(() -> {
      if (sessionId == null) {
        if (plugin.getConfig().isDebug()) {
          log.info("[DEBUG] Player settings ignored: %s - no active session".formatted(player.getName()));
        }
        return;
      }

      PlayerSettingsData.Builder builder = PlayerSettingsData.newBuilder()
        .setSessionId(sessionId.toString())
        .setViewDistance(player.getViewDistance())
        .setChatMode(player.getChatMode().name())
        .setChatColors(player.hasChatColors())
        .setMainHand(player.getMainHand().name());

      Locale locale = player.getLocale();
      if (locale != null) {
        builder.setLocale(locale.toLanguageTag());
      }

      PlayerSettingsData data = builder.build();
      MineAdsEvent protoEvent = TypeUtil.createPlayerSettingsEvent(data);

      plugin.getBatchProcessor().addEvent(protoEvent);
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPluginMessage(PluginMessageEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.CLIENT_BRAND_DATA)) {
      return;
    }

    if (!(event.getSender() instanceof ProxiedPlayer player)) {
      return;
    }

    if (!isBrandTag(event.getTag())) {
      return;
    }

    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId == null) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Client brand ignored: %s - no active session".formatted(player.getName()));
      }
      return;
    }

    scheduler.runAsync(() -> {
      String clientBrand = decodeClientBrand(event.getData());
      if (clientBrand == null || clientBrand.isBlank()) {
        return;
      }

      PlayerClientBrandData data = PlayerClientBrandData.newBuilder()
        .setSessionId(sessionId.toString())
        .setClientBrand(clientBrand)
        .build();
      MineAdsEvent protoEvent = TypeUtil.createPlayerClientBrandEvent(data);

      plugin.getBatchProcessor().addEvent(protoEvent);
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChat(ChatEvent event) {
    if (event.isCancelled()) {
      return;
    }

    if (!(event.getSender() instanceof ProxiedPlayer player)) {
      return;
    }

    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId == null) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Chat event ignored: %s - no active session".formatted(player.getName()));
      }
      return;
    }

    boolean isCommand = event.isCommand() || event.isProxyCommand();
    boolean isChatEnabled = isEventEnabled(MineAdsEvent.DataCase.CHAT_DATA);
    boolean isCommandEnabled = isEventEnabled(MineAdsEvent.DataCase.COMMAND_DATA);

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (isCommand) {
        if (isCommandEnabled) {
          if (plugin.getConfig().isDebug()) {
            String command = event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : "");
            log.info("[DEBUG] Player command: %s - %s".formatted(player.getName(), command));
          }
          PlayerCommandData.Builder dataBuilder = TypeUtil.createCommandDataBuilder(
            sessionId.toString(),
            event.getMessage(),
            true,
            plugin.getConfig().getDefaultMaxCommandArgs(),
            plugin.getConfig().getCommandArgLimits()
          );

          PlayerCommandData data = dataBuilder.build();

          MineAdsEvent protoEvent = TypeUtil.createCommandEvent(data);

          plugin.getBatchProcessor().addEvent(protoEvent);
        } else if (plugin.getConfig().isDebug()) {
          log.info("[DEBUG] Player command event ignored - COMMAND events disabled");
        }
      } else {
        if (isChatEnabled) {
          if (plugin.getConfig().isDebug()) {
            String message = event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + (event.getMessage().length() > 50 ? "..." : "");
            log.info("[DEBUG] Player chat: %s - %s".formatted(player.getName(), message));
          }
          PlayerChatData.Builder dataBuilder = PlayerChatData.newBuilder()
            .setSessionId(sessionId.toString());

          if (!plugin.getConfig().isDisableChatContent()) {
            dataBuilder.setMessage(event.getMessage());
          }

          PlayerChatData data = dataBuilder.build();

          MineAdsEvent protoEvent = TypeUtil.createChatEvent(data);

          plugin.getBatchProcessor().addEvent(protoEvent);
        } else if (plugin.getConfig().isDebug()) {
          log.info("[DEBUG] Player chat event ignored - CHAT events disabled");
        }
      }
    });
  }

  private boolean isBrandTag(String tag) {
    if (tag == null) {
      return false;
    }
    String normalized = tag.toLowerCase();
    return normalized.equals("minecraft:brand") || normalized.equals("mc|brand");
  }

  private String decodeClientBrand(byte[] data) {
    try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data))) {
      return inputStream.readUTF();
    } catch (IOException exception) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to decode client brand: " + exception.getMessage());
      }
      return null;
    }
  }

  private boolean isEventEnabled(MineAdsEvent.DataCase eventType) {
    return plugin.getConfig().isEventEnabled(eventType);
  }

  private SkinData extractSkinData(LoginResult loginProfile) {
    if (loginProfile == null || loginProfile.getProperties() == null) {
      return null;
    }

    return java.util.Arrays.stream(loginProfile.getProperties())
      .map(property -> SkinProperty.tryParse(property.getName(), property.getValue(), property.getSignature()))
      .flatMap(Optional::stream)
      .map(SkinData::fromProperty)
      .flatMap(Optional::stream)
      .findFirst()
      .orElse(null);
  }
}
