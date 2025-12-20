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
import gg.mineads.monitor.shared.ComponentHelper;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import gg.mineads.monitor.shared.event.TypeUtil;
import gg.mineads.monitor.shared.event.generated.MineAdsEvent;
import gg.mineads.monitor.shared.event.generated.TransactionData;
import gg.mineads.monitor.shared.permission.LuckPermsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log
@RequiredArgsConstructor
@Command("mineadsmonitor")
@Permission("mineadsmonitor.admin")
public class MineAdsCommand {

  private final MineAdsMonitorPlugin plugin;
  private final CommandManager<WrappedCommandSender> commandManager;

  /**
   * Resolves a player UUID from either a UUID string or a username.
   * First tries to parse the input as a UUID. If that fails, attempts to look up
   * the username using LuckPerms API (if available).
   *
   * @param usernameOrUuid The input string - either a UUID or username
   * @return An Optional containing the resolved UUID, or empty if resolution failed
   */
  private Optional<UUID> resolvePlayerUuid(String usernameOrUuid) {
    // First, try to parse as UUID
    try {
      return Optional.of(UUID.fromString(usernameOrUuid));
    } catch (IllegalArgumentException e) {
      // Not a valid UUID, try to look up as username
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Input '" + usernameOrUuid + "' is not a UUID, attempting username lookup");
      }
    }

    // Try to look up username via LuckPerms
    if (!LuckPermsUtil.isAvailable()) {
      log.warning("[MineAdsMonitor] Cannot resolve username '" + usernameOrUuid + "' - LuckPerms is not available");
      return Optional.empty();
    }

    return LuckPermsUtil.lookupUuidByUsername(plugin.getConfig(), usernameOrUuid);
  }

  @Command("")
  @CommandDescription("Displays help information for MineAds Monitor commands")
  public void rootCommand(final WrappedCommandSender sender) {
    MinecraftHelp.<WrappedCommandSender>builder()
      .commandManager(commandManager)
      .audienceProvider(ComponentHelper::commandSenderToAudience)
      .commandPrefix("/mineadsmonitor help")
      .messageProvider(MinecraftHelp.captionMessageProvider(
        commandManager.captionRegistry(),
        ComponentCaptionFormatter.miniMessage()
      ))
      .descriptionDecorator(MinecraftHelp.DescriptionDecorator.text())
      .commandFilter(c -> c.rootComponent().name().equals("mineadsmonitor") && !c.commandDescription().description().isEmpty())
      .maxResultsPerPage(Integer.MAX_VALUE)
      .build()
      .queryCommands("", sender);
  }

  @Suggestions("help_queries_mineadsmonitor")
  public List<String> suggestHelpQueries(CommandContext<WrappedCommandSender> ctx, String input) {
    return this.commandManager
      .createHelpHandler()
      .queryRootIndex(ctx.sender())
      .entries()
      .stream()
      .filter(e -> e.command().rootComponent().name().equals("mineadsmonitor"))
      .map(CommandEntry::syntax)
      .toList();
  }

  @Command("help [query]")
  @CommandDescription("Displays help information for MineAds Monitor commands")
  public void commandHelp(final WrappedCommandSender sender, @Argument(suggestions = "help_queries_mineadsmonitor") @Greedy String query) {
    MinecraftHelp.<WrappedCommandSender>builder()
      .commandManager(commandManager)
      .audienceProvider(ComponentHelper::commandSenderToAudience)
      .commandPrefix("/mineadsmonitor help")
      .messageProvider(MinecraftHelp.captionMessageProvider(
        commandManager.captionRegistry(),
        ComponentCaptionFormatter.miniMessage()
      ))
      .descriptionDecorator(MinecraftHelp.DescriptionDecorator.text())
      .commandFilter(c -> c.rootComponent().name().equals("mineadsmonitor") && !c.commandDescription().description().isEmpty())
      .build()
      .queryCommands(query == null ? "" : query, sender);
  }

  @Command("version")
  @CommandDescription("Displays the current version of MineAds Monitor")
  public void onVersion(final WrappedCommandSender sender) {
    if (plugin.getConfig().isDebug()) {
      log.info("[DEBUG] Version command executed");
    }
    sender.sendMessage(Component.text("Current plugin version: " + BuildData.VERSION, NamedTextColor.GREEN));
  }

  @Command("reload")
  @CommandDescription("Reloads the MineAds Monitor configuration")
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

  @Command("initial <transactionId> <usernameOrUuid> <packageId> <price> <currency>")
  @CommandDescription("Records an initial purchase event")
  public void onInitial(
    final WrappedCommandSender sender,
    @Argument(value = "transactionId") final String transactionId,
    @Argument(value = "usernameOrUuid") final String usernameOrUuid,
    @Argument(value = "packageId") final String packageId,
    @Argument(value = "price") final double price,
    @Argument(value = "currency") final String currency
  ) {
    // Resolve the player UUID
    Optional<UUID> resolvedUuid = resolvePlayerUuid(usernameOrUuid);
    if (resolvedUuid.isEmpty()) {
      sender.sendMessage(Component.text("Failed to resolve player UUID. Input '" + usernameOrUuid + "' is not a valid UUID and username lookup failed (LuckPerms may not be available).", NamedTextColor.RED));
      return;
    }

    String uuid = resolvedUuid.get().toString();

    // Create transaction data
    TransactionData transactionData = TransactionData.newBuilder()
      .setTransactionId(transactionId)
      .setUuid(uuid)
      .setPackageId(packageId)
      .setPrice(price)
      .setCurrency(currency)
      .build();

    MineAdsEvent transactionEvent = TypeUtil.createTransactionEvent(transactionData, MineAdsEvent.Builder::setInitialTransactionData);

    // Add event to batch processor
    if (plugin.isInitialized() && plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(transactionEvent);
      sender.sendMessage(Component.text("Initial purchase event recorded successfully", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Initial purchase event recorded for player: " + uuid + ", package: " + packageId + ", transaction: " + transactionId);
      }
    } else {
      sender.sendMessage(Component.text("Plugin not properly initialized - check plugin configuration", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to record initial purchase - plugin not initialized");
      }
    }
  }

  @Command("expiry <transactionId> <usernameOrUuid> <packageId> <price> <currency>")
  @CommandDescription("Records an expiry event")
  public void onExpiry(
    final WrappedCommandSender sender,
    @Argument(value = "transactionId") final String transactionId,
    @Argument(value = "usernameOrUuid") final String usernameOrUuid,
    @Argument(value = "packageId") final String packageId,
    @Argument(value = "price") final double price,
    @Argument(value = "currency") final String currency
  ) {
    // Resolve the player UUID
    Optional<UUID> resolvedUuid = resolvePlayerUuid(usernameOrUuid);
    if (resolvedUuid.isEmpty()) {
      sender.sendMessage(Component.text("Failed to resolve player UUID. Input '" + usernameOrUuid + "' is not a valid UUID and username lookup failed (LuckPerms may not be available).", NamedTextColor.RED));
      return;
    }

    String uuid = resolvedUuid.get().toString();

    // Create transaction data
    TransactionData transactionData = TransactionData.newBuilder()
      .setTransactionId(transactionId)
      .setUuid(uuid)
      .setPackageId(packageId)
      .setPrice(price)
      .setCurrency(currency)
      .build();

    MineAdsEvent transactionEvent = TypeUtil.createTransactionEvent(transactionData, MineAdsEvent.Builder::setExpiryTransactionData);

    // Add event to batch processor
    if (plugin.isInitialized() && plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(transactionEvent);
      sender.sendMessage(Component.text("Expiry event recorded successfully", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Expiry event recorded for player: " + uuid + ", package: " + packageId + ", transaction: " + transactionId);
      }
    } else {
      sender.sendMessage(Component.text("Plugin not properly initialized - check plugin configuration", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to record expiry - plugin not initialized");
      }
    }
  }

  @Command("renewal <transactionId> <usernameOrUuid> <packageId> <price> <currency>")
  @CommandDescription("Records a renewal event")
  public void onRenewal(
    final WrappedCommandSender sender,
    @Argument(value = "transactionId") final String transactionId,
    @Argument(value = "usernameOrUuid") final String usernameOrUuid,
    @Argument(value = "packageId") final String packageId,
    @Argument(value = "price") final double price,
    @Argument(value = "currency") final String currency
  ) {
    // Resolve the player UUID
    Optional<UUID> resolvedUuid = resolvePlayerUuid(usernameOrUuid);
    if (resolvedUuid.isEmpty()) {
      sender.sendMessage(Component.text("Failed to resolve player UUID. Input '" + usernameOrUuid + "' is not a valid UUID and username lookup failed (LuckPerms may not be available).", NamedTextColor.RED));
      return;
    }

    String uuid = resolvedUuid.get().toString();

    // Create transaction data
    TransactionData transactionData = TransactionData.newBuilder()
      .setTransactionId(transactionId)
      .setUuid(uuid)
      .setPackageId(packageId)
      .setPrice(price)
      .setCurrency(currency)
      .build();

    MineAdsEvent transactionEvent = TypeUtil.createTransactionEvent(transactionData, MineAdsEvent.Builder::setRenewalTransactionData);

    // Add event to batch processor
    if (plugin.isInitialized() && plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(transactionEvent);
      sender.sendMessage(Component.text("Renewal event recorded successfully", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Renewal event recorded for player: " + uuid + ", package: " + packageId + ", transaction: " + transactionId);
      }
    } else {
      sender.sendMessage(Component.text("Plugin not properly initialized - check plugin configuration", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to record renewal - plugin not initialized");
      }
    }
  }

  @Command("chargeback <transactionId> <usernameOrUuid> <packageId> <price> <currency>")
  @CommandDescription("Records a chargeback event")
  public void onChargeback(
    final WrappedCommandSender sender,
    @Argument(value = "transactionId") final String transactionId,
    @Argument(value = "usernameOrUuid") final String usernameOrUuid,
    @Argument(value = "packageId") final String packageId,
    @Argument(value = "price") final double price,
    @Argument(value = "currency") final String currency
  ) {
    // Resolve the player UUID
    Optional<UUID> resolvedUuid = resolvePlayerUuid(usernameOrUuid);
    if (resolvedUuid.isEmpty()) {
      sender.sendMessage(Component.text("Failed to resolve player UUID. Input '" + usernameOrUuid + "' is not a valid UUID and username lookup failed (LuckPerms may not be available).", NamedTextColor.RED));
      return;
    }

    String uuid = resolvedUuid.get().toString();

    // Create transaction data
    TransactionData transactionData = TransactionData.newBuilder()
      .setTransactionId(transactionId)
      .setUuid(uuid)
      .setPackageId(packageId)
      .setPrice(price)
      .setCurrency(currency)
      .build();

    MineAdsEvent transactionEvent = TypeUtil.createTransactionEvent(transactionData, MineAdsEvent.Builder::setChargebackTransactionData);

    // Add event to batch processor
    if (plugin.isInitialized() && plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(transactionEvent);
      sender.sendMessage(Component.text("Chargeback event recorded successfully", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Chargeback event recorded for player: " + uuid + ", package: " + packageId + ", transaction: " + transactionId);
      }
    } else {
      sender.sendMessage(Component.text("Plugin not properly initialized - check plugin configuration", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to record chargeback - plugin not initialized");
      }
    }
  }

  @Command("refund <transactionId> <usernameOrUuid> <packageId> <price> <currency>")
  @CommandDescription("Records a refund event")
  public void onRefund(
    final WrappedCommandSender sender,
    @Argument(value = "transactionId") final String transactionId,
    @Argument(value = "usernameOrUuid") final String usernameOrUuid,
    @Argument(value = "packageId") final String packageId,
    @Argument(value = "price") final double price,
    @Argument(value = "currency") final String currency
  ) {
    // Resolve the player UUID
    Optional<UUID> resolvedUuid = resolvePlayerUuid(usernameOrUuid);
    if (resolvedUuid.isEmpty()) {
      sender.sendMessage(Component.text("Failed to resolve player UUID. Input '" + usernameOrUuid + "' is not a valid UUID and username lookup failed (LuckPerms may not be available).", NamedTextColor.RED));
      return;
    }

    String uuid = resolvedUuid.get().toString();

    // Create transaction data
    TransactionData transactionData = TransactionData.newBuilder()
      .setTransactionId(transactionId)
      .setUuid(uuid)
      .setPackageId(packageId)
      .setPrice(price)
      .setCurrency(currency)
      .build();

    MineAdsEvent transactionEvent = TypeUtil.createTransactionEvent(transactionData, MineAdsEvent.Builder::setRefundTransactionData);

    // Add event to batch processor
    if (plugin.isInitialized() && plugin.getBatchProcessor() != null) {
      plugin.getBatchProcessor().addEvent(transactionEvent);
      sender.sendMessage(Component.text("Refund event recorded successfully", NamedTextColor.GREEN));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Refund event recorded for player: " + uuid + ", package: " + packageId + ", transaction: " + transactionId);
      }
    } else {
      sender.sendMessage(Component.text("Plugin not properly initialized - check plugin configuration", NamedTextColor.RED));
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Failed to record refund - plugin not initialized");
      }
    }
  }
}
