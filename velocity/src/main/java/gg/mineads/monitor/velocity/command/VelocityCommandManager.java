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
package gg.mineads.monitor.velocity.command;

import gg.mineads.monitor.shared.command.MineAdsCommandManager;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import gg.mineads.monitor.velocity.MineAdsMonitorVelocity;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;

public class VelocityCommandManager extends MineAdsCommandManager<MineAdsMonitorVelocity.Bootstrap> {

  public VelocityCommandManager(final MineAdsMonitorVelocity plugin) {
    super(plugin.getBootstrap());
  }

  @Override
  protected CommandManager<WrappedCommandSender> createCommandManager(MineAdsMonitorVelocity.Bootstrap plugin) {
    return new org.incendo.cloud.velocity.VelocityCommandManager<>(
      plugin.getOwningPlugin().getContainer(),
      plugin.getOwningPlugin().getProxyServer(),
      ExecutionCoordinator.asyncCoordinator(),
      SenderMapper.create(VelocityWrappedCommandSender::new, s -> ((VelocityWrappedCommandSender) s).getSource())
    );
  }
}
