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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gg.mineads.monitor.shared.AbstractMineAdsMonitorBootstrap;
import gg.mineads.monitor.shared.ComponentHelper;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.command.sender.WrappedCommandSender;
import lombok.extern.java.Log;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter;
import org.incendo.cloud.translations.TranslationBundle;
import org.incendo.cloud.translations.minecraft.extras.MinecraftExtrasTranslationBundle;

@Log
@SuppressFBWarnings(value = "EI2", justification = "Command manager intentionally retains the plugin reference for command handling.")
public abstract class MineAdsCommandManager<B extends AbstractMineAdsMonitorBootstrap> implements PlatformCommandManager {

  protected final B platformBootstrap;
  protected final MineAdsMonitorPlugin plugin;
  protected final CommandManager<WrappedCommandSender> commandManager;
  protected final AnnotationParser<WrappedCommandSender> annotationParser;

  @SuppressWarnings("unchecked")
  public MineAdsCommandManager(final B platformBootstrap, final MineAdsMonitorPlugin plugin) {
    this.platformBootstrap = platformBootstrap;
    this.plugin = plugin;
    this.commandManager = createCommandManager(platformBootstrap);
    this.annotationParser = new AnnotationParser<>(
      this.commandManager,
      WrappedCommandSender.class
    );

    if (commandManager instanceof BrigadierManagerHolder<?, ?> holder && holder.hasBrigadierManager()) {
      CloudBrigadierManager<WrappedCommandSender, ?> brigadierManager = (CloudBrigadierManager<WrappedCommandSender, ?>) holder.brigadierManager();
      brigadierManager.setNativeNumberSuggestions(true);
      brigadierManager.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
    }

    commandManager.captionRegistry().registerProvider(TranslationBundle.core(WrappedCommandSender::getLocale));
    commandManager.captionRegistry().registerProvider(MinecraftExtrasTranslationBundle.minecraftExtras(WrappedCommandSender::getLocale));

    MinecraftExceptionHandler
      .create(ComponentHelper::commandSenderToAudience)
      .defaultHandlers()
      .captionFormatter(ComponentCaptionFormatter.miniMessage())
      .registerTo(commandManager);
  }

  protected abstract CommandManager<WrappedCommandSender> createCommandManager(B platformBootstrap);

  @Override
  public void registerCommands() {
    if (plugin.getConfig().isDebug()) {
      log.info("[DEBUG] Registering MineAds commands");
    }
    this.annotationParser.parse(new MineAdsCommand(plugin, this.commandManager));
    if (plugin.getConfig().isDebug()) {
      log.info("[DEBUG] MineAds commands registered successfully");
    }
  }
}
