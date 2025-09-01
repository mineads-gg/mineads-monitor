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
package gg.mineads.monitor.shared.event.model;

import lombok.Data;

@Data
public class MineAdsPlayerChatEvent {

  private final MineAdsEvent event;

  // Convenience constructor
  public MineAdsPlayerChatEvent(String sessionId, String message) {
    PlayerChatData data = new PlayerChatData(sessionId, message);
    this.event = new MineAdsEvent("chat", data);
  }

}
