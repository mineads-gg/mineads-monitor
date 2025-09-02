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
package gg.mineads.monitor.bukkit.scheduler;

import com.tcoded.folialib.FoliaLib;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;

import java.util.concurrent.TimeUnit;

public class BukkitMineAdsScheduler implements MineAdsScheduler {

  private final FoliaLib foliaLib;

  public BukkitMineAdsScheduler(FoliaLib foliaLib) {
    this.foliaLib = foliaLib;
  }

  @Override
  public void scheduleAsync(Runnable task, long delay, long period, TimeUnit timeUnit) {
    foliaLib.getScheduler().runTimerAsync(task, delay, period, timeUnit);
  }

  @Override
  public void runAsync(Runnable task) {
    foliaLib.getScheduler().runAsync(ignored -> task.run());
  }
}
