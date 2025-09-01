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
package gg.mineads.monitor.shared.command;

import gg.mineads.monitor.shared.AbstractMineAdsMonitorBootstrap;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.PlatformBootstrap;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;

public abstract class MineAdsCommandManager<B extends PlatformBootstrap> implements PlatformCommandManager {

  protected final B platformBootstrap;
  protected final CommandManager<WrappedCommandSender> commandManager;
  protected final AnnotationParser<WrappedCommandSender> annotationParser;

  public MineAdsCommandManager(final B plugin) {
    this.platformBootstrap = plugin;
    this.commandManager = createCommandManager(plugin);
    this.annotationParser = new AnnotationParser<>(
      this.commandManager,
      WrappedCommandSender.class
    );
  }

  protected abstract CommandManager<WrappedCommandSender> createCommandManager(B plugin);

  @Override
  public void registerCommands() {
    this.annotationParser.parse(new MineAdsCommand(new MineAdsMonitorPlugin((AbstractMineAdsMonitorBootstrap) platformBootstrap), this.commandManager));
  }
}
