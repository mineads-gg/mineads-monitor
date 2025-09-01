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

import gg.mineads.monitor.shared.event.EventCollector;
import gg.mineads.monitor.shared.event.model.PlayerChatEvent;
import gg.mineads.monitor.shared.event.model.PlayerCommandEvent;
import gg.mineads.monitor.shared.event.model.PlayerLeaveEvent;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

  private final EventCollector eventCollector;

  public PlayerListener(EventCollector eventCollector) {
    this.eventCollector = eventCollector;
  }

  @EventHandler
  public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
    Player player = event.getPlayer();
    String rank = LuckPermsUtil.getPrimaryGroup(player.getUniqueId());

    eventCollector.addEvent(new gg.mineads.monitor.shared.event.model.PlayerJoinEvent(
      player.getLocale(),
      player.getAddress().getAddress().getHostAddress(),
      "Unknown", // Client brand is not available on Bukkit
      Bukkit.getBukkitVersion(),
      Bukkit.getOnlineMode(),
      rank
    ));
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    eventCollector.addEvent(new PlayerLeaveEvent());
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    eventCollector.addEvent(new PlayerChatEvent(event.getMessage()));
  }

  @EventHandler
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    eventCollector.addEvent(new PlayerCommandEvent(event.getMessage()));
  }
}
