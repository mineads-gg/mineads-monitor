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
package gg.mineads.monitor.shared.event.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
@AllArgsConstructor
public final class PlayerJoinData {
  @SerializedName("session_id")
  private final UUID sessionId;
  @SerializedName("locale")
  @Nullable
  private final String locale;
  @SerializedName("ip")
  @Nullable
  private final String ip;
  @SerializedName("client_brand")
  @Nullable
  private final String clientBrand;
  @SerializedName("minecraft_version")
  @Nullable
  private final String minecraftVersion;
  @SerializedName("online_mode")
  private final boolean onlineMode;
  @SerializedName("luck_perms_rank")
  @Nullable
  private final String luckPermsRank;
}
