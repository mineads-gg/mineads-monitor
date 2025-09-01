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
package gg.mineads.monitor.shared.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import gg.mineads.monitor.shared.event.model.EventType;
import lombok.Getter;

import java.util.Set;

@Getter
@Configuration
public class Config {
  @Comment("The plugin key for your server. You can get this from the MineAds dashboard.")
  private String pluginKey = "";

  @Comment("Enable/disable tracking of specific event types. By default, all events are enabled.")
  private Set<EventType> enabledEvents = Set.of(EventType.values());
}
