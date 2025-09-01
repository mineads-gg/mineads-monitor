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
import gg.mineads.monitor.shared.event.model.EventType;
import gg.mineads.monitor.shared.event.model.MineAdsEvent;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.session.PlayerSessionManager;

import java.util.UUID;

public class PlayerListener {

  private final BatchProcessor batchProcessor;
  private final Config config;

  public PlayerListener(BatchProcessor batchProcessor, Config config) {
    this.batchProcessor = batchProcessor;
    this.config = config;
  }

  @Subscribe
  public void onPostLogin(PostLoginEvent event) {
    if (!isEventEnabled(EventType.JOIN)) {
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.createSession(player.getUniqueId());
    String rank = LuckPermsUtil.getPrimaryGroup(player.getUniqueId());

    batchProcessor.addEvent(MineAdsEvent.playerJoin(
      sessionId,
      player.getEffectiveLocale() != null ? player.getEffectiveLocale().toString() : "en_US",
      player.getRemoteAddress().getAddress().getHostAddress(),
      player.getClientBrand(),
      String.valueOf(player.getProtocolVersion().getProtocol()),
      player.isOnlineMode(),
      rank
    ));
  }

  @Subscribe
  public void onDisconnect(DisconnectEvent event) {
    if (!isEventEnabled(EventType.LEAVE)) {
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.removeSession(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(MineAdsEvent.playerLeave(sessionId));
    }
  }

  @Subscribe
  public void onPlayerChat(PlayerChatEvent event) {
    if (!isEventEnabled(EventType.CHAT)) {
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(MineAdsEvent.playerChat(sessionId, event.getMessage()));
    }
  }

  @Subscribe
  public void onCommandExecute(CommandExecuteEvent event) {
    if (!isEventEnabled(EventType.COMMAND)) {
      return;
    }

    if (!(event.getCommandSource() instanceof Player player)) {
      return;
    }

    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(MineAdsEvent.playerCommand(sessionId, event.getCommand()));
    }
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
