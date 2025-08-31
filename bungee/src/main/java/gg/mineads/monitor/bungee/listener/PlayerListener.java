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

import gg.mineads.monitor.shared.event.EventCollector;
import gg.mineads.monitor.shared.event.model.PlayerChatEvent;
import gg.mineads.monitor.shared.event.model.PlayerCommandEvent;
import gg.mineads.monitor.shared.event.model.PlayerLeaveEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

  private final EventCollector eventCollector;
  private final LuckPerms luckPerms;

  public PlayerListener(EventCollector eventCollector) {
    this.eventCollector = eventCollector;
    this.luckPerms = getLuckPerms();
  }

  @EventHandler
  public void onPostLogin(PostLoginEvent event) {
    ProxiedPlayer player = event.getPlayer();
    String rank = getRank(player);

    eventCollector.addEvent(new gg.mineads.monitor.shared.event.model.PlayerJoinEvent(
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
    eventCollector.addEvent(new PlayerLeaveEvent());
  }

  @EventHandler
  public void onChat(ChatEvent event) {
    if (event.isCommand() || event.isProxyCommand()) {
      eventCollector.addEvent(new PlayerCommandEvent(event.getMessage()));
    } else {
      eventCollector.addEvent(new PlayerChatEvent(event.getMessage()));
    }
  }

  private String getRank(ProxiedPlayer player) {
    if (luckPerms == null) {
      return "Unknown";
    }

    User user = luckPerms.getUserManager().getUser(player.getUniqueId());
    if (user == null) {
      return "Unknown";
    }

    return user.getPrimaryGroup();
  }

  private LuckPerms getLuckPerms() {
    try {
      return net.luckperms.api.LuckPermsProvider.get();
    } catch (IllegalStateException e) {
      return null;
    }
  }
}
