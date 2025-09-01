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
package gg.mineads.monitor.bukkit.command;

import gg.mineads.monitor.bukkit.MineAdsMonitorBukkit;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public class BukkitCommandManager extends MineAdsCommandManager<MineAdsMonitorBukkit.Bootstrap> {

  public BukkitCommandManager(final MineAdsMonitorBukkit.Bootstrap bootstrap, final MineAdsMonitorPlugin plugin) {
    super(bootstrap, plugin);
  }

  @Override
  protected CommandManager<WrappedCommandSender> createCommandManager(MineAdsMonitorBukkit.Bootstrap platformBootstrap) {
    return new LegacyPaperCommandManager<>(
      platformBootstrap.getOwningPlugin(),
      ExecutionCoordinator.asyncCoordinator(),
      SenderMapper.create(BukkitWrappedCommandSender::new, s -> ((BukkitWrappedCommandSender) s).getSender())
    );
  }
}
