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
package gg.mineads.monitor.velocity.scheduler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import gg.mineads.monitor.velocity.MineAdsMonitorVelocity;

import java.util.concurrent.TimeUnit;

public class VelocityMineAdsScheduler implements MineAdsScheduler {

  private final MineAdsMonitorVelocity plugin;
  private final Scheduler scheduler;

  public VelocityMineAdsScheduler(MineAdsMonitorVelocity plugin, ProxyServer proxyServer) {
    this.plugin = plugin;
    this.scheduler = proxyServer.getScheduler();
  }

  @Override
  public void scheduleAsync(Runnable task, long delay, long period, TimeUnit timeUnit) {
    scheduler.buildTask(plugin, task)
      .delay(delay, timeUnit)
      .repeat(period, timeUnit)
      .schedule();
  }

  @Override
  public void scheduleAsyncDelayed(Runnable task, long delay, TimeUnit timeUnit) {
    scheduler.buildTask(plugin, task)
      .delay(delay, timeUnit)
      .schedule();
  }

  @Override
  public void runAsync(Runnable task) {
    scheduler.buildTask(plugin, task).schedule();
  }
}
