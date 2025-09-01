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
import gg.mineads.monitor.shared.batch.BatchProcessor;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerChatEvent;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerCommandEvent;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerJoinEvent;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerLeaveEvent;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.session.PlayerSessionManager;

public class PlayerListener {

  private final BatchProcessor batchProcessor;

  public PlayerListener(BatchProcessor batchProcessor) {
    this.batchProcessor = batchProcessor;
  }

  @Subscribe
  public void onPostLogin(PostLoginEvent event) {
    Player player = event.getPlayer();
    String sessionId = PlayerSessionManager.createSession(player.getUniqueId());
    String rank = LuckPermsUtil.getPrimaryGroup(player.getUniqueId());

    batchProcessor.addEvent(new MineAdsPlayerJoinEvent(
      sessionId,
      player.getEffectiveLocale().toString(),
      player.getRemoteAddress().getAddress().getHostAddress(),
      player.getClientBrand(),
      String.valueOf(player.getProtocolVersion().getProtocol()),
      player.isOnlineMode(),
      rank
    ));
  }

  @Subscribe
  public void onDisconnect(DisconnectEvent event) {
    Player player = event.getPlayer();
    String sessionId = PlayerSessionManager.removeSession(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(new MineAdsPlayerLeaveEvent(sessionId));
    }
  }

  @Subscribe
  public void onPlayerChat(PlayerChatEvent event) {
    Player player = event.getPlayer();
    String sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(new MineAdsPlayerChatEvent(sessionId, event.getMessage()));
    }
  }

  @Subscribe
  public void onCommandExecute(CommandExecuteEvent event) {
    if (!(event.getCommandSource() instanceof Player player)) {
      return;
    }

    String sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(new MineAdsPlayerCommandEvent(sessionId, event.getCommand()));
    }
  }
}
