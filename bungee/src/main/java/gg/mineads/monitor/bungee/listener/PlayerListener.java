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

import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import gg.mineads.monitor.shared.event.model.*;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.session.PlayerSessionManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class PlayerListener implements Listener {

  private final BatchProcessor batchProcessor;
  private final Config config;

  public PlayerListener(BatchProcessor batchProcessor, Config config) {
    this.batchProcessor = batchProcessor;
    this.config = config;
  }

  @EventHandler
  public void onPostLogin(PostLoginEvent event) {
    if (!isEventEnabled(EventType.JOIN)) {
      return;
    }

    ProxiedPlayer player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.createSession(player.getUniqueId());
    String rank = LuckPermsUtil.getPrimaryGroup(player.getUniqueId());

    PlayerJoinData data = new PlayerJoinData(
      sessionId,
      player.getLocale() != null ? player.getLocale().toString() : "en_US",
      player.getAddress() != null && player.getAddress().getAddress() != null
        ? player.getAddress().getAddress().getHostAddress() : "unknown",
      "Unknown",
      String.valueOf(player.getPendingConnection().getVersion()),
      player.getPendingConnection().isOnlineMode(),
      rank
    );
    batchProcessor.addEvent(MineAdsEvent.from(data));
  }

  @EventHandler
  public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    if (!isEventEnabled(EventType.LEAVE)) {
      return;
    }

    ProxiedPlayer player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.removeSession(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(MineAdsEvent.from(new PlayerLeaveData(sessionId)));
    }
  }

  @EventHandler
  public void onChat(ChatEvent event) {
    if (!(event.getSender() instanceof ProxiedPlayer player)) {
      return;
    }

    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId == null) {
      return;
    }

    if (event.isCommand() || event.isProxyCommand()) {
      if (isEventEnabled(EventType.COMMAND)) {
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerCommandData(sessionId, event.getMessage())));
      }
    } else {
      if (isEventEnabled(EventType.CHAT)) {
        batchProcessor.addEvent(MineAdsEvent.from(new PlayerChatData(sessionId, event.getMessage())));
      }
    }
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
