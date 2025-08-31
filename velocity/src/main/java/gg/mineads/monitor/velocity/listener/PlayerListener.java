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
import gg.mineads.monitor.shared.event.EventCollector;
import gg.mineads.monitor.shared.event.model.PlayerCommandEvent;
import gg.mineads.monitor.shared.event.model.PlayerLeaveEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

public class PlayerListener {

  private final EventCollector eventCollector;
  private final LuckPerms luckPerms;

  public PlayerListener(EventCollector eventCollector) {
    this.eventCollector = eventCollector;
    this.luckPerms = getLuckPerms();
  }

  @Subscribe
  public void onPostLogin(PostLoginEvent event) {
    Player player = event.getPlayer();
    String rank = getRank(player);

    eventCollector.addEvent(new gg.mineads.monitor.shared.event.model.PlayerJoinEvent(
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
    eventCollector.addEvent(new PlayerLeaveEvent());
  }

  @Subscribe
  public void onPlayerChat(PlayerChatEvent event) {
    eventCollector.addEvent(new gg.mineads.monitor.shared.event.model.PlayerChatEvent(event.getMessage()));
  }

  @Subscribe
  public void onCommandExecute(CommandExecuteEvent event) {
    eventCollector.addEvent(new PlayerCommandEvent(event.getCommand()));
  }

  private String getRank(Player player) {
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
