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
package gg.mineads.monitor.bungee;

import gg.mineads.monitor.shared.MineAdsMonitorBootstrap;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public class MineAdsMonitorBungee extends Plugin implements MineAdsMonitorBootstrap {
  private BungeeAudiences adventure;

  @Override
  public void onEnable() {
    this.adventure = BungeeAudiences.create(this);
    new Metrics(this, 27109);
  }

  @Override
  public void onDisable() {
    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }
}
