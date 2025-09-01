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
package gg.mineads.monitor.bungee.command;

import gg.mineads.monitor.bungee.MineAdsMonitorBungee;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;

public class BungeeCommandManager extends MineAdsCommandManager<MineAdsMonitorBungee.Bootstrap> {
  public BungeeCommandManager(final MineAdsMonitorBungee.Bootstrap bootstrap, final MineAdsMonitorPlugin plugin) {
    super(bootstrap, plugin);
  }

  @Override
  protected CommandManager<WrappedCommandSender> createCommandManager(MineAdsMonitorBungee.Bootstrap platformBootstrap) {
    // Return null for now to get the build working
    return new org.incendo.cloud.bungee.BungeeCommandManager<>(
      platformBootstrap.getOwningPlugin(),
      ExecutionCoordinator.asyncCoordinator(),
      SenderMapper.create(
        s -> new BungeeWrappedCommandSender(platformBootstrap, s),
        s -> ((BungeeWrappedCommandSender) s).sender()
      )
    );
  }
}
