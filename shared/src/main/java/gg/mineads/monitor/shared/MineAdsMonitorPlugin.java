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
package gg.mineads.monitor.shared;

import gg.mineads.monitor.shared.event.EventCollector;
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import lombok.Getter;

public class MineAdsMonitorPlugin {

  private final AbstractMineAdsMonitorBootstrap bootstrap;
  @Getter
  private EventCollector eventCollector;

  public MineAdsMonitorPlugin(AbstractMineAdsMonitorBootstrap bootstrap) {
    this.bootstrap = bootstrap;
  }

  public void onEnable() {
    // Delegate to bootstrap for core functionality
    bootstrap.onEnable();

    // Get the event collector after bootstrap initialization
    this.eventCollector = bootstrap.getEventCollector();
  }

  public void onDisable() {
    bootstrap.onDisable();
  }
}
