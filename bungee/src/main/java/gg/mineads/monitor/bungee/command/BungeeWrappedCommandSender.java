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

import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.CommandSender;

public class BungeeWrappedCommandSender implements WrappedCommandSender {

    private final CommandSender sender;

    public BungeeWrappedCommandSender(final CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(final Component component) {
        this.sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Override
    public boolean hasPermission(final String permission) {
        return this.sender.hasPermission(permission);
    }

    public CommandSender getSender() {
        return this.sender;
    }

}
