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
package gg.mineads.monitor.shared.event.model.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public final class PlayerJoinData implements EventData {
  @SerializedName("session_id")
  private final UUID sessionId;
  @SerializedName("uuid")
  private final UUID uuid;
  @SerializedName("username")
  private final String username;
  @SerializedName("locale")
  @Nullable
  private final String locale;
  @SerializedName("host")
  @Nullable
  private final String host;
  @SerializedName("client_brand")
  @Nullable
  private final String clientBrand;
  @SerializedName("protocol_version")
  @Nullable
  private final Integer protocolVersion;
  @SerializedName("online_mode")
  @Nullable
  private final Boolean onlineMode;
  @SerializedName("luckperms_ranks")
  @Nullable
  private final List<String> luckpermsRanks;
  @SerializedName("virtual_host")
  @Nullable
  private final String virtualHost;
}
