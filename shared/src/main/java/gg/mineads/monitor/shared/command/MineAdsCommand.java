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

import gg.mineads.monitor.data.BuildData;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import gg.mineads.monitor.shared.event.model.CraftingStorePurchaseData;
import gg.mineads.monitor.shared.event.model.MineAdsPurchaseEvent;
import gg.mineads.monitor.shared.event.model.PurchaseType;
import gg.mineads.monitor.shared.event.model.TebexPurchaseData;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.Nullable;

@Command("mineadsmonitor")
@RequiredArgsConstructor
public class MineAdsCommand {

  private final MineAdsMonitorPlugin plugin;
  private final CommandManager<WrappedCommandSender> commandManager;

  @Command("version")
  @Permission("mineadsmonitor.admin")
  public void onVersion(final WrappedCommandSender sender) {
    sender.sendMessage(Component.text("Current plugin version: " + BuildData.VERSION, NamedTextColor.GREEN));
  }

  @Command("tebex <id> <username> <transaction> <price> <currency> <packageName> [server] [date] [email] [ip] [packageId] [packagePrice] [packageExpiry] [purchaserName] [purchaserUuid] [purchaseQuantity]")
  @Permission("mineadsmonitor.purchase")
  public void onTebexPurchase(
    final WrappedCommandSender sender,
    @Argument(value = "id") final String id,
    @Argument(value = "username") final String username,
    @Argument(value = "transaction") final String transaction,
    @Argument(value = "price") final String price,
    @Argument(value = "currency") final String currency,
    @Argument(value = "packageName") final String packageName,
    @Argument(value = "server") @Nullable final String server,
    @Argument(value = "date") @Nullable final String date,
    @Argument(value = "email") @Nullable final String email,
    @Argument(value = "ip") @Nullable final String ip,
    @Argument(value = "packageId") @Nullable final String packageId,
    @Argument(value = "packagePrice") @Nullable final String packagePrice,
    @Argument(value = "packageExpiry") @Nullable final String packageExpiry,
    @Argument(value = "purchaserName") @Nullable final String purchaserName,
    @Argument(value = "purchaserUuid") @Nullable final String purchaserUuid,
    @Argument(value = "purchaseQuantity") @Nullable final String purchaseQuantity
  ) {
    // Create Tebex-specific data
    TebexPurchaseData tebexData = new TebexPurchaseData(
      id, username, transaction, price, currency, packageName,
      server, date, email, ip, packageId, packagePrice, packageExpiry,
      purchaserName, purchaserUuid, purchaseQuantity
    );

    // Create purchase event with type discriminator
    MineAdsPurchaseEvent purchaseEvent = new MineAdsPurchaseEvent(PurchaseType.TEBEX, tebexData);

    // Add event to batch processor
    if (plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(purchaseEvent);
      sender.sendMessage(Component.text("Tebex purchase event recorded successfully", NamedTextColor.GREEN));
    } else {
      sender.sendMessage(Component.text("Batch processor not initialized - check plugin configuration", NamedTextColor.RED));
    }
  }

  @Command("craftingstore <player> <uuid> <package_name> <cost> [uuid_dashed] [packages] [ingame_package_name] [steam_id] [amount] [transaction_id] [discord_id] [discord_name]")
  @Permission("mineadsmonitor.purchase")
  public void onCraftingStorePurchase(
    final WrappedCommandSender sender,
    @Argument(value = "player") final String player,
    @Argument(value = "uuid") final String uuid,
    @Argument(value = "package_name") final String packageName,
    @Argument(value = "cost") final String cost,
    @Argument(value = "uuid_dashed") @Nullable final String uuidDashed,
    @Argument(value = "packages") @Nullable final String packages,
    @Argument(value = "ingame_package_name") @Nullable final String ingamePackageName,
    @Argument(value = "steam_id") @Nullable final String steamId,
    @Argument(value = "amount") @Nullable final String amount,
    @Argument(value = "transaction_id") @Nullable final String transactionId,
    @Argument(value = "discord_id") @Nullable final String discordId,
    @Argument(value = "discord_name") @Nullable final String discordName
  ) {
    // Create CraftingStore-specific data
    CraftingStorePurchaseData craftingStoreData = new CraftingStorePurchaseData(
      player, uuid, packageName, cost,
      uuidDashed, packages, ingamePackageName, steamId, amount,
      transactionId, discordId, discordName
    );

    // Create purchase event with type discriminator
    MineAdsPurchaseEvent purchaseEvent = new MineAdsPurchaseEvent(PurchaseType.CRAFTING_STORE, craftingStoreData);

    // Add event to batch processor
    if (plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(purchaseEvent);
      sender.sendMessage(Component.text("CraftingStore purchase event recorded successfully", NamedTextColor.GREEN));
    } else {
      sender.sendMessage(Component.text("Batch processor not initialized - check plugin configuration", NamedTextColor.RED));
    }
  }
}
