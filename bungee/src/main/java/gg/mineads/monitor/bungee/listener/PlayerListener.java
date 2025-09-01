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

import gg.mineads.monitor.shared.batch.BatchProcessor;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerChatEvent;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerCommandEvent;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerJoinEvent;
import gg.mineads.monitor.shared.event.model.MineAdsPlayerLeaveEvent;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

  private final BatchProcessor batchProcessor;

  public PlayerListener(BatchProcessor batchProcessor) {
    this.batchProcessor = batchProcessor;
  }

  @EventHandler
  public void onPostLogin(PostLoginEvent event) {
    ProxiedPlayer player = event.getPlayer();
    String rank = LuckPermsUtil.getPrimaryGroup(player.getUniqueId());

    batchProcessor.addEvent(new MineAdsPlayerJoinEvent(
      player.getLocale().toString(),
      player.getAddress().getAddress().getHostAddress(),
      "Unknown",
      String.valueOf(player.getPendingConnection().getVersion()),
      player.getPendingConnection().isOnlineMode(),
      rank
    ));
  }

  @EventHandler
  public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    batchProcessor.addEvent(new MineAdsPlayerLeaveEvent());
  }

  @EventHandler
  public void onChat(ChatEvent event) {
    if (event.isCommand() || event.isProxyCommand()) {
      batchProcessor.addEvent(new MineAdsPlayerCommandEvent(event.getMessage()));
    } else {
      batchProcessor.addEvent(new MineAdsPlayerChatEvent(event.getMessage()));
    }
  }
}
