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

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.event.TypeUtil;
import gg.mineads.monitor.shared.event.generated.*;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.shared.session.PlayerSessionManager;
import gg.mineads.monitor.shared.session.SessionEventTracker;
import gg.mineads.monitor.shared.skin.SkinData;
import gg.mineads.monitor.shared.skin.property.SkinProperty;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log
@SuppressFBWarnings(value = "EI2", justification = "Listener needs the plugin reference to access config and batches.")
public class PlayerListener implements Listener {

  private static final int MAX_BRAND_ATTEMPTS = 6;
  private static final long BRAND_RETRY_DELAY_MS = 500L;

  private final MineAdsScheduler scheduler;
  private final MineAdsMonitorPlugin plugin;

  public PlayerListener(MineAdsScheduler scheduler, MineAdsMonitorPlugin plugin) {
    this.scheduler = scheduler;
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.JOIN_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player join event ignored - JOIN events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.createSession(player.getUniqueId());
    SkinData skinData = extractSkinData(player);

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      LuckPermsData luckPermsData = LuckPermsUtil.getLuckPermsData(plugin.getConfig(), player.getUniqueId());

      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player joined: %s (%s), session: %s, groups: %s".formatted(player.getName(), player.getUniqueId(), sessionId, luckPermsData != null ? luckPermsData.getGroupsList() : null));
      }

      // Check for update notification
      if (plugin.isOutdated() && player.hasPermission("mineadsmonitor.admin")) {
        Component updateMessage = Component.text("[MineAdsMonitor] A new version is available! Download it at: https://modrinth.com/project/mineads-monitor", NamedTextColor.YELLOW);
        player.sendMessage(updateMessage);
      }

      PlayerJoinData.Builder builder = PlayerJoinData.newBuilder()
        .setSessionId(sessionId.toString())
        .setUuid(player.getUniqueId().toString())
        .setUsername(player.getName())
        .setOnlineMode(player.getServer().getOnlineMode());

      if (luckPermsData != null) {
        builder.setLuckpermsData(luckPermsData);
      }

      String host = TypeUtil.getIPString(player.getAddress());
      if (host != null) {
        builder.setHost(host);
      }

      int protocolVersion = player.getProtocolVersion();
      if (protocolVersion != -1) {
        builder.setProtocolVersion(protocolVersion);
      }

      String virtualHost = TypeUtil.getHostString(player.getVirtualHost());
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

      if (isEventEnabled(MineAdsEvent.DataCase.CLIENT_BRAND_DATA)) {
        scheduleClientBrandCapture(player, sessionId, 0);
      }
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.LEAVE_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player quit event ignored - LEAVE events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
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

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.CHAT_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player chat event ignored - CHAT events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (sessionId != null) {
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
        log.info("[DEBUG] Player chat ignored: %s - no active session".formatted(player.getName()));
      }
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerSettings(PlayerClientOptionsChangeEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.SETTINGS_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player settings event ignored - PLAYER_SETTINGS events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());

    scheduler.runAsync(() -> {
      if (sessionId == null) {
        if (plugin.getConfig().isDebug()) {
          log.info("[DEBUG] Player settings ignored: %s - no active session".formatted(player.getName()));
        }
        return;
      }

      PlayerSettingsData.Builder builder = PlayerSettingsData.newBuilder()
        .setSessionId(sessionId.toString());

      String locale = event.getLocale();
      if (locale != null && !locale.isBlank()) {
        builder.setLocale(locale);
      }

      builder
        .setViewDistance(event.getViewDistance())
        .setChatMode(event.getChatVisibility().name())
        .setChatColors(event.hasChatColorsEnabled())
        .setMainHand(event.getMainHand().name())
        .setTextFilteringEnabled(event.hasTextFilteringEnabled())
        .setAllowsServerListings(event.allowsServerListings());

      PlayerSettingsData data = builder.build();
      if (SessionEventTracker.markSettingsSentIfFirst(sessionId)) {
        MineAdsEvent protoEvent = TypeUtil.createPlayerSettingsEvent(data);
        plugin.getBatchProcessor().addEvent(protoEvent);
      } else if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Skipping duplicate player settings event for session " + sessionId);
      }
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    if (!isEventEnabled(MineAdsEvent.DataCase.COMMAND_DATA)) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Player command event ignored - COMMAND events disabled");
      }
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());

    // Process event asynchronously to avoid blocking main thread
    scheduler.runAsync(() -> {
      if (sessionId != null) {
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
        log.info("[DEBUG] Player command ignored: %s - no active session".formatted(player.getName()));
      }
    });
  }

  private void scheduleClientBrandCapture(Player player, UUID sessionId, int attempt) {
    scheduler.runAsync(() -> {
      if (!SessionEventTracker.markBrandSentIfFirst(sessionId)) {
        if (plugin.getConfig().isDebug()) {
          log.info("[DEBUG] Skipping duplicate client brand for session " + sessionId);
        }
        return;
      }

      String clientBrand = player.getClientBrandName();
      if (clientBrand != null && !clientBrand.isBlank()) {
        PlayerClientBrandData data = PlayerClientBrandData.newBuilder()
          .setSessionId(sessionId.toString())
          .setClientBrand(clientBrand)
          .build();
        MineAdsEvent protoEvent = TypeUtil.createPlayerClientBrandEvent(data);
        plugin.getBatchProcessor().addEvent(protoEvent);
      } else if (attempt < MAX_BRAND_ATTEMPTS) {
        // Allow a later attempt to send once when data becomes available
        SessionEventTracker.clearBrand(sessionId);
        scheduler.scheduleAsyncDelayed(() -> scheduleClientBrandCapture(player, sessionId, attempt + 1), BRAND_RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
      } else if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Client brand not available for %s after %d attempts".formatted(player.getName(), attempt));
      }
    });
  }

  private boolean isEventEnabled(MineAdsEvent.DataCase eventType) {
    return plugin.getConfig().isEventEnabled(eventType);
  }

  private SkinData extractSkinData(Player player) {
    PlayerProfile profile = player.getPlayerProfile();
    return profile.getProperties().stream()
      .map(property -> SkinProperty.tryParse(property.getName(), property.getValue(), property.getSignature()))
      .flatMap(Optional::stream)
      .map(SkinData::fromProperty)
      .flatMap(Optional::stream)
      .findFirst()
      .orElse(null);
  }
}
