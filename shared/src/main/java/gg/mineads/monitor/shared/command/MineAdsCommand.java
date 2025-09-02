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
import gg.mineads.monitor.shared.event.model.MineAdsEvent;
import gg.mineads.monitor.shared.event.model.data.PurchaseData;
import gg.mineads.monitor.shared.event.model.purchase.CraftingStorePurchaseData;
import gg.mineads.monitor.shared.event.model.purchase.PurchaseType;
import gg.mineads.monitor.shared.event.model.purchase.TebexPurchaseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.Nullable;

@Command("mineadsmonitor")
@RequiredArgsConstructor
@Log
public class MineAdsCommand {

  private final MineAdsMonitorPlugin plugin;
  private final CommandManager<WrappedCommandSender> commandManager;

  @Command("version")
  @Permission("mineadsmonitor.admin")
  public void onVersion(final WrappedCommandSender sender) {
    if (plugin.getConfig().isDebug()) {
      log.info("[DEBUG] Version command executed");
    }
    sender.sendMessage(Component.text("Current plugin version: " + BuildData.VERSION, NamedTextColor.GREEN));
  }

  @Command("reload")
  @Permission("mineadsmonitor.admin")
  public void onReload(final WrappedCommandSender sender) {
    if (plugin.getConfig().isDebug()) {
      log.info("[DEBUG] Reload command executed");
    }
    sender.sendMessage(Component.text("Reloading MineAds Monitor configuration...", NamedTextColor.YELLOW));

    boolean success = plugin.reloadConfig();

    if (success) {
      sender.sendMessage(Component.text("Configuration reloaded successfully!", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Configuration reloaded successfully");
      }
    } else {
      sender.sendMessage(Component.text("Failed to reload configuration. Check console for details.", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Configuration reload failed");
      }
    }
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
    PurchaseData purchaseData = new PurchaseData(PurchaseType.TEBEX, tebexData);
    MineAdsEvent purchaseEvent = MineAdsEvent.from(purchaseData);

    // Add event to batch processor
    if (plugin.isInitialized() && plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(purchaseEvent);
      sender.sendMessage(Component.text("Tebex purchase event recorded successfully", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Tebex purchase event recorded for player: " + username + ", package: " + packageName);
      }
    } else {
      sender.sendMessage(Component.text("Plugin not properly initialized - check plugin configuration", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to record Tebex purchase - plugin not initialized");
      }
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
    PurchaseData purchaseData = new PurchaseData(PurchaseType.CRAFTING_STORE, craftingStoreData);
    MineAdsEvent purchaseEvent = MineAdsEvent.from(purchaseData);

    // Add event to batch processor
    if (plugin.isInitialized() && plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(purchaseEvent);
      sender.sendMessage(Component.text("CraftingStore purchase event recorded successfully", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] CraftingStore purchase event recorded for player: " + player + ", package: " + packageName);
      }
    } else {
      sender.sendMessage(Component.text("Plugin not properly initialized - check plugin configuration", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to record CraftingStore purchase - plugin not initialized");
      }
    }
  }
}
