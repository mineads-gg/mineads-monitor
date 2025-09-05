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
import gg.mineads.monitor.shared.event.generated.EventType;
import lombok.Getter;

import java.util.Set;

@Getter
@Configuration
public class Config {
  @Comment("The plugin key for your server. You can get this from the MineAds dashboard. Must start with 'pluginkey_'.")
  private String pluginKey = "";

  @Comment("Enable/disable tracking of specific event types. By default, all events are enabled.")
  private Set<EventType> enabledEvents = Set.of(
    EventType.INITIAL,
    EventType.EXPIRY,
    EventType.RENEWAL,
    EventType.CHARGEBACK,
    EventType.REFUND,
    EventType.CHAT,
    EventType.COMMAND,
    EventType.JOIN,
    EventType.LEAVE
  );

  @Comment("Enable debug logging for batch processing events. Useful for troubleshooting.")
  private boolean debug = false;

  @Comment("Disable sending chat message content. When enabled (default), full chat message content is sent with chat events. When disabled, only the fact that a message was sent is transmitted.")
  private boolean disableChatContent = false;

  @Comment("Disable sending command content. When enabled (default), full command content is sent with command events. When disabled, only the fact that a command was executed is transmitted.")
  private boolean disableCommandContent = false;
}
