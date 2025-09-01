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

import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.BatchProcessor;
import gg.mineads.monitor.shared.event.model.EventType;
import gg.mineads.monitor.shared.event.model.MineAdsEvent;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import gg.mineads.monitor.shared.session.PlayerSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

  private final BatchProcessor batchProcessor;
  private final Config config;

  public PlayerListener(BatchProcessor batchProcessor, Config config) {
    this.batchProcessor = batchProcessor;
    this.config = config;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!isEventEnabled(EventType.JOIN)) {
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.createSession(player.getUniqueId());
    String rank = LuckPermsUtil.getPrimaryGroup(player.getUniqueId());

    batchProcessor.addEvent(MineAdsEvent.playerJoin(
      sessionId,
      player.getLocale(),
      player.getAddress() != null && player.getAddress().getAddress() != null
        ? player.getAddress().getAddress().getHostAddress() : "unknown",
      "Unknown", // Client brand is not available on Bukkit
      Bukkit.getBukkitVersion(),
      Bukkit.getOnlineMode(),
      rank
    ));
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (!isEventEnabled(EventType.LEAVE)) {
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.removeSession(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(MineAdsEvent.playerLeave(sessionId));
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    if (!isEventEnabled(EventType.CHAT)) {
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(MineAdsEvent.playerChat(sessionId, event.getMessage()));
    }
  }

  @EventHandler
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    if (!isEventEnabled(EventType.COMMAND)) {
      return;
    }

    Player player = event.getPlayer();
    UUID sessionId = PlayerSessionManager.getSessionId(player.getUniqueId());
    if (sessionId != null) {
      batchProcessor.addEvent(MineAdsEvent.playerCommand(sessionId, event.getMessage()));
    }
  }

  private boolean isEventEnabled(EventType eventType) {
    return config.getEnabledEvents().contains(eventType);
  }
}
