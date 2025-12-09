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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gg.mineads.monitor.bungee.MineAdsMonitorBungee;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;
import java.util.Objects;

@SuppressFBWarnings(value = "EI2", justification = "Command sender reference is required to forward messages and permissions.")
public record BungeeWrappedCommandSender(MineAdsMonitorBungee.Bootstrap bootstrap, CommandSender sender) implements WrappedCommandSender {

  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "The wrapped sender must be exposed so Cloud can interact with it.")
  public CommandSender sender() {
    return this.sender;
  }

  @Override
  public void sendMessage(final Component component) {
    bootstrap.getAdventure().sender(sender).sendMessage(component);
  }

  @Override
  public boolean hasPermission(final String permission) {
    return this.sender.hasPermission(permission);
  }

  @Override
  public Locale getLocale() {
    if (sender instanceof ProxiedPlayer player) {
      return Objects.requireNonNullElse(player.getLocale(), Locale.ENGLISH);
    } else {
      return Locale.ENGLISH;
    }
  }
}
